/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.api.rest.git;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.git.GitRepositoryDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.git.GitRepositoryApi;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseCrudRs;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.git.GitRepository;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitRepositoryService;
import org.meveo.service.git.MeveoRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST interface to manage git repositories
 *
 * @author Clement Bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@RequestScoped
@Path("/git")
@Interceptors({ WsRestApiInterceptor.class })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.MULTIPART_FORM_DATA, "text/csv" })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/csv" })
@Api("GitRepositoryRs")
public class GitRepositoryRs extends BaseCrudRs<GitRepository, GitRepositoryDto> {

	@Inject
	private GitRepositoryApi gitRepositoryApi;

	@Inject
	private GitRepositoryService gitRepositoryService;

	@Inject
	@MeveoRepository
	private GitRepository meveoRepository;

	@Inject
	private GitClient gitClient;

	@Override
	public BaseCrudApi<GitRepository, GitRepositoryDto> getBaseCrudApi() {
		return gitRepositoryApi;
	}

	/**
	 * Create a new {@link GitRepository}
	 *
	 * @param postData data of the {@link GitRepository}
	 * @param username Optional - Username to connect to remote
	 * @param password Optional - Password to connect to remote
	 */
	@POST
	@Path("/repositories")
	@ApiOperation(value = "Create a new repository", notes = "If branch is specified and does not exist, will create it")
	public void create(@NotNull @ApiParam("Repository information") GitRepositoryDto postData, @QueryParam("username") @ApiParam("Username to connect to remote") String username,
			@QueryParam("password") @ApiParam("Password to connect to remote") String password, @QueryParam("branch") @ApiParam("Branch to checkout") String branch)
			throws MeveoApiException, BusinessException {
		gitRepositoryApi.create(postData, true, username, password);
		checkout(postData.getCode(), postData.getDefaultBranch(), false);
		if (branch != null) {
			if (!postData.getDefaultBranch().equals(branch)) {
				checkout(postData.getCode(), branch, true);
			}
			if (postData.getRemoteOrigin() != null) {
				pull(postData.getCode(), username, password);
			}
		}
	}

	/**
	 * Create a new {@link GitRepository} (delete existing one if any)
	 *
	 * @param postData data of the {@link GitRepository}
	 * @param username Optional - Username to connect to remote
	 * @param password Optional - Password to connect to remote
	 */
	@PUT
	@Path("/repositories/{code}")
	@ApiOperation(value = "Create or update a new repository", notes = "If repository with specified code exists, first delete it. If branch is specified and does not exist, will create it.")
	public void overwrite(@PathParam("code") String code, @NotNull @ApiParam("Repository information") GitRepositoryDto postData,
			@QueryParam("username") @ApiParam("Username to connect to remote") String username, @QueryParam("password") @ApiParam("Password to connect to remote") String password,
			@QueryParam("branch") @ApiParam("Branch to checkout") String branch) throws MeveoApiException, BusinessException {
		postData.setCode(code);
		final boolean exists = gitRepositoryApi.exists(postData);
		if (exists) {
			gitRepositoryApi.remove(postData.getCode());
		}

		gitRepositoryApi.create(postData, false, username, password);
		checkout(postData.getCode(), postData.getDefaultBranch(), false);
		if (branch != null) {
			if (!postData.getDefaultBranch().equals(branch)) {
				checkout(postData.getCode(), branch, true);
			}
			if (postData.getRemoteOrigin() != null) {
				pull(code, username, password);
			}
		}
	}

	/**
	 * Update the {@link GitRepository} with the given code
	 *
	 * @param postData data of the {@link GitRepository}
	 * @param code     Code of the {@link GitRepository} to update
	 */
	@POST
	@Path("/repositories/{code}")
	@ApiOperation("Update an existing repository")
	@ApiResponses({ @ApiResponse(code = 200, message = "If update is ok"), @ApiResponse(code = 404, message = "If specified repository does not exists") })
	public void update(@NotNull @ApiParam("Repository information") GitRepositoryDto postData, @PathParam("code") @ApiParam("Code of the repository") String code)
			throws BusinessException {
		postData.setCode(code);
		gitRepositoryApi.update(postData);
	}

	/**
	 * @return List of all {@link GitRepository} in database
	 */
	@GET
	@Path("/repositories")
	@ApiOperation("List all existing repositories")
	public List<GitRepositoryDto> list() {
		return gitRepositoryApi.list();
	}

