package org.meveo.api.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.MailerConfigurationDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Web service for managing.
 * 
 * @author Hien Bach
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/mailerConfiguration")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("MailerConfigurationRs")
public interface MailerConfigurationRs extends IBaseRs {

	/**
	 * @param mailerConfigurationDto mailer configuration dto
	 * @return action status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create or update mailer configuration")
	ActionStatus createOrUpdate(@ApiParam("Mailer configuration information") MailerConfigurationDto mailerConfigurationDto);
}
