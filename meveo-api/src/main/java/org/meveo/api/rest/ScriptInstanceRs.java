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
import org.meveo.api.dto.ScriptInstanceDto;
import org.meveo.api.dto.response.GetScriptInstanceResponseDto;
import org.meveo.api.dto.response.ScriptInstanceReponseDto;
import org.meveo.model.scripts.ScriptInstance;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link ScriptInstance}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author clement.bareth
 * @version 6.7.0
 */
@Path("/scriptInstance")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("ScriptInstanceRs")
public interface ScriptInstanceRs extends IBaseRs {

	/**
	 * @author clement.bareth
	 * @since 6.6.0
	 * @return the serialized codes of all existing scripts in database
	 */
	@GET
	@Path("/codes")
	@Produces(MediaType.APPLICATION_JSON)
	String getCodes();

	/**
	 * Create a new script instance.
	 * 
	 * @param postData The script instance's data
	 * @return Request processing status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create script instance")
	ScriptInstanceReponseDto create(@ApiParam("ScriptInstance information") ScriptInstanceDto postData);

	/**
	 * Update an existing script instance.
	 * 
	 * @param postData The script instance's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update script instance")
	ScriptInstanceReponseDto update(@ApiParam("ScriptInstance information") ScriptInstanceDto postData);

	/**
	 * Remove an existing script instance with a given code .
	 * 
	 * @param scriptInstanceCode The script instance's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{scriptInstanceCode}")
	@ApiOperation(value = "Remove script instance by code")
	ActionStatus remove(@PathParam("scriptInstanceCode") @ApiParam("Code of the script instance") String scriptInstanceCode);

	/**
	 * Find a script instance with a given code.
	 * 
	 * @param scriptInstanceCode The script instance's code
	 * @return script instance
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find script instance by code")
	GetScriptInstanceResponseDto find(@QueryParam("scriptInstanceCode") @ApiParam("Code of the script instance") String scriptInstanceCode);

	/**
	 * Create new or update an existing script instance with a given code.
	 * 
	 * @param postData The script instance's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update script instance")
	ScriptInstanceReponseDto createOrUpdate(@ApiParam("ScriptInstance information") ScriptInstanceDto postData);

	@POST
	@Path("/clear")
	@ApiOperation(value = "Clear the compiled scripts")
	void clear();
}
