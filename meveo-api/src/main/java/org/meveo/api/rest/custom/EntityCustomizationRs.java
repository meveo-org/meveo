package org.meveo.api.rest.custom;

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

import io.swagger.annotations.ApiParam;
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

/**
 * @author Andrius Karpavicius
 **/
@Path("/entityCustomization")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface EntityCustomizationRs extends IBaseRs {

    /**
     * Define a new custom entity template including fields and applicable actions
     * 
     * @param dto
     * @return
     */
    @POST
    @Path("/entity/")
    public ActionStatus createEntityTemplate(@ApiParam("Custom entity template information") CustomEntityTemplateDto dto);

    /**
     * Update custom entity template definition
     * 
     * @param dto
     * @return
     */
    @PUT
    @Path("/entity/")
    public ActionStatus updateEntityTemplate(@ApiParam("Custom entity template information") CustomEntityTemplateDto dto);

    /**
     * Remove custom entity template definition given its code
     * 
     * @param customEntityTemplateCode
     * @return
     */
    @DELETE
    @Path("/entity/{customEntityTemplateCode}")
    public ActionStatus removeEntityTemplate(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode);

    /**
     * Get custom entity template definition including its fields and applicable actions
     * 
     * @param customEntityTemplateCode
     * @return customEntityTemplateResponseDto
     */
    @GET
    @Path("/entity/{customEntityTemplateCode}")
    public CustomEntityTemplateResponseDto findEntityTemplate(@PathParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode);

    /**
     * List custom entity templates.
     * 
     * @param customEntityTemplateCode An optional and partial custom entity template code
     */
    @GET
    @Path("/entity/list")
    public CustomEntityTemplatesResponseDto listEntityTemplates(@QueryParam("customEntityTemplateCode") @ApiParam("Code of the custom entity template") String customEntityTemplateCode);

    /**
     * Define new or update existing custom entity template definition
     * 
     * @param dto
     * @return
     */
    @POST
    @Path("/cet/createOrUpdate")
    public ActionStatus createOrUpdateEntityTemplate(@ApiParam("Custom entity template information") CustomEntityTemplateDto dto);
    
    
    /**
     * To be sure the compatibility of above method we will create a new one.
     * Define new or update existing custom entity template definition
     * 
     * @param dto
     * @return
     */
    @POST
    @Path("/entity/createOrUpdate")
    public ActionStatus createOrUpdateCustumizedEntityTemplate(@ApiParam("Custom entity template information") CustomEntityTemplateDto dto);

    /**
     * Customize a standard Meveo entity definition by adding fields and/or custom actions
     * 
     * @param dto
     * @return
     */
    @PUT
    @Path("/customize/")
    public ActionStatus customizeEntity(@ApiParam("Entity customization information") EntityCustomizationDto dto);

    /**
     * Get customizations made on a standard Meveo entity given its class
     * 
     * @param customizedEntityClass Standard Meveo entity class name
     * @return
     */
    @GET
    @Path("/customize/{customizedEntityClass}")
    public EntityCustomizationResponseDto findEntityCustomizations(@PathParam("customizedEntityClass") @ApiParam("Standard meveo entity class name") String customizedEntityClass);

    /**
     * Define a new custom field
     * 
     * @param postData
     * @return
     */
    @POST
    @Path("/field/")
    public ActionStatus createField(@ApiParam("Custom field template information") CustomFieldTemplateDto postData);

    /**
     * Update existing custom field definition
     */
    @PUT
    @Path("/field/")
    public ActionStatus updateField(@ApiParam("Custom field template information") CustomFieldTemplateDto postData);

    /**
     * Remove custom field definition given its code and entity it applies to
     * 
     * @param customFieldTemplateCode Custom field template code
     * @param appliesTo Entity custom field applies to
     * @return
     */
    @DELETE
    @Path("/field/{customFieldTemplateCode}/{appliesTo}")
    public ActionStatus removeField(@PathParam("customFieldTemplateCode") @ApiParam("Code of the custom field template") String customFieldTemplateCode, @PathParam("appliesTo") @ApiParam("Entity custom field applies to") String appliesTo);

    /**
     * Get custom field definition
     * 
     * @param customFieldTemplateCode Custom field template code
     * @param appliesTo Entity custom field applies to
     * @return
     */
    @GET
    @Path("/field/")
    public GetCustomFieldTemplateReponseDto findField(@QueryParam("customFieldTemplateCode") @ApiParam("Code of the custom field template") String customFieldTemplateCode, @QueryParam("appliesTo") @ApiParam("Entity custom field applies to") String appliesTo);

    /**
     * Define new or update existing custom field definition
     * 
     * @param postData
     * @return
     */
    @POST
    @Path("/field/createOrUpdate")
    public ActionStatus createOrUpdateField(@ApiParam("Custom field template information") CustomFieldTemplateDto postData);

    /**
     * Define a new entity action
     * 
     * @param postData
     * @return
     */
    @POST
    @Path("/action/")
    public ActionStatus createAction(@ApiParam("Entity custom action information") EntityCustomActionDto postData);

    /**
     * Update existing entity action definition
     * 
     * @param dto
     * @return
     */
    @PUT
    @Path("/action/")
    public ActionStatus updateAction(@ApiParam("Entity custom action information") EntityCustomActionDto dto);

    /**
     * Remove entity action definition given its code and entity it applies to
     * 
     * @param actionCode Entity action code
     * @param appliesTo Entity that action applies to
     * @return
     */
    @DELETE
    @Path("/action/{actionCode}/{appliesTo}")
    public ActionStatus removeAction(@PathParam("actionCode") @ApiParam("Code of the entity action") String actionCode, @PathParam("appliesTo") @ApiParam("Entity that action applies to") String appliesTo);

    /**
     * Get entity action definition
     * 
     * @param actionCode Entity action code
     * @param appliesTo Entity that action applies to
     * @return
     */
    @GET
    @Path("/action/")
    public EntityCustomActionResponseDto findAction(@QueryParam("actionCode") @ApiParam("Code of the entity action") String actionCode, @QueryParam("appliesTo") @ApiParam("Entity that action applies to") String appliesTo);

    /**
     * Define new or update existing entity action definition
     * 
     * @param dto
     * @return
     */
    @POST
    @Path("/action/createOrUpdate")
    public ActionStatus createOrUpdateAction(@ApiParam("Entity custom action information") EntityCustomActionDto dto);

	/**
	 * Returns a List of BusinessEntities given a CustomFieldTemplate code. The
	 * CustomFieldTemplate is pulled from the database and entityClass is use in
	 * query. For example entity class is of type OfferTemplate, then it will
	 * return a list of OfferTemplates.
	 * 
	 * @param code
	 *            - CFT code
	 * @param wildcode
	 *            - code filter
	 * @return
	 */
	@GET
	@Path("/listBusinessEntityForCFVByCode/")
	BusinessEntityResponseDto listBusinessEntityForCFVByCode(@QueryParam("code") @ApiParam("Code of the custom field template") String code,
			@QueryParam("wildcode") @ApiParam("Code filter") String wildcode);

	/**
	 * Returns a list of filtered CustomFieldTemplate of an entity. The list of
	 * entity is evaluted againsts the entity with the given code.
	 * 
	 * @param appliesTo
	 *            - the type of entity to which the CFT applies. eg OFFER,
	 *            SERVICE.
	 * @param entityCode
	 *            - code of the entity
	 * @return
	 */
    @GET
    @Path("/entity/listELFiltered")
    public EntityCustomizationResponseDto listELFiltered(@QueryParam("appliesTo") @ApiParam("The custom field template applies to the type of entity") String appliesTo, @QueryParam("entityCode") @ApiParam("Code of the entity") String entityCode);

    @POST
    @Path("/entity/action/execute/{actionCode}/{appliesTo}/{entityCode}")
	ActionStatus execute(@PathParam("actionCode") @ApiParam("Code of the action") String actionCode, @PathParam("appliesTo") @ApiParam("The action applies to the entity") String appliesTo,
			@PathParam("entityCode") @ApiParam("Code of the entity")  String entityCode);

}