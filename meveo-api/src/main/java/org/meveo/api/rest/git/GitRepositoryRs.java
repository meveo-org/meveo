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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * REST interface to manage git repositories
 *
 * @author Clement Bareth
 * @lastModifiedVersion 6.4.0
 */
@RequestScoped
@Path("/git")
@Interceptors({WsRestApiInterceptor.class})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.MULTIPART_FORM_DATA, "text/csv"})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/csv"})
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
     * @return List of all {@link GitRepository} in database
     */
    @GET
    @Path("/repositories")
    public List<GitRepositoryDto> list() {
        return gitRepositoryApi.list();
    }

    /**
     * Create a new {@link GitRepository}
     *
     * @param postData data of the {@link GitRepository}
     */
    @POST
    @Path("/repositories")
    public void create(GitRepositoryDto postData) throws BusinessException {
        gitRepositoryApi.create(postData);
    }

    /**
     * Create a new {@link GitRepository} (delete existing one if any)
     *
     * @param postData data of the {@link GitRepository}
     */
    @PUT
    @Path("/repositories")
    public void overwrite(GitRepositoryDto postData) throws BusinessException {
        final boolean exists = gitRepositoryApi.exists(postData);
        if (exists) {
            gitRepositoryApi.remove(postData.getCode());
        }

        gitRepositoryApi.create(postData);
    }

    /**
     * Update the {@link GitRepository} with the given code
     *
     * @param postData data of the {@link GitRepository}
     * @param code     Code of the {@link GitRepository} to update
     */
    @POST
    @Path("/repositories/{code}")
    public void update(GitRepositoryDto postData, @PathParam("code") String code) throws BusinessException {
        postData.setCode(code);
        gitRepositoryApi.update(postData);
    }

    /**
     * Retrieve a {@link GitRepository} by code
     *
     * @param code Code of the {@link GitRepository} to retrieve
     * @return DTO representation of the found {@link GitRepository}
     */
    @GET
    @Path("/repositories/{code}")
    public GitRepositoryDto find(@PathParam("code") String code) throws MeveoApiException, EntityDoesNotExistsException {
        return gitRepositoryApi.find(code);
    }

    /**
     * Delete a {@link GitRepository}
     *
     * @param code Code of the {@link GitRepository} to delete
     */
    @DELETE
    @Path("/repositories/{code}")
    public void remove(@PathParam("code") String code) throws BusinessException {
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
    public void commit(@PathParam("code") String code, @FormParam("message") String message, @FormParam("pattern") List<String> patterns) throws BusinessException {
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
    public void push(@PathParam("code") String code, @FormParam("username") String username, @FormParam("password") String password) throws BusinessException {
        final GitRepository gitRepository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
        gitClient.push(gitRepository, username, password);
    }

    /**
     * Push the commits of a {@link GitRepository}
     *
     * @param code     Code of the {@link GitRepository} to pull
     * @param username Optional - Username to use during pull
     * @param password Optional - Password to use during pull
     */
    @POST
    @Path("/repositories/{code}/pull")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void pull(@PathParam("code") String code, @FormParam("username") String username, @FormParam("password") String password) throws BusinessException {
        final GitRepository gitRepository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
        gitClient.pull(gitRepository, username, password);
    }

    /**
     * Change the current branch of a {@link GitRepository}
     *
     * @param code         Code of the {@link GitRepository} to change branch
     * @param branch       Branch to checkout
     * @param createBranch Whether to create branch if not exists
     */
    @POST
    @Path("/repositories/{code}/checkout/{branch}")
    public void checkout(@PathParam("code") String code, @PathParam("branch") String branch, @QueryParam("create") boolean createBranch) throws BusinessException {
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
    @Path("/repositories/{code}/create/{branch}")
    public void createBranch(@PathParam("code") String code, @PathParam("branch") String branch) throws BusinessException {
        final GitRepository repository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
        gitClient.createBranch(repository, branch);
    }

    /**
     * Delete a branch of a {@link GitRepository}
     *
     * @param code   Code of the {@link GitRepository} where to delete the branch
     * @param branch Name of the branch to delete
     */
    @POST
    @Path("/repositories/{code}/create/{branch}")
    public void deleteBranch(@PathParam("code") String code, @PathParam("branch") String branch) throws BusinessException {
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
    @Path("/repositories/{code}/merge/{branch}/into/{to}")
    public Response merge(@PathParam("code") String code, @PathParam("branch") String from, @PathParam("to") String to) throws BusinessException {
        final GitRepository repository = code.equals(meveoRepository.getCode()) ? meveoRepository : gitRepositoryService.findByCode(code);
        final boolean mergeOk = gitClient.merge(repository, from, to);
        if (!mergeOk) {
            return Response.status(409).entity("Merge must be done manually").build();
        } else {
            return Response.ok().build();
        }
    }

}
