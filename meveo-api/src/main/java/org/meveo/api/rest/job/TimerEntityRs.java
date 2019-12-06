package org.meveo.api.rest.job;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.ApiParam;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.job.TimerEntityDto;
import org.meveo.api.dto.response.GetTimerEntityResponseDto;
import org.meveo.api.rest.IBaseRs;

import java.util.List;

/**
 * 
 * @author Manu Liwanag
 * 
 */
@Path("/timerEntity")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface TimerEntityRs extends IBaseRs {

    /**
     * Create a new timer entity
     * 
     * @param postData The timer entity's data
     * @return Request processing status
     */
    @Path("/create")
    @POST
    ActionStatus create(@ApiParam("Time entity information") TimerEntityDto postData);

    /**
     * Update an existing timer entity
     * 
     * @param postData The timer entity's data
     * @return Request processing status
     */
    @Path("/update")
    @POST
    ActionStatus update(@ApiParam("Time entity information") TimerEntityDto postData);

    /**
     * Create new or update an existing timer entity with a given code
     * 
     * @param postData The timer entity's data
     * @return Request processing status
     */
    @Path("/createOrUpdate")
    @POST
    ActionStatus createOrUpdate(@ApiParam("Time entity information") TimerEntityDto postData);

    /**
     * Find a timer entity with a given code 
     * 
     * @param timerEntityCode The timer entity's code
     * @return Return timerEntity
     */
    @Path("/{timerEntityCode}")
    @GET
    GetTimerEntityResponseDto find(@PathParam("timerEntityCode") @ApiParam("Code of the timer entity") String timerEntityCode);

    @GET
    List<TimerEntityDto> list();

}
