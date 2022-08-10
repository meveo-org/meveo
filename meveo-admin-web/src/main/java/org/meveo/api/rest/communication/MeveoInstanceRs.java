package org.meveo.api.rest.communication;

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
import org.meveo.api.dto.communication.MeveoInstanceDto;
import org.meveo.api.dto.response.communication.MeveoInstanceResponseDto;
import org.meveo.api.dto.response.communication.MeveoInstancesResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.communication.MeveoInstance;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing a {@link MeveoInstance}.
 * 
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since Jun 4, 2016 4:05:47 AM
 */
@Path("/communication/meveoInstance")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("MeveoInstanceRs")
public interface MeveoInstanceRs extends IBaseRs {

	/**
	 * Create a meveoInstance by dto.
	 *
	 * @param meveoInstanceDto meveo instance
	 * @return action status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create meveo instance information")
	ActionStatus create(@ApiParam("Meveo instance information") MeveoInstanceDto meveoInstanceDto);

	/**
	 * Update a meveoInstance by dto
	 *
	 * @param meveoInstanceDto
	 * @return
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update meveo instance information")
	ActionStatus update(@ApiParam("Meveo instance information") MeveoInstanceDto meveoInstanceDto);

	/**
	 * Find a meveoInstance by code
	 *
	 * @param code
	 * @return
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find meveo instance information")
	MeveoInstanceResponseDto find(@QueryParam("code") @ApiParam("Code of the meveo instance") String code);

	/**
	 * Remove a meveoInstance by code
	 *
	 * @param code
	 * @return
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation(value = "Remove meveo instance information")
	ActionStatus remove(@PathParam("code") @ApiParam("Code of the meveo instance") String code);

	/**
	 * List meveoInstances
	 *
	 * @return
	 */
	@GET
	@Path("/list")
	@ApiOperation(value = "List meveoInstances")
	MeveoInstancesResponseDto list();

	/**
	 * CreateOrUpdate a meveoInstance by dto
	 *
	 * @param meveoInstanceDto
	 * @return
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update meveo instance information")
	ActionStatus createOrUpdate(@ApiParam("Meveo instance information") MeveoInstanceDto meveoInstanceDto);

	/**
	 * Copy the remote maven repositories to this local meveo instance.
	 * 
	 * @param code code of the meveo instance
	 * @return status of the response
	 */
	@PUT
	@Path("/{code}/syncRepositories")
	ActionStatus syncRepositories(@PathParam("code") String code);
}
