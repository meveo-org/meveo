package org.meveo.api.rest.custom;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.EntityCustomActionDto;
import org.meveo.api.dto.EntityCustomizationDto;
import org.meveo.api.dto.response.BusinessEntityResponseDto;
import org.meveo.api.dto.response.CustomEntityTemplateResponseDto;
import org.meveo.api.dto.response.CustomEntityTemplatesResponseDto;
import org.meveo.api.dto.response.EntityCustomActionResponseDto;
import org.meveo.api.dto.response.EntityCustomizationResponseDto;
import org.meveo.api.dto.response.GetCustomFieldTemplateReponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.customEntities.CustomEntityTemplate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link CustomEntityTemplate}.
 * 
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/entityCustomization")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("EntityCustomizationRs")
public interface EntityCustomizationRs extends IBaseRs {

	/**
	 * Define a new custom entity template including fields and applicable actions
	 * 
	 * @param dto
	 * @return
	 */
	@POST
	@Path("/entity/")
	@ApiOperation(value = "Create custom entity template information")
	ActionStatus createEntityTemplate(@ApiParam("Custom entity template information") CustomEntityTemplateDto dto, @QueryParam("repository") List<String> repositories);

	/**
	 * Update custom entity template definition
	 * 
	 * @param dto
	 * @return
	 */
	@PUT
	@Path("/entity/")
	@ApiOperation(value = "Update custom entity template information")
	ActionStatus updateEntityTemplate(@ApiParam("Custom entity template information") CustomEntityTemplateDto dto, @QueryParam("withData") boolean withData);

	/**
	 * Remove custom entity template definition given its code
	 * 
	 * @param customEntityTemplateCode
	 * @return
	 */
	@DELETE
	@Path("/entity/{customEntityTemplateCode}")
	@ApiOperation(value = "Remove custom entity template information")
	ActionStatus removeEntityTemplate(
				@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode, 
				@QueryParam("withData") @DefaultValue("false") boolean withData
		);

	/**
	 * Get custom entity template definition including its fields and applicable
	 * actions
	 * 
	 * @param customEntityTemplateCode
	 * @return customEntityTemplateResponseDto
	 */
	@GET
	@Path("/entity/{customEntityTemplateCode}")
	@ApiOperation(value = "Find custom entity template information")
	CustomEntityTemplateResponseDto findEntityTemplate(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode);

	/**
	 * List custom entity templates.
	 * 
	 * @param customEntityTemplateCode An optional and partial custom entity
	 *                                 template code
	 */
	@GET
	@Path("/entity/list")
	@ApiOperation(value = "List custom entity template information")
	CustomEntityTemplatesResponseDto listEntityTemplates(@QueryParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode);

	/**
	 * Define new or update existing custom entity template definition
	 * 
	 * @param dto
	 * @return
	 */
	@POST
	@Path("/cet/createOrUpdate")
	@ApiOperation(value = "Create or update custom entity template information")
	ActionStatus createOrUpdateEntityTemplate(@ApiParam("Custom entity template information") CustomEntityTemplateDto dto, @QueryParam("withData") boolean withData, @QueryParam("repository") List<String> repositories);

	/**
	 * To be sure the compatibility of above method we will create a new one. Define
	 * new or update existing custom entity template definition
	 * 
	 * @param dto
	 * @return
	 */
	@POST
	@Path("/entity/createOrUpdate")
	@ApiOperation(value = "Create customized entity template information")
	ActionStatus createOrUpdateCustumizedEntityTemplate(@ApiParam("Custom entity template information") CustomEntityTemplateDto dto, @QueryParam("repository") List<String> repositories);

	/**
	 * Customize a standard Meveo entity definition by adding fields and/or custom
	 * actions
	 * 
	 * @param dto
	 * @return
	 */
	@PUT
	@Path("/customize/")
	@ApiOperation(value = "Information of entity customization")
	ActionStatus customizeEntity(@ApiParam("Entity customization information") EntityCustomizationDto dto);

	/**
	 * Get customizations made on a standard Meveo entity given its class
	 * 
	 * @param customizedEntityClass Standard Meveo entity class name
	 * @return
	 */
	@GET
	@Path("/customize/{customizedEntityClass}")
	@ApiOperation(value = "Find entity customized information")
	EntityCustomizationResponseDto findEntityCustomizations(@PathParam("customizedEntityClass") @ApiParam("Standard meveo entity class name") String customizedEntityClass);

	/**
	 * Define a new custom field
	 * 
	 * @param postData
	 * @return
	 */
	@POST
	@Path("/field/")
	@ApiOperation(value = "Create custom field template information")
	ActionStatus createField(@ApiParam("Custom field template information") CustomFieldTemplateDto postData);

	/**
	 * Update existing custom field definition
	 */
	@PUT
	@Path("/field/")
	@ApiOperation(value = "Update custom field template information")
	ActionStatus updateField(@ApiParam("Custom field template information") CustomFieldTemplateDto postData);

