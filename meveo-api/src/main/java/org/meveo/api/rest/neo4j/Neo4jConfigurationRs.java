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

import io.swagger.annotations.ApiParam;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.neo4j.Neo4jConfigurationDto;
import org.meveo.api.dto.response.neo4j.Neo4jConfigurationResponseDto;
import org.meveo.api.dto.response.neo4j.Neo4jConfigurationsResponseDto;
import org.meveo.api.rest.IBaseRs;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
@Path("/neo4j/configurations")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface Neo4jConfigurationRs extends IBaseRs {

	/**
	 * Create a new binary storage
	 */
	@POST
	@Path("/")
	ActionStatus create(@ApiParam("Binary storage information") Neo4jConfigurationDto postData);

	/**
	 * Update an existing binary storage
	 * 
	 * @param postData The binary storage's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	ActionStatus update(@ApiParam("Binary storage information") Neo4jConfigurationDto postData);

	/**
	 * Create new or update an existing binary storage
	 * 
	 * @param postData The binary storage's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	ActionStatus createOrUpdate(@ApiParam("Binary storage information") Neo4jConfigurationDto postData);

	/**
	 * Search for binary storage with a given code
	 */
	@GET
	@Path("/{code}")
	Neo4jConfigurationResponseDto find(@PathParam("code") @ApiParam("Code of the binary storage") String code);

	/**
	 * List binary storage
	 * 
	 * @return A list of binary storages
	 */
	@GET
	@Path("/")
	Neo4jConfigurationsResponseDto list();

	/**
	 * Remove an existing binary storage with a given code
	 * 
	 * @param code The binary storage's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{code}")
	public ActionStatus remove(@PathParam("code") @ApiParam("Code of the binary storage") String code);
}
