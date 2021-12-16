package org.meveo.service.communication.impl;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Singleton;
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
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.meveo.model.notification.WebNotification;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.notification.WebNotificationService;
import org.slf4j.Logger;

/**
 * This class allow web client to subscribe to WebNotifications and publish
 * messages
 */
@Path("/ssenotif")
@Singleton
public class SseManager {

	@Inject
	private Logger log;

	@Inject
	private WebNotificationService webNotificationService;

	@Context
	private Sse sse;

	private Map<String, Map<String, FilteringSink>> notifFilteringSinks = new ConcurrentHashMap<>();

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
		WebNotification webNotification = webNotificationService.findByCode(notif);
		if (webNotification == null) {
			throw new IllegalStateException("web notification not found.");
		}
		if (!webNotification.isActive()) {
			throw new IllegalStateException("web notification not active.");
		}

		Map<String, FilteringSink> filteringSinks = notifFilteringSinks.get(webNotification.getCode());
		if (filteringSinks == null) {
			filteringSinks = new HashMap<>();
			notifFilteringSinks.put(webNotification.getCode(), filteringSinks);
		}
		FilteringSink filteringSink = new FilteringSink(callerIp, userName, filterEL, sink);
		FilteringSink oldFilteringSink = filteringSinks.get(filteringSink.getKey());
		if (oldFilteringSink == null) {
			filteringSinks.put(filteringSink.getKey(), filteringSink);
			log.debug("notif={} with key={} has been successfully registered", notif, filteringSink.getKey());

		} else {
			if (oldFilteringSink.isClosed()) {
				filteringSinks.put(filteringSink.getKey(), filteringSink);
				log.debug("notif={} with key={} has been successfully re-registered", notif, filteringSink.getKey());
			}
		}

//		OutboundSseEvent.Builder eventBuilder = sse.newEventBuilder().name("create").data("hello world");
//		eventBuilder.id("1");
//		OutboundSseEvent event = eventBuilder.reconnectDelay(10000).build();
//
//		filteringSink.send(event);
//		sink.close();
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
		context.put("PUBLICATION_AUTHOR", userName);
		sendMessage("", webNotification.getCode(), "", message, context);
	}

	/**
	 * This method broadcast a message to all clients that registered for the given
	 * web notification code, and for which the filter match the context
	 * 
	 * @param id
	 * @param name
	 * @param comment
	 * @param data
	 * @param context
	 */
	public void sendMessage(String id, String name, String comment, String data, Map<Object, Object> context) {

		Map<String, FilteringSink> filteringSinks = notifFilteringSinks.get(name);
		if (filteringSinks == null) {
			log.debug("cannot send message to " + name + " as no one subscribed to it");
			return;
		}

		Map<String, FilteringSink> closedSinks = new HashMap<>();
		Map<String, FilteringSink> listeningSinks = new HashMap<>();
		for (Map.Entry<String, FilteringSink> entry : filteringSinks.entrySet()) {
			FilteringSink filteringSink = entry.getValue();
			if (filteringSink.isClosed()) {
				closedSinks.put(entry.getKey(), filteringSink);

			} else {
				if (filteringSink.getfilterEL() == null || filteringSink.getfilterEL().isEmpty()) {
					listeningSinks.put(entry.getKey(), filteringSink);

				} else {
					try {
						Object res = MeveoValueExpressionWrapper.evaluateExpression(filteringSink.getfilterEL(),
								context, Boolean.class);
						boolean result = (Boolean) res;
						if (result) {
							listeningSinks.put(entry.getKey(), filteringSink);
						}

					} catch (Exception e) {
						throw new IllegalStateException(
								"Expression " + filteringSink.getfilterEL() + " do not evaluate to boolean");
					}
				}
			}
		}

		if (listeningSinks.size() > 0) {
			OutboundSseEvent.Builder eventBuilder = sse.newEventBuilder().name(name).data(data);
			if (id != null) {
				eventBuilder.id(id);
			}
			if (comment != null) {
				eventBuilder.comment(comment);
			}
			OutboundSseEvent event = eventBuilder.reconnectDelay(10000).build();
			for (Map.Entry<String, FilteringSink> entry : listeningSinks.entrySet()) {
				FilteringSink filteringSink = entry.getValue();
				if (!filteringSink.isClosed()) {
					try {
						filteringSink.send(event);

					} catch (Exception e) {
						e.printStackTrace();
						log.error(e.getMessage());
					}

				} else {
					closedSinks.put(entry.getKey(), filteringSink);
				}
			}
		}

		if (closedSinks.size() > 0) {
			for (Map.Entry<String, FilteringSink> entry : closedSinks.entrySet()) {
				entry.getValue().close();
				filteringSinks.remove(entry.getKey());
			}
		}
	}

	public void removeNotification(String notificationCode) {

		Map<String, FilteringSink> filteringSinks = notifFilteringSinks.get(notificationCode);
		if (filteringSinks == null) {
			log.debug("remove notification: no one was listening");
			return;
		}

		for (Map.Entry<String, FilteringSink> entry : filteringSinks.entrySet()) {
			FilteringSink filteringSink = entry.getValue();
			if (!filteringSink.isClosed()) {
				log.debug("closing sink {}", filteringSink.getKey());
				filteringSink.close();
			}
		}
		filteringSinks.remove(notificationCode);
	}
}
