package org.meveo.neo4j.service;

public class Neo4JRequests {

    public static final String ADDITIONAL_LABELS = "labels";
    public static final String ALIAS = "alias";

    /**
     * Create a relationship between node with given ids
     * Parameters : <br>
     *     - startNodeId : Id of the start node <br>
     *     - endNodeId : Id of the target node <br>
     *     - relationshipLabel : Label of the relationship to create <br>
     */
    public final static StringBuffer createRelationship = new StringBuffer("MATCH (startNode) WHERE ID(startNode) = $startNodeId ")
            .append("WITH startNode ")
            .append("MATCH (endNode) WHERE ID(endNode) = $endNodeId ")
            .append("WITH startNode, endNode ")
            .append("MERGE (startNode)-[relationship:${relationshipLabel}]->(endNode)")
            .append("ON CREATE SET relationship.creation_date = timestamp()")
            .append("ON MATCH SET relationship.internal_updateDate = timestamp()")
            .append("RETURN relationship");

    /**
     * Delete a node with all its associated relations
     * Parameters : <br>
     * - cetCode : code of the node to delete <br>
     * - uniqueFields : unique fields that identify the node
     */
    public final static StringBuffer deleteCet = new StringBuffer("MATCH (n:${cetCode} ${uniqueFields})")
            .append(" WITH n, properties(n) as properties,  labels(n) as labels, ID(n) as id ")
            .append(" DETACH DELETE n")
            .append(" RETURN properties");

    public final static StringBuffer crtStatement = new StringBuffer("MATCH (${startAlias}:${startNode} ${starNodeKeys})")
            .append(" MATCH (${endAlias}:${endNode} ${endNodeKeys})")
            .append(" MERGE (${startAlias})-[relationship:${relationType}${fields}]->(${endAlias}) ")
            .append(" ON MATCH SET ${startAlias}.internal_updateDate=${updateDate}, ${endAlias}.internal_updateDate=${updateDate}, relationship.update_date = ${updateDate}")
            .append(" ON CREATE SET relationship.creation_date = ${updateDate}");


    public final static StringBuffer cetStatement = new StringBuffer("Merge (n:${cetCode}${fieldKeys}) ")
            .append("ON CREATE SET n = ${fields}, n.creation_date = timestamp()")
            .append("ON MATCH SET n += ${fields}, n.internal_updateDate = timestamp()");

    public final static StringBuffer additionalLabels = new StringBuffer(" WITH ${alias} ")
            .append("SET ${alias} ${labels}");

    public final static StringBuffer returnStatement = new StringBuffer(" RETURN ${alias} ");

    public final static StringBuffer createCet = new StringBuffer("CREATE (n:${cetCode}) ")
            .append("SET n = ${fields}");

    public final static StringBuffer findStartNodeId = new StringBuffer()
            .append("MATCH (startNode:${cetCode})-[:${crtCode}]->(:${endCetcode} ${fieldKeys})")
            .append(" RETURN ID(startNode)");

    public final static StringBuffer updateNodeWithId = new StringBuffer()
            .append("MATCH (startNode) WHERE ID(startNode) = $id")
            .append(" SET startNode += ${fields}");

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
}
