/**
 * 
 */
package org.meveo.api.rest.cache;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
@Path("/caches")
@Api("CacheRs")
@Produces(MediaType.APPLICATION_JSON)
public interface CacheRs {

	@POST
	@Path("/refresh")
	@ApiOperation("Clean and populate all caches")
	void refreshAll();

	@POST
	@Path("/populate")
	@ApiOperation("Populate all caches")
	void populateAll();

	@POST
	@Path("/{name}/refresh")
	@ApiOperation("Clean and popuplate a cache")
	void refresh(@PathParam("name") @ApiParam(value = "Name of the cache to refresh", example = "meveo-cft-cache") String cacheName);

	@POST
	@Path("/{name}/populate")
	@ApiOperation("Populate a cache")
	void populate(@PathParam("name") @ApiParam(value = "Name of the cache to populate", example = "meveo-cft-cache") String cacheName);

	@GET
	@Path("/{name}/status")
	@ApiOperation("Count elements inside a cache")
	int getNbElements(@PathParam("name") @ApiParam(value = "Name of the cache to count elements", example = "meveo-cft-cache") String cacheName);

	@GET
	@Path("/status")
	@ApiOperation("Count elements by cache")
	Map<String, Integer> getNbElementByCacheName();

}