package org.meveo.api.rest.dwh;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.dwh.BarChartDto;
import org.meveo.api.dto.dwh.ChartDto;
import org.meveo.api.dto.dwh.LineChartDto;
import org.meveo.api.dto.dwh.PieChartDto;
import org.meveo.api.dto.response.dwh.GetChartResponse;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.dwh.Chart;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link Chart}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/chart")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("ChartRs")
public interface ChartRs extends IBaseRs {

	/**
	 * Create a new chart
	 * 
	 * @param postData The chart's data
	 * @return Request processing status
	 */
	@POST
	@Path("/")
	@ApiOperation("Create chart information")
	ActionStatus create(@ApiParam("Chart information") ChartDto postData);

	/**
	 * Create a new bar chart
	 * 
	 * @param postData The bar chart's data
	 * @return Request processing status
	 */
	@POST
	@Path("/bar")
	@ApiOperation("Create a new bar chart")
	ActionStatus createBarChart(@ApiParam("Bar chart information") BarChartDto postData);

	/**
	 * Update an existing bar chart
	 * 
	 * @param postData The bar chart's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/bar")
	@ApiOperation("Update an existing bar chart")
	ActionStatus updateBarChart(@ApiParam("Bar chart information") BarChartDto postData);

	/**
	 * Create a new pie chart
	 * 
	 * @param postData The pie chart's data
	 * @return Request processing status
	 */
	@POST
	@Path("/pie")
	@ApiOperation("Create pie chart")
	ActionStatus createPieChart(@ApiParam("Pie chart information") PieChartDto postData);

	/**
	 * Update an existing pie chart
	 * 
	 * @param postData The pie chart's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/pie")
	@ApiOperation("Update an existing pie chart")
	ActionStatus updatePieChart(@ApiParam("Pie chart information") PieChartDto postData);

	/**
	 * Create a new line chart
	 * 
	 * @param postData The line chart's data
	 * @return Request processing status
	 */
	@POST
	@Path("/line")
	@ApiOperation("Create line chart")
	ActionStatus createLineChart(@ApiParam("Line chart information") LineChartDto postData);

	/**
	 * Update an existing line chart
	 * 
	 * @param postData The line chart's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/line")
	@ApiOperation("Update an existing line chart")
	ActionStatus updateLineChart(@ApiParam("Line chart information") LineChartDto postData);

	/**
	 * Update an existing chart
	 * 
	 * @param postData The chart's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation("Update an existing chart")
	ActionStatus update(@ApiParam("Chart information") ChartDto postData);

	/**
	 * Remove an existing chart with a given code
	 * 
	 * @param chartCode The chart's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/")
	@ApiOperation("Remove char by code")
	ActionStatus remove(@QueryParam("chartCode") @ApiParam("Code of the chart") String chartCode);

	/**
	 * Find a chart with a given code
	 * 
	 * @param chartCode The chart's code
	 * @return
	 */
	@GET
	@Path("/")
	@ApiOperation("Find char by code")
	GetChartResponse find(@QueryParam("chartCode") @ApiParam("Code of the chart") String chartCode);

	/**
	 * Create new or update an existing chart with a given code
	 *
	 * @param postData The chart's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation("Create or update char")
	ActionStatus createOrUpdate(@ApiParam("Chart information") ChartDto postData);

}
