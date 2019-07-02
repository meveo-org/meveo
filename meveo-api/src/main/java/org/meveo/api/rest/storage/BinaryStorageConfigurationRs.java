package org.meveo.api.rest.storage;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.storage.BinaryStorageConfigurationResponseDto;
import org.meveo.api.dto.response.storage.BinaryStorageConfigurationsResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.storage.BinaryStorageConfigurationDto;

/**
 * @author Edward P. Legaspi
 */
@Path("/storages/binaries")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface BinaryStorageConfigurationRs extends IBaseRs {

	/**
	 * Create a new binary storage
	 */
	@POST
	@Path("/")
	ActionStatus create(BinaryStorageConfigurationDto postData);

	/**
	 * Update an existing binary storage
	 * 
	 * @param postData The binary storage's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	ActionStatus update(BinaryStorageConfigurationDto postData);

	/**
	 * Create new or update an existing binary storage
	 * 
	 * @param postData The binary storage's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	ActionStatus createOrUpdate(BinaryStorageConfigurationDto postData);

	/**
	 * Search for binary storage with a given code
	 */
	@GET
	@Path("/{code}")
	BinaryStorageConfigurationResponseDto find(@PathParam("code") String code);

	/**
	 * List binary storage
	 * 
	 * @return A list of binary storages
	 */
	@GET
	@Path("/")
	BinaryStorageConfigurationsResponseDto list();

	/**
	 * Remove an existing binary storage with a given code
	 * 
	 * @param code The binary storage's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{code}")
	public ActionStatus remove(@PathParam("code") String code);

}