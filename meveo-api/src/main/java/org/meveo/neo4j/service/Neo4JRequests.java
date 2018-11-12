package org.meveo.neo4j.service;

public class Neo4JRequests {

    /**
     * Delete a node with all its associated relations
     * Parameters : <br>
     * - cetCode : code of the node to delete <br>
     * - uniqueFields : unique fields that identify the node
     */
    protected final static StringBuffer deleteCet = new StringBuffer("MATCH (n:${cetCode} ${uniqueFields})")
            .append(" DETACH DELETE n");

    protected final static StringBuffer crtStatement = new StringBuffer("MATCH (${startAlias}:${startNode} ${starNodeKeys})")
            .append(" MATCH (${endAlias}:${endNode} ${endNodeKeys})")
            .append(" MERGE (${startAlias})-[:${relationType}${fields}]->(${endAlias}) set ${startAlias}.internal_updateDate=${updateDate}, ${endAlias}.internal_updateDate=${updateDate}");

    protected final static StringBuffer cetStatement = new StringBuffer("Merge (n:${cetCode}${fieldKeys}) ")
            .append("ON CREATE SET n = ${fields}")
            .append("ON MATCH SET n += ${fields} return ID(n) as id");

    protected final static StringBuffer createCet = new StringBuffer("CREATE (n:${cetCode}) ")
            .append("SET n = ${fields}");

    protected final static StringBuffer findStartNodeId = new StringBuffer()
            .append("MATCH (startNode:${cetCode})-[:${crtCode}]->(:${endCetcode} ${fieldKeys})")
            .append(" RETURN ID(startNode)");

    protected final static StringBuffer updateNodeWithId = new StringBuffer()
            .append("MATCH (startNode) WHERE ID(startNode) = $id")
            .append(" SET startNode += ${fields}");

    protected final static String mergeOutGoingRelStatement = "MATCH (a:${cetCode})-[r]->(c) where ID(a) =${originNodeId} "
            + "MATCH (b:${cetCode})where ID(b) =${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(b, relType, {}, c) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";

    protected final static String mergeInGoingRelStatement = "MATCH (a:${cetCode})<-[r]-(c) where ID(a) =${originNodeId} "
            + "MATCH (b:${cetCode})where ID(b) =${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(c, relType, {}, b) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";

    protected final static String START_NODE_ALIAS = "start";
    protected final static String END_NODE_ALIAS = "end";
}
