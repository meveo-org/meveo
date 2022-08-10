package org.meveo.api.rest.communication;

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
import org.meveo.api.dto.communication.EmailTemplateDto;
import org.meveo.api.dto.response.communication.EmailTemplateResponseDto;
import org.meveo.api.dto.response.communication.EmailTemplatesResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.communication.email.EmailTemplate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link EmailTemplate}.
 * 
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since Jun 3, 2016 5:40:20 AM
 */
@Path("/communication/emailTemplate")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("EmailTemplateRs")
public interface EmailTemplateRs extends IBaseRs {

	/**
	 * Create an email template by dto
	 *
	 * @param emailTemplateDto The email template's data
	 * @return Request processing status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create an email template")
	ActionStatus create(@ApiParam("Email template information") EmailTemplateDto emailTemplateDto);

	/**
	 * update an emailTemplate by dto
	 *
	 * @param emailTemplateDto The email template's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update email template information")
	ActionStatus update(@ApiParam("Email template information") EmailTemplateDto emailTemplateDto);

	/**
	 * Find an email template with a given code
	 * 
	 * @param code The email template's code
	 * @return Returns an email template
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find an email template")
	EmailTemplateResponseDto find(@QueryParam("code") @ApiParam("Code of the email template") String code);

	/**
	 * remove an emailTemplate by code
	 * 
	 * @param code The email template's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation(value = "Remove an email template")
	ActionStatus remove(@PathParam("code") @ApiParam("Code of the email template") String code);

	/**
	 * List email templates
	 * 
	 * @return List of email templates
	 */
	@GET
	@Path("/list")
	@ApiOperation(value = "List email templates")
	EmailTemplatesResponseDto list();

	/**
	 * Create new or update an existing email template by dto
	 * 
	 * @param emailTemplateDto The email template's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create new or update an existing email template")
	ActionStatus createOrUpdate(@ApiParam("Email template information") EmailTemplateDto emailTemplateDto);
}
