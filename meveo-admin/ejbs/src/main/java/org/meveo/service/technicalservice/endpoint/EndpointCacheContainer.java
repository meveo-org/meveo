/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.service.technicalservice.endpoint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.technicalservice.endpoint.Endpoint;

/**
 * @author Clément Bareth *
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@Singleton
@Startup
public class EndpointCacheContainer {

	
	//@Inject
	//private Logger log;

	@Resource(lookup = "java:jboss/infinispan/cache/meveo/endpoints-results")
	private Cache<String, PendingResult> pendingExecutions;

	@Inject
	private EndpointService endpointService;

	//Map of Endpoint by code
	private Map<String, Endpoint> endpointLoadingCache = new HashMap<String, Endpoint>();


	@PostConstruct
	private void init() {
		List<Endpoint> allEndpoints=endpointService.list();
		for(Endpoint endpoint:allEndpoints){
			endpoint.getService();
			endpoint.getService().getCode();
			endpoint.getPathParametersNullSafe().forEach(e -> {});
			endpoint.getParametersMappingNullSafe().forEach(e -> {});
			endpointLoadingCache.put(endpoint.getCode(),endpoint);
		}
		//log.info("endpointLoadingCache init:"+endpointLoadingCache.size()+" entries");
		/*		CacheBuilder.newBuilder() //
				.expireAfterAccess(24, TimeUnit.HOURS) //
				.build(new CacheLoader<String, Endpoint>() { //
					@Override
					public Endpoint load(String key) {
						Endpoint result = endpointService.findByCode(key, Arrays.asList("service"));
						result.getService();
						result.getPathParametersNullSafe().forEach(e -> {
						});
						;
						result.getParametersMappingNullSafe().forEach(e -> {
						});
						;
						return result;
					}
				});*/
	}

	@Lock(LockType.READ)
	public PendingResult getPendingExecution(String key) {
		return pendingExecutions.get(key);
	}

	@Lock(LockType.WRITE)
	public void remove(String key) {
		pendingExecutions.remove(key);
	}

	@Lock(LockType.WRITE)
	public void put(String key, PendingResult value) {
		pendingExecutions.put(key, value);
	}

	public void removeEndpoint(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed Endpoint endpoint) {
		endpointLoadingCache.remove(endpoint.getCode());
		//log.info("endpointLoadingCache remove"+endpoint.getCode()+" :"+endpointLoadingCache.size()+" entries");
	}

	public void updateEndpoint(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated Endpoint endpoint) {
		if(endpoint.isActive()) {
			endpointLoadingCache.put(endpoint.getCode(), endpoint);
		} else{
			if(endpointLoadingCache.containsKey(endpoint.getCode())){
				endpointLoadingCache.remove(endpoint.getCode());
			}
		}
		//log.info("endpointLoadingCache update "+endpoint.getCode()+" :"+endpointLoadingCache.size()+" entries");
	}

	public void addEndpoint(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created Endpoint endpoint) {
		if(endpoint.isActive()) {
			endpointLoadingCache.put(endpoint.getCode(), endpoint);
		}
		//log.info("endpointLoadingCache add "+endpoint.getCode()+" :"+endpointLoadingCache.size()+" entries");
	}

	@Lock(LockType.READ)
	public Endpoint getEndpoint(String code) {
		if(endpointLoadingCache.containsKey(code)){
			return endpointLoadingCache.get(code);
		} else {
			return null;
		}
	}

	/*
	 * returns the endpoint with largest regex matching the path
	 */
	@Lock(LockType.READ)
	public Endpoint getEndpointForPath(String path, String method){
		Endpoint result=null;
		Iterator<Map.Entry<String,Endpoint>> it = endpointLoadingCache.entrySet().iterator();
		while (it.hasNext()) {
			Endpoint endpoint = it.next().getValue();
			if(endpoint.getMethod().getLabel().equals(method)){
				if(path.startsWith("/"+endpoint.getBasePath())){
					Matcher matcher = endpoint.getPathRegex().matcher(path);
					if(matcher.matches() || matcher.lookingAt()){
						if((result==null)||(result.getPathRegex().pattern().length()>endpoint.getPathRegex().pattern().length())){
							result=endpoint;
						}
					}
				}
			}
		}
		return result;
	}
}
