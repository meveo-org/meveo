package org.meveo.service.communication.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
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
@ApplicationScoped
@Singleton
public class SseManager {

	// private Map<String, SseBroadcaster> sseBroadcasters = new HashMap<>();
	public static Map<String, Map<String, FilteringSink>> notifFilteringSinks = new HashMap<>();

	@Inject
	Logger log;

	@Inject
	WebNotificationService webNotificationService;

	private Sse sse;

	public void register(Sse sse, String callerIp, String userName, final String notif,
			@QueryParam("filter") String filterEL, @Context SseEventSink sink) {

		WebNotification webNotification = webNotificationService.findByCode(notif);
		if (webNotification == null) {
			throw new IllegalStateException("web notification not found.");
		}
		if (!webNotification.isActive()) {
			throw new IllegalStateException("web notification not active.");
		}
		Map<String, FilteringSink> filteringSinks = SseManager.notifFilteringSinks.get(webNotification.getCode());
		if (filteringSinks == null) {
			filteringSinks = new HashMap<>();
			SseManager.notifFilteringSinks.put(webNotification.getCode(), filteringSinks);
		}
		FilteringSink filteringSink = new FilteringSink(callerIp, userName, filterEL, sink);
		if (filteringSinks.containsKey(filteringSink.getKey())) {
			log.info("the registration already exists for notif:" + notif + " and key " + filteringSink.getKey());
		} else {
			filteringSinks.put(filteringSink.getKey(), filteringSink);
		}

		this.sse = sse;
	}

	/**
	 * This method broadcast a message to all clients that registered for the given
	 * web notification code, and for wich the filter match the context
	 * 
	 * @param id
	 * @param name
	 * @param comment
	 * @param data
	 * @param context
	 */
	public void sendMessage(Sse sseParam, String id, String name, String comment, String data,
			Map<Object, Object> context) {

		if (sseParam != null) {
			this.sse = sseParam;

		} else {
			if (this.sse == null) {
				log.debug("Sse is null because there is no registered client");
				return;
			}
		}

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
					filteringSink.send(event);
				} else {
					closedSinks.put(entry.getKey(), filteringSink);
				}
			}
		}
		if (closedSinks.size() > 0) {
			for (Map.Entry<String, FilteringSink> entry : closedSinks.entrySet()) {
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
				filteringSink.close();
			}
		}
		filteringSinks.remove(notificationCode);
	}
}
