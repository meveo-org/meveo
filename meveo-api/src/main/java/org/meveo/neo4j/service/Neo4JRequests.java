package org.meveo.neo4j.service;

public class Neo4JRequests {

    public static final String ADDITIONAL_LABELS = "labels";
    public static final String ALIAS = "alias";

    public static final String INTERNAL_UPDATE_DATE = "updateDate";
    private static final String CREATION_DATE = "creationDate";

    /**
     * Create a relationship between node with given ids
     * Parameters : <br>
     * - startNodeId : Id of the start node <br>
     * - endNodeId : Id of the target node <br>
     * - relationshipLabel : Label of the relationship to create <br>
     */
    public final static StringBuffer createRelationship = new StringBuffer("MATCH (startNode) WHERE ID(startNode) = $startNodeId ")
            .append("WITH startNode ")
            .append("MATCH (endNode) WHERE ID(endNode) = $endNodeId ")
            .append("WITH startNode, endNode ")
            .append("MERGE (startNode)-[relationship :${relationshipLabel} ${fields}]->(endNode)")
            .append("ON CREATE SET relationship." + CREATION_DATE + " = timestamp()")
            .append("ON MATCH SET relationship." + INTERNAL_UPDATE_DATE + " = timestamp()")
            .append("RETURN relationship");

    /**
     * Delete a node with all its associated relations
     * Parameters : <br>
     * - cetCode : code of the node to delete <br>
     * - uniqueFields : unique fields that identify the node
     */
    final static StringBuffer deleteCet = new StringBuffer("MATCH (n:${cetCode} ${uniqueFields})")
            .append(" WITH n, properties(n) as properties,  labels(n) as labels, ID(n) as id ")
            .append(" DETACH DELETE n")
            .append(" RETURN properties");

    final static StringBuffer crtStatement = new StringBuffer("MATCH (${startAlias}:${startNode} ${starNodeKeys})")
            .append(" MATCH (${endAlias}:${endNode} ${endNodeKeys})")
            .append(" MERGE (${startAlias})-[relationship :${relationType} ${fields}]->(${endAlias}) ")
            .append(" ON MATCH SET ${startAlias}." + INTERNAL_UPDATE_DATE + "=${updateDate}, ${endAlias}." + INTERNAL_UPDATE_DATE + "=${updateDate}, relationship." + INTERNAL_UPDATE_DATE + " = ${updateDate}")
            .append(" ON CREATE SET relationship." + CREATION_DATE + " = ${updateDate}");


    public final static StringBuffer cetStatement = new StringBuffer("Merge (n:${cetCode}${fieldKeys}) ")
            .append("ON CREATE SET n = ${fields}, n." + CREATION_DATE + " = timestamp()")
            .append("ON MATCH SET n += ${fields}, n." + INTERNAL_UPDATE_DATE + " = timestamp()");

    public final static StringBuffer additionalLabels = new StringBuffer(" WITH ${alias} ")
            .append("SET ${alias} ${labels}");

    public final static StringBuffer returnStatement = new StringBuffer(" RETURN ${alias} ");

    final static StringBuffer findStartNodeId = new StringBuffer()
            .append("MATCH (startNode:${cetCode})-[:${crtCode}]->(:${endCetcode} ${fieldKeys})")
            .append(" RETURN ID(startNode)");

    final static StringBuffer updateNodeWithId = new StringBuffer()
            .append("MATCH (startNode) WHERE ID(startNode) = $id")
            .append(" SET startNode += ${fields}");

    final static String mergeOutGoingRelStatement = "MATCH (a:${cetCode})-[r]->(c) where ID(a) =${originNodeId} "
            + "MATCH (b:${cetCode})where ID(b) =${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(b, relType, {}, c) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";

    final static String mergeInGoingRelStatement = "MATCH (a:${cetCode})<-[r]-(c) where ID(a) =${originNodeId} "
            + "MATCH (b:${cetCode})where ID(b) =${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(c, relType, {}, b) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";

    final static String START_NODE_ALIAS = "start";
    final static String END_NODE_ALIAS = "end";
}
