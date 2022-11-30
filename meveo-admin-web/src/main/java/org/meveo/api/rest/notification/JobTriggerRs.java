package org.meveo.api.rest.notification;

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
import org.meveo.api.dto.notification.JobTriggerDto;
import org.meveo.api.dto.response.notification.GetJobTriggerResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.notification.JobTrigger;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link JobTrigger}.
 * 
 * @author Tyshan Shi
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/notification/jobTrigger")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("JobTriggerRs")
public interface JobTriggerRs extends IBaseRs {

	/**
	 * Create a new job trigger
	 * 
	 * @param postData The job trigger's data
	 * @return Request processing status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create job trigger")
	ActionStatus create(@ApiParam("Job trigger information") JobTriggerDto postData);

	/**
	 * Update an existing job trigger
	 * 
	 * @param postData The job trigger's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update job trigger")
	ActionStatus update(@ApiParam("Job trigger information") JobTriggerDto postData);

	/**
	 * Find a job trigger with a given code
	 * 
	 * @param notificationCode The job trigger's code
	 * @return
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find job trigger by code")
	GetJobTriggerResponseDto find(@QueryParam("notificationCode") @ApiParam("Code of the job trigger") String notificationCode);

	/**
	 * Remove an existing job trigger with a given code
	 * 
	 * @param notificationCode The job trigger's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{notificationCode}")
	@ApiOperation(value = "Remove job trigger by code")
	ActionStatus remove(@PathParam("notificationCode") @ApiParam("Code of the job trigger") String notificationCode);

	/**
	 * Create new or update an existing job trigger with a given code
	 * 
	 * @param postData The job trigger's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update job trigger")
	ActionStatus createOrUpdate(@ApiParam("Job trigger information") JobTriggerDto postData);
}
