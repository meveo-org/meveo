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

import org.infinispan.Cache;

import javax.annotation.Resource;
import javax.ejb.Startup;
import javax.ejb.Singleton;
import java.util.concurrent.Future;

@Singleton
@Startup
public class EndpointResultsCacheContainer {

    @Resource(lookup = "java:jboss/infinispan/cache/meveo/endpoints-results")
    private Cache<String, Future<String>> pendingExecutions;

    public Future<String> getPendingExecution(String key) {
        return pendingExecutions.get(key);
    }

    public void remove(String key){
        pendingExecutions.remove(key);
    }

    public void put(String key, Future<String> value){
        pendingExecutions.put(key, value);
    }
}
