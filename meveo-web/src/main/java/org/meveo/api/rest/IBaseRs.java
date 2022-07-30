package org.meveo.api.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.swagger.annotations.ApiOperation;
import org.meveo.api.dto.ActionStatus;

/**
 * @author Edward P. Legaspi
 **/
public interface IBaseRs {

    /**
     * Get version of application
     * 
     * @return Action status with version number as a message
     */
    @GET
    @Path("/version")
    @ApiOperation(value = "index", hidden = true)
    public ActionStatus index();

}
