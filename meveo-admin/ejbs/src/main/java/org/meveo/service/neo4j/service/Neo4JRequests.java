package org.meveo.service.neo4j.service;

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
    public final static StringBuffer createRelationship = new StringBuffer("MATCH (startNode) WHERE ID(startNode) = $startNodeId \n")
            .append("WITH startNode \n")
            .append("MATCH (endNode) WHERE ID(endNode) = $endNodeId \n")
            .append("WITH startNode, endNode \n")
            .append("MERGE (startNode)-[relationship :${relationshipLabel} ${fields}]->(endNode) \n")
            .append("ON CREATE SET relationship." + CREATION_DATE + " = timestamp() \n")
            .append("ON MATCH SET relationship." + INTERNAL_UPDATE_DATE + " = timestamp() \n")
            .append("RETURN relationship");

    /**
     * Delete a node with all its associated relations
     * Parameters : <br>
     * - cetCode : code of the node to delete <br>
     * - uniqueFields : unique fields that identify the node
     */
    public final static StringBuffer deleteCet = new StringBuffer("MATCH (n:${cetCode} ${uniqueFields}) \n")
            .append(" WITH n, properties(n) as properties,  labels(n) as labels, ID(n) as id \n")
            .append(" DETACH DELETE n \n")
            .append(" RETURN properties, labels, id");

    public final static StringBuffer crtStatement = new StringBuffer("MATCH (${startAlias}:${startNode} ${starNodeKeys}) \n")
            .append(" MATCH (${endAlias}:${endNode} ${endNodeKeys}) \n")
            .append(" MERGE (${startAlias})-[relationship :${relationType} ${fields}]->(${endAlias}) \n")
            .append(" ON MATCH SET ${startAlias}." + INTERNAL_UPDATE_DATE + "=${updateDate}, ${endAlias}." + INTERNAL_UPDATE_DATE + "=${updateDate}, relationship." + INTERNAL_UPDATE_DATE + " = ${updateDate} \n")
            .append(" ON CREATE SET relationship." + CREATION_DATE + " = ${updateDate} \n");

    public final static StringBuffer crtStatementByNodeIds = new StringBuffer()
            .append("MATCH (${startAlias}:${startNode}) \n")
            .append("WHERE ID(${startAlias}) = $startNodeId \n")
            .append("WITH ${startAlias} \n")
            .append("MATCH (${endAlias}:${endNode}) \n")
            .append("WHERE ID(${endAlias}) = $endNodeId \n")
            .append("WITH ${startAlias}, ${endAlias} \n")
            .append("MERGE (${startAlias})-[relationship :${relationType} ${fields}]->(${endAlias}) \n")
            .append("ON MATCH SET ${startAlias}." + INTERNAL_UPDATE_DATE + "= $updateDate, ${endAlias}." + INTERNAL_UPDATE_DATE + "= $updateDate, relationship." + INTERNAL_UPDATE_DATE + " = $updateDate \n")
            .append("ON CREATE SET relationship." + CREATION_DATE + " = $updateDate \n");

    public final static StringBuffer uniqueCrtStatementByNodeIds = new StringBuffer()
            .append("MATCH (${startAlias}:${startNode}) \n")
            .append("WHERE ID(${startAlias}) = $startNodeId \n")
            .append("WITH ${startAlias} \n")
            .append("MATCH (${endAlias}:${endNode}) \n")
            .append("WHERE ID(${endAlias}) = $endNodeId \n")
            .append("WITH ${startAlias}, ${endAlias} \n")
            .append("MERGE (${startAlias})-[relationship :${relationType}]->(${endAlias}) \n")
            .append("ON MATCH SET relationship += ${fields}, ${startAlias}." + INTERNAL_UPDATE_DATE + "= $updateDate, ${endAlias}." + INTERNAL_UPDATE_DATE + "= $updateDate, relationship." + INTERNAL_UPDATE_DATE + " = $updateDate \n")
            .append("ON CREATE SET relationship = ${fields}, relationship." + CREATION_DATE + " = $updateDate \n");

    public final static StringBuffer mergeCetStatement = new StringBuffer("MERGE (n:${cetCode}${fieldKeys}) \n")
            .append("ON CREATE SET n = ${fields}, n." + CREATION_DATE + " = timestamp() \n")
            .append("ON MATCH SET n += ${fields}, n." + INTERNAL_UPDATE_DATE + " = timestamp() \n");

    public final static StringBuffer createCetStatement = new StringBuffer()
            .append("CREATE (n:${cetCode}${fields}) \n")
            .append("SET n." + CREATION_DATE + " = timestamp() \n");

    public final static StringBuffer additionalLabels = new StringBuffer(" WITH ${alias} ")
            .append("SET ${alias} ${labels} \n");

    public final static StringBuffer returnStatement = new StringBuffer(" RETURN ${alias} ");

    public final static StringBuffer findStartNodeId = new StringBuffer()
            .append("MATCH (startNode:${cetCode})-[:${crtCode}]->(:${endCetcode} ${fieldKeys})")
            .append(" RETURN ID(startNode)");

    public final static StringBuffer updateNodeWithId = new StringBuffer()
            .append("MATCH (startNode) WHERE ID(startNode) = $id")
            .append(" SET startNode += ${fields}, startNode.\" + INTERNAL_UPDATE_DATE + \" = timestamp() \n");

    public final static String mergeOutGoingRelStatement = "MATCH (a:${cetCode})-[r]->(c) where ID(a) =${originNodeId} "
            + "MATCH (b:${cetCode})where ID(b) =${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(b, relType, {}, c) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";

    public final static String mergeInGoingRelStatement = "MATCH (a:${cetCode})<-[r]-(c) where ID(a) =${originNodeId} "
            + "MATCH (b:${cetCode})where ID(b) =${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(c, relType, {}, b) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";

    public final static String START_NODE_ALIAS = "start";
    public final static String END_NODE_ALIAS = "end";

    public final static StringBuffer createSourceNodeStatement = new StringBuffer("MERGE (source:Source { id: ${id} }) \n")
            .append("ON CREATE SET sourceId = ${sourceId}, sourceType = ${sourceType} \n")
            .append("WITH source \n")
            .append("MERGE (node)-[:HAS_SOURCE]->(source) \n")
            .append("WHERE ID(node) = ${nodeId} \n")
            .append("RETURN source");
}
