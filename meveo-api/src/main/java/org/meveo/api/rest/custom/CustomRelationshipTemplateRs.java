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

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.CustomRelationshipTemplateDto;
import org.meveo.api.dto.response.CustomRelationshipTemplateResponseDto;
import org.meveo.api.dto.response.CustomRelationshipTemplatesResponseDto;
import org.meveo.api.rest.IBaseRs;

/**
 * @author Rachid AITYAAZZA
 **/
@Path("/customRelationshipTemplate")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface CustomRelationshipTemplateRs extends IBaseRs {

    /**
     * Define a new custom relationship template including fields 
     * 
     */
    @POST
    @Path("/relationship/")
    ActionStatus createCustomRelationshipTemplate(CustomRelationshipTemplateDto dto);

    /**
     * Update custom relationship template definition
     * 
     */
    @PUT
    @Path("/relationship/")
    ActionStatus updateCustomRelationshipTemplate(CustomRelationshipTemplateDto dto);

    /**
     * Remove custom relationship template definition given its code
     * 
     */
    @DELETE
    @Path("/relationship/{customCustomRelationshipTemplateCode}/{startCustomEntityTemplateCode}/{endCustomEntityTemplateCode}")
    ActionStatus removeCustomRelationshipTemplate(@PathParam("customCustomRelationshipTemplateCode") String customCustomRelationshipTemplateCode);

    /**
     * Get custom relationship template definition including its fields 
     * 
     */
    @GET
    @Path("/relationship/{customCustomRelationshipTemplateCode}/{startCustomEntityTemplateCode}/{endCustomEntityTemplateCode}")
    CustomRelationshipTemplateResponseDto findCustomRelationshipTemplate(@PathParam("customCustomRelationshipTemplateCode") String customCustomRelationshipTemplateCode);

    /**
     * List custom relationship templates.
     * 
     * @param customCustomRelationshipTemplateCode An optional and partial custom relationship template code
     */
    @GET
    @Path("/relationship/list")
    CustomRelationshipTemplatesResponseDto listCustomRelationshipTemplates(@QueryParam("customCustomRelationshipTemplateCode") String customCustomRelationshipTemplateCode);

    /**
     * Define new or update existing custom relationship template definition
     * 
     */
    @POST
    @Path("/crt/createOrUpdate")
    ActionStatus createOrUpdateCustomRelationshipTemplate(CustomRelationshipTemplateDto dto);


}