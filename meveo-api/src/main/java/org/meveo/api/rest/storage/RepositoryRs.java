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
 * @lastModifiedVersion 6.4.0
 */
@Path("/storages/repositories")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.MULTIPART_FORM_DATA,  "text/csv"})
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/csv" })
public interface RepositoryRs extends IBaseBaseCrudRs {

	/**
	 * Create a new repository
	 */
	@POST
	@Path("/")
	ActionStatus create(RepositoryDto postData);

	/**
	 * Update an existing repository
	 * 
	 * @param postData The repository's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	ActionStatus update(RepositoryDto postData);

	/**
	 * Create new or update an existing repository
	 * 
	 * @param postData The repository's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	ActionStatus createOrUpdate(RepositoryDto postData);

	/**
	 * Search for repository with a given code
	 */
	@GET
	@Path("/{code}")
	RepositoryResponseDto find(@PathParam("code") String code);

	/**
	 * List repository
	 * 
	 * @return A list of repositorys
	 */
	@GET
	@Path("/")
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
	public ActionStatus remove(@PathParam("code") String code, @QueryParam("forceDelete") Boolean forceDelete);
	
}
