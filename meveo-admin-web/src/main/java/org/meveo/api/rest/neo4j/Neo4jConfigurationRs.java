package org.meveo.api.rest.neo4j;

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
import org.meveo.api.dto.neo4j.Neo4jConfigurationDto;
import org.meveo.api.dto.response.neo4j.Neo4jConfigurationResponseDto;
import org.meveo.api.dto.response.neo4j.Neo4jConfigurationsResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.neo4j.Neo4JConfiguration;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link Neo4JConfiguration}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/neo4j/configurations")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("Neo4jConfigurationRs")
public interface Neo4jConfigurationRs extends IBaseRs {

	/**
	 * Create a new binary storage
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create neo4j configuration")
	ActionStatus create(@ApiParam("Binary storage information") Neo4jConfigurationDto postData);

	/**
	 * Update an existing binary storage
	 * 
	 * @param postData The binary storage's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update neo4j configuration")
	ActionStatus update(@ApiParam("Binary storage information") Neo4jConfigurationDto postData);

	/**
	 * Create new or update an existing binary storage
	 * 
	 * @param postData The binary storage's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update neo4j configuration")
	ActionStatus createOrUpdate(@ApiParam("Binary storage information") Neo4jConfigurationDto postData);

	/**
	 * Search for binary storage with a given code
	 */
	@GET
	@Path("/{code}")
	@ApiOperation(value = "Find neo4j configuration by code")
	Neo4jConfigurationResponseDto find(@PathParam("code") @ApiParam("Code of the binary storage") String code);

	/**
	 * List binary storage
	 * 
	 * @return A list of binary storages
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "list of binary storages")
	Neo4jConfigurationsResponseDto list();

	/**
	 * Remove an existing binary storage with a given code
	 * 
	 * @param code The binary storage's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation(value = "Remove neo4j configuration by code")
	ActionStatus remove(@PathParam("code") @ApiParam("Code of the binary storage") String code);
}
