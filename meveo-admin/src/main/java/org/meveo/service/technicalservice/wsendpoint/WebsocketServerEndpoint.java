package org.meveo.service.technicalservice.wsendpoint;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Stateless;
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
import org.meveo.cache.UserMessageCacheContainerProvider;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.wsendpoint.WSEndpoint;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.FunctionService;
import org.meveo.service.script.ScriptInterface;
import org.slf4j.Logger;

@ServerEndpoint("/ws/{endpoint-name}")
@Stateless
public class WebsocketServerEndpoint {
	@Inject
	private Logger log;

	@Inject
	private WSEndpointService wsEndpointService;

	@Inject
	private ConcreteFunctionService concreteFunctionService;

	@Inject
	private UserMessageCacheContainerProvider userMessageCacheProvider;

	private static final String LIQUICHAIN_MODULE_CODE = "liquichain";

	private static Map<String, List<Session>> activeSessionsByEndpointCode = new ConcurrentHashMap<>();

	private void removeSession(Session session) {
		String endpointName = (String) session.getUserProperties().get("endpointName");
		if(endpointName != null) {
			List<Session> sessions = activeSessionsByEndpointCode.get(endpointName);
			if (sessions != null && sessions.contains(session)) {
				sessions.remove(session);
				log.info("removed session for endpoint {}, remains {}",endpointName,sessions.size());
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
			session.getUserProperties().put("endpointName", endpointName);
			if (username != null) {
				session.getUserProperties().put("username", username);
			}
			try {
				functionService = concreteFunctionService.getFunctionService(service.getCode());
				executionEngine = functionService.getExecutionEngine(service.getCode(), context);
				session.getUserProperties().put("functionCode",service.getCode());
				context.put("WS_EVENT", "open");
				context.put("WS_SESSION", session);
				executionEngine.execute(context);
			} catch (BusinessException e) {
				log.error("error on open",e);
				throw new IllegalArgumentException(
						"WSEndpoint's code " + service.getCode() + "is not valid, function is not found.", e);
			}
			sessions.add(session);
			log.info("endpointName={} with session={} has been successfully registered", endpointName, session.getId());
		} catch (Exception e) {
			log.error("error on open", e);
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
		String result = "message correctly processed";
		Map<String, Object> context = new HashMap<>();
		context.put("WS_SESSION", session);
		context.put("WS_EVENT", "message");
		context.put("WS_MESSAGE", message);
		try {
			FunctionService<?, ScriptInterface>functionService = concreteFunctionService.getFunctionService((String)session.getUserProperties().get("functionCode"));
			ScriptInterface executionEngine  = functionService.getExecutionEngine((String)session.getUserProperties().get("functionCode"), context);
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
		log.info("WebSocket connection closed with CloseCode: " + reason.getCloseCode());
		if(session.getUserProperties().get("functionCode")!=null){
			try {
				Map<String, Object> context = new HashMap<>();
				FunctionService<?, ScriptInterface>functionService = concreteFunctionService.getFunctionService((String)session.getUserProperties().get("functionCode"));
				ScriptInterface executionEngine  = functionService.getExecutionEngine((String)session.getUserProperties().get("functionCode"), context);
				context.put("WS_SESSION", session);
				context.put("WS_EVENT", "close");
				context.put("WS_REASON_CODE", reason.getCloseCode());
				context.put("WS_REASON_PHRASE", reason.getReasonPhrase());
				executionEngine.execute(context);
			} catch (Exception e) {
				log.error("Error while executing script ", e);
			}
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
			FunctionService<?, ScriptInterface>functionService = concreteFunctionService.getFunctionService((String)session.getUserProperties().get("functionCode"));
			ScriptInterface executionEngine  = functionService.getExecutionEngine((String)session.getUserProperties().get("functionCode"), context);
			context.put("WS_SESSION", session);
			context.put("WS_EVENT", "error");
			context.put("WS_ERROR", t.getMessage());
			executionEngine.execute(context);
		} catch (Exception e) {
			log.error("Error while executing script ", e);
		}
		removeSession(session);
	}


	public void consumeUserMessages(Session session, String cacheKey) {
		log.info("fetching and receiving Messages if any ");

		if(StringUtils.isBlank(cacheKey) || session == null){
			throw new RuntimeException("session and cacheKey is mandatory to consume cached messages");
		}

		if(session.isOpen()){
			List<String> removedMessages = new ArrayList<>();
			Optional<List<String>> messages = userMessageCacheProvider.getAllUserMessagesFromCache(cacheKey);
			if(messages.isPresent()){
				List<String> cachedMessages = messages.get();
				Iterator<String> cacheMsgIterator = cachedMessages.iterator();
				try{
					while (cacheMsgIterator.hasNext()) {
						String cacheMessage = cacheMsgIterator.next();
						session.getBasicRemote().sendText(cacheMessage);
						cacheMsgIterator.remove();
					}
					userMessageCacheProvider.updateMessagesInCache(cacheKey, cachedMessages);
				}catch(IOException e){
					log.error("Error while sending cached messages to user");
					e.printStackTrace();
					userMessageCacheProvider.updateMessagesInCache(cacheKey, cachedMessages);
				}
			}
		}
	}

	public void sendMessage(String enpointCode, String username, String txtMessage, boolean persistMessage) {
		String persistCacheKey = enpointCode+"_"+username;
		sendMessage(enpointCode, username, txtMessage, persistCacheKey, persistMessage);
	}

	public void sendMessage(String enpointCode, String username, String txtMessage, String persistCacheKey, boolean persistMessage) {
		log.info("sendMessage ");
		boolean messageSent = false;
		if(username==null){
			throw new RuntimeException("username is mandatory to send message");
		}
		List<Session> sessions = activeSessionsByEndpointCode.get(enpointCode);
		if(sessions!=null){
			messageSent = false;
			for (Session session : sessions) {
				if (session.isOpen()) {
					if (session.getUserProperties().get("username")!=null){
						if(session.getUserProperties().get("username").equals(username)) {
							session.getAsyncRemote().sendText(txtMessage);
							messageSent = true;
						}
					} else {
						log.warn("session {} has no username",session);
					}
				}
			}
		}
		if(persistCacheKey != null && !messageSent && persistMessage){
			userMessageCacheProvider.addUserMessageToCache(persistCacheKey, txtMessage);
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
