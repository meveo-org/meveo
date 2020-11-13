package org.meveo.api.rest.custom;

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
import org.meveo.api.dto.CustomEntityInstanceDto;
import org.meveo.api.dto.response.CustomEntityInstanceResponseDto;
import org.meveo.api.dto.response.CustomEntityInstancesResponseDto;
import org.meveo.api.dto.response.GetStatesResponse;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.rest.persistence.PersistenceRs;
import org.meveo.model.customEntities.CustomEntityInstance;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link CustomEntityInstance}.
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @deprecated Use {@link PersistenceRs} instead  with endpoint /{repository}/persistence.
 **/
@Path("/customEntityInstance")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("CustomEntityInstanceRs")
@Deprecated(since = "6.13")
public interface CustomEntityInstanceRs extends IBaseRs {

	/**
	 * Create a new custom entity instance using a custom entity template.
	 *
	 * @param dto                      The custom entity instance's data
	 * @param customEntityTemplateCode The custom entity template's code
	 * @return Request processing status
	 */
	@POST
	@Path("/{customEntityTemplateCode}")
	@ApiOperation(value = "Create code of the custom entity template")
	ActionStatus create(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode,
			@ApiParam("Custom entity instance information") CustomEntityInstanceDto dto);

	/**
	 * Update an existing custom entity instance using a custom entity template
	 * 
	 * @param dto                      The custom entity instance's data
	 * @param customEntityTemplateCode The custom entity template's code
	 * @return Request processing status
	 */
	@PUT
	@Path("/{customEntityTemplateCode}")
	@ApiOperation(value = "Update code of the custom entity template")
	ActionStatus update(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode,
			@ApiParam("Custom entity instance information") CustomEntityInstanceDto dto);

	/**
	 * Remove an existing custom entity instance with a given code from a custom
	 * entity template given by code
	 * 
	 * @param customEntityTemplateCode The custom entity template's code
	 * @param code                     The custom entity instance's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{customEntityTemplateCode}/{code}")
	@ApiOperation(value = "Delete custom entity template by code")
	ActionStatus remove(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode,
			@PathParam("code") @ApiParam("Code of the custom entity instance") String code);

	/**
	 * Find a #### with a given (exemple) code .
	 * 
	 * @param customEntityTemplateCode The custom entity template's code
	 * @param code                     The custom entity instance's code
	 * @return Return a customEntityInstance
	 */
	@GET
	@Path("/{customEntityTemplateCode}/{code}")
	@ApiOperation(value = "Find custom entity template by code")
	CustomEntityInstanceResponseDto find(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode,
			@PathParam("code") @ApiParam("Code of the custom entity instance") String code);

	/**
	 * List custom entity instances.
	 * 
	 * @param customEntityTemplateCode The custom entity instance's code
	 * @return A list of custom entity instances
	 */
	@GET
	@Path("/list/{customEntityTemplateCode}")
	@ApiOperation(value = "List custom entity template")
	CustomEntityInstancesResponseDto list(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity instance") String customEntityTemplateCode);

	/**
	 * Create new or update an existing custom entity instance with a given code.
	 * 
	 * @param dto                      The custom entity instance's data
	 * @param customEntityTemplateCode code of custome entity template.
	 * @return Request processing status
	 */
	@POST
	@Path("/{customEntityTemplateCode}/createOrUpdate")
	@ApiOperation(value = "Create or update code of the custom entity template")
	ActionStatus createOrUpdate(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode,
			@ApiParam("Custom entity instance information") CustomEntityInstanceDto dto);

	/**
	 * List states available of a given CEI
	 *
	 * @param customEntityTemplateCode The custom entity template's code
	 * @param customFieldTemplateCode The custom field template's code
	 * @param uuid The custom entity instance's uuid
	 * @return A list of states available of a given CEI
	 */
	@GET
	@Path("/states/{customEntityTemplateCode}/{customFieldTemplateCode}/{uuid}")
	@ApiOperation(value = "List states available of a given CEI")
	GetStatesResponse listStatesOfCei(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode, @PathParam("customFieldTemplateCode") @ApiParam("Code of the custom field template") String customFieldTemplateCode,
									  @PathParam("uuid") @ApiParam("Uuid of custom entity instance") String uuid);
}