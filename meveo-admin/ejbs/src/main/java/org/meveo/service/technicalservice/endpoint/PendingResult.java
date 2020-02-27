/**
 * 
 */
package org.meveo.service.technicalservice.endpoint;

import java.util.concurrent.CompletableFuture;

import org.meveo.service.script.ScriptInterface;

/**
 * @author clement.bareth
 *
 */
public class PendingResult {
	
	private CompletableFuture<EndpointResult> result;
	private ScriptInterface engine;
	/**
	 * @return the {@link #result}
	 */
	public CompletableFuture<EndpointResult> getResult() {
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(CompletableFuture<EndpointResult> result) {
		this.result = result;
	}
	/**
	 * @return the {@link #engine}
	 */
	public ScriptInterface getEngine() {
		return engine;
	}
	/**
	 * @param engine the engine to set
	 */
	public void setEngine(ScriptInterface engine) {
		this.engine = engine;
	}
	
	

}
