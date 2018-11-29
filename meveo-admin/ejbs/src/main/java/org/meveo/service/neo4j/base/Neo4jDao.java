package org.meveo.service.neo4j.base;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Updated;
import org.meveo.service.neo4j.service.Neo4JRequests;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Neo4jDao {

    private static final String FIELD_KEYS = "fieldKeys";
    private static final String FIELDS = "fields";
    public static final String ID = "id";

    @Inject
    private Neo4jConnectionProvider neo4jSessionFactory;

    @Inject
    @Updated
    private Event<org.neo4j.driver.v1.types.Node> nodeUpdatedEvent;

    @Inject
    @Created
    private Event<org.neo4j.driver.v1.types.Relationship> edgeCreatedEvent;

    @Inject
    @Updated
    private Event<org.neo4j.driver.v1.types.Relationship> edgeUpdatedEvent;

    @Inject
    @Created
    private Event<org.neo4j.driver.v1.types.Node> nodeCreatedEvent;

    /**
     * Create a Neo4J node
     *
     * @param cetCode Code of the corresponding CustomEntityTemplate
     * @param uniqueFields Unique fields that identifies the node
     * @param fields Properties of the node
     * @param labels Additionnal labels of the node
     * @return the id of the created node, or null if it has failed
     */
    public Long createNode(String cetCode, Map<String, Object> uniqueFields, Map<String, Object> fields, List<String> labels) {

        String alias = "n"; // Alias to use in query
        Long nodeId;        // Id of the created node

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("cetCode", cetCode);
        valuesMap.put(FIELD_KEYS, Values.value(uniqueFields));
        valuesMap.put(FIELDS, Values.value(fields));

        // Build statement
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        StringBuffer statement = appendAdditionalLabels(Neo4JRequests.cetStatement, labels, alias, valuesMap);
        statement = appendReturnStatement(statement, alias, valuesMap);
        String resolvedStatement = sub.replace(statement);
        resolvedStatement = resolvedStatement.replace('"', '\'');

        // Begin transaction
        Session session = neo4jSessionFactory.getSession();
        final Transaction transaction = session.beginTransaction();

        try {
            // Execute query and parse results
            final StatementResult result = transaction.run(resolvedStatement);
            final Node node = result.single().get(alias).asNode();

            //  If node has been created, fire creation event. If it was updated, fire update event.
            if(node.containsKey(Neo4JRequests.INTERNAL_UPDATE_DATE)){
                nodeUpdatedEvent.fire(node);
            }else{
                nodeCreatedEvent.fire(node);
            }

            nodeId = node.id();
            transaction.success();  // Commit transaction
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }
        return nodeId;
    }

    public void createRealtionBetweenNodes(Long startNodeId, String label, Long endNodeId, Map<String, Object> fields){

        /* Build values map */
        final Map<String, Object> values = new HashMap<>();
        values.put("startNodeId", startNodeId);
        values.put("relationshipLabel", label);
        values.put("endNodeId", endNodeId);
        final String fieldsString = getFieldsString(fields.keySet());
        values.put(FIELDS, fieldsString);
        values.putAll(fields);

        StrSubstitutor sub = new StrSubstitutor(values);
        String statement = sub.replace(Neo4JRequests.createRelationship);
        // Begin transaction
        Session session = neo4jSessionFactory.getSession();
        final Transaction transaction = session.beginTransaction();

        try {
            // Execute query and parse results
            final StatementResult result = transaction.run(statement, values);
            final Relationship relationship = result.single().get("relationship").asRelationship();

            //  If relationship has been created, fire creation event. If it was updated, fire update event.
            if(relationship.containsKey(Neo4JRequests.INTERNAL_UPDATE_DATE)){
                edgeUpdatedEvent.fire(relationship);
            }else{
                edgeCreatedEvent.fire(relationship);
            }

            transaction.success();  // Commit transaction
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }
    }

    /**
     * Append a return statement for the given query
     *
     * @param statement Base statement to extend
     * @param alias     Alias to refer in the query
     * @param valuesMap Map where are stored the statement's variables - Variables required for the query will be added to it
     * @return A new {@link StringBuffer} representing the extended query
     */
    public StringBuffer appendReturnStatement(final StringBuffer statement, String alias, Map<String, Object> valuesMap){
        valuesMap.put(Neo4JRequests.ALIAS, alias);
        return new StringBuffer(statement).append(Neo4JRequests.returnStatement);
    }

    /**
     * If additional labels are defined, append a query to the base statement to add them to the node.
     *
     * @param statement Base statement to extend
     * @param labels    Additional labels of the node
     * @param alias     Alias to refer in the query
     * @param valuesMap Map where are stored the statement's variables - Variables required for the query will be added to it
     * @return A new {@link StringBuffer} representing the extended query
     */
    public StringBuffer appendAdditionalLabels(final StringBuffer statement, List<String> labels, String alias, Map<String, Object> valuesMap) {
        StringBuffer copyStatement = new StringBuffer(statement);
        if (!labels.isEmpty()) {
            copyStatement.append(Neo4JRequests.additionalLabels);
            valuesMap.put(Neo4JRequests.ADDITIONAL_LABELS, buildLabels(labels));
            valuesMap.put(Neo4JRequests.ALIAS, alias);
        }
        return copyStatement;
    }

    /**
     * @param labels Additional labels defined for the CET / CRT
     * @return The labels give to the created entity
     */
    private String buildLabels(List<String> labels){
        StringBuilder labelsString = new StringBuilder();
        for (String label : labels){
            labelsString.append(" :").append(label);
        }
        return labelsString.toString();
    }

    public String getFieldsString(Set<String> strings) {
        return "{ " + strings
                .stream()
                .map(s -> s + ": $" + s)
                .collect(Collectors.joining(", ")) + " }";
    }

    public void createIndexLabelByProperty(String label, String property) {

        String statement = String.format("CREATE INDEX ON :%s(%s)", label, property);
        // Begin transaction
        Session session = neo4jSessionFactory.getSession();
        final Transaction transaction = session.beginTransaction();

        try {
            // Execute query and parse results
            final StatementResult result = transaction.run(statement);
            transaction.success();  // Commit transaction
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }
    }

    public void removeIndexLabelByProperty(String label, String property) {
        String statement = String.format("DROP INDEX ON :%s(%s)", label, property);
        // Begin transaction
        Session session = neo4jSessionFactory.getSession();
        final Transaction transaction = session.beginTransaction();

        try {
            // Execute query and parse results
            final StatementResult result = transaction.run(statement);
            transaction.success();  // Commit transaction
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }
    }
}
