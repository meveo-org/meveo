package org.meveo.api.rest.dwh;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.dwh.MeasurableQuantityDto;
import org.meveo.api.dto.response.dwh.GetListMeasurableQuantityResponse;
import org.meveo.api.dto.response.dwh.GetMeasurableQuantityResponse;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.serialize.RestDateParam;
import org.meveo.model.dwh.MeasurableQuantity;
import org.meveo.model.dwh.MeasurementPeriodEnum;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link MeasurableQuantity}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/measurableQuantity")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("MeasurableQuantityRs")
public interface MeasurableQuantityRs extends IBaseRs {

	/**
	 * @param postData posted data to API
	 * @return action status.
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create measurable quantity")
	ActionStatus create(@ApiParam("Measurable quantity information") MeasurableQuantityDto postData);

	/**
	 * Update Measurable quantity from mesearable quantities.
	 * 
	 * @param postData posted data.
	 * @return actions status.
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update measurable quantity")
	ActionStatus update(@ApiParam("Measurable quantity information") MeasurableQuantityDto postData);

	/**
	 * Get Measurable quantity from a given code.
	 * 
	 * @param code Measureable quantity's code
	 * @return
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find measurable quantity by code")
	GetMeasurableQuantityResponse find(@QueryParam("code") @ApiParam("Code of the measureable quantity") String code);

	/**
	 * 
	 * @param code     code of mesurable value.
	 * @param fromDate format yyyy-MM-dd'T'HH:mm:ss or yyyy-MM-dd
	 * @param toDate   format yyyy-MM-dd'T'HH:mm:ss or yyyy-MM-dd
	 * @param period   period in which mesurable value is calculated.
	 * @param mqCode   Measureable quantity's code
	 * @return mesurable value by date and period.
	 */
	@GET
	@Path("/findMVByDateAndPeriod")
	@ApiOperation(value = "Find measurable quantity by date and period")
	Response findMVByDateAndPeriod(@QueryParam("code") @ApiParam("Code of mesurable value") String code,
			@QueryParam("fromDate") @ApiParam("From date") @RestDateParam Date fromDate, @QueryParam("toDate") @ApiParam("To date") @RestDateParam Date toDate,
			@QueryParam("period") @ApiParam("Period in which mesurable value is calculated") MeasurementPeriodEnum period,
			@QueryParam("mqCode") @ApiParam("Code of the measureable quantity") String mqCode);

	/**
	 * Remove Measurable quantity with a given code.
	 * 
	 * @param code Measurable quantity's code
	 * @return action status.
	 */
	@Path("/{code}")
	@DELETE
	@ApiOperation(value = "Remove measurable quantity by code")
	ActionStatus remove(@PathParam("code") @ApiParam("Code of the measurable quantity") String code);

	/**
	 * List Measurable quantity with a given code.
	 * 
	 * @return A list of measurable quantities
	 */
	@Path("/list")
	@GET
	@ApiOperation(value = "List measurable quantity")
	GetListMeasurableQuantityResponse list();

}