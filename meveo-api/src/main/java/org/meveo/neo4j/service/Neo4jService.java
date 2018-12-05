package org.meveo.neo4j.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.api.dto.neo4j.Datum;
import org.meveo.api.dto.neo4j.Neo4jQueryResultDto;
import org.meveo.api.dto.neo4j.Result;
import org.meveo.api.dto.neo4j.SearchResultDTO;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.exceptions.InvalidCustomFieldException;
import org.meveo.export.RemoteAuthenticationException;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.Provider;
import org.meveo.model.crm.custom.CustomFieldIndexTypeEnum;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.customEntities.CETConstants;
import org.meveo.api.CETUtils;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.service.neo4j.base.Neo4jConnectionProvider;
import org.meveo.service.neo4j.base.Neo4jDao;
import org.meveo.service.neo4j.service.Neo4JRequests;
import org.meveo.service.neo4j.service.Neo4jResultParser;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.util.ApplicationProvider;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.neo4j.driver.v1.types.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * @author Rachid AITYAAZZA
 */
@Stateless
public class Neo4jService {

    private static final Logger log = LoggerFactory.getLogger(Neo4jService.class);
    private static final String FIELD_KEYS = "fieldKeys";
    private static final String FIELDS = "fields";
    public static final String ID = "id";
    public static final String SOURCE_TYPE = "sourceType";
    public static final String SOURCE_ID = "sourceId";

    @Inject
    private Neo4jConnectionProvider neo4jSessionFactory;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private Neo4jDao neo4jDao;

    @Inject
    @Removed
    private Event<org.neo4j.driver.v1.types.Node> nodeRemovedEvent;

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
    @ApplicationProvider
    protected Provider appProvider;

    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Long addCetNode(String cetCode, Map<String, Object> fieldValues, String sourceType, String sourceId) {
        return addCetNode(cetCode, fieldValues, false, sourceType, sourceId);
    }

    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Long addCetNode(String cetCode, Map<String, Object> fieldValues, boolean isTemporaryCET, String sourceType, String sourceId) {

        Long nodeId = null; // Id of the created node

        try {

            /* Retrieve corresponding CET or throw an error */
            CustomEntityTemplate cetEntity = customEntityTemplateService.findByCode(cetCode);
            if (cetEntity == null) {
                throw new ElementNotFoundException(cetCode, CustomEntityTemplate.class.getName());
            }

            /* Find unique fields and validate data */
            Map<String, CustomFieldTemplate> cetFields = customFieldTemplateService.findByAppliesTo(cetEntity.getAppliesTo());
            Map<String, Object> uniqueFields = new HashMap<>();
            Map<String, Object> fields = validateAndConvertCustomFields(cetFields, fieldValues, uniqueFields, true);
            fields.put(CETConstants.CET_ACTIVE_FIELD, "TRUE");
            uniqueFields.put(CETConstants.CET_ACTIVE_FIELD, "TRUE");
            fields.put(CETConstants.CET_UPDATE_DATE_FIELD, isTemporaryCET ? -1 : System.currentTimeMillis());

            /* Collect entity references */
            final List<CustomFieldTemplate> entityReferences = cetFields.values().stream()
                    .filter(customFieldTemplate -> customFieldTemplate.getFieldType().equals(CustomFieldTypeEnum.ENTITY))   // Entity references
                    .filter(customFieldTemplate -> fieldValues.get(customFieldTemplate.getCode()) != null)                  // Value is provided
                    .collect(Collectors.toList());

            /* Create referenced nodes and collect relationships to create */
            Map<Long, String> relationshipsToCreate = new HashMap<>();  // Map where the id of the target node is the key and the label of relationship is the value
            for (CustomFieldTemplate entityReference : entityReferences){
                Object referencedCetValue = fieldValues.get(entityReference.getCode());
                String referencedCetCode = entityReference.getEntityClazz();
                CustomEntityTemplate referencedCet = customEntityTemplateService.findByCode(referencedCetCode);
                Long createNodeId;
                if(referencedCet.isPrimitiveEntity()){    // If the CET is primitive, copy value in current node's value
                    fields.put(entityReference.getCode(), referencedCetValue);
                    Map<String, Object> valueMap = Collections.singletonMap("value", referencedCetValue);
                    createNodeId = neo4jDao.createNode(referencedCetCode, valueMap, valueMap, referencedCet.getLabels());
                }else{
                    @SuppressWarnings("unchecked") Map<String, Object> valueMap = (Map<String, Object>) referencedCetValue;
                    createNodeId = addCetNode(referencedCetCode, valueMap, sourceType, sourceId);
                }
                if(createNodeId != null){
                    String relationshipName = Optional.ofNullable(entityReference.getRelationshipName())
                            .orElseThrow(() -> new BusinessException("Relationship name must be provided !"));
                    relationshipsToCreate.put(createNodeId, relationshipName);
                }
            }

            if (CETConstants.QUERY_CATEGORY_CODE.equals(cetCode)) {
                String isSuperCategory = (String) fields.get("isSuperCategory");
                if (isSuperCategory != null && Boolean.valueOf(isSuperCategory)) {
                    fields.put(CETConstants.CET_ACTIVE_FIELD, "FALSE");
                    uniqueFields.remove(CETConstants.CET_ACTIVE_FIELD);
                }
            }

            final List<String> labels = cetEntity.getLabels();
            final Long createNodeId = neo4jDao.createNode(cetCode, uniqueFields, fields, labels);
            nodeId = createNodeId;

            /* Create relationships collected in the relationshipsToCreate map */
            if(nodeId != null){
                final Map<String, Object> values = new HashMap<>();
                values.put(SOURCE_TYPE, sourceType);
                values.put(SOURCE_ID, sourceId);
                relationshipsToCreate.forEach((targetId, label) -> neo4jDao.createRealtionBetweenNodes(createNodeId, label, targetId, values));
                neo4jDao.createSourceNodeStatement(sourceType, sourceId, values);
            }

        } catch (BusinessException e) {
            log.error("addCetNode cetCode={}, errorMsg={}", cetCode, e.getMessage(), e);
        } catch (ELException e) {
            log.error("Error while resolving EL : ", e);
        }
        /* Create relationships to referenced nodes */

        return nodeId;
    }

