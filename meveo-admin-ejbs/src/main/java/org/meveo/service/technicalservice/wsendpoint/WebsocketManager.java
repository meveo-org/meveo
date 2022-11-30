/**
 * 
 */
package org.meveo.service.technicalservice.wsendpoint;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.meveo.admin.exception.BusinessException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.admin.MvCredential;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.wsendpoint.WebsocketClient;
import org.meveo.service.admin.impl.credentials.CredentialHelperService;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.ScriptInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public class WebsocketManager {
	
	private static Logger log = LoggerFactory.getLogger(WebsocketManager.class);

	@Inject
	private WebsocketClientService wsClientService;

	@Inject
	private ConcreteFunctionService concreteFunctionService;

	@Inject
	private WebsocketExecutionService websocketExecutionService;
	
	@Inject
	private CredentialHelperService credentialHelperService;
	
	private final Map<String, WebsocketClientInstance> websocketClients = new ConcurrentHashMap<>();
	
	@PostConstruct
	public void init() {
		log.info("Opening active websocket clients");
		
    	for (WebsocketClient client : wsClientService.listActive()) {
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
			if (function == null) { 
				function = concreteFunctionService.getExecutionEngine(client.getService(), new HashMap<>());
			}
			
	    	instance = new WebsocketClientInstance(client, function, websocketExecutionService);
			websocketClients.put(client.getCode(), instance);

	    	if (client.isSecured()) {
	    		String url = MeveoValueExpressionWrapper.evaluateExpression(client.getUrl(), new HashMap<>(), String.class);
	    		String domainName = new URI(url).getHost();
	    		MvCredential credential = credentialHelperService.getCredential(domainName);
	    		instance.setCredential(credential);
	    		
	    		if (credential == null) {
	    			log.warn("Can't find credential for domain name {}", domainName);
	    		}
	    	}
	    	
			instance.connect();
			log.info("Connection to client {} {}", client.getCode(), instance.isConnected() ? "succeeded" : "failed");
			
			return instance.isConnected();
		} catch (Exception e) {
			if (instance != null) {
				instance.setError(e.getLocalizedMessage());
			}
			log.warn("Failed to connect to websocket {}", client.getCode(), e);
			return false;
		}
    }
    
	public boolean sendMessage(String websocketCode, String username, String txtMessage, boolean persistMessage) {
		String persistCacheKey = websocketCode+"_"+username;
		return sendMessage(websocketCode, username, txtMessage, persistCacheKey, persistMessage);
	}
	
	public boolean sendMessage(String websocketCode, String username, String txtMessage, String persistCacheKey, boolean persistMessage) {
		boolean messageSent = false;

		var instance = websocketClients.get(websocketCode);
		if (instance != null) {
			if (!instance.isConnected()) {
				try {
					instance.connect();
				} catch (Exception e) {
					return false;
				}
			}
			var session = instance.getSession();
			if (session.isOpen()) {
				session.getAsyncRemote().sendText(txtMessage);
				messageSent = true;
			}
		}
		
		return messageSent;
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
    
    @Asynchronous
    public void resetClient(@Observes @Updated WebsocketClient client) throws Exception {
    	closeClient(client);
    	initWebSocketClient(client);
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateFunction(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated Function function) {
    	List<WebsocketClient> clients = wsClientService.findByServiceCode(function.getCode());
    	if (clients != null) {
    		for (var client : clients) {
    			try {
    				resetClient(client);
    			} catch (Exception e) {
    				//NOOP
    			}
    		}
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
    
    public String getStatus(WebsocketClient client) {
	if (client==null || client.getCode()==null) {
    		return "inactive";
	}
    	WebsocketClientInstance instance = websocketClients.get(client.getCode());
    	if (instance == null) {
    		return "inactive";
    	}
    	
    	return instance.getStatus();
    }
    
}
