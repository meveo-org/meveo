/**
 * 
 */
package org.meveo.api.rest.technicalservice.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public interface IEndpointRs {

	Response create(EndpointDto endpointDto) throws BusinessException;

	Response createOrReplace(EndpointDto endpointDto) throws BusinessException;

	Response list(String serviceCode);

	/**
	 * Delete a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to delete
	 */
	Response delete(String code) throws BusinessException, EntityDoesNotExistsException;

	/**
	 * Find a {@link Endpoint} by code
	 *
	 * @param code Code of the {@link Endpoint} to find
	 */
	Response find(String code);

	/**
	 * Check exist a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to check
	 */
	Response exists(String code);

	/**
	 * Get script of a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to get script
	 */
	String getScript(String code, HttpServletRequest servletRequest) throws EntityDoesNotExistsException, IOException;

	/**
	 * Generate open api json of a {@link Endpoint}
	 *
	 * @param code Code of the {@link Endpoint} to generate open api json
	 */
	Response generateOpenApiJson(String code);

	/**
	 * Generates and returns the request schema of a given endpoint.
	 * 
	 * @param code code of the endpoint
	 * @return request schema of the given endpoint
	 */
	String requestSchema(String code);

	/**
	 * Generates and returns the response schema of a given endpoint.
	 *
	 * @param code code of the endpoint
	 * @return response schema of the given endpoint
	 */
	String responseSchema(String code);

}