    /**
     * Persist an instance of {@link CustomRelationshipTemplate}
     *
     * @param crtCode          Code of the CustomRelationshipTemplate instance
     * @param crtValues        Properties of the link
     * @param startFieldValues Filters on start node
     * @param endFieldValues   Filters on end node
     * @throws BusinessException If error happens
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addCRT(String crtCode,
                       Map<String, Object> crtValues,
                       Map<String, Object> startFieldValues,
                       Map<String, Object> endFieldValues,
                       String sourceType,
                       String sourceId,String startCode, String endCode)
            throws BusinessException, ELException {

        log.info("Persisting link with crtCode = {}", crtCode);

        /* Try to retrieve the associated CRT */
        CustomRelationshipTemplate customRelationshipTemplate = customRelationshipTemplateService.findByStartAndEndCodes(crtCode, startCode, endCode);
        if (customRelationshipTemplate == null) {
            log.error("Can't find CRT with code {}", crtCode);
            throw new ElementNotFoundException(crtCode, CustomRelationshipTemplate.class.getName());
        }

        /* Recuperation of the custom fields of the CRT */
        Map<String, CustomFieldTemplate> crtCustomFields = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getAppliesTo());
        log.info("Custom fields are : ", crtCustomFields);

        /* Recuperation of the custom fields of the source node */
        Map<String, CustomFieldTemplate> startNodeFields = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getStartNode().getAppliesTo());
        Map<String, Object> startNodeFieldValues = validateAndConvertCustomFields(startNodeFields, startFieldValues, null, true);
        log.info("Filters on start node :" + startNodeFieldValues);
        Map<String, Object> startNodeKeysMap = getNodeKeys(customRelationshipTemplate.getStartNode().getAppliesTo(), startNodeFieldValues);

        /* Recuperation of the custom fields of the target node */
        Map<String, CustomFieldTemplate> endNodeFields = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getEndNode().getAppliesTo());
        Map<String, Object> endNodeFieldValues = validateAndConvertCustomFields(endNodeFields, endFieldValues, null, true);
        log.info("Filters on end node : " + endNodeFieldValues);
        Map<String, Object> endNodeKeysMap = getNodeKeys(customRelationshipTemplate.getEndNode().getAppliesTo(), endNodeFieldValues);

        log.info("startNodeKeysMap:" + startNodeKeysMap);
        log.info("endNodeKeysMap:" + endNodeKeysMap);

        /* If matching source and target exists, persist the link */
        if (startNodeKeysMap.size() > 0 && endNodeKeysMap.size() > 0) {
            Map<String, Object> crtFields = validateAndConvertCustomFields(crtCustomFields, crtValues, null, true);
            crtFields.put(SOURCE_TYPE, sourceType);
            crtFields.put(SOURCE_ID, sourceId);
            saveCRT2Neo4j(customRelationshipTemplate, startNodeKeysMap, endNodeKeysMap, crtFields, false);
            neo4jDao.createSourceNodeStatement(sourceType, sourceId, crtValues);
        }

    }

    /**
     * Save CRT to Neo4j
     *
     * @param customRelationshipTemplate Template of the CRT
     * @param startNodeKeysMap           Unique fields values of the start node
     * @param endNodeKeysMap             Unique fields values of the start node
     * @param crtFields                  Fields values of the relationship
     */
    private void saveCRT2Neo4j(CustomRelationshipTemplate customRelationshipTemplate, Map<String, Object> startNodeKeysMap,
                               Map<String, Object> endNodeKeysMap, Map<String, Object> crtFields, boolean isTemporaryCET) {

        final String relationshipAlias = "relationship";    // Alias to use in query

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("startAlias", Neo4JRequests.START_NODE_ALIAS);
        valuesMap.put("endAlias", Neo4JRequests.END_NODE_ALIAS);
        valuesMap.put("startNode", customRelationshipTemplate.getStartNode().getCode());
        valuesMap.put("endNode", customRelationshipTemplate.getEndNode().getCode());
        valuesMap.put("relationType", customRelationshipTemplate.getName());
        valuesMap.put("starNodeKeys", Values.value(startNodeKeysMap));
        valuesMap.put("endNodeKeys", Values.value(endNodeKeysMap));
        valuesMap.put("updateDate", isTemporaryCET ? -1 : System.currentTimeMillis());
        final String fieldsString = neo4jDao.getFieldsString(crtFields.keySet());
        valuesMap.put(FIELDS, fieldsString);
        valuesMap.putAll(crtFields);

        // Build the statement
        StringBuffer statement = neo4jDao.appendReturnStatement(Neo4JRequests.crtStatement, relationshipAlias, valuesMap);
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedStatement = sub.replace(statement);

        // Begin Neo4J transaction
        final Session session = neo4jSessionFactory.getSession();
        final Transaction transaction = session.beginTransaction();

        try {
            /* Execute query and parse result inside a relationship.
            If relationship was created fire creation event, fire update event when updated. */

            final StatementResult result = transaction.run(resolvedStatement, valuesMap);  // Execute query

            // Fire notification for each relation created or updated
            for (Record record : result.list()){
                final Relationship relationship = record.get(relationshipAlias).asRelationship();  // Parse relationship

                if (relationship.containsKey("update_date")) {  // Check if relationship contains the "update_date" key
                    edgeUpdatedEvent.fire(relationship);        // Fire update event if contains the key
                } else {
                    edgeCreatedEvent.fire(relationship);        // Fire creation event if does not contains the key
                }
            }

            transaction.success();  // Commit transaction

        } finally {
            transaction.close();    // Close Neo4J transaction
            session.close();        // Close Neo4J session
        }
    }

    /**
     * Persist a source node of an unique relationship.
     * If a relationship that targets the target node exists, then we merge the fields of the start in parameter to
     * the fields of the source node of the relationship.
     * If such a relation does not exists, we create the source node with it fields.
     *
     * @param crtCode Code of the source node to update or create
     * @param startNodeValues Values to assign to the start node
     * @param endNodeValues Filters on the target node values
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addSourceNodeUniqueCrt(String crtCode,
                                       Map<String, Object> startNodeValues,
                                       Map<String, Object> endNodeValues,
                                       String sourceType,
                                       String sourceId,
                                       String startCode,
                                       String endCode) throws BusinessException, ELException {

        // Get relationship template
        final CustomRelationshipTemplate customRelationshipTemplate = customRelationshipTemplateService.findByStartAndEndCodes(crtCode, startCode, endCode);

        // Extract unique fields values for the start node
        Map<String, CustomFieldTemplate> endNodeCfts = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getEndNode().getAppliesTo());
        Map<String, CustomFieldTemplate> startNodeCfts = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getStartNode().getAppliesTo());
        final Map<String, Object> endNodeUniqueFields = new HashMap<>();
        Map<String, Object> endNodeConvertedValues = validateAndConvertCustomFields(endNodeCfts, endNodeValues, endNodeUniqueFields, true);
        Map<String, Object> startNodeConvertedValues = validateAndConvertCustomFields(startNodeCfts, startNodeValues, null, true);

        // Map the variables declared in the statement
        Map<String, Object> valuesMap = new HashMap<>();
        final String cetCode = customRelationshipTemplate.getStartNode().getCode();
        valuesMap.put("cetCode", cetCode);
        valuesMap.put("crtCode", crtCode);
        valuesMap.put("endCetcode", customRelationshipTemplate.getEndNode().getCode());

        // Prepare the key maps for unique fields and start node fields
        final String uniqueFieldStatements = neo4jDao.getFieldsString(endNodeConvertedValues.keySet());
        final String startNodeValuesStatements = neo4jDao.getFieldsString(startNodeConvertedValues.keySet());

        // No unique fields has been found
        if(endNodeUniqueFields.isEmpty()){
            log.error("At least one unique field must be provided for target entity [code = {}, fields = {}]. " +
                    "Unique fields are : {}", customRelationshipTemplate.getEndNode().getCode(), endNodeValues, endNodeUniqueFields);
            throw new BusinessException("Unique field must be provided");
        }

        // Assign the keys names
        valuesMap.put(FIELD_KEYS, uniqueFieldStatements);
        valuesMap.put(FIELDS, startNodeValuesStatements);

        // Create the substitutor
        StrSubstitutor sub = new StrSubstitutor(valuesMap);

        // Values of the keys defined in valuesMap
        Map<String, Object> parametersValues =  new HashMap<>();
        parametersValues.putAll(startNodeConvertedValues);
        parametersValues.putAll(endNodeConvertedValues);

        final Session session = neo4jSessionFactory.getSession();
        final Transaction transaction = session.beginTransaction();

        // Try to find the id of the source node
        String findStartNodeStatement = getStatement(sub, Neo4JRequests.findStartNodeId);
        final StatementResult run = transaction.run(findStartNodeStatement, parametersValues);

        try {
            try {

                /* Update the source node with the found id */

                final String startNodeAlias = "startNode";   // Alias to use in queries

                // Retrieve ID of the node
                final Record idRecord = run.single();
                final Value id = idRecord.get(0);
                parametersValues.put(ID, id);

                // Create statement
                StringBuffer statement = neo4jDao.appendAdditionalLabels(Neo4JRequests.updateNodeWithId, customRelationshipTemplate.getStartNode().getLabels(), startNodeAlias, valuesMap);
                statement = neo4jDao.appendReturnStatement(statement, startNodeAlias, valuesMap);
                String updateStatement = getStatement(sub, statement);

                final StatementResult result = transaction.run(updateStatement, parametersValues);  // Execute query

                // Fire node update event
                org.neo4j.driver.v1.types.Node startNode = result.single().get(startNodeAlias).asNode();
                nodeUpdatedEvent.fire(startNode);

            } catch (NoSuchRecordException e) {

                /* Create the source node */

                addCetNode(cetCode, startNodeValues, sourceType, sourceId);

            }

            transaction.success();

        }catch (Exception e){

            transaction.failure();
            log.error("Transaction for persisting entity with code {} and fields {} was rolled back : {}", cetCode, startNodeValues, e.getMessage());
            throw new BusinessException(e);

        }finally {

            session.close();
            transaction.close();

        }

    }


    public void deleteEntity(String cetCode, Map<String, Object> values) throws BusinessException {

        /* Get entity template */
        final CustomEntityTemplate customEntityTemplate = customEntityTemplateService.findByCode(cetCode);

        /* Extract unique fields values for node */
        final Map<String, Object> uniqueFields = getNodeKeys(customEntityTemplate.getAppliesTo(), values);

        /* No unique fields has been found */
        if (uniqueFields.isEmpty()) {
            throw new BusinessException("At least one unique field must be provided for cet to delete");
        }

        final String uniqueFieldStatement = neo4jDao.getFieldsString(uniqueFields.keySet());

        /* Map the variables declared in the statement */
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("cetCode", cetCode);
        valuesMap.put("uniqueFields", uniqueFieldStatement);

        String deleteStatement = getStatement(new StrSubstitutor(valuesMap), Neo4JRequests.deleteCet);

        /* Start transaction */
        Session session = neo4jSessionFactory.getSession();
        Transaction transaction = session.beginTransaction();

        try {

            /* Delete the node and all its associated relationships and fire node deletion event */

            // Execute query
            final StatementResult result = transaction.run(deleteStatement, values);
            Record record = result.single();
            transaction.success();

            // Fire deletion event
            final Map<String, Value> properties = record.get("properties").asMap(e -> e);   // Parse properties
            final List<String> labels = record.get("labels").asList(Value::asString);       // Parse labels
            final long id = record.get("id").asLong();                                      // Parse id
            final InternalNode internalNode = new InternalNode(id, labels, properties);     // Create Node object
            nodeRemovedEvent.fire(internalNode);                                            // Fire notification

        } catch (Exception e) {

            log.error("Cannot delete node with code {} and values {} : {}", cetCode, values, e.getMessage());
            transaction.failure();
            throw new BusinessException(e);

        } finally {

            /* End transaction */
            session.close();
            transaction.close();

        }

    }

    private static String getStatement(StrSubstitutor sub, StringBuffer findStartNodeId) {
        return sub.replace(findStartNodeId).replace('"', '\'');
    }

    public String callNeo4jWithStatement(StringBuffer statement, Map<String, Object> values){
        StrSubstitutor sub = new StrSubstitutor(values);
        String resolvedStatement = sub.replace(statement);
        log.info("resolvedStatement : {}", resolvedStatement);
        resolvedStatement = resolvedStatement.replace('"', '\'');
        Response response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/transaction/commit", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), "{\"statements\":[{\"statement\":\"" + resolvedStatement + "\"}]}");
        return response.readEntity(String.class);
    }

    private Map<String, Object> getNodeKeys(String appliesTo, Map<String, Object> convertedFieldValues) {
        Map<String, Object> nodeKeysMap = new HashMap<>();
        List<CustomFieldTemplate> retrievedCft = customFieldTemplateService.findCftUniqueFieldsByApplies(appliesTo);
        for (CustomFieldTemplate cf : retrievedCft) {
            if (!StringUtils.isBlank(convertedFieldValues.get(cf.getCode()))) {
                    nodeKeysMap.put(cf.getCode(), convertedFieldValues.get(cf.getCode()));
            }
        }
        return nodeKeysMap;
    }


    public Response callNeo4jRest(String baseurl, String url, String username, String password, String body) {
        try {
            ResteasyClient client = new ResteasyClientBuilder().build();
            ResteasyWebTarget target = client.target(baseurl + url);
            log.info("callNeo4jRest {} with body : {}", baseurl + url, body);
            BasicAuthentication basicAuthentication = new BasicAuthentication(username, password);
            target.register(basicAuthentication);
            Response response = target.request().post(Entity.json(body));
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED || response.getStatus() == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new RemoteAuthenticationException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                } else {
                    log.debug("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                }
            }
            return response;
        } catch (Exception e) {
            log.error("Failed to communicate with neo4j. Reason {}", (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<String, Object> validateAndConvertCustomFields(Map<String, CustomFieldTemplate> customFieldTemplates, Map<String, Object> fieldValues, Map<String, Object> uniqueFields, boolean checkCustomFields) throws BusinessException, ELException {
        Map<String, Object> convertedFields = new HashMap<>();
        List<CustomFieldTemplate> expressionFields = new ArrayList<>();
        Iterator<Entry<String, CustomFieldTemplate>> customFieldTemplatesEntries = customFieldTemplates.entrySet().iterator();
        while (customFieldTemplatesEntries.hasNext()) {
            Entry<String, CustomFieldTemplate> cftEntry = customFieldTemplatesEntries
                    .next();
            CustomFieldTemplate cft = cftEntry.getValue();
            if (checkCustomFields && cft == null) {
                log.error("No custom field template found with code={} for entity {}. Value will be ignored.", cftEntry.getKey(), CustomFieldTemplate.class);
                throw new InvalidCustomFieldException("Custom field template with code " + cftEntry.getKey() + " not found.");
            }
            Object fieldValue = fieldValues.get(cftEntry.getKey());
            // Validate that value is not empty when field is mandatory
            boolean isEmpty = fieldValue == null && (cft.getFieldType() != CustomFieldTypeEnum.EXPRESSION);
            if (cft.isValueRequired() && isEmpty) {
                final String message = "CFT with code " + cft.getCode() + " is not provided";
                log.error(message);
                throw new InvalidCustomFieldException(message);
            }
            // Validate that value is valid (min/max, regexp). When
            // value is a list or a map, check separately each value
            if (fieldValue != null
                    && (cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.DOUBLE ||
                    cft.getFieldType() == CustomFieldTypeEnum.LONG || cft.getFieldType() == CustomFieldTypeEnum.BOOLEAN ||
                    cft.getFieldType() == CustomFieldTypeEnum.EXPRESSION)) {
                List valuesToCheck = new ArrayList<>();
                if (fieldValue instanceof Map) {
                    // Skip Key item if Storage type is Matrix
                    if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {
                        for (Entry<String, Object> mapEntry : ((Map<String, Object>) fieldValue).entrySet()) {
                            if (CustomFieldValue.MAP_KEY.equals(mapEntry.getKey())) {
                                continue;
                            }
                            valuesToCheck.add(mapEntry.getValue());
                        }
                    } else {
                        valuesToCheck.add(fieldValue);
                    }
                } else if (fieldValue instanceof List) {
                    convertedFields.put(cft.getCode(), fieldValue);
                    continue;
                } else {
                    valuesToCheck.add(fieldValue);
                }
                for (Object valueToCheck : valuesToCheck) {
                    if (cft.getFieldType() == CustomFieldTypeEnum.STRING && !"null".equals(valueToCheck)) {
                        String stringValue = null;
                        if (valueToCheck instanceof Integer) {
                            stringValue = ((Integer) valueToCheck).toString();
                        } else if (valueToCheck instanceof Map) {
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.create();
                            String mapToJson = gson.toJson(valueToCheck).replaceAll("'", "’").replaceAll("\"", "");
                            convertedFields.put(cft.getCode(), mapToJson);
                            continue;
                        } else {
                            stringValue = (String) valueToCheck;
                        }
                        stringValue = stringValue.trim().replaceAll("'", "’").replaceAll("\"", "");
                        stringValue = stringValue.replaceAll("\n", " ");

                        if (cft.getMaxValue() == null) {
                            cft.setMaxValue(CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING);
                        }
                        // Validate String length
                        if (stringValue.length() > cft.getMaxValue()) {
                            throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + stringValue + " length is longer then " + cft.getMaxValue() + " symbols");
                        // Validate String regExp
                        } else if (cft.getRegExp() != null) {
                            try {
                                Pattern pattern = Pattern.compile(cft.getRegExp());
                                Matcher matcher = pattern.matcher(stringValue);
                                if (!matcher.matches()) {
                                    throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + stringValue + " does not match regular expression "
                                            + cft.getRegExp());
                                }
                            } catch (PatternSyntaxException pse) {
                                throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " definition specifies an invalid regular expression " + cft.getRegExp());
                            }
                        }
                        if (fieldValue instanceof String) {
                            fieldValue = ((String) fieldValue).trim().replaceAll("'", "’").replaceAll("\"", "").trim();
                        }

                    } else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
                        Long longValue;
                        if (valueToCheck instanceof Integer) {
                            longValue = ((Integer) valueToCheck).longValue();
                        } else if(valueToCheck instanceof String){
                            longValue = Long.parseLong((String) valueToCheck);
                        } else {
                            longValue = (Long) valueToCheck;
                        }

                        if (cft.getMaxValue() != null && longValue.compareTo(cft.getMaxValue()) > 0) {
                            throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + longValue + " is bigger then " + cft.getMaxValue()
                                    + ". Allowed value range is from " + (cft.getMinValue() == null ? "unspecified" : cft.getMinValue()) + " to "
                                    + (cft.getMaxValue() == null ? "unspecified" : cft.getMaxValue()) + ".");

                        } else if (cft.getMinValue() != null && longValue.compareTo(cft.getMinValue()) < 0) {
                            throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + longValue + " is smaller then " + cft.getMinValue()
                                    + ". Allowed value range is from " + (cft.getMinValue() == null ? "unspecified" : cft.getMinValue()) + " to "
                                    + (cft.getMaxValue() == null ? "unspecified" : cft.getMaxValue()) + ".");
                        }
                    } else if (cft.getFieldType() == CustomFieldTypeEnum.DOUBLE) {
                        Double doubleValue;
                        if (valueToCheck instanceof Integer) {
                            doubleValue = ((Integer) valueToCheck).doubleValue();
                        } else if(valueToCheck instanceof String){
                            doubleValue = Double.parseDouble((String) valueToCheck);
                        } else {
                            doubleValue = (Double) valueToCheck;
                        }
                        if (cft.getMaxValue() != null && doubleValue.compareTo(cft.getMaxValue().doubleValue()) > 0) {
                            throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + doubleValue + " is bigger then " + cft.getMaxValue()
                                    + ". Allowed value range is from " + (cft.getMinValue() == null ? "unspecified" : cft.getMinValue()) + " to "
                                    + (cft.getMaxValue() == null ? "unspecified" : cft.getMaxValue()) + ".");
                        } else if (cft.getMinValue() != null && doubleValue.compareTo(cft.getMinValue().doubleValue()) < 0) {
                            throw new InvalidCustomFieldException("Custom field " + cft.getCode() + " value " + doubleValue + " is smaller then " + cft.getMinValue()
                                    + ". Allowed value range is from " + (cft.getMinValue() == null ? "unspecified" : cft.getMinValue()) + " to "
                                    + (cft.getMaxValue() == null ? "unspecified" : cft.getMaxValue()) + ".");
                        }
                    }
                    if (cft.isUnique() && uniqueFields != null) {
                        uniqueFields.put(cft.getCode(), fieldValue);
                    }
                    convertedFields.put(cft.getCode(), fieldValue);
                }
            }
            if (cft.getFieldType() == CustomFieldTypeEnum.EXPRESSION) {
                fieldValue = setExpressionField(fieldValues, cft, convertedFields);
                convertedFields.put(cft.getCode(), fieldValue);
                if (cft.isUnique() && uniqueFields != null) {
                    uniqueFields.put(cft.getCode(), fieldValue);
                }
            }
            //TODO: Handle DATE customfield type
        }
        //loop CFT a second time to set expression fields that are composed by other expressionFields
        for (CustomFieldTemplate cft : expressionFields) {
            Object fieldValue = setExpressionField(fieldValues, cft, convertedFields);
            convertedFields.put(cft.getCode(), fieldValue);
            if (cft.isUnique() && uniqueFields != null) {
                uniqueFields.put(cft.getCode(), fieldValue);
            }
        }
        return convertedFields;
    }

    private Object setExpressionField(Map<String, Object> fieldValues, CustomFieldTemplate cft, Map<String, Object> convertedFields) throws ELException {
        Object evaluatedExpression = MeveoValueExpressionWrapper.evaluateExpression(cft.getDefaultValue(), fieldValues.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)), String.class);

        log.info("validateAndConvertCustomFields {} ExpressionFieldValue1={}", cft.getCode(), evaluatedExpression);

        evaluatedExpression = evaluatedExpression.toString().replaceAll("'", "’").replaceAll("\"", "").replaceAll("-null", "").replaceAll("-", "");
        evaluatedExpression = evaluatedExpression.toString().replaceAll("\n", " ");
        if (cft.getExpressionSeparator() != null) {
            String duplicateSeparator = cft.getExpressionSeparator() + cft.getExpressionSeparator();
            while (evaluatedExpression.toString().contains(duplicateSeparator)) {
                evaluatedExpression = evaluatedExpression.toString().replaceAll(duplicateSeparator, cft.getExpressionSeparator());
            }
            evaluatedExpression = evaluatedExpression.toString().endsWith(cft.getExpressionSeparator()) ? evaluatedExpression.toString().substring(0, evaluatedExpression.toString().length() - 1) : evaluatedExpression.toString();
            evaluatedExpression = evaluatedExpression.toString().startsWith(cft.getExpressionSeparator()) ? evaluatedExpression.toString().substring(1) : evaluatedExpression.toString();
        }
        log.info("validateAndConvertCustomFields {} ExpressionFieldValue2={}", cft.getCode(), evaluatedExpression);
        Object fieldValue = !StringUtils.isBlank(evaluatedExpression) ? evaluatedExpression : fieldValues.get(cft.getCode());
        if (fieldValue != null) {
            if (cft.getIndexType() == CustomFieldIndexTypeEnum.INDEX_NEO4J) {
                convertedFields.put(cft.getCode() + "_IDX", CETUtils.stripAndFormatFields(fieldValue.toString().toLowerCase()));
            } else if (cft.isUnique()) {
                fieldValue = CETUtils.stripAndFormatFields(fieldValue.toString());
            }
            fieldValues.put(cft.getCode(), fieldValue);
        }
        return fieldValue;
    }

    public String executeQuery(String query, Map<String, Object> valuesMap) {
        if (query != null) {
            StrSubstitutor sub = new StrSubstitutor(valuesMap);
            String resolvedStatement = sub.replace(query);
            resolvedStatement = resolvedStatement.replace('"', '\'');
            log.info("executeQuery resolvedStatement : {}", resolvedStatement);
            String neo4jQuery = "{\"query\" : \"" + resolvedStatement + "\"}";
            return getNeo4jData(neo4jQuery, true);
        }
        return null;
    }

    public void mergeNodes(String cetCode, Long originNodeId, Long targetNodeId) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("cetCode", cetCode);
        valuesMap.put("originNodeId", originNodeId);
        valuesMap.put("targetNodeId", targetNodeId);
        StrSubstitutor sub = new StrSubstitutor(valuesMap);

        String resolvedOutGoingRelStatement = sub.replace(Neo4JRequests.mergeOutGoingRelStatement);
        log.info("mergeNodes resolvedOutGoingRelStatement:{}", resolvedOutGoingRelStatement);
        String statement = "{\"statements\":[{\"statement\":\"" + resolvedOutGoingRelStatement + "\",\"resultDataContents\":[\"row\"]}]}";
        Response response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/transaction/commit", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), statement);
        String result = response.readEntity(String.class);
        log.info("mergeNodes OutGoingRelStatement result={}", result);

        String resolvedInGoingRelStatement = sub.replace(Neo4JRequests.mergeInGoingRelStatement);
        log.info("mergeNodes resolvedOutGoingRelStatement:{}", resolvedInGoingRelStatement);
        String inGoingsReltatement = "{\"statements\":[{\"statement\":\"" + resolvedInGoingRelStatement + "\",\"resultDataContents\":[\"row\"]}]}";
        Response inGoingsRelResponse = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/transaction/commit", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), inGoingsReltatement);
        String inGoingsRelResult = inGoingsRelResponse.readEntity(String.class);
        log.info("mergeNodes InGoingRelStatement result={}", inGoingsRelResult);

    }

    public String getNeo4jData(String query, boolean getOnlyFirstElement) {
        StringBuffer result = null;
        Response response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/cypher", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), query);
        String jsonResult = response.readEntity(String.class);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Neo4jQueryResultDto iepSearchResultDTO = gson.fromJson(jsonResult, Neo4jQueryResultDto.class);
        if (iepSearchResultDTO != null && iepSearchResultDTO.getData() != null && iepSearchResultDTO.getData().size() > 0) {
            result = new StringBuffer();
            if (getOnlyFirstElement) {
                return iepSearchResultDTO.getData() != null && iepSearchResultDTO.getData().size() > 0 && iepSearchResultDTO.getData().get(0).size() > 0 ? iepSearchResultDTO.getData().get(0).get(0) : null;
            }
            String sep = "";
            for (List<String> item : iepSearchResultDTO.getData()) {
                result.append(sep).append(item.get(0));
                if (item.size() > 1) {
                    result.append("(").append(item.get(1)).append(")");
                }
                sep = ", ";
            }
            return result.toString();
        }

        return null;
    }

    public List<String> getNeo4jResult(String query) {
        query = "{\"query\" : \"" + query + "\"}";
        Response response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/cypher", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), query);
        String jsonResult = response.readEntity(String.class);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Neo4jQueryResultDto iepSearchResultDTO = gson.fromJson(jsonResult, Neo4jQueryResultDto.class);
        return iepSearchResultDTO != null && iepSearchResultDTO.getData() != null && !iepSearchResultDTO.getData().isEmpty() ? iepSearchResultDTO.getData().get(0) : new ArrayList<String>();
    }

    public String getNeo4jRowData(String jsonResult) {
        log.info("getNeo4jRowData jsonResult={}", jsonResult);
        StringBuffer result = new StringBuffer();
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            SearchResultDTO searchResultDTO = gson.fromJson(jsonResult, SearchResultDTO.class);
            String sep = "";
            for (Result searchResult : searchResultDTO.getResults()) {
                for (Datum data : searchResult.getData()) {
                    if (data.getRow() != null) {
                        for (String row : data.getRow()) {
                            result.append(sep).append(row);
                            sep = "|";
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("getNeo4jRowData error", e);
        }
        return result.toString();
    }

    public Neo4jQueryResultDto getNeo4jData(String query) {
        query = "{\"query\" : \"" + query + "\"}";
        Response response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/cypher", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), query);
        String jsonResult = response.readEntity(String.class);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(jsonResult, Neo4jQueryResultDto.class);
    }
}
