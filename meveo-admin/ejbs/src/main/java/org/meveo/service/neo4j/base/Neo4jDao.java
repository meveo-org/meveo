package org.meveo.service.neo4j.base;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Updated;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;
import org.meveo.service.neo4j.graph.Neo4jEntity;
import org.meveo.service.neo4j.graph.Neo4jRelationship;
import org.meveo.service.neo4j.service.Neo4JRequests;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class Neo4jDao {

    private static final String FIELD_KEYS = "fieldKeys";
    private static final String FIELDS = "fields";
    private static final String UPDATABLE_FIELDS = "updatableFields";
    public static final String ID = "id";

    Logger LOGGER = LoggerFactory.getLogger(Neo4jDao.class);

    @Inject
    private Neo4jConnectionProvider neo4jSessionFactory;

    @Inject
    @Updated
    private Event<Neo4jEntity> nodeUpdatedEvent;

    @Inject
    @Created
    private Event<Neo4jRelationship> edgeCreatedEvent;

    @Inject
    @Updated
    private Event<Neo4jRelationship> edgeUpdatedEvent;

    @Inject
    @Created
    private Event<Neo4jEntity> nodeCreatedEvent;

    public void updateIDL(String neo4jConfiguration, String idl) {
        Transaction transaction = null;

        try (Session session = neo4jSessionFactory.getSession(neo4jConfiguration)){
            transaction = session.beginTransaction();
            transaction.run("call graphql.idl($idl)", Collections.singletonMap("idl", idl));

            transaction.success();
            transaction.close();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.failure();
                transaction.close();
            }

            LOGGER.error("Cannot update IDL for repository {} : {}", neo4jConfiguration, e.getMessage());
        }
    }

    public Map<String, Object> executeGraphQLQuery(String neo4JConfiguration, String query, Map<String, Object> variables, String operationName) {

        /* Build values map */
        final Map<String, Object> values = new HashMap<>();
        values.put("query", query);
        values.put("variables", (variables != null) ? variables : new HashMap<>());
        values.put("operationName", (operationName != null) ? operationName : "");

        StringBuilder statement = new StringBuilder("call graphql.execute($query,$variables,$operationName)");
        // Begin transaction
        Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();

        try {
            // Execute query and parse results
            final StatementResult result = transaction.run(statement.toString(), values);
            transaction.success();  // Commit transaction
            return result.list()
                    .stream()
                    .map(record -> record.get("result"))
                    .map(Value::asMap)
                    .findFirst()
                    .orElseGet(Collections::emptyMap);
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error(e.getMessage());
            return null;
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

    }

    /**
     * Merge a Neo4J node based on its unique fields
     *
     * @param neo4JConfiguration Neo4J coordinates
     * @param cetCode            Code of the corresponding CustomEntityTemplate
     * @param uniqueFields       Unique fields that identifies the node
     * @param fields             Properties of the node
     * @param labels             Additionnal labels of the node
     * @return the id of the created node, or null if it has failed
     */
    public Long mergeNode(String neo4JConfiguration, String cetCode, Map<String, Object> uniqueFields, Map<String, Object> fields, Map<String, Object> updatableFields, List<String> labels) {

        String alias = "n"; // Alias to use in query
        Long nodeId = null;        // Id of the created node

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("cetCode", cetCode);
        valuesMap.put(FIELD_KEYS, Values.value(uniqueFields));
        valuesMap.put(FIELDS, Values.value(fields));
        valuesMap.put(UPDATABLE_FIELDS, Values.value(updatableFields));

        // Build statement
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        StringBuffer statement = Neo4JRequests.mergeCetStatement;

        if (labels != null) {
            statement = appendAdditionalLabels(statement, labels, alias, valuesMap);
        }

        statement = appendReturnStatement(statement, alias, valuesMap);
        String resolvedStatement = sub.replace(statement);
        resolvedStatement = resolvedStatement.replace('"', '\'');

        // Begin transaction
        Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();

        Node node = null;

        try {
            // Execute query and parse results
            LOGGER.info(resolvedStatement + "\n");
            final StatementResult result = transaction.run(resolvedStatement);
            node = result.single().get(alias).asNode();
            transaction.success();  // Commit transaction
            nodeId = node.id();
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error(e.getMessage());
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

        if (node != null) {
            final Neo4jEntity neo4jEntity = new Neo4jEntity(node, neo4JConfiguration);
            //  If node has been created, fire creation event. If it was updated, fire update event.
            if (node.containsKey(Neo4JRequests.INTERNAL_UPDATE_DATE)) {
                nodeUpdatedEvent.fire(neo4jEntity);
            } else {
                nodeCreatedEvent.fire(neo4jEntity);
            }
        }

        return nodeId;
    }

    public Long createNode(String neo4JConfiguration, String cetCode, Map<String, Object> fields, List<String> labels) {

        String alias = "n"; // Alias to use in query
        Long nodeId = null;        // Id of the created node

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("cetCode", cetCode);
        valuesMap.put(FIELDS, Values.value(fields));

        // Build statement
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        StringBuffer statement = Neo4JRequests.createCetStatement;

        if (labels != null) {
            statement = appendAdditionalLabels(statement, labels, alias, valuesMap);
        }

        statement = appendReturnStatement(statement, alias, valuesMap);
        String resolvedStatement = sub.replace(statement);
        resolvedStatement = resolvedStatement.replace('"', '\'');

        // Begin transaction
        Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();

        Node node = null;

        try {
            // Execute query and parse results
            LOGGER.info(resolvedStatement + "\n");
            final StatementResult result = transaction.run(resolvedStatement);
            node = result.single().get(alias).asNode();
            transaction.success();  // Commit transaction
            nodeId = node.id();
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error(e.getMessage());
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

        if (node != null) {
            nodeCreatedEvent.fire(new Neo4jEntity(node, neo4JConfiguration));
        }

        return nodeId;
    }

    public void updateNodeByNodeId(String neo4JConfiguration, Long nodeId, Map<String, Object> fields) {

        String alias = "startNode"; // Alias to use in query

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(ID, nodeId);
        valuesMap.put(FIELDS, Values.value(fields));

        // Build statement
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        StringBuffer statement = Neo4JRequests.updateNodeWithId;

        statement = appendReturnStatement(statement, alias, valuesMap);
        String resolvedStatement = sub.replace(statement);
        resolvedStatement = resolvedStatement.replace('"', '\'');

        // Begin transaction
        Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();

        Node node = null;

        try {
            // Execute query and parse results
            LOGGER.info(resolvedStatement + "\n");
            final StatementResult result = transaction.run(resolvedStatement, valuesMap);
            node = result.single().get(alias).asNode();
            transaction.success();  // Commit transaction
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error(e.getMessage());
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

        if (node != null) {
            nodeUpdatedEvent.fire(new Neo4jEntity(node, neo4JConfiguration));
        }

    }

    public Set<Long> executeUniqueConstraint(String neo4JConfiguration, CustomEntityTemplateUniqueConstraint uniqueConstraint, Map<String, Object> fields, String cetCode) {
        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("cetCode", cetCode);
        valuesMap.put(FIELDS, Values.value(fields));

        // Build statement
        StringBuffer statement = new StringBuffer(uniqueConstraint.getCypherQuery());
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedStatement = sub.replace(statement);
        resolvedStatement = resolvedStatement.replace('"', '\'');

        // Begin transaction
        Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();

        try {
            // Execute query and parse results
            LOGGER.info(resolvedStatement + "\n");
            final StatementResult result = transaction.run(resolvedStatement, fields);
            Set<Long> ids = result.list()
                    .stream()
                    .map(record -> record.get(CustomEntityTemplateUniqueConstraint.RETURNED_ID_PROPERTY_NAME))
                    .filter(value -> !value.isNull())
                    .map(Value::asLong)
                    .collect(Collectors.toSet());

            transaction.success();

            return ids;
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error(e.getMessage());
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

        return Collections.emptySet();
    }

    public void createRelationBetweenNodes(String neo4JConfiguration, Long startNodeId, String label, Long endNodeId, Map<String, Object> fields) {

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
        Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();
        Relationship relationship = null;

        try {
            // Execute query and parse results
            final StatementResult result = transaction.run(statement, values);
            relationship = result.single().get("relationship").asRelationship();

            transaction.success();  // Commit transaction
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error(e.getMessage());
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

        if (relationship != null) {
            //  If relationship has been created, fire creation event. If it was updated, fire update event.
            final Neo4jRelationship neo4jRelationship = new Neo4jRelationship(relationship, neo4JConfiguration);
            if (relationship.containsKey(Neo4JRequests.INTERNAL_UPDATE_DATE)) {
                edgeUpdatedEvent.fire(neo4jRelationship);
            } else {
                edgeCreatedEvent.fire(neo4jRelationship);
            }
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
    public StringBuffer appendReturnStatement(final StringBuffer statement, String alias, Map<String, Object> valuesMap) {
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
    private String buildLabels(List<String> labels) {
        StringBuilder labelsString = new StringBuilder();
        for (String label : labels) {
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

}
