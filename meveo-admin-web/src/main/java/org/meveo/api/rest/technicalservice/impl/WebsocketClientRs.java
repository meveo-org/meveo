/**
 * 
 */
package org.meveo.api.rest.technicalservice.impl;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.technicalservice.wsendpoint.WebsocketClientDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.rest.BusinessRs;
import org.meveo.model.technicalservice.wsendpoint.WebsocketClient;
import org.meveo.service.technicalservice.wsendpoint.WebsocketManager;

@Path("/websockets/clients")
public class WebsocketClientRs extends BusinessRs<WebsocketClient, WebsocketClientDto>{
	
	@Inject
	private WebsocketManager websocketManager;
	
	@Path("/{code}/connect")
	@POST
	@Transactional
	public boolean tryConnect(@PathParam("code") String code) throws BusinessException {
		WebsocketClient client = api.getPersistenceService().findByCode(code);
		if (client != null) {
			return websocketManager.connect(client, null);
		} else {
			throw new EntityDoesNotExistsException(WebsocketClient.class, code);
		}
	}
	
	@Path("/{code}/close")
	@POST
	public void close(@PathParam("code") String code) throws Exception {
		WebsocketClient client = api.getPersistenceService().findByCode(code);
		if (client != null) {
			websocketManager.closeClient(client);
		} else {
			throw new EntityDoesNotExistsException(WebsocketClient.class, code);
		}
	}
	
	@Path("/{code}/message")
	@POST
	public boolean sendMessage(@PathParam("code") String code, String message) {
		return websocketManager.sendMessage(code, null, message, false);
	}
}
