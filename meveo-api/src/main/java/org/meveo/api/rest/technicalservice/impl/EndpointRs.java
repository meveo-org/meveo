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

import io.swagger.annotations.ApiParam;
import org.jboss.resteasy.annotations.cache.Cache;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.technicalservice.endpoint.EndpointDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.technicalservice.endpoint.EndpointApi;
import org.meveo.model.technicalservice.endpoint.Endpoint;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

/**
 * Rest endpoint for managing service endpoints
 *
 * @author clement.bareth
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @since 04.02.2019
 * @version 6.5.0
 */
@Path("/endpoint")
@DeclareRoles({ "endpointManagement" })
@RolesAllowed({ "endpointManagement" })
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
	public Response delete(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) throws BusinessException, EntityDoesNotExistsException {
		endpointApi.delete(code);
		return Response.noContent().build();
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
	public String getScript(@PathParam("code") @ApiParam("Code of the endpoint") String code) throws EntityDoesNotExistsException, IOException {
		return endpointApi.getEndpointScript(code);
	}

	/**
	 * Find a {@link Endpoint} by code
	 *
	 * @param code Code of the {@link Endpoint} to find
	 */
	@GET
	@Path("/{code}")
	@Produces(MediaType.APPLICATION_JSON)
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
	public Response exists(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) {
		final EndpointDto endpointDto = endpointApi.findByCode(code);
		if (endpointDto != null) {
			return Response.noContent().build();
		}
		return Response.status(404).build();
	}

	/**
	 * Generate open api json of a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to generate open api json
	 */
	@GET
	@Path("/openApi/{code}")
	public Response generateOpenApiJson(@PathParam("code") @NotNull @ApiParam("Code of the endpoint") String code) {

		return endpointApi.generateOpenApiJson(uriContextInfo.getBaseUri().toString(), code);
	}

}
