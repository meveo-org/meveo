package org.meveo.api.rest.custom;

import java.util.List;

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
import org.meveo.model.customEntities.CustomRelationshipTemplate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link CustomRelationshipTemplate}.
 * 
 * @author Rachid AITYAAZZA
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/customRelationshipTemplate")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("CustomRelationshipTemplateRs")
public interface CustomRelationshipTemplateRs extends IBaseRs {

	/**
	 * Define a new custom relationship template including fields
	 * 
	 */
	@POST
	@Path("/relationship/")
	@ApiOperation(value = "Create custom relationship template information")
	ActionStatus createCustomRelationshipTemplate(@ApiParam("Custom relationship template information") CustomRelationshipTemplateDto dto, @QueryParam("repository") List<String> repositories);

	/**
	 * Update custom relationship template definition
	 * 
	 */
	@PUT
	@Path("/relationship/")
	@ApiOperation(value = "Update custom relationship template information")
	ActionStatus updateCustomRelationshipTemplate(@ApiParam("Custom relationship template information") CustomRelationshipTemplateDto dto);

	/**
	 * Remove custom relationship template definition given its code
	 * 
	 */
	@DELETE
	@Path("/relationship/{customCustomRelationshipTemplateCode}/{startCustomEntityTemplateCode}/{endCustomEntityTemplateCode}")
	@ApiOperation(value = "Remove custom relationship template information")
	ActionStatus removeCustomRelationshipTemplate(
			@PathParam("customCustomRelationshipTemplateCode") @ApiParam("Code of the custom relationship template") String customCustomRelationshipTemplateCode);

	/**
	 * Get custom relationship template definition including its fields
	 * 
	 */
	@GET
	@Path("/relationship/{customCustomRelationshipTemplateCode}/{startCustomEntityTemplateCode}/{endCustomEntityTemplateCode}")
	@ApiOperation(value = "Find custom relationship template information")
	CustomRelationshipTemplateResponseDto findCustomRelationshipTemplate(
			@PathParam("customCustomRelationshipTemplateCode") @ApiParam("Code of the custom relationship template") String customCustomRelationshipTemplateCode);

	/**
	 * List custom relationship templates.
	 * 
	 * @param customCustomRelationshipTemplateCode An optional and partial custom
	 *                                             relationship template code
	 */
	@GET
	@Path("/relationship/list")
	@ApiOperation(value = "List custom relationship template information")
	CustomRelationshipTemplatesResponseDto listCustomRelationshipTemplates(
			@QueryParam("customCustomRelationshipTemplateCode") @ApiParam("Code of the custom relationship template") String customCustomRelationshipTemplateCode);

	/**
	 * Define new or update existing custom relationship template definition
	 * 
	 */
	@POST
	@Path("/crt/createOrUpdate")
	@ApiOperation(value = "Create or update custom relationship template information")
	ActionStatus createOrUpdateCustomRelationshipTemplate(@ApiParam("Custom relationship template information") CustomRelationshipTemplateDto dto, @QueryParam("repository") List<String> repositories);

}