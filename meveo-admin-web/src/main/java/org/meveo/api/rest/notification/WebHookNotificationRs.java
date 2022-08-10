package org.meveo.api.rest.notification;

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
import org.meveo.api.dto.notification.WebHookDto;
import org.meveo.api.dto.response.notification.GetWebHookNotificationResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.notification.WebHook;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link WebHook}. A web hook can be tied to an event fire by
 * the system to perform an additional web action.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/notification/webhook")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("WebHookNotificationRs")
public interface WebHookNotificationRs extends IBaseRs {

	/**
	 * Create a new web hook notification
	 * 
	 * @param postData The web hook notification's data
	 * @return Request processing status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create web hook notification")
	ActionStatus create(@ApiParam("Web hook information") WebHookDto postData);

	/**
	 * Update an existing web hook notification
	 * 
	 * @param postData The web hook notification's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update web hook notification")
	ActionStatus update(@ApiParam("Web hook information") WebHookDto postData);

	/**
	 * Find a web hook notification with a given code
	 * 
	 * @param notificationCode The web hook notification's code
	 * @return
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find web hook notification by code")
	GetWebHookNotificationResponseDto find(@QueryParam("notificationCode") @ApiParam("Code of the web hook") String notificationCode);

	/**
	 * Remove an existing web hook notification with a given code
	 * 
	 * @param notificationCode The web hook notification's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{notificationCode}")
	@ApiOperation(value = "Remove web hook notification by code")
	ActionStatus remove(@PathParam("notificationCode") @ApiParam("Code of the web hook") String notificationCode);

	/**
	 * Create new or update an existing web hook notification with a given code
	 * 
	 * @param postData The web hook notification's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update web hook notification")
	ActionStatus createOrUpdate(@ApiParam("Web hook information") WebHookDto postData);
}
