/**
 * 
 */
package org.meveo.api.technicalservice.wsendpoint;

import javax.inject.Inject;

import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.technicalservice.wsendpoint.WebsocketClientDto;
import org.meveo.model.technicalservice.wsendpoint.WebsocketClient;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.technicalservice.wsendpoint.WebsocketClientService;

public class WebsocketClientApi extends BaseCrudApi<WebsocketClient, WebsocketClientDto>{

	@Inject
	private WebsocketClientService service;
	
	public WebsocketClientApi() {
		super(WebsocketClient.class, WebsocketClientDto.class);
	}

	@Override
	public IPersistenceService<WebsocketClient> getPersistenceService() {
		return service;
	}

}
