/**
 * 
 */
package org.meveo.model.technicalservice.endpoint;

import java.util.Map;

import org.meveo.model.NotifiableEntity;

@NotifiableEntity
public class EndpointExecutionResult {
	private final Endpoint endpoint;
	private final Map<String, Object> parameters;
	private Map<String, Object> results;
	private Throwable error;
	
	public EndpointExecutionResult(Endpoint endpoint, Map<String, Object> parameters) {
		this.endpoint = endpoint;
		this.parameters = parameters;
	}

	/**
	 * @return the {@link #results}
	 */
	public Map<String, Object> getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(Map<String, Object> results) {
		this.results = results;
	}

	/**
	 * @return the {@link #error}
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(Throwable error) {
		this.error = error;
	}

	/**
	 * @return the {@link #endpoint}
	 */
	public Endpoint getEndpoint() {
		return endpoint;
	}

	/**
	 * @return the {@link #parameters}
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
}
