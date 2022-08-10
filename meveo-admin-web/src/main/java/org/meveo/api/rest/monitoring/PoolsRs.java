/**
 * 
 */
package org.meveo.api.rest.monitoring;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.api.rest.technicalservice.stats.EndpointPoolsStats;

import com.fasterxml.jackson.annotation.JsonValue;

@Produces(MediaType.APPLICATION_JSON)
public class PoolsRs {

	@Inject
	private EndpointPoolsStats endpointPoolStats;
	
	@Path("/endpoints")
	public EndpointPoolsStats getEndpointPoolsStats() {
		return endpointPoolStats;
	}
	
	@GET
	@JsonValue
	public Map<String, Object> getAll() {
		return Map.of("endpoints", endpointPoolStats);
	}
}
