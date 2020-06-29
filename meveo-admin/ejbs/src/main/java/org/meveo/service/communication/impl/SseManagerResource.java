package org.meveo.service.communication.impl;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.meveo.model.notification.WebNotification;
import org.meveo.service.notification.WebNotificationService;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
@Path("/sse")
public class SseManagerResource {

	@Inject
	Logger log;

	@Inject
	WebNotificationService webNotificationService;

	@Inject
	private SseManager sseManager;

	@Context
	private Sse sse;

	@GET
	@Path("/register/{notif}")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public void register(@PathParam("notif") final String notif, @QueryParam("filter") String filterEL,
			@Context SseEventSink sink, @Context HttpServletRequest requestContext, @Context SecurityContext context) {
		String callerIp = requestContext.getRemoteAddr();
		String userName = "";
		if (context != null) {
			Principal principal = context.getUserPrincipal();
			if (principal != null) {
				userName = principal.getName();
			}
		}

		log.debug("register from " + callerIp + " username=" + userName);
		if (sink == null) {
			throw new IllegalStateException("No client connected.");
		}
		if (notif == null) {
			throw new IllegalStateException("No web notification name set.");
		}

		sseManager.register(sse, callerIp, userName, notif, filterEL, sink);
	}

	@POST
	@Path("/publish/{notif}")
	public void broadcast(@PathParam("notif") final String notif, String message,
			@Context HttpServletRequest requestContext, @Context SecurityContext securityContext) throws IOException {
		
		String callerIp = requestContext.getRemoteAddr();
		String userName = "";
		if (securityContext != null) {
			Principal principal = securityContext.getUserPrincipal();
			if (principal != null) {
				userName = principal.getName();
			}
		}
		log.debug(
				"broadcast from " + callerIp + " username=" + userName + " to notif:" + notif + " message:" + message);
		if (notif == null) {
			throw new IllegalStateException("No web notification name set.");
		}
		WebNotification webNotification = webNotificationService.findByCode(notif);
		if (webNotification == null) {
			throw new IllegalStateException("web notification not found.");
		}
		if (!webNotification.isActive()) {
			throw new IllegalStateException("web notification not active.");
		}
		if (!webNotification.isPublicationAllowed()) {
			throw new IllegalStateException("web notification do not allow publication.");
		}
		Map<Object, Object> context = new HashMap<>();
		context.put("PUBLICATION_MESSAGE", message);
		context.put("PUBLICATION_IP", callerIp);
		context.put("PUBLICATION_AUTHOR", userName);
		sseManager.sendMessage(sse, "", webNotification.getCode(), "", message, context);
	}
}
