package org.meveo.api.rest.technicalservice;

import org.meveo.api.rest.technicalservice.impl.EndpointRequest;
import org.meveo.api.rest.technicalservice.impl.EndpointResponse;
import org.meveo.service.script.Script;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
public abstract class EndpointScript extends Script {
	
	protected EndpointRequest endpointRequest;
	
	/**
	 * Will always be <code>null</code> if endpoint is asychronous
	 */
	protected EndpointResponse endpointResponse;

	@JsonIgnore
	public void setEndpointRequest(EndpointRequest endpointRequest) {
		this.endpointRequest = endpointRequest;
	}

	public List<Locale> getIntendedLocales(){
		List<Locale> locales = new ArrayList<>();
		if(this.endpointRequest != null){
			locales = Collections.list(this.endpointRequest.getLocales());
			if (!new HashSet(locales).contains(this.endpointRequest.getLocale())){
				locales.add(this.endpointRequest.getLocale());
			}
		}
		if (!new HashSet(locales).contains(Locale.getDefault())){
			locales.add(Locale.getDefault());
		}
		return locales;
	}

	@JsonIgnore
	public void setEndpointResponse(EndpointResponse endpointResponse) {
		this.endpointResponse = endpointResponse;
	}

}
