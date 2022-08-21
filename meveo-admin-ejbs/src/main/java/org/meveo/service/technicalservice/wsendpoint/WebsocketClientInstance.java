/**
 * 
 */
package org.meveo.service.technicalservice.wsendpoint;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.meveo.admin.exception.BusinessException;
import org.meveo.model.admin.MvCredential;
import org.meveo.model.technicalservice.wsendpoint.WebsocketClient;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.script.ScriptInterface;

@ClientEndpoint
public class WebsocketClientInstance implements AutoCloseable {
	
	private final WebsocketClient client;
	private final ScriptInterface function;
	private final WebsocketExecutionService websocketExecutionService;
	
	private String error;
	private Session session;
	private MvCredential credential;
	private boolean connecting = false;
	
	public WebsocketClientInstance(WebsocketClient client, ScriptInterface function,WebsocketExecutionService websocketExecutionService) {
		this.client = client;
		this.function = function;
		this.websocketExecutionService = websocketExecutionService;
	}
	
	/**
	 * @param credential the credential to set
	 */
	public void setCredential(MvCredential credential) {
		this.credential = credential;
	}
	
	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}
	
	public void connect() throws Exception {
		//TODO: Implement retry
        ClientManager clientManager = ClientManager.createClient();
        String url = credential != null
        		? MeveoValueExpressionWrapper.evaluateToStringIgnoreErrors(client.getUrl(), "credential", credential)
        		: client.getUrl();

        connecting = true;
        try {
        	session = clientManager.connectToServer(this, new URI(url));
        	this.error = null;
        } finally {
        	connecting = false;
        }
	}
	
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		websocketExecutionService.onOpen(session, config, client, function);
	}
	
	@OnMessage
	public String onMessage(Session session, String message) {
		Map<String, Object> context = new HashMap<>();
		context.put("WS_SESSION", session);
		context.put("WS_EVENT", "message");
		context.put("WS_MESSAGE", message);
		try {
			function.execute(context);
			return (String) context.get("WS_RESPONSE");
			
		} catch (BusinessException e) {
			//TODO: Print error
		}
		return null;
	}
	
	@OnClose
	public void onClose(Session session, CloseReason reason) {
		websocketExecutionService.onClose(session, reason, function);
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		this.error = t.getLocalizedMessage();
		websocketExecutionService.error(session, t, function);
	}

	@Override
	public void close() throws Exception {
		if (session != null) {
			session.close();
			session = null;
			this.connecting = false;
			this.error = null;
		}
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public boolean isConnected() {
		if (session == null) {
			return false;
		}
		return session.isOpen();
	}
	
	public String getStatus() {
		if (isConnected()) {
			return "connected";
		} else if(connecting) {
			return "connecting";
		} else if (error != null){
			return error;
		} else {
			return "inactive";
		}
	}

}
