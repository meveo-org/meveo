/**
 * 
 */
package org.meveo.service.technicalservice.wsendpoint;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.meveo.admin.exception.BusinessException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.admin.MvCredential;
import org.meveo.model.technicalservice.wsendpoint.WebsocketClient;
import org.meveo.service.admin.impl.credentials.CredentialHelperService;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.ScriptInterface;
import org.slf4j.Logger;

@Startup
@Singleton
public class WebsocketManager {
	
	@Inject
	private Logger log;

	@Inject
	private WebsocketClientService wsEndpointService;

	@Inject
	private ConcreteFunctionService concreteFunctionService;

	@Inject
	private WebsocketExecutionService websocketExecutionService;
	
	@Inject
	private CredentialHelperService credentialHelperService;
	
    @Resource
    protected TimerService timerService;
	
	private final Map<String, WebsocketClientInstance> websocketClients = new ConcurrentHashMap<>();
	
//	@PostConstruct
//    public void postConstruct() {
//		LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(2, ChronoUnit.MINUTES));
//		Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
//		
//		timerService.createSingleActionTimer(date, new TimerConfig(null, false));
//    }
	
	// @Timeout
	@PostConstruct
	public void init() {
		log.info("Opening active websocket clients");
		
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
			if (function == null) { 
				function = concreteFunctionService.getExecutionEngine(client.getService(), new HashMap<>());
			}
			
	    	instance = new WebsocketClientInstance(client, function, websocketExecutionService);
	    	
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
			websocketClients.put(client.getCode(), instance);
			
			log.info("Connection to client {} {}", client.getCode(), instance.isConnected() ? "succeeded" : "failed");
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
