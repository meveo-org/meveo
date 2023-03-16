/**
 * 
 */
package org.meveo.service.technicalservice.wsendpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.technicalservice.wsendpoint.Websocket;
import org.meveo.service.script.ScriptInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketExecutionService {

	private static Logger log = LoggerFactory.getLogger(WebsocketExecutionService.class);

	public boolean onOpen(Session session, EndpointConfig config, Websocket wsEndpoint, ScriptInterface executionEngine) {
		Map<String, Object> context = new HashMap<>();
		
		try {
			try {
				context.put("WS_EVENT", "open");
				context.put("WS_SESSION", session);
				executionEngine.execute(context);
			} catch (BusinessException e) {
				log.error("error on open", e);
			}
			
			log.info("endpointName={} with session={} has been successfully registered", wsEndpoint.getCode(), session.getId());
		} catch (Exception e) {
			log.error("error on open", e);
			try {
				session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, e.getMessage()));
			} catch (IOException ex) {
				log.error("error while trying to close the websocket", ex);
				throw new RuntimeException(e);
			}
			return false;
		}
		
		return true;
	}
	
	public String onMessage(Session session, String message, ScriptInterface executionEngine) {
		String result = null;
		
		Map<String, Object> context = new HashMap<>();
		context.put("WS_SESSION", session);
		context.put("WS_EVENT", "message");
		context.put("WS_MESSAGE", message);
		try {
			executionEngine.execute(context);
			return (String) context.get("WS_RESPONSE");
			
		} catch (BusinessException e) {
			result = "error while processing message " + e.getMessage();
		}
		return result;
	}
	
	public void onClose(Session session, CloseReason reason, ScriptInterface executionEngine) {
		log.info("WebSocket connection closed with CloseCode: " + reason.getCloseCode());
		try {
			Map<String, Object> context = new HashMap<>();
			context.put("WS_SESSION", session);
			context.put("WS_EVENT", "close");
			context.put("WS_REASON_CODE", reason.getCloseCode());
			context.put("WS_REASON_PHRASE", reason.getReasonPhrase());
			executionEngine.execute(context);
		} catch (Exception e) {
			log.error("Error while executing script ", e);
		}
	}
	
	public void error(Session session, Throwable t, ScriptInterface executionEngine) {
		log.error("error in session {} : {}", session.getId(), t.getMessage());
		try {
			Map<String, Object> context = new HashMap<>();
			context.put("WS_SESSION", session);
			context.put("WS_EVENT", "error");
			context.put("WS_ERROR", t.getMessage());
			executionEngine.execute(context);
		} catch (Exception e) {
			log.error("Error while executing script ", e);
		}
	}
}
