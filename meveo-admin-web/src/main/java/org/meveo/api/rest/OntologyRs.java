package org.meveo.api.rest;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.cache.Cache;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.service.crm.impl.JSONSchemaGenerator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing ontology.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/ontology")
@Produces({ MediaType.APPLICATION_JSON })
@Api("OntologyRs")
public class OntologyRs extends BaseRs {

	@Inject
	private JSONSchemaGenerator jsonSchemaGenerator;

	/**
	 * Returns a schema given the following filters.
	 * 
	 * @param onlyActivated if is only activated
	 * @param categoryCode  code of the category
	 * @return String schema representation
	 */
	@GET
	@Cache(maxAge = 86400)
	@ApiOperation("Finds a schema with the given filters")
	public String getSchema(@DefaultValue("true") @QueryParam("onlyActivated") @ApiParam("Whether to only activated schema") boolean onlyActivated,
			@QueryParam("category") @ApiParam("Code of the category") String categoryCode) {
		return jsonSchemaGenerator.generateSchema("ontology", onlyActivated, categoryCode);
	}
}
