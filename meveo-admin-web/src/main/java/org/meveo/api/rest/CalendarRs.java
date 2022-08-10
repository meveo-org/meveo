package org.meveo.api.rest;

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

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.CalendarDto;
import org.meveo.api.dto.response.GetCalendarResponse;
import org.meveo.api.dto.response.ListCalendarResponse;
import org.meveo.model.catalog.Calendar;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link Calendar}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/calendar")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("CalendarRs")
public interface CalendarRs extends IBaseRs {

	/**
	 * Create a new calendar.
	 * 
	 * @param postData The calendar's data
	 * @return Request processing status
	 */
	@Path("/")
	@POST
	@ApiOperation(value = "Create calendar information")
	ActionStatus create(@ApiParam("Calendar information") CalendarDto postData);

	/**
	 * Update calendar.
	 * 
	 * @param postData calendar infos
	 * @return Request processing status
	 */
	@Path("/")
	@PUT
	@ApiOperation(value = "Update calendar information")
	ActionStatus update(@ApiParam("Calendar information") CalendarDto postData);

	/**
	 * Search for calendar with a given code.
	 * 
	 * @param calendarCode The calendar's code
	 * @return calendar if exists
	 */
	@Path("/")
	@GET
	@ApiOperation(value = "Find calendar information")
	GetCalendarResponse find(@QueryParam("calendarCode") @ApiParam("Code of the calendar") String calendarCode);

	/**
	 * Retrieve a list of all calendars.
	 * 
	 * @return list of all calendars
	 */
	@Path("/list")
	@ApiOperation(value = "Retrieve a list of all calendars")
	@GET
	ListCalendarResponse list();

	/**
	 * Remove calendar with a given code.
	 * 
	 * @param calendarCode The calendar's code
	 * @return action result
	 */
	@Path("/{calendarCode}")
	@DELETE
	@ApiOperation(value = "Remove calendar information")
	ActionStatus remove(@PathParam("calendarCode") @ApiParam("Code of the calendar") String calendarCode);

	/**
	 * Create new or update an existing calendar with a given code.
	 * 
	 * @param postData The calendars data
	 * @return Request processing status
	 */
	@Path("/createOrUpdate")
	@POST
	@ApiOperation(value = "Create or update calendar information")
	ActionStatus createOrUpdate(@ApiParam("Calendar information") CalendarDto postData);

}
