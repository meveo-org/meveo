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
import java.net.URI;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.technicalservice.endpoint.EndpointDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.technicalservice.endpoint.EndpointApi;
import org.meveo.model.technicalservice.endpoint.Endpoint;

/**
 * Rest endpoint for managing service endpoints
 *
 * @author clement.bareth
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @since 04.02.2019
 * @version 6.9.0
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class EndpointRs extends BaseRs implements IEndpointRs {

	@EJB
	private EndpointApi endpointApi;

	@Context
	private UriInfo uriContextInfo;

	@Override
	public Response create(EndpointDto endpointDto) throws BusinessException {
		try {
			final Endpoint endpoint = endpointApi.create(endpointDto);
			return Response.status(201).entity(endpoint.getId()).build();
		} catch (NullPointerException e) {
			throw new NotFoundException("Function " + endpointDto.getServiceCode() + "does not exists.");
		}
	}

	@Override
	public Response createOrReplace(EndpointDto endpointDto) throws BusinessException {
		final Endpoint endpoint = endpointApi.createOrReplace(endpointDto);
		if (endpoint != null) {
			return Response.status(201).entity(endpoint.getId()).build();
		} else {
			return Response.noContent().build();
		}
	}

	@Override
	public Response list(String serviceCode) {
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
	@Override
	public Response delete(String code) throws BusinessException, EntityDoesNotExistsException {
		endpointApi.delete(code);
		return Response.noContent().build();
	}

	/**
	 * Find a {@link Endpoint} by code
	 *
	 * @param code Code of the {@link Endpoint} to find
	 */
	@Override
	public Response find(String code) {
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
	@Override
	public Response exists(String code) {
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
	@Override
	public String getScript(String code, HttpServletRequest servletRequest) throws EntityDoesNotExistsException, IOException {
		final URI contextUri = URI.create(servletRequest.getRequestURL().toString()).resolve(servletRequest.getContextPath());
		return endpointApi.getEndpointScript(contextUri.toString(), code);
	}

	/**
	 * Generate open api json of a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to generate open api json
	 */
	@Override
	public Response generateOpenApiJson(String code) {
		final URI contextUri =URI.create(uriContextInfo.getAbsolutePath().toString()).resolve(httpServletRequest.getContextPath());
		String basePath=contextUri.toString()+"/rest/";
		return endpointApi.generateOpenApiJson(basePath, code);
	}

	/**
	 * Generates and returns the request schema of a given endpoint.
	 * 
	 * @param code code of the endpoint
	 * @return request schema of the given endpoint
	 */
	@Override
	public String requestSchema(String code) {
		return endpointApi.requestSchema(code);
	}

	/**
	 * Generates and returns the response schema of a given endpoint.
	 *
	 * @param code code of the endpoint
	 * @return response schema of the given endpoint
	 */
	@Override
	public String responseSchema(String code) {
		return endpointApi.responseSchema(code);
	}

}
