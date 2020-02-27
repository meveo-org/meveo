package org.meveo.api.rest.job;

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
import org.meveo.api.dto.job.JobInstanceDto;
import org.meveo.api.dto.job.JobInstanceInfoDto;
import org.meveo.api.dto.job.TimerEntityDto;
import org.meveo.api.dto.response.job.JobExecutionResultResponseDto;
import org.meveo.api.dto.response.job.JobInstanceResponseDto;
import org.meveo.api.dto.response.job.TimerEntityResponseDto;
import org.meveo.api.rest.IBaseRs;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing job execution.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/job")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("JobRs")
public interface JobRs extends IBaseRs {

	/**
	 * Execute a given job instance info
	 * 
	 * @param postData job instance info's data
	 * @return request processing status
	 */
	@POST
	@Path("/execute")
	@ApiOperation(value = "Execute job")
	JobExecutionResultResponseDto execute(@ApiParam("Job instance info information") JobInstanceInfoDto postData);

	/**
	 * Stop a given job instance info
	 * 
	 * @param jobInstanceCode job instance code
	 * @return request processing status
	 */
	@POST
	@Path("/stop")
	@ApiOperation(value = "Stop job")
	ActionStatus stop(@PathParam("jobInstanceCode") @ApiParam("Code of the job instance") String jobInstanceCode);

	/**
	 * Create a new job instance
	 * 
	 * @param postData The job instance's data
	 * @return request processing status
	 */
	@Path("/create")
	@POST
	@ApiOperation(value = "Create job")
	ActionStatus create(@ApiParam("Job instance info information") JobInstanceDto postData);

	/**
	 * Update an existing job instance
	 * 
	 * @param postData The job instance's data
	 * @return request processing status
	 */
	@Path("/")
	@PUT
	@ApiOperation(value = "Update job")
	ActionStatus update(@ApiParam("Job instance info information") JobInstanceDto postData);

	/**
	 * Create new or update an existing job instance with a given code
	 * 
	 * @param postData The job instance's data
	 * @return request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update job")
	ActionStatus createOrUpdate(@ApiParam("Job instance info information") JobInstanceDto postData);

	/**
	 * Find a job instance with a given code
	 * 
	 * @param jobInstanceCode string to match the code of JobInstance
	 * @return object containing the matched JobInstance
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find job by code")
	JobInstanceResponseDto find(@QueryParam("jobInstanceCode") @ApiParam("Code of the job instance") String jobInstanceCode);

	/**
	 * Remove an existing job instance with a given code
	 * 
	 * @param jobInstanceCode The job instance's code
	 * @return request processing status
	 */
	@DELETE
	@Path("/{jobInstanceCode}")
	@ApiOperation(value = "Remove job by code")
	ActionStatus remove(@PathParam("jobInstanceCode") @ApiParam("Code of the job instance") String jobInstanceCode);

	// timer

	/**
	 * Create a new timer entity
	 * 
	 * @param postData The timer entity's data
	 * @return request processing status
	 */
	@Path("/timer/")
	@POST
	@ApiOperation(value = "Create timer entity")
	ActionStatus createTimer(@ApiParam("Timer entity information") TimerEntityDto postData);

	/**
	 * Update an existing timer entity
	 * 
	 * @param postData The timer entity's data
	 * @return request processing status
	 */
	@Path("/timer/")
	@PUT
	@ApiOperation(value = "Update timer entity")
	ActionStatus updateTimer(@ApiParam("Timer entity information") TimerEntityDto postData);

	/**
	 * Create new or update an existing timer entity with a given code
	 * 
	 * @param postData The timer entity's data
	 * @return request processing status
	 */
	@Path("/timer/createOrUpdate/")
	@POST
	@ApiOperation(value = "Create or update timer entity")
	ActionStatus createOrUpdateTimer(@ApiParam("Timer entity information") TimerEntityDto postData);

	/**
	 * Find a timer with a given code
	 * 
	 * @param timerCode The timer's code
	 * @return request processing status
	 */
	@GET
	@Path("/timer/")
	@ApiOperation(value = "Find timer entity by code")
	TimerEntityResponseDto findTimer(@QueryParam("timerCode") @ApiParam("Code of the time") String timerCode);

	/**
	 * Remove an existing timer with a given code
	 * 
	 * @param timerCode The timer's code
	 * @return request processing status
	 */
	@DELETE
	@Path("/timer/{timerCode}")
	@ApiOperation(value = "Remove timer entity by code")
	ActionStatus removeTimer(@PathParam("timerCode") @ApiParam("Code of the time") String timerCode);

	/**
	 * Find a job execution result with a given id
	 * 
	 * @param code                 string to match the code of the JobInstance
	 * @param jobExecutionResultId A jobExecutionResultId
	 * @return object containing the JobExecutionResultImpl
	 */
	@GET
	@Path("/jobReport")
	@ApiOperation(value = "Find job execution result by code")
	JobExecutionResultResponseDto findJobExecutionResult(@QueryParam("code") @ApiParam("Code of the job instance to match") String code,
			@QueryParam("id") @ApiParam("A job execution result id") Long jobExecutionResultId);

}
