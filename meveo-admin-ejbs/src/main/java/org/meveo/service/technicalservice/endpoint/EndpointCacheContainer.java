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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;
import org.infinispan.Cache;
import org.meveo.elresolver.ELException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointPool;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.script.CharSequenceCompiler;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.script.ScriptInterface;
import org.meveo.service.script.weld.MeveoBeanManager;
import org.meveo.service.technicalservice.endpoint.pool.ScriptInterfacePoolFactory;
import org.slf4j.Logger;

/**
 * @author Clément Bareth *
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@Singleton
@Startup
public class EndpointCacheContainer {


	@Inject
	private Logger log;

	@Resource(lookup = "java:jboss/infinispan/cache/meveo/endpoints-results")
	private Cache<String, PendingResult> pendingExecutions;

	@Inject
	private EndpointService endpointService;

	//Map of Endpoint by code
	private Map<String, Endpoint> endpointLoadingCache = new HashMap<String, Endpoint>();

	private Map<String, ObjectPool<ScriptInterface>> endpointPool = new ConcurrentHashMap<>();

	@Inject
	private ScriptInstanceService scriptInstanceService;

	@PostConstruct
	private void init() {
		List<Endpoint> allEndpoints = endpointService.list();
		for(Endpoint endpoint : allEndpoints) {
			endpoint.getService();
			endpoint.getService().getCode();
			endpoint.getPathParametersNullSafe().forEach(e -> {});
			endpoint.getParametersMappingNullSafe().forEach(e -> {});
			endpointLoadingCache.put(endpoint.getCode(), endpoint);

			if (endpoint.getPool() != null && endpoint.getPool().isUsePool()) {
				try {
					endpointPool.put(endpoint.getCode(), buildPool(endpoint));
				} catch (Exception e) {
					log.error("Failed to build pool for {}", endpoint.getCode(), e);
				}
			}
		}
	}

	@PreDestroy
	private void destroy() {
		endpointPool.values().forEach(ObjectPool::close);
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
		if (endpoint.getPool() != null && endpoint.getPool().isUsePool()) {
			var pool = endpointPool.remove(endpoint.getCode());
			if (pool != null) {
				pool.close();
			}
		}
	}

	public void updatePool(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated ScriptInstance script) {
		endpointLoadingCache.values()
				.stream()
				.filter(endpoint -> endpoint.getService().getId().equals(script.getId()))
				.findFirst()
				.ifPresent(endpoint -> {
					if (endpoint.getPool() != null && endpoint.getPool().isUsePool()) {
						var pool = endpointPool.remove(endpoint.getCode());

						if (endpoint.isActive()) {
							endpointPool.put(endpoint.getCode(), buildPool(endpoint));
						}

						if (pool != null) {
							pool.close();
						}
					}
				});
	}

	public void updateEndpoint(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated Endpoint endpoint) {
		if(endpoint.isActive()) {
			endpointLoadingCache.put(endpoint.getCode(), endpoint);
		} else{
			if(endpointLoadingCache.containsKey(endpoint.getCode())){
				endpointLoadingCache.remove(endpoint.getCode());
			}
		}

		if (endpoint.getPool() != null && endpoint.getPool().isUsePool()) {
			var pool = endpointPool.remove(endpoint.getCode());
			if (endpoint.isActive()) {
				endpointPool.put(endpoint.getCode(), buildPool(endpoint));
			}
			if (pool != null) {
				pool.close();
			}

		}
	}

	public void addEndpoint(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created Endpoint endpoint) {
		if(endpoint.isActive()) {
			endpointLoadingCache.put(endpoint.getCode(), endpoint);
			if (endpoint.getPool() != null && endpoint.getPool().isUsePool()) {
				var pool = endpointPool.remove(endpoint.getCode());
				endpointPool.put(endpoint.getCode(), buildPool(endpoint));
				if (pool != null) {
					pool.close();
				}
			}
		}
	}

	@Lock(LockType.READ)
	public Endpoint getEndpoint(String code) {
		if(endpointLoadingCache.containsKey(code)){
			return endpointLoadingCache.get(code);
		} else {
			return null;
		}
	}

	@Lock(LockType.READ)
	public ScriptInterface getPooledScript(Endpoint endpoint) throws Exception {
		return endpointPool.get(endpoint.getCode()).borrowObject();
	}

	@Lock(LockType.READ)
	public void returnPooledScript(Endpoint endpoint, ScriptInterface script) {
		var pool = this.endpointPool.get(endpoint.getCode());
		if (pool != null) {
			try {
				pool.returnObject(script);
			} catch (Exception e) {
				log.error("Failed to return script to pool", e);
			}
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
						if((result==null)||(endpoint.getPathRegex().pattern().length()>result.getPathRegex().pattern().length())){
							result=endpoint;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Method used for statistics building
	 *
	 * @return the list of endpoint's code that has an active pool
	 */
	public Collection<String> getPooledEndpoints() {
		return endpointPool.keySet();
	}

	public int getNbActiveInPool(String endpointCode) {
		return endpointPool.get(endpointCode).getNumActive();
	}

	public int getNbIdleInPool(String endpointCode) {
		return endpointPool.get(endpointCode).getNumIdle();
	}

	private ObjectPool<ScriptInterface> buildPool(Endpoint endpoint) {
		String scriptCode = endpoint.getService().getCode();

		Class<ScriptInterface> compiledScript;
		try {
			// Make sure the class is loaded
			scriptInstanceService.loadClassInCache(scriptCode);

			var mBeanManager = MeveoBeanManager.getInstance();
			compiledScript = CharSequenceCompiler.getCompiledClass(scriptCode);
			var bean = mBeanManager.createBean(compiledScript);
			Instance<ScriptInterface> instance = mBeanManager.getWeldInstance(bean, compiledScript);

			var factory = new ScriptInterfacePoolFactory(endpoint.getService(), compiledScript, instance);
			ObjectPool<ScriptInterface> pool = new GenericObjectPool<ScriptInterface>(factory);

			EndpointPool poolConfig = endpoint.getPool();

			if (StringUtils.isAllBlank(poolConfig.getMaxIdleTime(), poolConfig.getMax())) {
				pool = new SoftReferenceObjectPool<>(factory);

			} else {
				pool = new GenericObjectPool<ScriptInterface>(factory);
				if (!StringUtils.isBlank(poolConfig.getMaxIdleTime())) {
					Integer minEvictableIdle = MeveoValueExpressionWrapper.evaluateExpression(poolConfig.getMaxIdleTime(), null, Integer.class);
					((GenericObjectPool<ScriptInterface>)  pool).setSoftMinEvictableIdle(Duration.of(minEvictableIdle, ChronoUnit.SECONDS));
					((GenericObjectPool<ScriptInterface>)  pool).setTimeBetweenEvictionRuns(Duration.of(minEvictableIdle, ChronoUnit.SECONDS));
				}

				if (!StringUtils.isBlank(poolConfig.getMax())) {
					Integer maxTotal = MeveoValueExpressionWrapper.evaluateExpression(poolConfig.getMax(), null, Integer.class);
					((GenericObjectPool<ScriptInterface>)  pool).setMaxTotal(maxTotal);
				}

				if (!StringUtils.isBlank(poolConfig.getMin())) {
					Integer min = MeveoValueExpressionWrapper.evaluateExpression(poolConfig.getMin(), null, Integer.class);
					((GenericObjectPool<ScriptInterface>)  pool).setMinIdle(min);
				}
			}

			if (!StringUtils.isBlank(poolConfig.getMin())) {
				Integer min = MeveoValueExpressionWrapper.evaluateExpression(poolConfig.getMin(), null, Integer.class);
				try {
					pool.addObjects(min);
				} catch (Exception e) {
					log.error("Failed to add objects to pool", e);
				}
			}

			return pool;
		} catch (ELException | ClassNotFoundException e) {
			log.error("Failed to configure script pool", e);
			throw new RuntimeException(e);
		}

	}
}
