package org.meveo.service.communication.impl;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.meveo.model.notification.WebNotification;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.notification.WebNotificationService;
import org.slf4j.Logger;

@ServerEndpoint("/wsnotif/{notif-name}")
@Singleton
public class WebsocketNotifManager {

	@Inject
	private Logger log;

	@Inject
	private WebNotificationService webNotificationService;

	private Map<String, List<Session>> notifSessions = new ConcurrentHashMap<>();

	@OnMessage
	public String onMessage(Session session, String message) {
		Map<Object, Object> context = new HashMap<>();
		context.put("PUBLICATION_MESSAGE", message);
		context.put("PUBLICATION_AUTHOR", session.getUserProperties().get("username"));
		sendMessage("", (String) session.getUserProperties().get("notifname"), message, context);
		return "message sent";
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config, @PathParam("notif-name") String notifName) {
		try {
			String username = null;
			Principal principal = session.getUserPrincipal();
			if (principal != null) {
				username = principal.getName();
			}
			if (notifName == null) {
				throw new IllegalStateException("No web notification name set.");
			}
			WebNotification webNotification = webNotificationService.findByCode(notifName);
			if (webNotification == null) {
				throw new IllegalStateException("web notification not found.");
			}
			if (!webNotification.isActive()) {
				throw new IllegalStateException("web notification not active.");
			}

			List<Session> sessions = notifSessions.get(notifName);
			if (sessions == null) {
				sessions = new ArrayList<>();
				notifSessions.put(webNotification.getCode(), sessions);
			}
			String filterEL = null;
			if (session.getRequestParameterMap().containsKey("filter")) {
				filterEL = session.getRequestParameterMap().get("filter").get(0);
				session.getUserProperties().put("filterEL", filterEL);
			}
			session.getUserProperties().put("username", username);
			session.getUserProperties().put("notifname", notifName);
			sessions.add(session);
			log.info("username={} notif={} with session={} has been successfully registered", username, notifName,
					session.getId());
		} catch (Exception e) {
			try {
				session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, e.getMessage()));
			} catch (IOException ex) {
				log.error("error while trying to close the websocket", ex);
				throw new RuntimeException(e);
			}
		}
	}

	private void removeSession(Session session) {
		String notifName = (String) session.getUserProperties().get("notifname");
		List<Session> sessions = notifSessions.get(notifName);
		if (sessions != null && sessions.contains(session)) {
			sessions.remove(session);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		log.info("WebSocket connection closed with CloseCode: " + reason.getCloseCode());
		removeSession(session);
	}

	@OnError
	public void error(Session session, Throwable t) {
		log.error("error in session {} : {}", session.getId(), t.getMessage());
		removeSession(session);
	}

	public void sendMessage(String id, String name, String data, Map<Object, Object> context) {
		List<Session> sessions = notifSessions.get(name);
		List<Session> listeningSessions = new ArrayList<>();
		List<Session> sessionsToRemove = new ArrayList<>();
		for (Session session : sessions) {
			if (session.isOpen()) {
				if (session.getUserProperties().get("filterEL") == null
						|| ((String) session.getUserProperties().get("filterEL")).isEmpty()) {
					listeningSessions.add(session);
				} else {
					try {
						Object res = MeveoValueExpressionWrapper.evaluateExpression(
								(String) session.getUserProperties().get("filterEL"), context, Boolean.class);
						boolean result = (Boolean) res;
						if (result) {
							listeningSessions.add(session);
						}

					} catch (Exception e) {
						throw new IllegalStateException("Expression " + session.getUserProperties().get("filterEL")
								+ " do not evaluate to boolean");
					}
				}
			} else {
				sessionsToRemove.add(session);
			}
		}
		if (listeningSessions.size() > 0) {
			String txtMessage = "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"data\":\"" + data + "\"}";
			for (Session session : listeningSessions) {
				if (session.isOpen()) {
					session.getAsyncRemote().sendText(txtMessage);
				} else {
					sessionsToRemove.add(session);
				}
			}
		}
		if (sessionsToRemove.size() > 0) {
			sessions.removeAll(sessionsToRemove);
		}
	}

	public void removeNotification(String notificationCode) {
		List<Session> sessions = notifSessions.get(notificationCode);
		if (sessions == null) {
			log.debug("remove notification: no one was listening");
			return;
		}

		for (Session session : sessions) {
			try {
				session.close();
			} catch (IOException e) {
				log.debug("exception while closing websocket :{}", e.getMessage());
			}
		}
		notifSessions.remove(notificationCode);
	}
}
