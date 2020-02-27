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
import org.meveo.api.rest.IBaseBaseCrudRs;
import org.meveo.api.storage.BinaryStorageConfigurationDto;
import org.meveo.model.storage.BinaryStorageConfiguration;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link BinaryStorageConfiguration}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/storages/binaries")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.MULTIPART_FORM_DATA, "text/csv" })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/csv" })
@Api("BinaryStorageConfigurationRs")
public interface BinaryStorageConfigurationRs extends IBaseBaseCrudRs {

	/**
	 * Create a new binary storage
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create binary storage")
	ActionStatus create(@ApiParam("Binary storage information") BinaryStorageConfigurationDto postData);

	/**
	 * Update an existing binary storage
	 * 
	 * @param postData The binary storage's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update binary storage")
	ActionStatus update(@ApiParam("Binary storage information") BinaryStorageConfigurationDto postData);

	/**
	 * Create new or update an existing binary storage
	 * 
	 * @param postData The binary storage's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update binary storage")
	ActionStatus createOrUpdate(@ApiParam("Binary storage information") BinaryStorageConfigurationDto postData);

	/**
	 * Search for binary storage with a given code
	 */
	@GET
	@Path("/{code}")
	@ApiOperation(value = "Find binary storage by code")
	BinaryStorageConfigurationResponseDto find(@PathParam("code") @ApiParam("Code of the binary storage") String code);

	/**
	 * List binary storage
	 * 
	 * @return A list of binary storages
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "List of binary storages")
	BinaryStorageConfigurationsResponseDto list();

	/**
	 * Remove an existing binary storage with a given code
	 * 
	 * @param code The binary storage's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation(value = "Remove binary storage by code")
	ActionStatus remove(@PathParam("code") @ApiParam("Code of the binary storage") String code);

}