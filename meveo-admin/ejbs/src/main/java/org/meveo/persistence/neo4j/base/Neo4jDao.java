
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
package org.meveo.persistence.neo4j.base;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;
import org.meveo.persistence.neo4j.graph.Neo4jEntity;
import org.meveo.persistence.neo4j.graph.Neo4jRelationship;
import org.meveo.persistence.neo4j.helper.CypherHelper;
import org.meveo.persistence.neo4j.service.Neo4JRequests;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
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
    private static final String CET_CODE = "cetCode";
    public static final String NODE_ID = "NODE_ID";

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

    @Inject
    private CypherHelper cypherHelper;

	/**
	 * Drop an unique constraint on a given label and property if it does not exists yet
	 *
	 * @param neo4jConfiguration Repository code
	 * @param label              Label on which to drop the constraint
	 * @param property           Property on which to drop the constraint
	 */
    public void dropUniqueConstraint(String neo4jConfiguration, String label, String property) {
        StringBuilder query = new StringBuilder()
        		.append("DROP CONSTRAINT ON (n:").append(label).append(")\n")
        		.append("ASSERT n.").append(property).append(" IS UNIQUE");

        cypherHelper.update(
        		neo4jConfiguration,
        		query.toString(),
        		e -> LOGGER.debug("Unique constraint {}({}) does not exists", label, property)
    		);
    }

	/**
	 * Create an unique constraint on a given label and property if it does not exists yet
	 *
	 * @param neo4jConfiguration Repository code
	 * @param label              Label on which to add the constraint
	 * @param property           Property on which to add the constraint
	 */
    public void addUniqueConstraint(String neo4jConfiguration, String label, String property) {
        StringBuilder query = new StringBuilder()
        		.append("CREATE CONSTRAINT ON (n:").append(label).append(")\n")
        		.append("ASSERT n.").append(property).append(" IS UNIQUE");

        cypherHelper.update(
        		neo4jConfiguration,
        		query.toString(),
        		e -> LOGGER.debug("Unique constraint {}({}) already exists", label, property)
    		);
    }

    /**
	 * Create an index on a given label and property if it does not exists yet
	 *
	 * @param neo4jConfiguration Repository code
	 * @param label              Label on which to add the index
	 * @param property           Property on which to add the index
	 */
    public void createIndex(String neo4jConfiguration, String label, String property) {
        StringBuilder createIndexQuery = new StringBuilder("CREATE INDEX ON :").append(label).append("(").append(property).append(")");

        cypherHelper.update(
    		neo4jConfiguration,
    		createIndexQuery.toString(),
    		e -> LOGGER.debug("Index on {}({}) already exists", label, property)
		);
    }

    /**
	 * Drop an index on a given label and property if it does not exists yet
	 *
	 * @param neo4jConfiguration Repository code
	 * @param label              Label on which to drop the index
	 * @param property           Property on which to drop the index
	 */
    public void removeIndex(String neo4jConfiguration, String label, String property){
        StringBuilder dropIndex = new StringBuilder("DROP INDEX ON :").append(label).append("(").append(property).append(")");

        cypherHelper.update(
    		neo4jConfiguration,
    		dropIndex.toString(),
    		e -> LOGGER.debug("Index on {}({}) does not exists", label, property)
		);
    }

    /**
     * Remove a node using its UUID
     *
     * @param neo4jconfiguration Repository code
     * @param label              Label of the node to remove
     * @param uuid               UUID of the node to remove
     */
    public void removeNode(String neo4jconfiguration, String label, String uuid) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("MATCH (n:").append(label).append(") \n")
                .append("WHERE n.meveo_uuid = $uuid")
                .append("DETACH DELETE n ;");

        cypherHelper.execute(neo4jconfiguration, queryBuilder.toString(), Collections.singletonMap("uuid", uuid));
    }

    /**
     * Remove a relation using its UUID
     *
     * @param neo4jconfiguration Repository code
     * @param label              Label of the relation to remove
     * @param uuid               UUID of the relation to remove
     */
    public void removeRelation(String neo4jconfiguration, String label, String uuid) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("MATCH ()-[n:").append(label).append("]-() \n")
                .append("WHERE n.meveo_uuid = $uuid")
                .append("DELETE n ;");

        cypherHelper.execute(neo4jconfiguration, queryBuilder.toString(), Collections.singletonMap("uuid", uuid));
    }

    /**
     * Retrieve relationships instances base on relationship label and target's label and uuid
     *
     * @param neo4jConfiguration    Repository code
     * @param targetLabel           Target node's label
     * @param relationLabel         Relationship's label
     * @param targetUuid            Target node's UUID
     * @return the list of matched UUIDs
     */
    public List<String> findRelationIdByTargetId(String neo4jConfiguration, String targetLabel, String relationLabel, String targetUuid) {
        final Map<String, Object> values = new HashMap<>();
        values.put("relationLabel", relationLabel);
        values.put("targetLabel", targetLabel);

        try (Session session = neo4jSessionFactory.getSession(neo4jConfiguration);
             Transaction transaction = session.beginTransaction()) {

            StrSubstitutor sub = new StrSubstitutor(values);
            String statement = sub.replace(Neo4JRequests.findRelationIdByTargetId);

            final List<Record> result = transaction.run(statement, Collections.singletonMap(NODE_ID, targetUuid)).list();

            return result.stream()
                    .map(record -> record.get(0).asString())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Retrieve node instances base on relationship's label and uuid
     *
     * @param neo4jConfiguration Repository code
     * @param sourceLabel        Source node's label
     * @param relationLabel      Relationship's label
     * @param relationUUID       Relationship's UUID
     * @return the UUID of the source node
     * @throws NoSuchRecordException if the relation does not exists
     */
    public String findSourceNodeId(String neo4jConfiguration, String sourceLabel, String relationLabel, String relationUUID) throws NoSuchRecordException {
        final Map<String, Object> values = new HashMap<>();
        values.put("relationLabel", relationLabel);
        values.put("sourceLabel", sourceLabel);

        try (
                Session session = neo4jSessionFactory.getSession(neo4jConfiguration);
                Transaction transaction = session.beginTransaction()
        ) {

            StrSubstitutor sub = new StrSubstitutor(values);
            String statement = sub.replace(Neo4JRequests.findSourceNodeByRelationId);

            return transaction.run(statement, Collections.singletonMap(NODE_ID, relationUUID))
                    .single()
                    .get(0)
                    .asString();
        }
    }

    /**
     * Retrieves a node values by UUID
     *
     * @param neo4jConfiguration Repository code
     * @param label              Label of the node
     * @param uuid               UUID of the node
     * @return the values of the node
     */
    public Map<String, Object> findNodeById(String neo4jConfiguration, String label, String uuid) {
        return findNodeById(neo4jConfiguration, label, uuid, null);

    }

    /**
     * Retrieves a node values by UUID
     *
     * @param neo4jConfiguration Repository code
     * @param label              Label of the node
     * @param uuid               UUID of the node
     * @param fields             fields to return
     * @return the values of the node
     */
    public Map<String, Object> findNodeById(String neo4jConfiguration, String label, String uuid, List<String> fields) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (n:").append(label).append(") \n")
                .append("WHERE n.meveo_uuid = $uuid \n");

        query.append("RETURN n { ");

        if(fields != null && !fields.isEmpty()){
            for(String field : fields){
                query.append(".").append(field).append(", ");
            }
            query.delete(query.length() - 2, query.length());
        } else {
            query.append(".*");
        }

        query.append(" }");

        return cypherHelper.execute(
                neo4jConfiguration,
                query.toString(),
                Collections.singletonMap("uuid", uuid),
                (transaction, result) -> result.single().get(0).asMap()
        );

    }

    public String findNodeId(String neo4jConfiguration, String code, Map<String, Object> fieldsKeys){
        Transaction transaction = null;

        final Map<String, Object> values = new HashMap<>();
        values.put(FIELD_KEYS, Values.value(fieldsKeys));
        values.put(CET_CODE, code);

        try (Session session = neo4jSessionFactory.getSession(neo4jConfiguration)){
            transaction = session.beginTransaction();

            StrSubstitutor sub = new StrSubstitutor(values);
            String statement = sub.replace(Neo4JRequests.findNodeId);

            final Record result = transaction.run(statement, fieldsKeys).single();
            transaction.success();

            return result.get(0).asString();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.failure();
            }

            LOGGER.error("Cannot find id of node with label {} and key values {} for repository {} : {}", code, fieldsKeys, neo4jConfiguration, e.getMessage());
            return null;
        } finally {
            if (transaction != null) {
                transaction.close();
            }
        }
    }

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

            LOGGER.error("Cannot update IDL for repository {}", neo4jConfiguration, e);
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
            LOGGER.error("Error while executing a GraphQL query", e);
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
    public String mergeNode(String neo4JConfiguration, String cetCode, Map<String, Object> uniqueFields, Map<String, Object> fields, Map<String, Object> updatableFields, List<String> labels) {

        String alias = "n"; // Alias to use in query
        String nodeId = null;        // Id of the created node

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CET_CODE, cetCode);
        valuesMap.put(FIELD_KEYS, getFieldsString(uniqueFields.keySet()));
        valuesMap.put(FIELDS, getFieldsString(fields.keySet()));
        valuesMap.put(UPDATABLE_FIELDS, getFieldsString(updatableFields.keySet()));

        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.putAll(uniqueFields);
        fieldValues.putAll(fields);
        fieldValues.putAll(updatableFields);

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
            final StatementResult result = transaction.run(resolvedStatement, fieldValues);
            node = result.single().get(alias).asNode();
            transaction.success();  // Commit transaction
            nodeId = getMeveoUUID(node);
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error("Error while merging Neo4J nodes", e);
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

    private String getMeveoUUID(Node node) {
        return node.get("meveo_uuid").asString();
    }

    public String createNode(String neo4JConfiguration, String cetCode, Map<String, Object> fields, List<String> labels) {

        String alias = "n"; // Alias to use in query
        String nodeId = null;        // Id of the created node

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CET_CODE, cetCode);
        valuesMap.put(FIELDS, getFieldsString(fields.keySet()));

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
            final StatementResult result = transaction.run(resolvedStatement, fields);
            node = result.single().get(alias).asNode();
            transaction.success();  // Commit transaction
            nodeId = getMeveoUUID(node);
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error("Error while creating a Neo4J node", e);
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

    public void updateNodeByNodeId(String neo4JConfiguration, String nodeId, Map<String, Object> fields, List<String> labels) {

        String alias = "startNode"; // Alias to use in query

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(NODE_ID, nodeId);
        valuesMap.put(FIELDS, getFieldsString(fields.keySet()));

        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(NODE_ID, nodeId);
        fieldValues.putAll(fields);

        // Build statement
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        StringBuffer statement = Neo4JRequests.updateNodeWithId;

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
            final StatementResult result = transaction.run(resolvedStatement, fieldValues);
            node = result.single().get(alias).asNode();
            transaction.success();  // Commit transaction
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error("Error while updating a Neo4J node: {}", nodeId, e);
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

        if (node != null) {
            nodeUpdatedEvent.fire(new Neo4jEntity(node, neo4JConfiguration));
        }

    }

    public Set<String> executeUniqueConstraint(String neo4JConfiguration, CustomEntityTemplateUniqueConstraint uniqueConstraint, Map<String, Object> fields, String cetCode) {
        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CET_CODE, cetCode);
        valuesMap.put(FIELDS, getFieldsString(fields.keySet()));

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
            Set<String> ids = result.list()
                    .stream()
                    .map(record -> record.get(CustomEntityTemplateUniqueConstraint.RETURNED_ID_PROPERTY_NAME))
                    .filter(value -> !value.isNull())
                    .map(Value::asString)
                    .collect(Collectors.toSet());

            transaction.success();

            return ids;
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error("Error while executing a UniqueConstraint", e);
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

        return Collections.emptySet();
    }

    public void createRelationBetweenNodes(String neo4JConfiguration, String startNodeId, String label, String endNodeId, Map<String, Object> fields) {

        /* Build values map */
        final Map<String, Object> values = new HashMap<>();
        values.put("startNodeId", startNodeId);
        values.put("relationshipLabel", label);
        values.put("endNodeId", endNodeId);
        final String fieldsString = getFieldsString(fields.keySet());
        values.put(FIELDS, fieldsString);
        values.putAll(fields);

        if(StringUtils.isBlank(label)){
            throw new IllegalArgumentException("Cannot create relation between " + startNodeId + " and " + endNodeId + " with fields "+ fields + " : relationship label must be provided");
        }

        StrSubstitutor sub = new StrSubstitutor(values);
        String statement = sub.replace(Neo4JRequests.createRelationship);
        // Begin transaction
        Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();
        Relationship relationship = null;

        try {
            // Execute query and parse results
            final StatementResult result = transaction.run(statement, values);
            relationship = result.list()
                .stream()
                .findFirst()
                .map(record -> record.get("relationship").asRelationship()).orElseThrow(() -> new IllegalStateException("No relationship created"));

            transaction.success();  // Commit transaction
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error("Error while creating a relation between 2 Neo4J nodes: ({})-[:{}]->({})", startNodeId, label, endNodeId, e);
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
