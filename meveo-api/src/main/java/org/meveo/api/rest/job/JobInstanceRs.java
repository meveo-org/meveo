package org.meveo.api.rest.job;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.job.JobInstanceDto;
import org.meveo.api.dto.response.job.JobInstanceResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.jobs.JobInstance;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link JobInstance}.
 * 
 * @author Manu Liwanag
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/jobInstance")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("JobInstanceRs")
public interface JobInstanceRs extends IBaseRs {

	/**
	 * Create a new job instance
	 * 
	 * @param postData The job instance's data
	 * @return Request processing status
	 */
	@Path("/create")
	@POST
	@ApiOperation(value = "Create job instance")
	ActionStatus create(@ApiParam("Job instance information") JobInstanceDto postData);

	/**
	 * Update an existing job instance
	 * 
	 * @param postData The job instance's data
	 * @return Request processing status
	 */
	@Path("/update")
	@POST
	@ApiOperation(value = "Update job instance")
	ActionStatus update(@ApiParam("Job instance information") JobInstanceDto postData);

	/**
	 * Create new or update an existing job instance with a given code
	 * 
	 * @param postData The job instance's data
	 * @return Request processing status
	 */
	@Path("/createOrUpdate")
	@POST
	@ApiOperation(value = "Create or update job instance")
	ActionStatus createOrUpdate(@ApiParam("Job instance information") JobInstanceDto postData);

	/**
	 * Find a job instance with a given code
	 * 
	 * @param jobInstanceCode The job instance's code
	 * @return
	 */
	@Path("/")
	@GET
	@ApiOperation(value = "Find job instance by code")
	JobInstanceResponseDto find(@QueryParam("jobInstanceCode") @ApiParam("Code of the job instance") String jobInstanceCode);

	/**
	 * Remove an existing job instance with a given code
	 * 
	 * @param jobInstanceCode The job instance's code
	 * @return Request processing status
	 */
	@Path("/{jobInstanceCode}")
	@DELETE
	@ApiOperation(value = "Remove job instance by code")
	ActionStatus remove(@PathParam("jobInstanceCode") @ApiParam("Code of the job instance") String jobInstanceCode);

}