	/**
	 * Retrieve a {@link GitRepository} by code
	 *
	 * @param code Code of the {@link GitRepository} to retrieve
	 * @return DTO representation of the found {@link GitRepository}
	 */
	@GET
	@Path("/repositories/{code}")
	@ApiOperation("Retrieve one repository by code")
	@Produces(MediaType.APPLICATION_JSON)
	public GitRepositoryDto find(@PathParam("code") @ApiParam("Code of the repository") String code) throws MeveoApiException, EntityDoesNotExistsException {
		return gitRepositoryApi.findWithMeta(code);
	}

	/**
	 * @param code   Code of the repo
	 * @param branch Branch of the repo
	 * @return Zipped file of the repository content
	 */
	@GET
	@GZIP
	@Path("/repositories/{code}")
	@ApiOperation(value = "Get zipped repository content", notes = "If no branch are provided, will export the current branch")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportZip(@PathParam("code") @ApiParam("Code of the repository") String code, @QueryParam("branch") @ApiParam("Branch to export") String branch)
			throws Exception {
		GitRepositoryDto gitRepositoryDto = gitRepositoryApi.find(code);
		if (gitRepositoryDto == null) {
			throw new EntityDoesNotExistsException(GitRepository.class, code);
		}

		byte[] bytes = gitRepositoryApi.exportZip(code, branch);
		final String name = branch != null ? code + "-" + branch : code + "-" + gitRepositoryDto.getCurrentBranch();
		return Response.ok(bytes, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=\"" + name + ".zip\"") // optional
				.build();
	}

	/**
	 * Delete a {@link GitRepository}
	 *
	 * @param code Code of the {@link GitRepository} to delete
	 */
	@DELETE
	@Path("/repositories/{code}")
	@ApiOperation("Remove a repository by code")
	public void remove(@PathParam("code") @ApiParam("Code of the repository") String code) throws BusinessException {
		gitRepositoryApi.remove(code);
	}

