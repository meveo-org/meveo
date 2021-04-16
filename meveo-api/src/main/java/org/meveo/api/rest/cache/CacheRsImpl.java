/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

package org.meveo.api.rest.cache;

import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.cache.NotificationCacheContainerProvider;

/**
 * API for managing the all application caches.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class CacheRsImpl implements CacheRs {

	@Inject
	private CustomFieldsCacheContainerProvider cache;
	
	@Inject
	private NotificationCacheContainerProvider notifCache;

	@Override
	public void refreshAll() {
		cache.refreshCache(null);
	}

	@Override
	public void populateAll() {
		cache.populateCache(null, false);
	}

	@Override
	public void refresh(String cacheName) {
		cache.refreshCache(cacheName);
	}

	@Override
	public void populate(String cacheName) {
		cache.populateCache(cacheName, false);
	}

	@Override
	public int getNbElements(String cacheName) {
		return this.cache.getCaches().get(cacheName).size();
	}

	@Override
	public Map<String, Integer> getNbElementByCacheName() {
		Map<String, Integer> caches = this.cache.getCaches().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, c -> c.getValue().size()));
		Map<String, Integer> notifCaches = this.notifCache.getCaches().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, c -> c.getValue().size()));
		caches.putAll(notifCaches);
		return caches;
	}

}
