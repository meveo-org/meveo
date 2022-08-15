/**
 * 
 */
package org.meveo.service.technicalservice.wsendpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.cache.UserMessageCacheContainerProvider;
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
	
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
    	for (WebsocketClient client : wsEndpointService.listActive()) {
    		// Init function
    		ScriptInterface function;
    		try {
    			function = concreteFunctionService.getExecutionEngine(client.getService(), new HashMap<>());
			} catch (BusinessException e) {
				log.error("Failed to init function", e);
				continue;
			}
    		
    		// Connect to server (async process)
    		CompletableFuture.runAsync(() -> {
    			WebsocketClientInstance instance = new WebsocketClientInstance(client, function, websocketExecutionService);
    			try {
    				instance.connect();
    				websocketClients.put(client.getCode(), instance);
    			} catch (Exception e) {
    				log.warn("Failed to connect to websocket {}", client.getCode(), e);
    			}
    		});
    	}
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
