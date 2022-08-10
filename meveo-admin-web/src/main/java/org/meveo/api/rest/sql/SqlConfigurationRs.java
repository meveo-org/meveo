package org.meveo.api.rest.sql;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.sql.SqlConfigurationResponseDto;
import org.meveo.api.dto.response.sql.SqlConfigurationsResponseDto;
import org.meveo.api.dto.sql.SqlConfigurationDto;
import org.meveo.model.sql.SqlConfiguration;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link SqlConfiguration}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since 6.6.0
 */
@Path("/sql/configurations")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("SqlConfigurationRs")
public interface SqlConfigurationRs {

	/**
	 * Create a new sqlConfiguration
	 */
	@POST
	@Path("/")
	@ApiOperation("Create a new sqlConfiguration")
	ActionStatus create(@ApiParam("Data source configuration info") SqlConfigurationDto postData);

	/**
	 * Update an existing sqlConfiguration
	 * 
	 * @param postData The sqlConfiguration's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation("Update an existing sqlConfiguration")
	ActionStatus update(@ApiParam("Data source configuration info") SqlConfigurationDto postData);

	/**
	 * Create new or update an existing sqlConfiguration
	 * 
	 * @param postData The sqlConfiguration's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation("Create new or update an existing sqlConfiguration")
	ActionStatus createOrUpdate(@ApiParam("Data source configuration info") SqlConfigurationDto postData);

	/**
	 * Search for sqlConfiguration with a given code
	 */
	@GET
	@Path("/{code}")
	@ApiOperation("Search for sqlConfiguration with a given code")
	SqlConfigurationResponseDto find(@PathParam("code") @ApiParam("code of the connection") String code);

	/**
	 * List sqlConfiguration
	 * 
	 * @return A list of sqlConfigurations
	 */
	@GET
	@Path("/")
	@ApiOperation("List sql configuration")
	SqlConfigurationsResponseDto list();

	/**
	 * Removes an existing sqlConfiguration with a given code
	 * 
	 * @param code The sqlConfiguration's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation("Removes an existing sqlConfiguration with a given code")
	public ActionStatus remove(@PathParam("code") @ApiParam("code of the connection") String code);
	
	/**
	 * Initializes custom tables for the given configuration
	 * 
	 * @param code Code of the configuration
	 */
	@POST
	@Path("/{code}/initialize")
	@ApiOperation("Initializes custom tables for the given configuration")
	public void initialize(@PathParam("code") @ApiParam("Code of the configuration to initialize") String code);

}
