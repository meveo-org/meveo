
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

import com.google.common.collect.ImmutableMap;
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
import org.neo4j.driver.v1.exceptions.ClientException;
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

import static org.meveo.persistence.neo4j.service.Neo4JRequests.CREATION_DATE;
import static org.meveo.persistence.neo4j.service.Neo4JRequests.INTERNAL_UPDATE_DATE;

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
     * Remove all data that has the given label
     *
     * @param neo4jConfiguration Code of the Neo4J instance
     * @param label              Label to remove
     */
    public void removeByLabel(String neo4jConfiguration, String label) {
        String query = String.format("MATCH (n:%s) DETACH DELETE n", label);

        cypherHelper.update(
                neo4jConfiguration,
                query,
                e -> LOGGER.debug("Cannot delete data with label {}", label)
        );
    }

    public void removeNodeByUUID(String neo4jconfiguration, String label, String uuid){
        StringBuilder queryBuilder = new StringBuilder()
                .append("MATCH (n:").append(label).append(") \n")
                .append("WHERE n.meveo_uuid = $id \n")
                .append("DETACH DELETE n \n")
                .append("RETURN n");

        cypherHelper.execute(
                neo4jconfiguration,
                queryBuilder.toString(),
                Collections.singletonMap("id", uuid),
                (transaction, result) -> {
                    final Record single = result.single();
                    final Node deletedNode = single.get(0).asNode();
                    if(deletedNode != null){
                        LOGGER.info("Node with id {} and uuid {} deleted", deletedNode.id(), uuid);
                        transaction.success();
                    } else {
                        LOGGER.error("Node with uuid {} not deleted", uuid);
                        transaction.failure();
                    }
                    return null;
                },
                e -> LOGGER.error("Cannot remove node with uuid {}", uuid, e)
        );
    }

    /**
     * Remove a node using its id
     *
     * @param neo4jconfiguration Repository code
     */
    public void removeNode(String neo4jconfiguration, Long id) {
        StringBuilder queryBuilder = new StringBuilder()
                .append("MATCH (n) \n")
                .append("WHERE ID(n) = $id \n")
                .append("DETACH DELETE n \n")
                .append("RETURN n");

        cypherHelper.execute(
                neo4jconfiguration,
                queryBuilder.toString(),
                Collections.singletonMap("id", id),
                (transaction, result) -> {
                    final Record single = result.single();
                    final Node deletedNode = single.get(0).asNode();
                    if(deletedNode != null){
                        LOGGER.info("Node with id {} deleted", deletedNode.id());
                        transaction.success();
                    } else {
                        LOGGER.error("Node with id {} not deleted", id);
                        transaction.failure();
                    }
                    return null;
                },
                e -> LOGGER.error("Cannot remove node with id {}", id, e)
        );
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

        Transaction transaction = null;
        try (Session session = neo4jSessionFactory.getSession(neo4jConfiguration)){
            transaction = session.beginTransaction();
            final StatementResult result = transaction.run(query.toString(), Collections.singletonMap("uuid", uuid));
            return result.single().get(0).asMap();

        } finally {
            if (transaction != null) {
                transaction.close();
            }
        }

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
            
            if(!(e instanceof NoSuchRecordException)) {
	            LOGGER.error("Cannot find id of node with label {} and key values {} for repository {} : {}", code, fieldsKeys, neo4jConfiguration, e);
            }
            
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
            LOGGER.error("[{}] Error while executing a GraphQL query : {}", neo4JConfiguration, query,  e);
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
		return mergeNode(neo4JConfiguration, cetCode, uniqueFields, fields, updatableFields, labels, null);
	}

	/**
     * Merge a Neo4J node based on its unique fields
     *
     * @param neo4JConfiguration Neo4J coordinates
     * @param cetCode            Code of the corresponding CustomEntityTemplate
     * @param uniqueFields       Unique fields that identifies the node
     * @param fields             Properties of the node
     * @param labels             Additionnal labels of the node
     * @param uuid TODO
     * @return the id of the created node, or null if it has failed
     */
    public String mergeNode(String neo4JConfiguration, String cetCode, Map<String, Object> uniqueFields, Map<String, Object> fields, Map<String, Object> updatableFields, List<String> labels, String uuid) {

        String alias = "n"; // Alias to use in query
        String nodeId = null;        // Id of the created node

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CET_CODE, cetCode);

        Set<String> fieldsKeys = new HashSet<>(uniqueFields.keySet());
        Map<String, Object> fieldValues = new HashMap<>();

        if(uuid != null) {
            fieldsKeys.add("meveo_uuid");
            fieldValues.put("meveo_uuid", uuid);
        }

        valuesMap.put(FIELD_KEYS, getFieldsString(fieldsKeys));
        valuesMap.put(FIELDS, getFieldsString(fields.keySet()));
        valuesMap.put(UPDATABLE_FIELDS, getFieldsString(updatableFields.keySet()));

        fieldValues.putAll(uniqueFields);
        fieldValues.putAll(fields);
        fieldValues.putAll(updatableFields);
        if(uuid != null) {
            fieldValues.put(NODE_ID, uuid);
        } else {
            fieldValues.put(NODE_ID, UUID.randomUUID().toString());
        }

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
            List<Record> results = result.list();

            if(results.size() > 0) {
                LOGGER.warn("Multiple nodes affected by merge.\n\nquery = {}\n\nvariables = {}", resolvedStatement, fieldValues);
            }

            node = results.get(0).get(alias).asNode();
            transaction.success();  // Commit transaction
            nodeId = getMeveoUUID(node);
        } catch (Exception e) {
            transaction.failure();
            LOGGER.error("Error while merging Neo4J nodes.\n\nquery = {}\n\nvariables = {}", resolvedStatement, fieldValues, e);
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

        if (node != null) {
            final Neo4jEntity neo4jEntity = new Neo4jEntity(node, neo4JConfiguration);
            //  If node has been created, fire creation event. If it was updated, fire update event.
            if (node.containsKey(INTERNAL_UPDATE_DATE)) {
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
		return createNode(neo4JConfiguration, cetCode, fields, labels, null);
	}

	public String createNode(String neo4JConfiguration, String cetCode, Map<String, Object> fields, List<String> labels, String uuid) {

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
        
        Map<String, Object> fieldValues = new HashMap<>(fields);
        if(uuid == null) {
        	fieldValues.put(NODE_ID, UUID.randomUUID().toString());
        } else {
        	fieldValues.put(NODE_ID, uuid);
        }

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

    public void updateNodeByNodeId(String neo4JConfiguration, String nodeId, String cetCode, Map<String, Object> fields, List<String> labels) {

        String alias = "startNode"; // Alias to use in query

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(NODE_ID, nodeId);
        valuesMap.put(FIELDS, getFieldsString(fields.keySet()));
        valuesMap.put(CET_CODE, cetCode);

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
            LOGGER.error("Error while executing UniqueConstraint {}", uniqueConstraint, e);
        } finally {
            // End session and transaction
            transaction.close();
            session.close();
        }

        return Collections.emptySet();
    }

    public void createRelationBetweenNodes(String neo4JConfiguration, String startNodeId, String startNodeLabel, String label, String endNodeId, String endNodeLabel, Map<String, Object> fields) {

        /* Build values map */
        final Map<String, Object> values = new HashMap<>();
        values.put("startNodeId", startNodeId);
        values.put("relationshipLabel", label);
        values.put("endNodeId", endNodeId);
        values.put("startNodeLabel", startNodeLabel);
        values.put("endNodeLabel", endNodeLabel);
        final String fieldsString = getFieldsString(fields.keySet());
        values.put(FIELDS, fieldsString);
        values.putAll(fields);
        values.put(NODE_ID, UUID.randomUUID().toString());

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
            if (relationship.containsKey(INTERNAL_UPDATE_DATE)) {
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

    /**
     * Update the first node with the properties of the second node
     *
     * @param neo4JConfiguration Configuration code of the neo4j instance
     * @param firstNodeId        First node to merge
     * @param secondNodeId       Second node to merge
     */
    public void mergeAndRemoveNodes(String neo4JConfiguration, Long firstNodeId, Long secondNodeId) {
        String queryStr = "MATCH (n1) WHERE ID(n1) = $firstId \n" +
                "WITH n1, n1.creationDate as creationDate, n1.meveo_uuid as uuid \n" +
                "MATCH (n2) WHERE ID(n2) = $secondId \n" +
                "WITH n1, n2, creationDate, uuid, properties(n2) as props \n" +
                "DETACH DELETE (n2) \n" +
                "WITH n1, n2, creationDate, props, uuid \n" +
                "SET n1 += props, n1.creationDate = creationDate, n1.meveo_uuid = uuid \n" +
                "RETURN n1, n2";

        cypherHelper.execute(
                neo4JConfiguration,
                queryStr,
                ImmutableMap.of("firstId", firstNodeId, "secondId", secondNodeId),
                (transaction, result) -> {
                    final Record single = result.single();
                    final Node nodeA = single.get(0).asNode();
                    final Node nodeB = single.get(1).asNode();
                    if(nodeA != null & nodeB != null){
                        LOGGER.info("Properties of node {} added to node {} and node {} removed", nodeB.id(), nodeA.id(), nodeB.id());
                        transaction.success();
                    } else {
                        LOGGER.error("Properties of node {} and {} not merged and node {} not removed", secondNodeId, firstNodeId, secondNodeId);
                        transaction.failure();
                    }
                    return null;
                },
                e -> LOGGER.error("Error while merging nodes {} and {}", firstNodeId, secondNodeId, e)
        );
    }

    /**
     * Find all the relationships related to a node
     *
     * @param neo4JConfiguration Configuration code of the neo4j instance
     * @param nodeId             Node id
     */
    public List<Relationship> findRelationships(String neo4JConfiguration, Long nodeId, String label){
        // First, retrieves relationships of second node
        String findAllRelationshipQuery = "MATCH (n:" + label + ")-[r]-() \n" +
                "WHERE ID(n) = $secondNodeId \n" +
                "RETURN r";

        return cypherHelper.execute(
                neo4JConfiguration,
                findAllRelationshipQuery,
                ImmutableMap.of("secondNodeId", nodeId),
                (transaction, result) -> result.list().stream().map(r -> r.get(0)).map(Value::asRelationship).collect(Collectors.toList()),
                e -> LOGGER.error("Error retriving relationships of node {}", nodeId, e)
        );

    }

    /**
     * Find all the relationships of a given type related to a node
     *
     * @param neo4JConfiguration Configuration code of the neo4j instance
     * @param nodeId             Node id
     * @param type               Type of the relationships to retrieve
     */
    public List<Relationship> findRelationships(String neo4JConfiguration, String nodeId, String type){
        // First, retrieves relationships of second node
        String findAllRelationshipQuery = "MATCH (n)-[r:" + type + "]-() \n" +
                "WHERE n.meveo_uuid = $nodeId \n" +
                "RETURN r";

        return cypherHelper.execute(
                neo4JConfiguration,
                findAllRelationshipQuery,
                ImmutableMap.of("nodeId", nodeId),
                (transaction, result) -> result.list().stream().map(r -> r.get(0)).map(Value::asRelationship).collect(Collectors.toList()),
                e -> LOGGER.error("Error retriving relationships of type {} of node {}", type, nodeId, e)
        );

    }

    /**
     * Create or update a given relationship
     *
     * @param neo4JConfiguration Configuration code of the neo4j instance
     * @param startNodeId        Source node UUID of the relation
     * @param endNodeId          Target node UUID of the relation
     * @param relationId         UUID of the relation to merge
     * @param label              Type of the relation
     * @param uniqueFields       Unique fields used to do the merge
     * @param fields             Fields to update
     * @param date               Date of the operation
     */
    public void mergeRelationshipById(String neo4JConfiguration, Long startNodeId, Long endNodeId, String relationId, String label, Map<String, Object> uniqueFields, Map<String, Object> fields, Long date) {

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("startNodeId", startNodeId);
        arguments.put("endNodeId", endNodeId);
        arguments.put("relationId", relationId);
        arguments.putAll(uniqueFields);
        arguments.put("fields", fields);
        arguments.put("date", date);

        String uniqueFieldLiteral =  uniqueFields.isEmpty() ? "" : " " + getFieldsString(uniqueFields.keySet()) + " ";

        LOGGER.info("Attaching relation {} to node {}", relationId, startNodeId);

        String createRelationshipQuery = new StringBuffer("MATCH (startNode) WHERE ID(startNode) = $startNodeId \n")
                .append("WITH startNode \n")
                .append("MATCH (endNode) WHERE ID(endNode) = $endNodeId \n")
                .append("WITH startNode, endNode \n")
                .append("MERGE (startNode)-[relationship:").append(label).append(uniqueFieldLiteral).append("]-(endNode) \n")
                .append("ON CREATE SET relationship += $fields, relationship.").append(CREATION_DATE).append(" = $date, relationship.meveo_uuid = $relationId \n")
                .append("ON MATCH SET relationship += $fields,  relationship.").append(INTERNAL_UPDATE_DATE).append(" = $date \n")
                .append("RETURN relationship")
                .toString();

        cypherHelper.execute(
                neo4JConfiguration,
                createRelationshipQuery,
                arguments,
                (t, r) -> {
                    final Record single = r.single();
                    final Relationship relationship = single.get(0).asRelationship();
                    if(relationship == null){
                        LOGGER.error("Relationship not created.\nParams: {}\nRequest: {}", arguments, createRelationshipQuery);
                    }else{
                        LOGGER.info("Relationship {} attached ({})", relationship.id(), relationId);
                        t.success();
                    }
                    return relationship;
                },
                e -> LOGGER.error("Error merging relationship {} on node {}", relationId, startNodeId, e)
        );


    }

    public List<Node> orderNodesAscBy(String property, Collection<String> uuids, String neo4JConfiguration){
        // Use labels to speed up things

        String orderByQuery = new StringBuffer("MATCH (n) WHERE n.meveo_uuid IN $uuids \n")
                .append("RETURN n \n")
                .append("ORDER BY n.").append(property).append(" ASC \n")
                .toString();

        return cypherHelper.execute(
                neo4JConfiguration,
                orderByQuery,
                ImmutableMap.of("uuids", uuids),
                (transaction, result) -> result.list().stream().map(r -> r.get(0)).map(Value::asNode).collect(Collectors.toList()),
                e -> LOGGER.error("Error sorting nodes {} by {} in ascending order", uuids, property)
        );
    }
    

	/**
	 * Remove nodes that are targeted by given outgoing relationships from source node
	 * 
	 * @param neo4JConfiguration Code of the configuration to update
	 * @param sourceNodeUuid     Source node id
	 * @param sourceNodeLabel    Source node label
	 * @param relationshipType   Outgoing relationship type
	 * @param targetNodeLabel    Label of the nodes to delete
	 */
    public void detachDeleteTargets(String neo4JConfiguration, String sourceNodeUuid, String sourceNodeLabel, String relationshipType, String targetNodeLabel) {
    	String detachDeleteQuery = new StringBuffer("MATCH (n:")
    			.append(sourceNodeLabel)
    			.append(")-[:")
    			.append(relationshipType)
    			.append("]->(t:")
    			.append(targetNodeLabel)
    			.append(") \n")
    			.append("WHERE n.meveo_uuid = $uuid \n")
    			.append("DETACH DELETE t")
    			.toString();
    	
    	cypherHelper.update(
    			neo4JConfiguration, 
    			detachDeleteQuery, 
    			ImmutableMap.of("uuid", sourceNodeUuid), 
    			e -> LOGGER.error("Error deleting target {} nodes of outgoing relationships with type {} from node {} ({})", targetNodeLabel, relationshipType, sourceNodeUuid, sourceNodeLabel, e)
		);
    }
    
	/**
	 * Remove nodes that are targeted by given outgoing relationships from source node
	 *
	 * @param neo4JConfiguration Code of the configuration to update
	 * @param sourceNodeUuid     Source node id
	 * @param sourceNodeLabel    Source node label
	 * @param relationshipType   Outgoing relationship type
	 * @param targetNodeLabel    Label of the nodes to delete
	 */
    public void detachDeleteTargets(String neo4JConfiguration, String sourceNodeUuid, String sourceNodeLabel, String relationshipType, String targetNodeLabel, Map<String, Object> filters) {
    	String detachDeleteQuery = new StringBuffer("MATCH (n:")
    			.append(sourceNodeLabel)
    			.append(")-[:")
    			.append(relationshipType)
    			.append("]->(t:")
    			.append(targetNodeLabel)
    			.append(" ")
    			.append(getFieldsString(filters.keySet()))
    			.append(" ) \n")
    			.append("WHERE n.meveo_uuid = $uuid \n")
    			.append("DETACH DELETE t")
    			.toString();

    	Map<String, Object> arguments = new HashMap<>();
    	arguments.put("uuid", sourceNodeUuid);
    	arguments.putAll(filters);

    	cypherHelper.update(
    			neo4JConfiguration,
    			detachDeleteQuery,
    			arguments,
    			e -> LOGGER.error("Error deleting target {} nodes of outgoing relationships with type {} from node {} ({})", targetNodeLabel, relationshipType, sourceNodeUuid, sourceNodeLabel, e)
		);
    }

    public List<Node> findNodesBySourceNodeIdAndRelationships(String neo4JConfiguration, String sourceNodeUuid, String sourceNodeLabel, String relationshipType, String targetNodeLabel) {
    	String findQuery = new StringBuffer("MATCH (n:")
    			.append(sourceNodeLabel)
    			.append(")-[:")
    			.append(relationshipType)
    			.append("]->(t:")
    			.append(targetNodeLabel)
    			.append(") \n")
    			.append("WHERE n.meveo_uuid = $uuid \n")
    			.append("RETURN t")
    			.toString();
    	
        return cypherHelper.execute(
        		neo4JConfiguration,
        		findQuery,
    			ImmutableMap.of("uuid", sourceNodeUuid),
                (transaction, result) -> result.list().stream().map(r -> r.get(0)).map(Value::asNode).collect(Collectors.toList()),
    			e -> LOGGER.error("Error retrieving target {} nodes of outgoing relationships with type {} from node {} ({})", targetNodeLabel, relationshipType, sourceNodeUuid, sourceNodeLabel, e)
        );
    }

}
