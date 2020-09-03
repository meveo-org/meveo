package org.meveo.api.rest.custom.impl;

import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.custom.CustomEntityInstanceAuditApi;
import org.meveo.api.dto.custom.CustomEntityInstanceAuditsResponseDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.model.customEntities.CustomEntityTemplate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
@Path("/customEntityInstanceAudits")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("CustomEntityInstanceAuditApiRs")
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class CustomEntityInstanceAuditApiRs extends BaseRs {

	@Inject
	private CustomFieldsCacheContainerProvider cache;

	@Inject
	private CustomEntityInstanceAuditApi customEntityInstanceAuditApi;

	@GET
	@Path("/{cetCode}/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get persistence")
	public CustomEntityInstanceAuditsResponseDto auditLogs(@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode,
			@PathParam("uuid") @ApiParam("uuid") String uuid, @ApiParam("Paging and filtering information") PagingAndFiltering pagingAndFiltering)
			throws EntityDoesNotExistsException, IOException {

		final CustomEntityTemplate customEntityTemplate = cache.getCustomEntityTemplate(cetCode);
		if (customEntityTemplate == null) {
			throw new NotFoundException();
		}

		CustomEntityInstanceAuditsResponseDto result = new CustomEntityInstanceAuditsResponseDto();
		try {
			result = customEntityInstanceAuditApi.list(cetCode, uuid, pagingAndFiltering);

		} catch (InvalidParameterException e) {
			return result;
		}

		return result;
	}
}
