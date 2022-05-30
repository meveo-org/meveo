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

@Path("/monitoring")
@Produces(MediaType.APPLICATION_JSON)
public class MonitoringRs {
	
	@Inject
	private PoolsRs poolsRs;
	
	@Path("pools")
	public PoolsRs getPools() {
		return poolsRs;
	}
	
	@GET
	public Map<String, Object> getAll() {
		return Map.of("pools", poolsRs);
	}
	
}
