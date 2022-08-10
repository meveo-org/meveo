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
import org.meveo.api.dto.technicalservice.endpoint.EndpointDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.technicalservice.endpoint.Endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/endpoint")
@DeclareRoles({ "endpointManagement" })
@RolesAllowed({ "endpointManagement" })
@Api("EndpointRs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface IEndpointRs {

	@POST
	Response create(@Valid @NotNull EndpointDto endpointDto) throws BusinessException;
	
	@PUT
	Response createOrReplace(@Valid @NotNull EndpointDto endpointDto) throws BusinessException;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	Response list(@QueryParam("service") String serviceCode);

	/**
	 * Delete a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to delete
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation(value = "Delete endpoint")
	Response delete(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) throws BusinessException, EntityDoesNotExistsException;

	/**
	 * Find a {@link Endpoint} by code
	 *
	 * @param code Code of the {@link Endpoint} to find
	 */
	
	@GET
	@Path("/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Find endpoint by code")
	Response find(@PathParam("code") @NotNull @ApiParam("Code of the endpoint")  String code);

	/**
	 * Check exist a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to check
	 */
	@HEAD
	@Path("/{code}")
	@ApiOperation(value = "Check exist an endpoint")
	Response exists(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code);

	/**
	 * Get script of a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to get script
	 */
	@GET
	@Path("/{code}.js")
    @Cache(maxAge = 86400)
	@Produces("application/javascript")
	@ApiOperation(value = " Get script of the endpoint")
	String getScript(@PathParam("code") @ApiParam("Code of the endpoint") String code, @Context HttpServletRequest servletRequest) throws EntityDoesNotExistsException, IOException;

	/**
	 * Generate open api json of a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to generate open api json
	 */
	@GET
	@Path("/openApi/{code}")
	@ApiOperation(value = "Generate open api json of the endpoint")
	Response generateOpenApiJson(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code);

	/**
	 * Generates and returns the request schema of a given endpoint.
	 * 
	 * @param code code of the endpoint
	 * @return request schema of the given endpoint
	 */
	@GET
	@Path("/schema/{code}/request")
	@ApiOperation(value = "Generates and returns the request schema of a given endpoint.")
	String requestSchema(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code);

	/**
	 * Generates and returns the response schema of a given endpoint.
	 *
	 * @param code code of the endpoint
	 * @return response schema of the given endpoint
	 */
	@GET
	@Path("/schema/{code}/response")
	@ApiOperation(value = "Generates and returns the response schema of a given endpoint.")
	String responseSchema(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code);

}