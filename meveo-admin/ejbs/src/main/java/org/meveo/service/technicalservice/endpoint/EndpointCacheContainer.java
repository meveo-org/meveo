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

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Singleton
@Startup
public class EndpointCacheContainer {

    @Resource(lookup = "java:jboss/infinispan/cache/meveo/endpoints-results")
    private Cache<String, PendingResult> pendingExecutions;

    @Inject
    private EndpointService endpointService;

    private volatile LoadingCache<String, Endpoint> endpointLoadingCache;

    @PostConstruct
    private void init() {
        endpointLoadingCache = CacheBuilder.newBuilder()
                .expireAfterAccess(24, TimeUnit.HOURS)
                .build(new CacheLoader<String, Endpoint>() {
                    @Override
                    public Endpoint load(String key) {
                        return endpointService.findByCode(key);
                    }
                });
    }

    public PendingResult getPendingExecution(String key) {
        return pendingExecutions.get(key);
    }

    public void remove(String key){
        pendingExecutions.remove(key);
    }

    public void put(String key, PendingResult value){
        pendingExecutions.put(key, value);
    }

    public void removeEndpoint(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed Endpoint endpoint) {
        endpointLoadingCache.invalidate(endpoint.getCode());
    }

    public void updateEndpoint(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated Endpoint endpoint) {
        endpointLoadingCache.put(endpoint.getCode(), endpoint);
    }

    public void addEndpoint(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created Endpoint endpoint) {
        endpointLoadingCache.put(endpoint.getCode(), endpoint);
    }

    public Endpoint getEndpoint(String code){
        try {
            return endpointLoadingCache.getUnchecked(code);
        } catch (CacheLoader.InvalidCacheLoadException e){
            return null;
        }
    }
}
