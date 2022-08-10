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
import org.meveo.api.dto.notification.ScriptNotificationDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.notification.GetNotificationResponseDto;
import org.meveo.api.dto.response.notification.InboundRequestsResponseDto;
import org.meveo.api.dto.response.notification.NotificationHistoriesResponseDto;
import org.meveo.api.dto.response.notification.ScriptNotificationsResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.notification.ScriptNotification;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link script notification}. Supported events are available
 * at {@link script notificationEventTypeEnum}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 **/
@Path("/notification/script")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("ScriptNotificationRs")
public interface ScriptNotificationRs extends IBaseRs {

	/**
	 * Create a new script notification
	 * 
	 * @param postData The script notification's data
	 * @return Request processing status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create script notification")
	ActionStatus create(@ApiParam("Notification information") ScriptNotificationDto postData);

	/**
	 * Update an existing script notification
	 * 
	 * @param postData The script notification's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update script notification")
	ActionStatus update(@ApiParam("Notification information") ScriptNotificationDto postData);

	/**
	 * Find a script notification with a given code
	 * 
	 * @param script notificationCode The script notification's code
	 * @return
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find script notification by code")
	GetNotificationResponseDto find(
			@QueryParam("notificationCode") @ApiParam("Code of the script notification") String notificationCode);

	/**
	 * List {@linkplain ScriptNotification}
	 * 
	 * @return A list of script notification
	 */
	@POST
	@Path("/list")
	@ApiOperation(value = "List web notification information ")
	ScriptNotificationsResponseDto list(
			@ApiParam("Paging and filtering information") PagingAndFiltering pagingAndFiltering);

	/**
	 * Remove an existing script notification with a given code
	 * 
	 * @param script notificationCode The script notification's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{notificationCode}")
	@ApiOperation(value = "Remove script notification by code")
	ActionStatus remove(
			@PathParam("notificationCode") @ApiParam("Code of the script notification") String notificationCode);

	/**
	 * List the script notification history
	 * 
	 * @return
	 */
	@GET
	@Path("/listNotificationHistory")
	NotificationHistoriesResponseDto listNotificationHistory();

	/**
	 * List inbound requests
	 * 
	 * @return
	 */
	@GET
	@Path("/listInboundRequest")
	InboundRequestsResponseDto listInboundRequest();

	/**
	 * Create new or update an existing script notification with a given code
	 * 
	 * @param postData The script notification's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update script notification")
	ActionStatus createOrUpdate(@ApiParam("Notification information") ScriptNotificationDto postData);
}
