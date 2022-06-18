/**
 * 
 */
package org.meveo.api.rest.technicalservice.stats;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Produces(MediaType.APPLICATION_JSON)
public class EndpointPoolStats {

	private final int nbActive;
	private final int nbIdle;
	
	public EndpointPoolStats(int nbActive, int nbIdle) {
		this.nbActive = nbActive;
		this.nbIdle = nbIdle;
	}

	@GET
	@JsonIgnore
	public EndpointPoolStats get() {
		return this;
	}

	@GET
	@Path("/nbActive")
	public int getNbActive() {
		return nbActive;
	}

	@GET
	@Path("/nbIdle")
	public int getNbIdle() {
		return nbIdle;
	}
	
	@GET
	@Path("/nbTotal")
	public int getNbTotal() {
		return nbActive + nbIdle;
	}
	
	
}
