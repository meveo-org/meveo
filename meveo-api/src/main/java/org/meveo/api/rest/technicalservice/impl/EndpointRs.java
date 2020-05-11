/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

package org.meveo.api.rest.technicalservice.impl;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.technicalservice.endpoint.EndpointDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.technicalservice.endpoint.EndpointApi;
import org.meveo.model.technicalservice.endpoint.Endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Rest endpoint for managing service endpoints
 *
 * @author clement.bareth
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @since 04.02.2019
 * @version 6.9.0
 */
@Path("/endpoint")
@DeclareRoles({ "endpointManagement" })
@RolesAllowed({ "endpointManagement" })
@Api("EndpointRs")
public class EndpointRs extends BaseRs {

	@EJB
	private EndpointApi endpointApi;

	@Context
	private UriInfo uriContextInfo;

	@POST
	public Response create(@Valid @NotNull EndpointDto endpointDto) throws BusinessException {
		try {
			final Endpoint endpoint = endpointApi.create(endpointDto);
			return Response.status(201).entity(endpoint.getId()).build();
		} catch (NullPointerException e) {
			throw new NotFoundException("Function " + endpointDto.getServiceCode() + "does not exists.");
		}
	}

	@PUT
	public Response createOrReplace(@Valid @NotNull EndpointDto endpointDto) throws BusinessException {
		final Endpoint endpoint = endpointApi.createOrReplace(endpointDto);
		if (endpoint != null) {
			return Response.status(201).entity(endpoint.getId()).build();
		} else {
			return Response.noContent().build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response list(@QueryParam("service") String serviceCode) {
		List<EndpointDto> dtoList;
		if (serviceCode != null) {
			dtoList = endpointApi.findByServiceCode(serviceCode);
		} else {
			dtoList = endpointApi.list();
		}
		return Response.ok(dtoList).build();
	}

	/**
	 * Delete a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to delete
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation(value = "Delete endpoint")
	public Response delete(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) throws BusinessException, EntityDoesNotExistsException {
		endpointApi.delete(code);
		return Response.noContent().build();
	}

	/**
	 * Find a {@link Endpoint} by code
	 *
	 * @param code Code of the {@link Endpoint} to find
	 */
	@GET
	@Path("/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Find endpoint by code")
	public Response find(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) {
		final EndpointDto endpointDto = endpointApi.findByCode(code);
		if (endpointDto != null) {
			return Response.ok(endpointDto).build();
		}
		return Response.status(404).build();
	}

	/**
	 * Check exist a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to check
	 */
	@HEAD
	@Path("/{code}")
	@ApiOperation(value = "Check exist an endpoint")
	public Response exists(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) {
		final EndpointDto endpointDto = endpointApi.findByCode(code);
		if (endpointDto != null) {
			return Response.noContent().build();
		}
		return Response.status(404).build();
	}

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
	public String getScript(@PathParam("code") @ApiParam("Code of the endpoint") String code) throws EntityDoesNotExistsException, IOException {
		return endpointApi.getEndpointScript(uriContextInfo.getBaseUri().toString(), code);
	}

	/**
	 * Generate open api json of a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to generate open api json
	 */
	@GET
	@Path("/openApi/{code}")
	@ApiOperation(value = "Generate open api json of the endpoint")
	public Response generateOpenApiJson(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) {

		return endpointApi.generateOpenApiJson(uriContextInfo.getBaseUri().toString(), code);
	}
	
	/**
	 * Generates and returns the request schema of a given endpoint.
	 * 
	 * @param code code of the endpoint
	 * @return request schema of the given endpoint
	 */
	@GET
	@Path("/schema/{code}/request")
	@ApiOperation(value = "Generates and returns the request schema of a given endpoint.")
	public String requestSchema(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) {
		return endpointApi.requestSchema(code);
	}
	
	/**
	 * Generates and returns the response schema of a given endpoint.
	 * 
	 * @param code code of the endpoint
	 * @return response schema of the given endpoint
	 */
	@GET
	@Path("/schema/{code}/response")
	@ApiOperation(value = "Generates and returns the response schema of a given endpoint.")
	public String responseSchema(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) {
		return endpointApi.responseSchema(code);
	}

}
