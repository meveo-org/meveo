/**
 * 
 */
package org.meveo.service.technicalservice.wsendpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.websocket.Session;

import org.meveo.admin.exception.BusinessException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.technicalservice.wsendpoint.WebsocketClient;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.ScriptInterface;
import org.slf4j.Logger;

@ApplicationScoped
public class WebsocketManager {
	
	@Inject
	private Logger log;

	@Inject
	private WebsocketClientService wsEndpointService;

	@Inject
	private ConcreteFunctionService concreteFunctionService;

	@Inject
	private WebsocketExecutionService websocketExecutionService;
	
	private final Map<String, WebsocketClientInstance> websocketClients = new ConcurrentHashMap<>();
	
	@Transactional
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
    	for (WebsocketClient client : wsEndpointService.listActive()) {
    		try {
    			initWebSocketClient(client);
    		} catch (BusinessException e) {
    			log.error("Failed to init function", e);
    			continue;
    		}
    	}
    }
    
    public void closeClient(@Observes @Removed WebsocketClient client) throws Exception {
    	var clientInstance = websocketClients.remove(client.getCode());
    	if (clientInstance != null) {
    		clientInstance.close();
    	}
    }
    
    @Transactional
    public boolean connect(WebsocketClient client, ScriptInterface function) {
    	WebsocketClientInstance instance = websocketClients.get(client.getCode());
    	if (instance != null && instance.isConnected()) {
    		return true;
    	}
    	
		try {
			if (function == null) function = concreteFunctionService.getExecutionEngine(client.getService(), new HashMap<>());
	    	instance = new WebsocketClientInstance(client, function, websocketExecutionService);
			instance.connect();
			websocketClients.put(client.getCode(), instance);
			return instance.isConnected();
		} catch (Exception e) {
			log.warn("Failed to connect to websocket {}", client.getCode(), e);
			return false;
		}
    }
    
	public void sendMessage(String websocketCode, String username, String txtMessage, boolean persistMessage) {
		String persistCacheKey = websocketCode+"_"+username;
		sendMessage(websocketCode, username, txtMessage, persistCacheKey, persistMessage);
	}
	
	public void sendMessage(String websocketCode, String username, String txtMessage, String persistCacheKey, boolean persistMessage) {
		boolean messageSent = false;

		var instance = websocketClients.get(websocketCode);
		if (instance != null) {
			if (!instance.isConnected()) {
				//TODO: Auto-reconnect
//				connect(null, null);
//				instance = websocketClients.get(websocketCode);
			}
			var session = instance.getSession();
			if (session.isOpen()) {
				session.getAsyncRemote().sendText(txtMessage);
				messageSent = true;
			}
		} else {
			// TODO use WebsockerServerEndpoint
		}
	}
    
    @Transactional
    public void initWebSocketClient(@Observes @Created WebsocketClient client) throws BusinessException {
    	if (!client.isActive()) {
    		return;
    	}
    	
    	var function = concreteFunctionService.getExecutionEngine(client.getService(), new HashMap<>());
    	
		// Connect to server (async process)
		CompletableFuture.runAsync(() -> {
			connect(client, function);
		});
    }
    
    @Transactional
    public void resetClient(@Observes @Updated WebsocketClient client) throws Exception {
    	closeClient(client);
    	initWebSocketClient(client);
    }
    
    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
    	websocketClients.values().forEach(t -> {
			try {
				t.close();
			} catch (Exception e) {
				log.error("Failed to close websocket session");
			}
		});
    }

}
