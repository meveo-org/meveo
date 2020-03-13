package org.meveo.service.technicalservice.endpoint.schema;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents the schema of a given endpoint with get and post
 * parameters.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 */
public class EndpointSchema {

	/**
	 * Name of the endpoint schema.
	 */
	private String name;

	/**
	 * Description of the endpoint schema.
	 */
	private String description;

	/**
	 * GET or POST parameters of a given endpoint.
	 */
	private Map<String, EndpointParameter> endpointParameters;

	public void addEndpointParameter(String key, EndpointParameter value) {

		if (endpointParameters == null) {
			endpointParameters = new HashMap<>();
		}

		if (key != null) {
			endpointParameters.put(key, value);
		}
	}

	/**
	 * Retrieves the name.
	 * 
	 * @return name of this endpoint schema
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name name of the endpoint schema
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieves the description.
	 * 
	 * @return description of this endpoint schema
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description description of the endpoint schema
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Retrieves the parameters. A parameter can be GET or POST.
	 * 
	 * @return parameters of this endpoint schema
	 */
	public Map<String, EndpointParameter> getEndpointParameters() {
		return endpointParameters;
	}

	/**
	 * Sets the parameters.
	 * 
	 * @param endpointParameters parameters of the endpoint schema
	 */
	public void setEndpointParameters(Map<String, EndpointParameter> endpointParameters) {
		this.endpointParameters = endpointParameters;
	}
}
