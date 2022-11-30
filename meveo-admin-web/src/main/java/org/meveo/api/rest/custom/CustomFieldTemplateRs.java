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
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.response.GetCustomFieldTemplateReponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.crm.CustomFieldTemplate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link CustomFieldTemplate}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/customFieldTemplate")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("CustomFieldTemplateRs")
public interface CustomFieldTemplateRs extends IBaseRs {

	/**
	 * Define a new custom field
	 * 
	 * @param postData
	 * @return
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create custom field template information")
	ActionStatus create(@ApiParam("Custom field template information") CustomFieldTemplateDto postData);

	/**
	 * Update existing custom field definition
	 * 
	 * @param postData
	 * @return
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update existing custom field definition")
	ActionStatus update(@ApiParam("Custom field template information") CustomFieldTemplateDto postData);

	/**
	 * Remove custom field definition given its code and entity it applies to
	 * 
	 * @param customFieldTemplateCode
	 * @param appliesTo
	 * @return
	 */
	@DELETE
	@Path("/{customFieldTemplateCode}/{appliesTo}")
	@ApiOperation(value = "Remove custom field template information")
	ActionStatus remove(@PathParam("customFieldTemplateCode") @ApiParam("Code of the custom field template") String customFieldTemplateCode,
			@PathParam("appliesTo") @ApiParam("applies to") String appliesTo);

	/**
	 * Get custom field definition
	 * 
	 * @param customFieldTemplateCode
	 * @param appliesTo
	 * @return
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find custom field template information")
	GetCustomFieldTemplateReponseDto find(@QueryParam("customFieldTemplateCode") @ApiParam("Code of the custom field template") String customFieldTemplateCode,
			@QueryParam("appliesTo") @ApiParam("applies to") String appliesTo);

	/**
	 * Define new or update existing custom field definition
	 * 
	 * @param postData
	 * @return
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update custom field template information")
	ActionStatus createOrUpdate(@ApiParam("Custom field template information") CustomFieldTemplateDto postData);
}