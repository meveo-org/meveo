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

import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Relationship;

public class Neo4jRelationship extends InternalRelationship implements Neo4jItem {

    private final String repository;

    public Neo4jRelationship(Relationship relationship, String repository){
        super(relationship.id(), relationship.startNodeId(), relationship.endNodeId(), relationship.type(), relationship.asMap(Values::value));
        this.repository = repository;
    }

    @Override
    public String repository() {
        return repository;
    }
}
