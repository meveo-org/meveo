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
package org.meveo.api.technicalservice.wsendpoint;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.technicalservice.wsendpoint.WSEndpointDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.wsendpoint.WSEndpoint;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.FunctionService;
import org.meveo.service.script.ScriptInterface;
import org.meveo.service.technicalservice.wsendpoint.WSEndpointService;

/**
 * API for managing technical services endpoints
 *
 */
@Stateless
public class WSEndpointApi extends BaseCrudApi<WSEndpoint, WSEndpointDto> {

	@EJB
	private WSEndpointService wsendpointService;

	@Inject
	private ConcreteFunctionService concreteFunctionService;


	public WSEndpointApi() {
		super(WSEndpoint.class, WSEndpointDto.class);
	}

	public WSEndpointApi(WSEndpointService endpointService, ConcreteFunctionService concreteFunctionService) {
		super(WSEndpoint.class, WSEndpointDto.class);
		this.wsendpointService = endpointService;
		this.concreteFunctionService = concreteFunctionService;
	}

	public String getEndpointScript(String baseUrl, String code) throws EntityDoesNotExistsException, IOException {
		
		//if(code.equals(WSEndpoint.ENDPOINT_INTERFACE_JS)) {
		//	return esGeneratorService.buildBaseEndpointInterface(baseUrl);
		//}
		
		WSEndpoint endpoint = wsendpointService.findByCode(code);
		if (endpoint == null) {
			throw new EntityDoesNotExistsException(WSEndpoint.class, code);
		}

		if (!isUserAuthorized(endpoint)) {
			throw new UserNotAuthorizedException();
		}

		//return esGeneratorService.buildJSInterface(baseUrl, endpoint);
		return null;
	}


	/**
	 * Create a wsendpoint
	 *
	 * @param wsendpointDto Configuration of the wsendpoint
	 * @return the created WSEndpoint
	 */
	public WSEndpoint create(WSEndpointDto wsendpointDto) throws BusinessException {
		try {
			WSEndpoint wsendpoint = fromDto(wsendpointDto);
			wsendpointService.create(wsendpoint);
			return wsendpoint;
		} catch (EntityDoesNotExistsException e) {
			throw new BusinessException("The wsendpoint references a missing element",e); 
		}
	}

	/**
	 * Create or update an endpoint
	 *
	 * @param wsendpointDto Configuration of the endpoint
	 * @return null if wsendpoint was updated, the endpoint if it was created
	 */
	public WSEndpoint createOrReplace(WSEndpointDto wsendpointDto) throws BusinessException {
		WSEndpoint wsendpoint = wsendpointService.findByCode(wsendpointDto.getCode());
		if (wsendpoint != null) {
			update(wsendpoint, wsendpointDto);
			return wsendpoint;
		} else {
			return create(wsendpointDto);
		}
	}

	/**
	 * Remove the ws endpoint with the given code
	 *
	 * @param code Code of the wsendpoint to remove
	 * @throws BusinessException if wsendpoint does not exists
	 */
	public void delete(String code) throws BusinessException, EntityDoesNotExistsException {

		// Retrieve existing entity
		WSEndpoint wsendpoint = wsendpointService.findByCode(code);
		if (wsendpoint == null) {
			throw new EntityDoesNotExistsException("WSEndpoint with code " + code + " does not exists");
		}

		wsendpointService.remove(wsendpoint);

	}

	/**
	 * Retrieve an wsendpoint by its code
	 *
	 * @param code Code of the wsendpoint to retrieve
	 * @return DTO of the wsendpoint corresponding to the given code
	 */
	public WSEndpointDto findByCode(String code) {
		var wsendpoint = wsendpointService.findByCode(code);
		if(wsendpoint == null) {
			return null;
		}
		return toDto(wsendpoint);
	}

	/**
	 * Retrieve wsendpoints by service code
	 *
	 * @param code Code of the service used to filter endpoints
	 * @return DTOs of the endpoints that are associated to the given technical
	 *         service
	 */
	public List<WSEndpointDto> findByServiceCode(String code) {
		return wsendpointService.findByServiceCode(code).stream().map(this::toDto).collect(Collectors.toList());
	}

	/**
	 * Retrieve all wsendpoints
	 *
	 * @return DTOs for all stored endpoints
	 */
	public List<WSEndpointDto> list() {
		return wsendpointService.list().stream().map(this::toDto).collect(Collectors.toList());
	}

