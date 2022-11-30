package org.meveo.api.rest.storage;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.storage.RepositoriesResponseDto;
import org.meveo.api.dto.response.storage.RepositoryResponseDto;
import org.meveo.api.rest.IBaseBaseCrudRs;
import org.meveo.api.storage.RepositoryDto;
import org.meveo.model.storage.Repository;

/**
 * REST API for managing {@link Repository}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/storages/repositories")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.MULTIPART_FORM_DATA,  "text/csv"})
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/csv" })
@Api("Repository")
public interface RepositoryRs extends IBaseBaseCrudRs {

	/**
	 * Create a new repository
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create repository")
	ActionStatus create(@ApiParam("Repository information") RepositoryDto postData);

	/**
	 * Update an existing repository
	 * 
	 * @param postData The repository's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update repository")
	ActionStatus update(@ApiParam("Repository information") RepositoryDto postData);

	/**
	 * Create new or update an existing repository
	 * 
	 * @param postData The repository's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update repository")
	ActionStatus createOrUpdate(@ApiParam("Repository information") RepositoryDto postData);

	/**
	 * Search for repository with a given code
	 */
	@GET
	@Path("/{code}")
	@ApiOperation(value = "Find repository by code")
	RepositoryResponseDto find(@PathParam("code") @ApiParam("Code of the repository") String code);

	/**
	 * List repository
	 * 
	 * @return A list of repositorys
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "List of repositorys")
	RepositoriesResponseDto list();

	/**
	 * Remove an existing repository with a given code
	 * 
	 * @param code The repository's code
	 * @param forceDelete if true, delete the children of the repository
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation(value = "Remove repository by code")
	ActionStatus remove(@PathParam("code") @ApiParam("Code of the repository") String code, @QueryParam("forceDelete") @ApiParam("Whether to delete the children of the repository") Boolean forceDelete);
	
}
