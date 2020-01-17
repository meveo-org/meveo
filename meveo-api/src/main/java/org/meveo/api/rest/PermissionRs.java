package org.meveo.api.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.response.PermissionResponseDto;
import org.meveo.model.security.Permission;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link Permission}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/permission")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("PermissionRs")
public interface PermissionRs extends IBaseRs {

	/**
	 * List of permissions
	 * 
	 * @return A list of permissions
	 */
	@Path("/list")
	@GET
	@ApiOperation(value = "List of permissions")
	PermissionResponseDto list();

	@Path("/whitelist")
	@PUT
	@ApiOperation(value = "Add to white list information")
	void addToWhiteList(@FormParam("permission") @ApiParam("Permission") String permission, @FormParam("id") @ApiParam("Id") String id,
			@FormParam("role") @ApiParam("Role") String role);

	@Path("/blacklist")
	@PUT
	@ApiOperation(value = "Add to black list information")
	void addToBlackList(@FormParam("permission") @ApiParam("Permission") String permission, @FormParam("id") @ApiParam("Id") String id,
			@FormParam("role") @ApiParam("Role") String role);

	@Path("/whitelist")
	@DELETE
	@ApiOperation(value = "Remove from white list by permission and id and role")
	void removeFromWhiteList(@FormParam("permission") @ApiParam("Permission") String permission, @FormParam("id") @ApiParam("Id") String id,
			@FormParam("role") @ApiParam("Role") String role);

	@Path("/blacklist")
	@DELETE
	@ApiOperation(value = "Remove from black list by permission and id and role")
	void removeFromBlackList(@FormParam("permission") @ApiParam("Permission") String permission, @FormParam("id") @ApiParam("Id") String id,
			@FormParam("role") @ApiParam("Role") String role);
}