	private void update(WSEndpoint wsendpoint, WSEndpointDto wsendpointDto) throws BusinessException {

		try {
			wsendpoint = fromDto(wsendpointDto, wsendpoint);
		} catch (EntityDoesNotExistsException e) {
			throw new BusinessException("The endpoint references a missing element", e); 
		}

		wsendpointService.update(wsendpoint);
	}

	@Override
	public WSEndpointDto toDto(WSEndpoint wsendpoint) {
		WSEndpointDto wsendpointDto = new WSEndpointDto();
		wsendpointDto.setCode(wsendpoint.getCode());
		wsendpointDto.setDescription(wsendpoint.getDescription());
		wsendpointDto.setServiceCode(wsendpoint.getService().getCode());
		wsendpointDto.setSecured(wsendpoint.isSecured());
		return wsendpointDto;
	}

	@Override
	public WSEndpoint fromDto(WSEndpointDto wsendpointDto) throws EntityDoesNotExistsException  {
		return fromDto(wsendpointDto, null);
	}

	public WSEndpoint fromDto(WSEndpointDto wsendpointDto, WSEndpoint wsendpoint) throws EntityDoesNotExistsException  {

		if (wsendpoint == null) {
			wsendpoint = new WSEndpoint();
		}

		// Code
		wsendpoint.setCode(wsendpointDto.getCode());

		// Description
		wsendpoint.setDescription(wsendpointDto.getDescription());

		// Secured
		wsendpoint.setSecured(wsendpointDto.isSecured());
		
		// Technical Service
		if (!StringUtils.isBlank(wsendpointDto.getServiceCode())) {
			try {
				final FunctionService<?, ScriptInterface> functionService = concreteFunctionService
						.getFunctionService(wsendpointDto.getServiceCode());
				Function service = functionService.findByCode(wsendpointDto.getServiceCode());
				wsendpoint.setService(service);
			} catch (ElementNotFoundException e) {
				throw new EntityDoesNotExistsException("wsendpoint's serviceCode is not linked to a function : " + e.getLocalizedMessage());
			}
		}

		return wsendpoint;
	}

	public boolean isUserAuthorized(WSEndpoint endpoint) {
		if(!endpoint.isSecured()) {
			return true;
		}
		return currentUser.hasRole(WSEndpointService.getEndpointPermission(endpoint));
	}

	@Override
	public WSEndpointDto find(String code) throws MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {
		return findByCode(code);
	}

	@Override
	public WSEndpoint createOrUpdate(WSEndpointDto dtoData) throws MeveoApiException, BusinessException {
		return createOrReplace(dtoData);
	}

	@Override
	public IPersistenceService<WSEndpoint> getPersistenceService() {
		return wsendpointService;
	}

	@Override
	public boolean exists(WSEndpointDto dto) {
		return findByCode(dto.getCode()) != null;
	}

	public Response generateOpenApiJson(@NotNull String baseUrl, @NotNull String code) {

		WSEndpoint endpoint = wsendpointService.findByCode(code);
		if (endpoint == null) {
			return Response.noContent().build();
		}

		if (!isUserAuthorized(endpoint)) {
			return Response.status(403).entity("You are not authorized to access this wsendpoint").build();
		}

		//TODO
		//return Response.ok(Json.pretty(swaggerDocService.generateOpenApiJson(baseUrl, endpoint))).build();
		return null;
	}

	/**
	 * Generates the request schema of wsendpoint onMessage function
	 * 
	 * @param code code of the endpoint
	 * @return request schema of the given endpoint
	 */
	public String requestSchema(@NotNull String code) {

		WSEndpoint endpoint = wsendpointService.findByCode(code);

		//TODO
		//return endpointRequestSchemaService.generateRequestSchema(endpoint);
		return null;
	}

	/**
	 * Generates the response schema of wsendpoint onMessage function
	 * 
	 * @param code code of the endpoint
	 * @return response schema of the given endpoint
	 */
	public String responseSchema(@NotNull String code) {

		WSEndpoint endpoint = wsendpointService.findByCode(code);

		//TODO
		//return endpointResponseSchemaService.generateResponseSchema(endpoint);
		return null;
	}

	@Override
	public void remove(WSEndpointDto dto) throws MeveoApiException, BusinessException {
		this.delete(dto.getCode());
	}
	
}
