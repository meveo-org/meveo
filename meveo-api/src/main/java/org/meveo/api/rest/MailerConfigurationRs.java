package org.meveo.api.rest;

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

public interface MailerConfigurationRs extends IBaseRs {

    /**
     * @param mailerConfigurationDto mailer configuration dto
     * @return action status
     */
    @POST
    @Path("/")
    ActionStatus createOrUpdate(MailerConfigurationDto mailerConfigurationDto);
}
