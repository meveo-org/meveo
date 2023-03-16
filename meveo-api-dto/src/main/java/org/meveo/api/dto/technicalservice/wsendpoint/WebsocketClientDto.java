/**
 * 
 */
package org.meveo.api.dto.technicalservice.wsendpoint;

import org.meveo.model.technicalservice.wsendpoint.WebsocketClient;

public class WebsocketClientDto extends WSEndpointDto {
	
	private static final long serialVersionUID = 1L;
	
	private String url;
	private int retryDelayInSeconds = 60;
	private int nbMaxRetry = 100;
	private String service;
	
	public WebsocketClientDto() {
		
	}
	
	public WebsocketClientDto(WebsocketClient client) {
		setSecured(client.isSecured());
		setActive(client.isActive());
		setCode(client.getCode());
		setDescription(client.getDescription());
		setDisabled(client.isDisabled());
		setNbMaxRetry(client.getNbMaxRetry());
		setRetryDelayInSeconds(client.getRetryDelayInSeconds());
		setUrl(client.getUrl());
		if (client.getService() != null) {
			setServiceCode(client.getService().getCode());
			setService(client.getService().getCode());
		}
	}
	
	/**
	 * @return the {@link #service}
	 */
	public String getService() {
		return service;
	}



	/**
	 * @param service the service to set
	 */
	public void setService(String service) {
		this.service = service;
	}



	/**
	 * @return the {@link #url}
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the {@link #retryDelayInSeconds}
	 */
	public int getRetryDelayInSeconds() {
		return retryDelayInSeconds;
	}
	/**
	 * @param retryDelayInSeconds the retryDelayInSeconds to set
	 */
	public void setRetryDelayInSeconds(int retryDelayInSeconds) {
		this.retryDelayInSeconds = retryDelayInSeconds;
	}
	/**
	 * @return the {@link #nbMaxRetry}
	 */
	public int getNbMaxRetry() {
		return nbMaxRetry;
	}
	/**
	 * @param nbMaxRetry the nbMaxRetry to set
	 */
	public void setNbMaxRetry(int nbMaxRetry) {
		this.nbMaxRetry = nbMaxRetry;
	}

}
