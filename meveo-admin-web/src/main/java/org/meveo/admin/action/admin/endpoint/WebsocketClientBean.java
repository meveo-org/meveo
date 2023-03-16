/**
 * 
 */
package org.meveo.admin.action.admin.endpoint;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.meveo.admin.action.BaseCrudBean;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.technicalservice.wsendpoint.WebsocketClientDto;
import org.meveo.api.technicalservice.wsendpoint.WebsocketClientApi;
import org.meveo.model.technicalservice.wsendpoint.WebsocketClient;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.technicalservice.wsendpoint.WebsocketClientService;
import org.meveo.service.technicalservice.wsendpoint.WebsocketManager;

@Named
@ViewScoped
public class WebsocketClientBean extends BaseCrudBean<WebsocketClient, WebsocketClientDto>{

	private static final long serialVersionUID = 8225383347060126595L;

	@Inject
	private transient WebsocketClientApi api;
	
	@Inject
	private transient WebsocketClientService service;
	
	@Inject
	private transient WebsocketManager websocketManager;
	
	private String message;
	
	public WebsocketClientBean() {
		super(WebsocketClient.class); 
	}
	
	@Override
	public BaseCrudApi<WebsocketClient, WebsocketClientDto> getBaseCrudApi() {
		return api;
	}

	@Override
	protected IPersistenceService<WebsocketClient> getPersistenceService() {
		return service;
	}
	
	@Override
	public String getEditViewName() {
		return "websocketClientDetail";
	}

	@Override
	protected String getListViewName() {
		return "websocketClients";
	}
	
	public String getStatus(WebsocketClient client) {
		String status = websocketManager.getStatus(client);
		return status;
	}
	
	public String getStatus() {
		return this.getStatus(entity);
	}
	
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @return the {@link #message}
	 */
	public String getMessage() {
		return message;
	}
	
	@Transactional
	public void connect() {
		boolean result = websocketManager.connect(entity, null);
		if (result) {
			messages.info("Connection successful");
		} else {
			messages.error("Connection failed");
		}
	}
	
	public void close() throws Exception {
		websocketManager.closeClient(entity);
	}
	
	public void sendMessage() {
		boolean result = websocketManager.sendMessage(entity.getCode(), null, message, false);
		if (result) {
			messages.info("Message sent");
		} else {
			messages.error("Message not sent");
		}
	}

}