	/**
	 * Commit the content of a {@link GitRepository}
	 *
	 * @param code     Code of the {@link GitRepository} to commit
	 * @param message  Commit message
	 * @param patterns Patterns of the files to stage
	 */
	@POST
	@Path("/repositories/{code}/commit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "Commit content of a repository", notes = "Will commit every file if patterns is empty")
	public void commit(@PathParam("code") @ApiParam("Code of the repository") String code, @FormParam("message") @ApiParam("Commit message") String message,
			@FormParam("pattern") @ApiParam("Patterns of the files to commit") List<String> patterns) throws BusinessException {
		final GitRepository gitRepository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
		gitClient.commit(gitRepository, patterns, message);
	}

	/**
	 * Push the commits of a {@link GitRepository}
	 *
	 * @param code     Code of the {@link GitRepository} to push
	 * @param username Optional - Username to use during push
	 * @param password Optional - Password to use during push
	 */
	@POST
	@Path("/repositories/{code}/push")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation("Push the commit to remote origin")
	@ApiResponses({ @ApiResponse(code = 200, message = "If push is successful"), @ApiResponse(code = 400, message = "If repository has no remote") })
	public void push(@PathParam("code") @ApiParam("Code of the repository") String code, @FormParam("username") @ApiParam("Username to use during push") String username,
			@FormParam("password") @ApiParam("Password to use during push") String password) throws BusinessException {
		final GitRepository gitRepository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
		gitClient.push(gitRepository, username, password);
	}

	/**
	 * Pull the commits of a {@link GitRepository}
	 *
	 * @param code     Code of the {@link GitRepository} to pull
	 * @param username Optional - Username to use during pull
	 * @param password Optional - Password to use during pull
	 */
	@POST
	@Path("/repositories/{code}/pull")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation("Pull changes from remote origin")
	@ApiResponses({ @ApiResponse(code = 200, message = "If pull is successful"), @ApiResponse(code = 400, message = "If repository has no remote") })
	public void pull(@PathParam("code") @ApiParam("Code of the repository") String code, @FormParam("username") @ApiParam("Username to use during pull") String username,
			@FormParam("password") @ApiParam("Password to use during pull") String password) throws BusinessException {
		final GitRepository gitRepository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
		gitClient.pull(gitRepository, username, password);
	}
	
	/**
	 * Fetch the commits of a {@link GitRepository}
	 *
	 * @param code     Code of the {@link GitRepository} to pull
	 * @param username Optional - Username to use during pull
	 * @param password Optional - Password to use during pull
	 */
	@POST
	@Path("/repositories/{code}/fetch")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation("Pull changes from remote origin")
	@ApiResponses({ @ApiResponse(code = 200, message = "If pull is successful"), @ApiResponse(code = 400, message = "If repository has no remote") })
	public void fetch(@PathParam("code") @ApiParam("Code of the repository") String code, @FormParam("username") @ApiParam("Username to use during fetch") String username,
			@FormParam("password") @ApiParam("Password to use during fetch") String password) throws BusinessException {
		final GitRepository gitRepository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
		gitClient.fetch(gitRepository, username, password);
	}

	/**
	 * Change the current branch of a {@link GitRepository}
	 *
	 * @param code         Code of the {@link GitRepository} to change branch
	 * @param branch       Branch to checkout
	 * @param createBranch Whether to create branch if not exists
	 */
	@POST
	@Path("/repositories/{code}/branches/{branch}/checkout")
	@ApiOperation("Checkout a branch")
	public void checkout(@PathParam("code") @ApiParam("Code of the repository") String code, 
			@PathParam("branch") @ApiParam("Name of the branch to checkout") String branch,
			@QueryParam("create") @ApiParam("Whether to create branch if it does not exist") boolean createBranch) throws BusinessException {
		final GitRepository repository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
		gitClient.checkout(repository, branch, createBranch);
	}

	/**
	 * Create a new branch based on the current branch of a {@link GitRepository}
	 *
	 * @param code   Code of the {@link GitRepository} where to create new branch
	 * @param branch Name of the branch to create
	 */
	@POST
	@Path("/repositories/{code}/branches/{branch}")
	@ApiOperation("Create a branch")
	public void createBranch(@PathParam("code") @ApiParam("Code of the repository") String code, @PathParam("branch") @ApiParam("Name of the branch to create") String branch)
			throws BusinessException {
		final GitRepository repository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
		gitClient.createBranch(repository, branch);
	}

	/**
	 * Delete a branch of a {@link GitRepository}
	 *
	 * @param code   Code of the {@link GitRepository} where to delete the branch
	 * @param branch Name of the branch to delete
	 */
	@DELETE
	@Path("/repositories/{code}/branches/{branch}")
	@ApiOperation("Delete a branch")
	public void deleteBranch(@PathParam("code") @ApiParam("Code of the repository") String code, @PathParam("branch") @ApiParam("Name of the branch to delte") String branch)
			throws BusinessException {
		final GitRepository repository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
		gitClient.deleteBranch(repository, branch);
	}

	/**
	 * Merge two branch of a {@link GitRepository}
	 *
	 * @param code Code of the {@link GitRepository} where to merge branches
	 * @param from Name of the source branch
	 * @param to   Name of the target branch
	 */
	@POST
	@Path("/repositories/{code}/branches/merge")
	@ApiOperation("Merge one branch into another")
	public Response merge(@PathParam("code") @ApiParam("Code of the repository") String code, @FormParam("source") @NotNull @ApiParam("Source branch of the merge") String from,
			@FormParam("target") @NotNull @ApiParam("Target branch of the merge") String to) throws BusinessException {
		final GitRepository repository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
		final boolean mergeOk = gitClient.merge(repository, from, to);
		if (!mergeOk) {
			return Response.status(409).entity("Merge must be done manually").build();
		} else {
			return Response.ok().build();
		}
	}

	/**
	 * Import new repository with content. Delete existing data
	 *
	 * @param uploadForm Contains the zipped file and the DTO
	 * @param code       Code of the repo
	 */
	@PUT
	@Path("/repositories/{code}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation("Upload a repository")
	public void importZipOverride(@GZIP @MultipartForm @NotNull @ApiParam("Upload form") GitRepositoryUploadForm uploadForm,
			@PathParam("code") @ApiParam("Code of the repository") String code) throws Exception {
		GitRepositoryDto repository = uploadForm.getRepository() != null ? uploadForm.getRepository() : new GitRepositoryDto();
		repository.setCode(code);
		gitRepositoryApi.importZip(uploadForm.getData(), repository, true);
	}

	/**
	 * Import new repository with content. Do nothing if data already exists
	 *
	 * @param uploadForm Contains the zipped file and the DTO
	 */
	@POST
	@Path("/repositories")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation("Upload a new repository")
	public void importZip(@GZIP @MultipartForm @ApiParam("Upload form") @NotNull GitRepositoryUploadForm uploadForm) throws Exception {
		gitRepositoryApi.importZip(uploadForm.getData(), uploadForm.getRepository(), false);
	}

}
