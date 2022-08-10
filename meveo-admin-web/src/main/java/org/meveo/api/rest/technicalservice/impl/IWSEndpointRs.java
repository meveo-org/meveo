/**
 * 
 */
package org.meveo.api.rest.technicalservice.impl;

import java.io.IOException;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.Cache;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.technicalservice.wsendpoint.WSEndpointDto;
import org.meveo.api.exception.EntityDoesNotExistsException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/wsendpoint")
@DeclareRoles({ "endpointManagement" })
@RolesAllowed({ "endpointManagement" })
@Api("WSEndpointRs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface IWSEndpointRs {

	@POST
	Response create(@Valid @NotNull WSEndpointDto endpointDto) throws BusinessException;
	
	@PUT
	Response createOrReplace(@Valid @NotNull WSEndpointDto endpointDto) throws BusinessException;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Response list(@QueryParam("service") String serviceCode);

	/**
	 * Delete a {@link WSEndpoint}
	 *
	 * @param code Code of the {@link WSEndpoint} to delete
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation(value = "Delete wsendpoint")
	Response delete(@PathParam("code") @NotNull @ApiParam("Code of the wsendpoint") String code) throws BusinessException, EntityDoesNotExistsException;

	/**
	 * Find a {@link WSEndpoint} by code
	 *
	 * @param code Code of the {@link WSEndpoint} to find
	 */
	
	@GET
	@Path("/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Find wsendpoint by code")
	Response find(@PathParam("code") @NotNull @ApiParam("Code of the wsendpoint")  String code);

	/**
	 * Check exist a {@link WSEndpoint}
	 *
	 * @param code Code of the {@link WSEndpoint} to check
	 */
	@HEAD
	@Path("/{code}")
	@ApiOperation(value = "Check exist a wsendpoint")
	Response exists(@PathParam("code") @NotNull @ApiParam("Code of the wsendpoint") String code);

	/**
	 * Get script of a {@link WSEndpoint}
	 *
	 * @param code Code of the {@link WSEndpoint} to get script
	 */
	@GET
	@Path("/{code}.js")
    @Cache(maxAge = 86400)
	@Produces("application/javascript")
	@ApiOperation(value = " Get script of the wsendpoint")
	String getScript(@PathParam("code") @ApiParam("Code of the wsendpoint") String code, @Context HttpServletRequest servletRequest) throws EntityDoesNotExistsException, IOException;

	/**
	 * Generate open api json of a {@link WSEndpoint}
	 *
	 * @param code Code of the {@link WSEndpoint} to generate open api json
	 */
	@GET
	@Path("/openApi/{code}")
	@ApiOperation(value = "Generate open api json of the wsendpoint")
	Response generateOpenApiJson(@PathParam("code") @NotNull @ApiParam("Code of the wsendpoint") String code);

	/**
	 * Generates and returns the request schema of a given wsendpoint onMessage data.
	 * 
	 * @param code code of the wsendpoint
	 * @return request schema of the given wsendpoint
	*/
	@GET
	@Path("/schema/{code}/request")
	@ApiOperation(value = "Generates and returns the request schema of a given wsendpoint onMessage data.")
	String requestSchema(@PathParam("code") @NotNull @ApiParam("Code of the wsendpoint onMessage data") String code);

	/**
	 * Generates and returns the response schema of a given wsendpoint onMessage data.
	 *
	 * @param code code of the wsendpoint
	 * @return response schema of the given wsendpoint onMessage data
    */
	@GET
	@Path("/schema/{code}/response")
	@ApiOperation(value = "Generates and returns the response schema of a given wsendpoint onMessage data.")
	String responseSchema(@PathParam("code") @NotNull @ApiParam("Code of the wsendpoint onMessage data") String code);
}