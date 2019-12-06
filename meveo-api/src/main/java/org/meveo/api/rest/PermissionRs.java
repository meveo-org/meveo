package org.meveo.api.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.ApiParam;
import org.meveo.api.dto.response.PermissionResponseDto;

@Path("/permission")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface PermissionRs extends IBaseRs {

	/**
     * List of permissions
     * 
     * @return A list of permissions
     */
    @Path("/list")
    @GET
    PermissionResponseDto list();
    
    @Path("/whitelist")
    @PUT
    void addToWhiteList(@FormParam("permission") @ApiParam("Permission") String permission, @FormParam("id") @ApiParam("Id") String id, @FormParam("role") @ApiParam("Role") String role);
    
    @Path("/blacklist")
    @PUT
    void addToBlackList(@FormParam("permission") @ApiParam("Permission") String permission, @FormParam("id") @ApiParam("Id") String id, @FormParam("role") @ApiParam("Role") String role);
    
    @Path("/whitelist")
    @DELETE
    void removeFromWhiteList(@FormParam("permission") @ApiParam("Permission") String permission, @FormParam("id") @ApiParam("Id") String id, @FormParam("role") @ApiParam("Role") String role);
    
    @Path("/blacklist")
    @DELETE
    void removeFromBlackList(@FormParam("permission") @ApiParam("Permission") String permission, @FormParam("id") @ApiParam("Id") String id, @FormParam("role") @ApiParam("Role") String role);
}
