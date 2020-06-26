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
import org.meveo.api.dto.notification.WebNotificationDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.notification.WebNotificationResponseDto;
import org.meveo.api.dto.response.notification.WebNotificationsResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.notification.WebNotification;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
@Path("/notification/web")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("WebNotificationRs")
public interface WebNotificationRs extends IBaseRs {

	/**
	 * Creates a new web notification
	 * 
	 * @param postData The web notification's data
	 * @return Request processing status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Creates web notification")
	ActionStatus create(@ApiParam("Web notification information") WebNotificationDto postData);

	/**
	 * Updates a new web notification
	 * 
	 * @param postData The web notification's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Updates web notification")
	ActionStatus update(@ApiParam("Web notification information") WebNotificationDto postData);

	/**
	 * Find a web notification with a given code
	 * 
	 * @param webnotificationCode The web notification's code
	 * @return Web notification instance
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find web notification by code")
	WebNotificationResponseDto find(
			@QueryParam("notificationCode") @ApiParam("Code of the web notification") String notificationCode);

	/**
	 * List {@linkplain WebNotification}
	 * 
	 * @return A list of filtered web notification
	 */
	@POST
	@Path("/list")
	@ApiOperation(value = "List web notification information ")
	WebNotificationsResponseDto list(
			@ApiParam("Paging and filtering information") PagingAndFiltering pagingAndFiltering);

	/**
	 * Remove an existing web notification with a given code
	 * 
	 * @param notificationCode The web notification's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{notificationCode}")
	@ApiOperation(value = "Remove web notification by code")
	ActionStatus remove(
			@PathParam("notificationCode") @ApiParam("Code of the web notification") String notificationCode);

	/**
	 * Create new or update an existing web notification with a given code
	 * 
	 * @param postData The web notification's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update web notification")
	ActionStatus createOrUpdate(@ApiParam("web notification information") WebNotificationDto postData);
}
