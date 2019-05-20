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

package org.meveo.persistence.scheduler;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableSet;

public class SchedulerPersistenceContext {
    private final Map<String, Set<EntityRef>> nodeReferencesByNodeName = new ConcurrentHashMap<>();

    public Set<EntityRef> getNodeReferences(String nodeName) {
        return nodeReferencesByNodeName.computeIfAbsent(nodeName, key -> Collections.emptySet());
    }

    public void putNodeReferences(String nodeName, Set<EntityRef> entityRef) {
        nodeReferencesByNodeName.put(nodeName, ImmutableSet.copyOf(entityRef));
    }
}
