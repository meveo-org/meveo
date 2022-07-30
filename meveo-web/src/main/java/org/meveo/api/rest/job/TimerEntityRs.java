package org.meveo.api.rest.job;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.job.TimerEntityDto;
import org.meveo.api.dto.response.GetTimerEntityResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.jobs.TimerEntity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API use to manage {@link TimerEntity}. {@link TimerEntity} is use to run a
 * job on a defined schedule.
 * 
 * @author Manu Liwanag
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/timerEntity")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("TimerEntityRs")
public interface TimerEntityRs extends IBaseRs {

	/**
	 * Create a new timer entity
	 * 
	 * @param postData The timer entity's data
	 * @return Request processing status
	 */
	@Path("/create")
	@POST
	@ApiOperation(value = "Create time entity")
	ActionStatus create(@ApiParam("Time entity information") TimerEntityDto postData);

	/**
	 * Update an existing timer entity
	 * 
	 * @param postData The timer entity's data
	 * @return Request processing status
	 */
	@Path("/update")
	@POST
	@ApiOperation(value = "Update time entity")
	ActionStatus update(@ApiParam("Time entity information") TimerEntityDto postData);

	/**
	 * Create new or update an existing timer entity with a given code
	 * 
	 * @param postData The timer entity's data
	 * @return Request processing status
	 */
	@Path("/createOrUpdate")
	@POST
	@ApiOperation(value = "Create or update time entity")
	ActionStatus createOrUpdate(@ApiParam("Time entity information") TimerEntityDto postData);

	/**
	 * Find a timer entity with a given code
	 * 
	 * @param timerEntityCode The timer entity's code
	 * @return Return timerEntity
	 */
	@Path("/{timerEntityCode}")
	@GET
	@ApiOperation(value = "Find time entity by code")
	GetTimerEntityResponseDto find(@PathParam("timerEntityCode") @ApiParam("Code of the timer entity") String timerEntityCode);

	@GET
	List<TimerEntityDto> list();

}
