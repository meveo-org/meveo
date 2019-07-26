package org.meveo.api.rest.technicalservice;

import org.meveo.api.rest.technicalservice.impl.EndpointRequest;
import org.meveo.api.rest.technicalservice.impl.EndpointResponse;
import org.meveo.service.script.Script;

public abstract class EndpointScript extends Script {
	
	protected EndpointRequest endpointRequest;
	
	/**
	 * Will always be <code>null</code> if endpoint is asychronous
	 */
	protected EndpointResponse endpointResponse;

	public void setEndpointRequest(EndpointRequest endpointRequest) {
		this.endpointRequest = endpointRequest;
	}

	public void setEndpointResponse(EndpointResponse endpointResponse) {
		this.endpointResponse = endpointResponse;
	}

}
