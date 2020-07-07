/**
 * 
 */
package org.meveo.api.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.cache.Cache;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.FunctionCategoryApi;
import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.api.rest.impl.BaseCrudRs;
import org.meveo.model.scripts.FunctionCategory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
@Path("/function/category")
@Api("FunctionCategoryRs")
@Produces(MediaType.APPLICATION_JSON)
public class FunctionCategoryRs extends BaseCrudRs<FunctionCategory, BusinessEntityDto> {
	
	@Inject
	private FunctionCategoryApi fcApi;

	@Override
	public BaseCrudApi<FunctionCategory, BusinessEntityDto> getBaseCrudApi() {
		return fcApi;
	}
	
	/** @return all function categories */
	@GET
	@ApiOperation("Retrieve all function categories")
	@Cache(maxAge = 3600)
	@Produces(MediaType.APPLICATION_JSON)
	public List<BusinessEntityDto> list() {
		return fcApi.findAll();
	}
	
	

}
