package org.meveo.api.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.FilterDto;
import org.meveo.api.dto.response.GetFilterResponseDto;
import org.meveo.model.filter.Filter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link Filter}.
 * 
 * @author Tyshan Shi
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/filter")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("FilterRs")
public interface FilterRs extends IBaseRs {

	/**
	 * Create new or update an existing filter with a given code
	 * 
	 * @param postData The filter's data
	 * @return Request processing status
	 */
	@Path("/createOrUpdate")
	@POST
	@ApiOperation(value = "Create or update filter")
	ActionStatus createOrUpdate(@ApiParam("Filter information") FilterDto postData);

	/**
	 * Find a filter with a given code
	 *
	 * @param filterCode The job instance's code
	 * @return
	 */
	@Path("/")
	@GET
	@ApiOperation(value = "Find filter by code")
	GetFilterResponseDto find(@QueryParam("filterCode") @ApiParam("Code of the filter") String filterCode);
}
