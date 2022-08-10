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

package org.meveo.persistence.neo4j.graph;

import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Neo4jEntity extends InternalNode implements Neo4jItem {

    private final String repository;

    public Neo4jEntity(Node node, String repository) {
        super(node.id(), getLabels(node), node.asMap(Values::value));
        this.repository = repository;
    }

    private static Collection<String> getLabels(Node node) {
        if(node.labels() instanceof Collection){
            return (Collection<String>) node.labels();
        }
        return StreamSupport.stream(node.labels().spliterator(), false)
                    .collect(Collectors.toList());
    }

    @Override
    public String repository() {
        return repository;
    }
}
