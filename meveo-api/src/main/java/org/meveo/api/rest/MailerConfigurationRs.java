package org.meveo.api.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.MailerConfigurationDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Web service for managing.
 * 
 * @author Hien Bach
 **/
@Path("/mailerConfiguration")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("Mailer configuration")
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
