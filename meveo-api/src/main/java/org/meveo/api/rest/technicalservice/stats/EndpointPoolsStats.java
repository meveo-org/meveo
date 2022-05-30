/**
 * 
 */
package org.meveo.api.rest.technicalservice.stats;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.meveo.service.technicalservice.endpoint.EndpointCacheContainer;

import com.fasterxml.jackson.annotation.JsonValue;

@Produces(MediaType.APPLICATION_JSON)
public class EndpointPoolsStats {
	
	@Inject
	private EndpointCacheContainer endpointCache;
	
	@GET
	@JsonValue
	public Map<String, EndpointPoolStats> getAll() {
		return endpointCache.getPooledEndpoints().stream()
				.collect(Collectors.toMap(key -> key, key -> getOne(key)));
	}
	
	@Path("/{code}")
	public EndpointPoolStats getOne(@PathParam("code") String code) {
		if (!endpointCache.getPooledEndpoints().contains(code)) {
			return null;
		}
		
		return new EndpointPoolStats(endpointCache.getNbActiveInPool(code), endpointCache.getNbIdleInPool(code));
	}
	
}
