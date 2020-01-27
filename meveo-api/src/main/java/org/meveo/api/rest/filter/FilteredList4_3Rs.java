package org.meveo.api.rest.filter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.meveo.api.dto.filter.FilteredListDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.filter.Filter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link Filter} prior to version 4.4.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/filteredList4_3")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("FilteredList4_3Rs")
public interface FilteredList4_3Rs extends IBaseRs {

	@Path("/")
	@GET
	@ApiOperation(value = "List filter")
	Response list(@QueryParam("filter") @ApiParam("Filter") String filter, @QueryParam("firstRow") @ApiParam("Number of first row") Integer firstRow,
			@QueryParam("numberOfRows") @ApiParam("Number of rows") Integer numberOfRows);

	@Path("/xmlInput")
	@POST
	@ApiOperation(value = "List filter by xml input")
	Response listByXmlInput(@ApiParam("Filtered List information") FilteredListDto postData);

	/**
	 * Execute a search in Elastic Search on all fields (_all field)
	 * 
	 * @param classnamesOrCetCodes Entity classes to match - full class name
	 * @param query                Query - words (will be joined by AND) or query
	 *                             expression (+word1 - word2)
	 * @param from                 Pagination - starting record
	 * @param size                 Pagination - number of records per page
	 * @return Request processing status
	 */
	@Path("/search")
	@GET
	@ApiOperation(value = "Search filte by classnames or cet code")
	Response search(@QueryParam("classnamesOrCetCodes") @ApiParam("Entity classes to match") String[] classnamesOrCetCodes, @QueryParam("query") @ApiParam("Query") String query,
			@QueryParam("from") @ApiParam("Starting record") Integer from, @QueryParam("size") @ApiParam("Number of records per page") Integer size);

	/**
	 * Execute a search in Elastic Search on given fields for given values. Query
	 * values by field are passed in extra query parameters in a form of
	 * fieldName=valueToMatch
	 * 
	 * @param classnamesOrCetCodes Entity classes to match - full class name
	 * @param from                 Pagination - starting record
	 * @param size                 Pagination - number of records per page
	 * @param info                 provides request URI information
	 * @return Request processing status
	 */
	@Path("/searchByField")
	@GET
	@ApiOperation(value = "Search filter by field")
	Response searchByField(@QueryParam("classnamesOrCetCodes") @ApiParam("Entity classes to match") String[] classnamesOrCetCodes,
			@QueryParam("from") @ApiParam("Starting record") Integer from, @QueryParam("size") @ApiParam("Number of records per page") Integer size,
			@Context @ApiParam("Request URI information") UriInfo info);
}