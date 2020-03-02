package org.meveo.service.technicalservice.endpoint.schema;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 */
public class EndpointSchema {

	private String name;
	private String description;
	private Map<String, EndpointParameter> endpointParameters;

	public void addEndpointParameter(String key, EndpointParameter value) {

		if (endpointParameters == null) {
			endpointParameters = new HashMap<String, EndpointParameter>();
		}

		endpointParameters.put(key, value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, EndpointParameter> getEndpointParameters() {
		return endpointParameters;
	}

	public void setEndpointParameters(Map<String, EndpointParameter> endpointParameters) {
		this.endpointParameters = endpointParameters;
	}
}
