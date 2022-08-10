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

package org.meveo.persistence.neo4j.service;

import static org.meveo.persistence.neo4j.base.Neo4jDao.NODE_ID;

public class Neo4JRequests {

    public static final String ADDITIONAL_LABELS = "labels";
    public static final String ALIAS = "alias";

    public static final String INTERNAL_UPDATE_DATE = "updateDate";
    public static final String CREATION_DATE = "creationDate";

    /**
     * Create a relationship between node with given ids
     * Parameters : <br>
     * - startNodeId : Id of the start node <br>
     * - endNodeId : Id of the target node <br>
     * - relationshipLabel : Label of the relationship to create <br>
     */
    public final static StringBuffer createRelationship = new StringBuffer("MATCH (startNode:${startNodeLabel}) WHERE startNode.meveo_uuid = $startNodeId \n")
            .append("WITH startNode \n")
            .append("MATCH (endNode:${endNodeLabel}) WHERE endNode.meveo_uuid = $endNodeId \n")
            .append("WITH startNode, endNode \n")
            .append("MERGE (startNode)-[relationship :${relationshipLabel} ${fields}]->(endNode) \n")
            .append("ON CREATE SET relationship." + CREATION_DATE + " = timestamp(), relationship.meveo_uuid = $").append(NODE_ID).append("\n")
            .append("ON MATCH SET relationship." + INTERNAL_UPDATE_DATE + " = timestamp() \n")
            .append("RETURN relationship");

    /**
     * Delete a node with all its associated relations
     * Parameters : <br>
     * - cetCode : code of the node to delete <br>
     * - uniqueFields : unique fields that identify the node
     */
    public final static StringBuffer deleteCet = new StringBuffer("MATCH (n:${cetCode} ${uniqueFields}) \n")
            .append("WITH n, properties(n) as properties,  labels(n) as labels, n.meveo_uuid as id \n")
            .append("DETACH DELETE n \n")
            .append("RETURN properties, labels, id");

    public final static StringBuffer crtStatement = new StringBuffer("MATCH (${startAlias}:${startNode} ${starNodeKeys}) \n")
            .append("MATCH (${endAlias}:${endNode} ${endNodeKeys}) \n")
            .append("MERGE (${startAlias})-[relationship :${relationType} ${fields}]->(${endAlias}) \n")
            .append("ON MATCH SET ${startAlias}." + INTERNAL_UPDATE_DATE + "=${updateDate}, ${endAlias}." + INTERNAL_UPDATE_DATE + "= ${updateDate}, relationship." + INTERNAL_UPDATE_DATE + " = ${updateDate} \n")
            .append("ON CREATE SET relationship." + CREATION_DATE + " = ${updateDate} , relationship.meveo_uuid = $").append(NODE_ID).append("\n");

    public final static StringBuffer crtStatementByNodeIds = new StringBuffer()
            .append("MATCH (${startAlias}:${startNode}) \n")
            .append("WHERE ${startAlias}.meveo_uuid = $startNodeId \n")
            .append("WITH ${startAlias} \n")
            .append("MATCH (${endAlias}:${endNode}) \n")
            .append("WHERE ${endAlias}.meveo_uuid = $endNodeId \n")
            .append("WITH ${startAlias}, ${endAlias} \n")
            .append("MERGE (${startAlias})-[relationship :${relationType} ${fields}]->(${endAlias}) \n")
            .append("ON MATCH SET ${startAlias}." + INTERNAL_UPDATE_DATE + " = $updateDate, ${endAlias}." + INTERNAL_UPDATE_DATE + " = $updateDate, relationship." + INTERNAL_UPDATE_DATE + " = $updateDate \n")
            .append("ON CREATE SET relationship." + CREATION_DATE + " = $updateDate, relationship.meveo_uuid = $").append(NODE_ID).append("\n");