	/**
	 * Remove custom field definition given its code and entity it applies to
	 * 
	 * @param customFieldTemplateCode Custom field template code
	 * @param appliesTo               Entity custom field applies to
	 * @return
	 */
	@DELETE
	@Path("/field/{customFieldTemplateCode}/{appliesTo}")
	@ApiOperation(value = "Remove custom field template information")
	ActionStatus removeField(@PathParam("customFieldTemplateCode") @ApiParam("Code of the custom field template") String customFieldTemplateCode,
			@PathParam("appliesTo") @ApiParam("Entity custom field applies to") String appliesTo);

	/**
	 * Get custom field definition
	 * 
	 * @param customFieldTemplateCode Custom field template code
	 * @param appliesTo               Entity custom field applies to
	 * @return
	 */
	@GET
	@Path("/field/")
	@ApiOperation(value = "Find custom field template information")
	GetCustomFieldTemplateReponseDto findField(@QueryParam("customFieldTemplateCode") @ApiParam("Code of the custom field template") String customFieldTemplateCode,
			@QueryParam("appliesTo") @ApiParam("Entity custom field applies to") String appliesTo);

	/**
	 * Define new or update existing custom field definition
	 * 
	 * @param postData
	 * @return
	 */
	@POST
	@Path("/field/createOrUpdate")
	@ApiOperation(value = "Create or update custom field template information")
	ActionStatus createOrUpdateField(@ApiParam("Custom field template information") CustomFieldTemplateDto postData);

	/**
	 * Define a new entity action
	 * 
	 * @param postData
	 * @return
	 */
	@POST
	@Path("/action/")
	@ApiOperation(value = "Create entity custom action information")
	ActionStatus createAction(@ApiParam("Entity custom action information") EntityCustomActionDto postData);

	/**
	 * Update existing entity action definition
	 * 
	 * @param dto
	 * @return
	 */
	@PUT
	@Path("/action/")
	@ApiOperation(value = "Update entity custom action information")
	ActionStatus updateAction(@ApiParam("Entity custom action information") EntityCustomActionDto dto);

	/**
	 * Remove entity action definition given its code and entity it applies to
	 * 
	 * @param actionCode Entity action code
	 * @param appliesTo  Entity that action applies to
	 * @return
	 */
	@DELETE
	@Path("/action/{actionCode}/{appliesTo}")
	@ApiOperation(value = "Delete entity custom action information")
	ActionStatus removeAction(@PathParam("actionCode") @ApiParam("Code of the entity action") String actionCode,
			@PathParam("appliesTo") @ApiParam("Entity that action applies to") String appliesTo);

	/**
	 * Get entity action definition
	 * 
	 * @param actionCode Entity action code
	 * @param appliesTo  Entity that action applies to
	 * @return
	 */
	@GET
	@Path("/action/")
	@ApiOperation(value = "Find entity custom action information")
	EntityCustomActionResponseDto findAction(@QueryParam("actionCode") @ApiParam("Code of the entity action") String actionCode,
			@QueryParam("appliesTo") @ApiParam("Entity that action applies to") String appliesTo);

	/**
	 * Define new or update existing entity action definition
	 * 
	 * @param dto
	 * @return
	 */
	@POST
	@Path("/action/createOrUpdate")
	@ApiOperation(value = "Create or update entity custom action information")
	ActionStatus createOrUpdateAction(@ApiParam("Entity custom action information") EntityCustomActionDto dto);

	/**
	 * Returns a list of filtered CustomFieldTemplate of an entity. The list of
	 * entity is evaluted againsts the entity with the given code.
	 * 
	 * @param appliesTo  - the type of entity to which the CFT applies. eg OFFER,
	 *                   SERVICE.
	 * @param entityCode - code of the entity
	 * @return
	 */
	@GET
	@Path("/entity/listELFiltered")
	@ApiOperation(value = "List custom field template applies to the type of entity")
	EntityCustomizationResponseDto listELFiltered(@QueryParam("appliesTo") @ApiParam("The custom field template applies to the type of entity") String appliesTo,
			@QueryParam("entityCode") @ApiParam("Code of the entity") String entityCode);

	@POST
	@Path("/entity/action/execute/{actionCode}/{appliesTo}/{entityCode}")
	@ApiOperation(value = "Execute")
	ActionStatus execute(@PathParam("actionCode") @ApiParam("Code of the action") String actionCode,
			@PathParam("appliesTo") @ApiParam("The action applies to the entity") String appliesTo, @PathParam("entityCode") @ApiParam("Code of the entity") String entityCode);

	/**
	 * Generates and returns the response schema of the custom entity template.
	 *
	 * @param cetCode code of the custom entity template
	 * @return response schema of the custom entity template
	 */
	@GET
	@Path("/entity/schema/{cetCode}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Generates and returns the response schema of the custom entity template.")
	Response responseJsonSchema(@PathParam("cetCode") @NotNull @ApiParam("Code of the custom entity template") String cetCode);

}