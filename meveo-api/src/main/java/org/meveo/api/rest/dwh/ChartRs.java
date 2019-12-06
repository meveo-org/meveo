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

import io.swagger.annotations.ApiParam;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.dwh.BarChartDto;
import org.meveo.api.dto.dwh.ChartDto;
import org.meveo.api.dto.dwh.LineChartDto;
import org.meveo.api.dto.dwh.PieChartDto;
import org.meveo.api.dto.response.dwh.GetChartResponse;
import org.meveo.api.rest.IBaseRs;

@Path("/chart")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface ChartRs extends IBaseRs {

    /**
     * Create a new chart
     * 
     * @param postData The chart's data
     * @return Request processing status
     */
    @POST
    @Path("/")
    ActionStatus create(@ApiParam("Chart information") ChartDto postData);

    /**
     * Create a new bar chart
     * 
     * @param postData The bar chart's data
     * @return Request processing status
     */
    @POST
    @Path("/bar")
    ActionStatus createBarChart(@ApiParam("Bar chart information") BarChartDto postData);

    /**
     * Update an existing bar chart
     * 
     * @param postData The bar chart's data
     * @return Request processing status
     */
    @PUT
    @Path("/bar")
    ActionStatus updateBarChart(@ApiParam("Bar chart information") BarChartDto postData);

    /**
     * Create a new pie chart
     * 
     * @param postData The pie chart's data
     * @return Request processing status
     */
    @POST
    @Path("/pie")
    ActionStatus createPieChart(@ApiParam("Pie chart information") PieChartDto postData);

    /**
     * Update an existing pie chart
     * 
     * @param postData The pie chart's data
     * @return Request processing status
     */
    @PUT
    @Path("/pie")
    ActionStatus updatePieChart(@ApiParam("Pie chart information") PieChartDto postData);

    /**
     * Create a new line chart
     * 
     * @param postData The line chart's data
     * @return Request processing status
     */
    @POST
    @Path("/line")
    ActionStatus createLineChart(@ApiParam("Line chart information") LineChartDto postData);

    /**
     * Update an existing line chart
     * 
     * @param postData The line chart's data
     * @return Request processing status
     */
    @PUT
    @Path("/line")
    ActionStatus updateLineChart(@ApiParam("Line chart information") LineChartDto postData);

    /**
     * Update an existing chart
     * 
     * @param postData The chart's data
     * @return Request processing status
     */
    @PUT
    @Path("/")
    ActionStatus update(@ApiParam("Chart information") ChartDto postData);


    /**
     * Remove an existing chart with a given code 
     * 
     * @param chartCode The chart's code
     * @return Request processing status
     */
    @DELETE
    @Path("/")
    ActionStatus remove(@QueryParam("chartCode") @ApiParam("Code of the chart") String chartCode);

    /**
     * Find a chart with a given code 
     * 
     * @param chartCode The chart's code
     * @return
     */
    @GET
    @Path("/")
    GetChartResponse find(@QueryParam("chartCode") @ApiParam("Code of the chart") String chartCode);

    /**
     * Create new or update an existing chart with a given code
     *
     * @param postData The chart's data
     * @return Request processing status
     */
    @POST
    @Path("/createOrUpdate")
    ActionStatus createOrUpdate(@ApiParam("Chart information") ChartDto postData);

}