    public final static StringBuffer uniqueCrtStatementByNodeIds = new StringBuffer()
            .append("MATCH (${startAlias}:${startNode}) \n")
            .append("WHERE ${startAlias}.meveo_uuid = $startNodeId \n")
            .append("WITH ${startAlias} \n")
            .append("MATCH (${endAlias}:${endNode}) \n")
            .append("WHERE ${endAlias}.meveo_uuid = $endNodeId \n")
            .append("WITH ${startAlias}, ${endAlias} \n")
            .append("MERGE (${startAlias})-[relationship :${relationType}]->(${endAlias}) \n")
            .append("ON MATCH SET relationship += ${fields}, ${startAlias}." + INTERNAL_UPDATE_DATE + "= $updateDate, ${endAlias}." + INTERNAL_UPDATE_DATE + "= $updateDate, relationship." + INTERNAL_UPDATE_DATE + " = $updateDate \n")
            .append("ON CREATE SET relationship = ${fields}, relationship." + CREATION_DATE + " = $updateDate, relationship.meveo_uuid = $").append(NODE_ID).append("\n");

    public final static StringBuffer mergeCetStatement = new StringBuffer("MERGE (n:${cetCode}${fieldKeys}) \n")
            .append("ON CREATE SET n.meveo_uuid = $").append(NODE_ID).append(", n += ${fields}, n." + CREATION_DATE + " = timestamp()\n")
            .append("ON MATCH SET n += ${updatableFields}, n." + INTERNAL_UPDATE_DATE + " = timestamp() \n");

    public final static StringBuffer createCetStatement = new StringBuffer()
            .append("CREATE (n:${cetCode}${fields}) \n")
            .append("SET n." + CREATION_DATE + " = timestamp(), n.meveo_uuid = $").append(NODE_ID).append("\n");

    public final static StringBuffer additionalLabels = new StringBuffer("\nWITH ${alias}\n")
            .append("SET ${alias} ${labels} \n");

    public final static StringBuffer returnStatement = new StringBuffer("\nRETURN ${alias}\n");

    public final static StringBuffer findStartNodeId = new StringBuffer()
            .append("MATCH (startNode:${cetCode})-[:${crtCode}]->(:${endCetcode} ${fieldKeys}) \n")
            .append("RETURN startNode.meveo_uuid");

    public final static StringBuffer findRelationIdByTargetId = new StringBuffer()
            .append("MATCH ()-[r:${relationLabel}]->(t:${targetLabel}) \n")
            .append("WHERE t.meveo_uuid = $").append(NODE_ID).append("\n")
            .append("RETURN r.meveo_uuid");

    public final static StringBuffer findSourceNodeByRelationId = new StringBuffer()
            .append("MATCH (n:${sourceLabel})-[r:${relationLabel}]-() \n")
            .append("WHERE r.meveo_uuid = $").append(NODE_ID).append(" \n")
            .append("RETURN n.meveo_uuid");

    public final static StringBuffer findNodeId = new StringBuffer()
            .append("MATCH (n:${cetCode} ${fieldKeys})\n")
            .append("RETURN n.meveo_uuid");

    public final static StringBuffer updateNodeWithId = new StringBuffer()
            .append("MATCH (startNode:${cetCode}) WHERE startNode.meveo_uuid = $").append(NODE_ID).append("\n")
            .append("SET startNode += ${fields}, startNode." + INTERNAL_UPDATE_DATE + " = timestamp() \n");

    public final static String mergeOutGoingRelStatement = "MATCH (a:${cetCode})-[r]->(c) where a.meveo_uuid = ${originNodeId} "
            + "MATCH (b:${cetCode}) where b.meveo_uuid = ${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(b, relType, {}, c) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";

    public final static String mergeInGoingRelStatement = "MATCH (a:${cetCode})<-[r]-(c) where a.meveo_uuid = ${originNodeId} "
            + "MATCH (b:${cetCode})where b.meveo_uuid =${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(c, relType, {}, b) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";

    public final static String START_NODE_ALIAS = "startNode";
    public final static String END_NODE_ALIAS = "endNode";
}
