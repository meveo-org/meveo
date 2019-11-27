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

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @version 6.6.0
 * @since 6.6.0
 */
@Path("/sql/configurations")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface SqlConfigurationRs {

	/**
	 * Create a new sqlConfiguration
	 */
	@POST
	@Path("/")
	ActionStatus create(SqlConfigurationDto postData);

	/**
	 * Update an existing sqlConfiguration
	 * 
	 * @param postData The sqlConfiguration's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	ActionStatus update(SqlConfigurationDto postData);

	/**
	 * Create new or update an existing sqlConfiguration
	 * 
	 * @param postData The sqlConfiguration's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	ActionStatus createOrUpdate(SqlConfigurationDto postData);

	/**
	 * Search for sqlConfiguration with a given code
	 */
	@GET
	@Path("/{code}")
	SqlConfigurationResponseDto find(@PathParam("code") String code);

	/**
	 * List sqlConfiguration
	 * 
	 * @return A list of sqlConfigurations
	 */
	@GET
	@Path("/")
	SqlConfigurationsResponseDto list();

	/**
	 * Remove an existing sqlConfiguration with a given code
	 * 
	 * @param code The sqlConfiguration's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{code}")
	public ActionStatus remove(@PathParam("code") String code);

}
