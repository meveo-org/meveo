package org.meveo.service.technicalservice.wsendpoint;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.CloseReason.CloseCodes;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.wsendpoint.WSEndpoint;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.FunctionService;
import org.meveo.service.script.ScriptInterface;
import org.slf4j.Logger;

@ServerEndpoint("/ws/{endpoint-name}")
@Singleton
public class WebsocketServerEndpoint {
	@Inject
	private Logger log;

	@Inject
	private WSEndpointService wsEndpointService;

	@Inject
	private ConcreteFunctionService concreteFunctionService;

	private Map<String, List<Session>> activeSessionsByEndpointCode = new ConcurrentHashMap<>();

	private void removeSession(Session session) {
		String endpointName = (String) session.getUserProperties().get("endpointName");
		if(endpointName != null) {
			List<Session> sessions = activeSessionsByEndpointCode.get(endpointName);
			if (sessions != null && sessions.contains(session)) {
				sessions.remove(session);
				log.info("removed session, remains {}",sessions.size());
			}
		} else {
			for(List<Session> sessions : activeSessionsByEndpointCode.values()){
				if (sessions != null && sessions.contains(session)) {
					sessions.remove(session);
					log.info("removed session, remains {}",sessions.size());
				}
			}
		}
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config, @PathParam("endpoint-name") String endpointName) {
		log.info("onOpen activeSessionsByEndpointCode.count={}", activeSessionsByEndpointCode.size());
		try {
			String username = null;
			Principal principal = session.getUserPrincipal();
			if (principal != null) {
				username = principal.getName();
			}
			if (endpointName == null) {
				throw new IllegalStateException("No ws endpoint name set.");
			}
			WSEndpoint wsEndpoint = wsEndpointService.findByCode(endpointName);
			if (wsEndpoint == null) {
				throw new IllegalStateException("ws endpoint not found.");
			}
			if (!wsEndpoint.isActive()) {
				throw new IllegalStateException("ws endpoint not active.");
			}
			if (wsEndpoint.isSecured() && username == null) {
				throw new IllegalStateException("ws endpoint not found.");
				// FIXME: check permissions
			}
			if (wsEndpoint.getService() == null) {
				throw new IllegalStateException("invalid ws endpoint, no function set.");
			}

			Function service = wsEndpoint.getService();
			Map<String, Object> context = new HashMap<>();
			FunctionService<?, ScriptInterface> functionService;
			ScriptInterface executionEngine = null;
			List<Session> sessions = activeSessionsByEndpointCode.get(endpointName);
			if (sessions == null) {
				sessions = new ArrayList<>();
				activeSessionsByEndpointCode.put(wsEndpoint.getCode(), sessions);
				log.info("onOpen create session list for {}", wsEndpoint.getCode());
			}
			try {
				functionService = concreteFunctionService.getFunctionService(service.getCode());
				executionEngine = functionService.getExecutionEngine(service.getCode(), context);
				session.getUserProperties().put("executionEngine",executionEngine);
				context.put("WS_EVENT", "open");
				context.put("WS_SESSION", session);
				executionEngine.execute(context);
			} catch (BusinessException e) {
				throw new IllegalArgumentException(
						"WSEndpoint's code " + service.getCode() + "is not valid, function is not found.", e);
			}
			session.getUserProperties().put("endpointName", endpointName);
			//session.getUserProperties().put("context", context);
			//session.getUserProperties().put("executionEngine", executionEngine);
			if (username != null) {
				session.getUserProperties().put("username", username);
			}
			sessions.add(session);
			log.info("endpointName={} with session={} has been successfully registered", endpointName, session.getId());
		} catch (Exception e) {
			try {
				session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, e.getMessage()));
			} catch (IOException ex) {
				log.error("error while trying to close the websocket", ex);
				throw new RuntimeException(e);
			}
		}

	}

	// @RequirePermission(value = DefaultPermission.EXECUTE_ENDPOINT, orRole =
	// DefaultRole.ADMIN)
	@OnMessage
	public String onMessage(Session session, String message) {
		log.info("onMessage activeSessionsByEndpointCode.count={}", activeSessionsByEndpointCode.size());
		String result = "message correctly processed";
		Map<String, Object> context = new HashMap<>();
		ScriptInterface executionEngine = (ScriptInterface) session.getUserProperties().get("executionEngine");
		context.put("WS_SESSION", session);
		context.put("WS_EVENT", "message");
		context.put("WS_MESSAGE", message);
		try {
			executionEngine.execute(context);
		} catch (BusinessException e) {
			result = "error while processing message " + e.getMessage();
		}
		return result;
	}


	// @RequirePermission(value = DefaultPermission.EXECUTE_ENDPOINT, orRole =
	// DefaultRole.ADMIN)
	@OnClose
	public void onClose(Session session, CloseReason reason) {
		log.info("onClose activeSessionsByEndpointCode.count={}", activeSessionsByEndpointCode.size());
		log.info("WebSocket connection closed with CloseCode: " + reason.getCloseCode());
		try {
			Map<String, Object> context = new HashMap<>();
			ScriptInterface executionEngine = (ScriptInterface) session.getUserProperties().get("executionEngine");
			context.put("WS_SESSION", session);
			context.put("WS_EVENT", "close");
			context.put("WS_REASON_CODE", reason.getCloseCode());
			context.put("WS_REASON_PHRASE", reason.getReasonPhrase());
			executionEngine.execute(context);
		} catch (Exception e) {
			log.error("Error while executing script ", e);
		}
		removeSession(session);
	}

	// @RequirePermission(value = DefaultPermission.EXECUTE_ENDPOINT, orRole =
	// DefaultRole.ADMIN)
	@OnError
	public void error(Session session, Throwable t) {
		log.error("error in session {} : {}", session.getId(), t.getMessage());
		try {
			Map<String, Object> context = new HashMap<>();
			ScriptInterface executionEngine = (ScriptInterface) session.getUserProperties().get("executionEngine");
			context.put("WS_SESSION", session);
			context.put("WS_EVENT", "error");
			context.put("WS_ERROR", t.getMessage());
			executionEngine.execute(context);
		} catch (Exception e) {
			log.error("Error while executing script ", e);
		}
		removeSession(session);
	}

	public void sendMessage(String enpointCode, String username, String txtMessage) {
		log.info("sendMessage activeSessionsByEndpointCode.count={}", activeSessionsByEndpointCode.size());
		List<Session> sessions = activeSessionsByEndpointCode.get(enpointCode);
		List<Session> listeningSessions = new ArrayList<>();
		List<Session> sessionsToRemove = new ArrayList<>();
		for (Session session : sessions) {
			if (session.isOpen()) {
				if (username != null && session.getUserProperties().get("username").equals(username)) {
					listeningSessions.add(session);
				}
			} else {
				sessionsToRemove.add(session);
			}
		}
		if (listeningSessions.size() > 0) {
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
			log.info("garbage collected {} sessions, remains {}",sessionsToRemove.size(),sessions.size());
		}
	}

	public void broadcastMessage(String id, String name, String data, Map<Object, Object> context) {
		log.info("broadcastMessage activeSessionsByEndpointCode.count={}", activeSessionsByEndpointCode.size());
		List<Session> sessions = activeSessionsByEndpointCode.get(name);
		List<Session> listeningSessions = new ArrayList<>();
		List<Session> sessionsToRemove = new ArrayList<>();
		for (Session session : sessions) {
			if (session.isOpen()) {
				if (session.getUserProperties().get("filterEL") == null
						|| ((String) session.getUserProperties().get("filterEL")).isEmpty()) {
					listeningSessions.add(session);
				} else {
					try {
						Object res = null;// MeveoValueExpressionWrapper.evaluateExpression(
						// (String)session.getUserProperties().get("filterEL"),
						// context, Boolean.class);
						boolean result = Boolean.TRUE;// (Boolean) res;
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
			log.info("garbage collected {} sessions, remains {}",sessionsToRemove.size(),sessions.size());
		}
	}

}
