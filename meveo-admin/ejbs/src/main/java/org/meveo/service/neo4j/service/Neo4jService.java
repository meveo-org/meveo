package org.meveo.service.neo4j.service;

import com.google.common.collect.ImmutableMap;
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
import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;
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
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.neo4j.graph.Neo4jEntity;
import org.meveo.service.neo4j.graph.Neo4jRelationship;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.neo4j.scheduler.NodeReference;
import org.meveo.util.ApplicationProvider;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jService.class);

    private static final Logger log = LoggerFactory.getLogger(Neo4jService.class);
    private static final String FIELD_KEYS = "fieldKeys";
    private static final String FIELDS = "fields";
    public static final String ID = "id";

    @Inject
    private Neo4jConnectionProvider neo4jSessionFactory;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private Neo4jDao neo4jDao;

    @Inject
    @Removed
    private Event<Neo4jEntity> nodeRemovedEvent;

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
    @ApplicationProvider
    protected Provider appProvider;

    @SuppressWarnings("unchecked")
    public Set<NodeReference> addCetNode(String neo4JConfiguration, String cetCode, Map<String, Object> fieldValues) {

        Set<NodeReference> nodeReferences = new HashSet<>();

        try {

            /* Retrieve corresponding CET or throw an error */
            CustomEntityTemplate cet = customEntityTemplateService.findByCode(cetCode);
            if (cet == null) {
                throw new ElementNotFoundException(cetCode, CustomEntityTemplate.class.getName());
            }

            /* If pre-persist script was defined, execute it. fieldValues map may be modified by the script */
            if (cet.getPrePersistScript() != null) {
                scriptInstanceService.execute(cet.getPrePersistScript().getCode(), fieldValues);
            }

            /* Find unique fields and validate data */
            Map<String, CustomFieldTemplate> cetFields = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

            // Fallback to when entity is defined as primitive but does not have associated CFT
            if (cet.isPrimitiveEntity()) {
                CustomFieldTemplate valueCft = cetFields.get("value");
                if (valueCft == null) {
                    valueCft = new CustomFieldTemplate();
                    CustomEntityTemplateService.turnIntoPrimitive(cet, valueCft);
                    customFieldTemplateService.create(valueCft);
                    cetFields.put("value", valueCft);
                }
            }

            Map<String, Object> uniqueFields = new HashMap<>();
            Map<String, Object> fields = validateAndConvertCustomFields(cetFields, fieldValues, uniqueFields, true);
            fields.put(CETConstants.CET_ACTIVE_FIELD, "TRUE");
            if (!uniqueFields.isEmpty()) {
                uniqueFields.put(CETConstants.CET_ACTIVE_FIELD, "TRUE");
            }

            /* Collect entity references */
            final List<CustomFieldTemplate> entityReferences = cetFields.values().stream()
                    .filter(customFieldTemplate -> customFieldTemplate.getFieldType().equals(CustomFieldTypeEnum.ENTITY))   // Entity references
                    .filter(customFieldTemplate -> fieldValues.get(customFieldTemplate.getCode()) != null)                  // Value is provided
                    .collect(Collectors.toList());

            /* Create referenced nodes and collect relationships to create */
            Map<NodeReference, String> relationshipsToCreate = new HashMap<>();  // Map where the id of the target node is the key and the label of relationship is the value
            for (CustomFieldTemplate entityReference : entityReferences) {
                Object referencedCetValue = fieldValues.get(entityReference.getCode());
                String referencedCetCode = entityReference.getEntityClazzCetCode();
                CustomEntityTemplate referencedCet = customEntityTemplateService.findByCode(referencedCetCode);

                Collection<Object> values;
                if (entityReference.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
                    values = ((Collection<Object>) referencedCetValue);
                } else {
                    values = Collections.singletonList(referencedCetValue);
                }

                for (Object value : values) {
                    Set<NodeReference> relatedNodeReferences;
                    if (referencedCet.isPrimitiveEntity()) {    // If the CET is primitive, copy value in current node's value
                        fields.put(entityReference.getCode(), value);
                        Map<String, Object> valueMap = new HashMap<>();
                        valueMap.put("value", value);

                        // If there is no unique constraints defined, directly merge node
                        if (referencedCet.getUniqueConstraints().isEmpty()) {
                            List<String> additionalLabels = getAdditionalLabels(referencedCet);
                            if (referencedCet.getPrePersistScript() != null) {
                                scriptInstanceService.execute(referencedCet.getPrePersistScript().getCode(), valueMap);
                            }
                            Long createdNodeId = neo4jDao.mergeNode(neo4JConfiguration, referencedCetCode, valueMap, valueMap, valueMap, additionalLabels);
                            relatedNodeReferences = Collections.singleton(new NodeReference(createdNodeId));
                        } else {
                            relatedNodeReferences = addCetNode(neo4JConfiguration, referencedCetCode, valueMap);
                        }
                    } else {
                        // Referenced CET is not primitive
                        if (value instanceof Map) {
                            @SuppressWarnings("unchecked") Map<String, Object> valueMap = (Map<String, Object>) value;
                            relatedNodeReferences = addCetNode(neo4JConfiguration, referencedCetCode, valueMap);
                        } else {
                            throw new IllegalArgumentException("CET " + referencedCetCode + " should be a primitive entity");
                        }
                    }
                    if (relatedNodeReferences != null) {
                        String relationshipName = Optional.ofNullable(entityReference.getRelationshipName())
                                .orElseThrow(() -> new BusinessException("Relationship name must be provided !"));
                        for (NodeReference nodeReference : relatedNodeReferences) {
                            relationshipsToCreate.put(nodeReference, relationshipName);
                        }
                    }
                }
            }

            if (CETConstants.QUERY_CATEGORY_CODE.equals(cetCode)) {
                String isSuperCategory = (String) fields.get("isSuperCategory");
                if (isSuperCategory != null && Boolean.valueOf(isSuperCategory)) {
                    fields.put(CETConstants.CET_ACTIVE_FIELD, "FALSE");
                    uniqueFields.remove(CETConstants.CET_ACTIVE_FIELD);
                }
            }

            // Let's make sure that the unique constraints are well sorted by trust score and then sort by their position
            Comparator<CustomEntityTemplateUniqueConstraint> comparator = Comparator
                    .comparingInt(CustomEntityTemplateUniqueConstraint::getTrustScore)
                    .thenComparingInt(CustomEntityTemplateUniqueConstraint::getPosition);
            List<CustomEntityTemplateUniqueConstraint> applicableConstraints = cet.getUniqueConstraints()
                    .stream()
                    .filter(uniqueConstraint -> isApplicableConstraint(fields, uniqueConstraint))
                    .sorted(comparator)
                    .collect(Collectors.toList());

            final List<String> labels = getAdditionalLabels(cet);
            if (applicableConstraints.isEmpty()) {
                if (uniqueFields.isEmpty()) {
                    Long nodeId = neo4jDao.createNode(neo4JConfiguration, cetCode, fields, labels);
                    nodeReferences.add(new NodeReference(nodeId));
                } else {
                    Map<String, Object> editableFields = getEditableFields(cetFields, fields);

                    Long nodeId = neo4jDao.mergeNode(neo4JConfiguration, cetCode, uniqueFields, fields, editableFields, labels);
                    nodeReferences.add(new NodeReference(nodeId));
                }
            } else {
                /* Apply unique constraints */
                boolean appliedUniqueConstraint = false;
                for (CustomEntityTemplateUniqueConstraint uniqueConstraint : applicableConstraints) {
                    Set<Long> ids = neo4jDao.executeUniqueConstraint(neo4JConfiguration, uniqueConstraint, fields, cet.getCode());

                    if (uniqueConstraint.getTrustScore() == 100 && ids.size() > 1) {
                        String joinedIds = ids.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                        LOGGER.error("UniqueConstraints with 100 trust score shouldn't return more than 1 ID (code = {}; IDs = {})",
                                uniqueConstraint.getCode(), joinedIds);
                    }

                    for (Long id : ids) {
                        appliedUniqueConstraint = true;

                        if (uniqueConstraint.getTrustScore() < 100) {
                            // If the trust rating is lower than 100%, we create the entity and create a relationship between the found one and the created one
                            // XXX: Update the found node too?
                            //TODO: Handle case where the unique constraint query return more than one elements and that the trust score is below 100
                            Long createdNodeId = neo4jDao.createNode(neo4JConfiguration, cetCode, fields, labels);
                            neo4jDao.createRelationBetweenNodes(neo4JConfiguration, createdNodeId, "SIMILAR_TO", id, ImmutableMap.of(
                                    "trustScore", uniqueConstraint.getTrustScore(),
                                    "constraintCode", uniqueConstraint.getCode()
                            ));
                            nodeReferences.add(new NodeReference(createdNodeId));
                            nodeReferences.add(new NodeReference(id, uniqueConstraint.getTrustScore(), uniqueConstraint.getCode()));
                        } else {
                            Map<String, Object> updatableFields = new HashMap<>(fields);
                            uniqueFields.keySet().forEach(updatableFields::remove);

                            neo4jDao.updateNodeByNodeId(neo4JConfiguration, id, updatableFields);
                            nodeReferences.add(new NodeReference(id));
                        }
                    }

                    if (appliedUniqueConstraint) {
                        break;
                    }
                }

                if (!appliedUniqueConstraint) {
                    if (uniqueFields.isEmpty()) {
                        Long nodeId = neo4jDao.createNode(neo4JConfiguration, cetCode, fields, labels);
                        nodeReferences.add(new NodeReference(nodeId));
                    } else {
                        Map<String, Object> editableFields = getEditableFields(cetFields, fields);

                        Long nodeId = neo4jDao.mergeNode(neo4JConfiguration, cetCode, uniqueFields, fields, editableFields, labels);
                        nodeReferences.add(new NodeReference(nodeId));
                    }
                }
            }

            /* Create relationships collected in the relationshipsToCreate map */
            for (NodeReference nodeReference : nodeReferences) {
                if (!nodeReference.isTrusted()) {
                    continue;
                }

                for (Entry<NodeReference, String> relationshipsEntry : relationshipsToCreate.entrySet()) {
                    NodeReference relatedNodeReference = relationshipsEntry.getKey();
                    if (!relatedNodeReference.isTrusted()) {
                        continue;
                    }

                    String relationshipType = relationshipsEntry.getValue();
                    final Map<String, Object> values = Collections.emptyMap();
                    neo4jDao.createRelationBetweenNodes(neo4JConfiguration, nodeReference.getId(), relationshipType, relatedNodeReference.getId(), values);
                }
            }
        } catch (BusinessException e) {
            log.error("addCetNode cetCode={}, errorMsg={}", cetCode, e.getMessage(), e);
        } catch (ELException e) {
            log.error("Error while resolving EL : ", e);
        }
        /* Create relationships to referenced nodes */

        return nodeReferences;
    }

    private Map<String, Object> getEditableFields(Map<String, CustomFieldTemplate> cetFields, Map<String, Object> convertedFields) {
        Map<String, Object> editableFields = new HashMap<>();
        editableFields.putAll(convertedFields);
        cetFields.values()
            .stream()
            .filter(customFieldTemplate -> !customFieldTemplate.isAllowEdit() || customFieldTemplate.isUnique())
            .map(CustomFieldTemplate::getCode)
            .forEach(editableFields::remove);
        return editableFields;
    }

    private boolean isApplicableConstraint(Map<String, Object> fields, CustomEntityTemplateUniqueConstraint uniqueConstraint) {

        Map<Object, Object> userMap = new HashMap<>();
        userMap.put("entity", fields);

        if (StringUtils.isBlank(uniqueConstraint.getApplicableOnEl())) {
            return true;
        }

        try {
            Object isApplicable = MeveoValueExpressionWrapper.evaluateExpression(uniqueConstraint.getApplicableOnEl(), userMap, Boolean.class);
            if (isApplicable != null && !(isApplicable instanceof Boolean)) {
                LOGGER.error("Expression " + uniqueConstraint.getApplicableOnEl() + " do not evaluate to boolean but " + isApplicable.getClass());
                return false;
            } else if (isApplicable != null) {
                return (boolean) isApplicable;
            }
        } catch (ELException e) {
            LOGGER.error("Cannot evaluate expression", e);
            return false;
        }

        return false;
    }

    /**
     * Persist an instance of {@link CustomRelationshipTemplate}
     *
     * @param neo4JConfiguration Neo4J coordinates
     * @param crtCode            Code of the CustomRelationshipTemplate instance
     * @param crtValues          Properties of the link
     * @param startFieldValues   Filters on start node
     * @param endFieldValues     Filters on end node
     * @throws BusinessException If error happens
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addCRTByNodeValues(
            String neo4JConfiguration,
            String crtCode,
            Map<String, Object> crtValues,
            Map<String, Object> startFieldValues,
            Map<String, Object> endFieldValues)
            throws BusinessException, ELException {

        log.info("Persisting link with crtCode = {}", crtCode);

        /* Try to retrieve the associated CRT */
        CustomRelationshipTemplate customRelationshipTemplate = customRelationshipTemplateService.findByCode(crtCode);
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
            saveCRT2Neo4j(neo4JConfiguration, customRelationshipTemplate, startNodeKeysMap, endNodeKeysMap, crtFields, false);
        }

    }

    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addCRTByNodeIds(
            String neo4JConfiguration,
            String crtCode,
            Map<String, Object> crtValues,
            Long startNodeId,
            Long endNodeId
    ) throws BusinessException, ELException {

        log.info("Persisting link with crtCode = {}", crtCode);

        /* Try to retrieve the associated CRT */
        CustomRelationshipTemplate customRelationshipTemplate = customRelationshipTemplateService.findByCode(crtCode);
        if (customRelationshipTemplate == null) {
            log.error("Can't find CRT with code {}", crtCode);
            throw new ElementNotFoundException(crtCode, CustomRelationshipTemplate.class.getName());
        }

        /* Recuperation of the custom fields of the CRT */
        Map<String, CustomFieldTemplate> crtCustomFields = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getAppliesTo());
        log.info("Custom fields are : ", crtCustomFields);

        Map<String, Object> crtFields = validateAndConvertCustomFields(crtCustomFields, crtValues, null, true);

        saveCRT2Neo4jByNodeIds(neo4JConfiguration, customRelationshipTemplate, startNodeId, endNodeId, crtFields, false);
    }

    /**
     * Save CRT to Neo4j
     *
     * @param neo4JConfiguration         Neo4J coordinates
     * @param customRelationshipTemplate Template of the CRT
     * @param startNodeKeysMap           Unique fields values of the start node
     * @param endNodeKeysMap             Unique fields values of the start node
     * @param crtFields                  Fields values of the relationship
     */
    public void saveCRT2Neo4j(String neo4JConfiguration,
                              CustomRelationshipTemplate customRelationshipTemplate, Map<String, Object> startNodeKeysMap,
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
        final Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();

        List<Record> recordList = new ArrayList<>();

        try {
            /* Execute query and parse result inside a relationship.
            If relationship was created fire creation event, fire update event when updated. */

            final StatementResult result = transaction.run(resolvedStatement, valuesMap);  // Execute query

            // Fire notification for each relation created or updated
            recordList = result.list();

            transaction.success();  // Commit transaction

        } catch (Exception e) {

            log.error(e.getMessage());
            transaction.failure();

        } finally {
            transaction.close();    // Close Neo4J transaction
            session.close();        // Close Neo4J session
        }

        for (Record record : recordList) {
            final Neo4jRelationship relationship = new Neo4jRelationship(record.get(relationshipAlias).asRelationship(), neo4JConfiguration);  // Parse relationship

            if (relationship.containsKey("update_date")) {  // Check if relationship contains the "update_date" key
                edgeUpdatedEvent.fire(relationship);        // Fire update event if contains the key
            } else {
                edgeCreatedEvent.fire(relationship);        // Fire creation event if does not contains the key
            }
        }
    }

    public void saveCRT2Neo4jByNodeIds(String neo4JConfiguration, CustomRelationshipTemplate customRelationshipTemplate, Long startNodeId,
                                       Long endNodeId, Map<String, Object> crtFields, boolean isTemporaryCET) {

        final String relationshipAlias = "relationship";    // Alias to use in query

        // Build values map
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("startAlias", Neo4JRequests.START_NODE_ALIAS);
        valuesMap.put("endAlias", Neo4JRequests.END_NODE_ALIAS);
        valuesMap.put("startNode", customRelationshipTemplate.getStartNode().getCode());
        valuesMap.put("endNode", customRelationshipTemplate.getEndNode().getCode());
        valuesMap.put("relationType", customRelationshipTemplate.getName());
        valuesMap.put("startNodeId", startNodeId);
        valuesMap.put("endNodeId", endNodeId);
        valuesMap.put("updateDate", isTemporaryCET ? -1 : System.currentTimeMillis());
        final String fieldsString = neo4jDao.getFieldsString(crtFields.keySet());
        valuesMap.put(FIELDS, fieldsString);
        valuesMap.putAll(crtFields);

        // Build the statement
        StringBuffer statement;

        if (customRelationshipTemplate.isUnique()) {
            statement = neo4jDao.appendReturnStatement(Neo4JRequests.uniqueCrtStatementByNodeIds, relationshipAlias, valuesMap);
        } else {
            statement = neo4jDao.appendReturnStatement(Neo4JRequests.crtStatementByNodeIds, relationshipAlias, valuesMap);
        }
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedStatement = sub.replace(statement);

        // Begin Neo4J transaction
        final Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();

        List<Record> recordList = new ArrayList<>();

        try {
            /* Execute query and parse result inside a relationship.
            If relationship was created fire creation event, fire update event when updated. */
            LOGGER.info(resolvedStatement);

            final StatementResult result = transaction.run(resolvedStatement, valuesMap);  // Execute query

            // Fire notification for each relation created or updated
            recordList = result.list();

            transaction.success();  // Commit transaction

        } catch (Exception e) {

            log.error(e.getMessage());
            transaction.failure();

        } finally {
            transaction.close();    // Close Neo4J transaction
            session.close();        // Close Neo4J session
        }

        for (Record record : recordList) {
            final Neo4jRelationship relationship = new Neo4jRelationship(record.get(relationshipAlias).asRelationship(), neo4JConfiguration);  // Parse relationship

            if (relationship.containsKey("update_date")) {  // Check if relationship contains the "update_date" key
                edgeUpdatedEvent.fire(relationship);        // Fire update event if contains the key
            } else {
                edgeCreatedEvent.fire(relationship);        // Fire creation event if does not contains the key
            }
        }
    }

    /**
     * Persist a source node of an unique relationship.
     * If a relationship that targets the target node exists, then we merge the fields of the start in parameter to
     * the fields of the source node of the relationship.
     * If such a relation does not exists, we create the source node with it fields.
     *
     * @param neo4JConfiguration Neo4J coordinates
     * @param crtCode            Code of the source node to update or create
     * @param startNodeValues    Values to assign to the start node
     * @param endNodeValues      Filters on the target node values
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addSourceNodeUniqueCrt(String neo4JConfiguration,
                                       String crtCode,
                                       Map<String, Object> startNodeValues,
                                       Map<String, Object> endNodeValues) throws BusinessException, ELException {

        // Get relationship template
        final CustomRelationshipTemplate customRelationshipTemplate = customRelationshipTemplateService.findByCode(crtCode);

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
        if (endNodeUniqueFields.isEmpty()) {
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
        Map<String, Object> parametersValues = new HashMap<>();
        parametersValues.putAll(startNodeConvertedValues);
        parametersValues.putAll(endNodeConvertedValues);

        final Session session = neo4jSessionFactory.getSession(neo4JConfiguration);
        final Transaction transaction = session.beginTransaction();

        // Try to find the id of the source node
        String findStartNodeStatement = getStatement(sub, Neo4JRequests.findStartNodeId);
        final StatementResult run = transaction.run(findStartNodeStatement, parametersValues);

        Neo4jEntity startNode = null;

        try {
            try {

                /* Update the source node with the found id */

                final String startNodeAlias = "startNode";   // Alias to use in queries

                // Retrieve ID of the node
                final Record idRecord = run.single();
                final Value id = idRecord.get(0);
                parametersValues.put(ID, id);

                // Create statement
                CustomEntityTemplate startCet = customRelationshipTemplate.getStartNode();
                List<String> additionalLabels = getAdditionalLabels(startCet);
                StringBuffer statement = neo4jDao.appendAdditionalLabels(Neo4JRequests.updateNodeWithId, additionalLabels, startNodeAlias, valuesMap);
                statement = neo4jDao.appendReturnStatement(statement, startNodeAlias, valuesMap);
                String updateStatement = getStatement(sub, statement);

                final StatementResult result = transaction.run(updateStatement, parametersValues);  // Execute query

                // Fire node update event
                startNode = new Neo4jEntity(result.single().get(startNodeAlias).asNode(), neo4JConfiguration);

            } catch (NoSuchRecordException e) {

                /* Create the source node */

                addCetNode(neo4JConfiguration, cetCode, startNodeValues);

            }

            transaction.success();

        } catch (Exception e) {

            transaction.failure();
            log.error("Transaction for persisting entity with code {} and fields {} was rolled back : {}", cetCode, startNodeValues, e.getMessage());
            throw new BusinessException(e);

        } finally {

            session.close();
            transaction.close();

        }

        if (startNode != null) {
            nodeUpdatedEvent.fire(startNode);
        }

    }

    private List<String> getAdditionalLabels(CustomEntityTemplate cet) {
        List<String> additionalLabels = new ArrayList<>(cet.getLabels());
        additionalLabels.addAll(getAllSuperTemplateLabels(cet));
        return additionalLabels;
    }


    public void deleteEntity(String neo4jConfiguration, String cetCode, Map<String, Object> values) throws BusinessException {

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
        Session session = neo4jSessionFactory.getSession(neo4jConfiguration);
        Transaction transaction = session.beginTransaction();

        InternalNode internalNode = null;

        try {

            /* Delete the node and all its associated relationships and fire node deletion event */

            // Execute query
            final StatementResult result = transaction.run(deleteStatement, values);
            for (Record record : result.list()) {

                // Fire deletion event
                final Map<String, Value> properties = record.get("properties").asMap(e -> e);   // Parse properties
                final List<String> labels = record.get("labels").asList(Value::asString);       // Parse labels
                final long id = record.get("id").asLong();                                      // Parse id
                internalNode = new InternalNode(id, labels, properties);     // Create Node object
            }
            transaction.success();

        } catch (Exception e) {

            log.error("Cannot delete node with code {} and values {} : {}", cetCode, values, e.getMessage());
            transaction.failure();
            throw new BusinessException(e);

        } finally {

            /* End transaction */
            session.close();
            transaction.close();

        }

        if (internalNode != null) {
            nodeRemovedEvent.fire(new Neo4jEntity(internalNode, neo4jConfiguration));    // Fire notification
        }

    }

    private List<String> getAllSuperTemplateLabels(CustomEntityTemplate customEntityTemplate) {
        List<String> labels = new ArrayList<>();
        CustomEntityTemplate parent = customEntityTemplate.getSuperTemplate();
        while (parent != null) {
            labels.add(parent.getCode());
            parent = parent.getSuperTemplate();
        }
        return labels;
    }

    private static String getStatement(StrSubstitutor sub, StringBuffer findStartNodeId) {
        return sub.replace(findStartNodeId).replace('"', '\'');
    }

    public String callNeo4jWithStatement(StringBuffer statement, Map<String, Object> values) {
        StrSubstitutor sub = new StrSubstitutor(values);
        String resolvedStatement = sub.replace(statement);
        log.info("resolvedStatement : {}", resolvedStatement);
        resolvedStatement = resolvedStatement.replace('"', '\'');
        Response response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/transaction/flush", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), "{\"statements\":[{\"statement\":\"" + resolvedStatement + "\"}]}");
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
        for (Entry<String, CustomFieldTemplate> cftEntry : customFieldTemplates.entrySet()) {
            CustomFieldTemplate cft = cftEntry.getValue();
            if (checkCustomFields && cft == null) {
                log.error("No custom field template found with code={} for entity {}. Value will be ignored.", cftEntry.getKey(), CustomFieldTemplate.class);
                throw new InvalidCustomFieldException("Custom field template with code " + cftEntry.getKey() + " not found.");
            }
            Object fieldValue = fieldValues.get(cftEntry.getKey());
            try {

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
                            String stringValue;
                            if (valueToCheck instanceof Integer) {
                                stringValue = ((Integer) valueToCheck).toString();
                            } else if (valueToCheck instanceof Map) {
                                GsonBuilder builder = new GsonBuilder();
                                Gson gson = builder.create();
                                String mapToJson = gson.toJson(valueToCheck).replaceAll("'", "’").replaceAll("\"", "");
                                convertedFields.put(cft.getCode(), mapToJson);
                                continue;
                            } else {
                                stringValue = String.valueOf(valueToCheck);
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
                            } else if (valueToCheck instanceof String) {
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
                            } else if (valueToCheck instanceof String) {
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
            } catch (NumberFormatException e) {
                if (cft != null && fieldValue != null) {
                    LOGGER.error(
                            "Wrong data type format for {}#{}\nExpected type : {}, value is : {}\nSkipping field value\n",
                            cft.getAppliesTo(),
                            cft.getCode(),
                            cft.getFieldType(),
                            fieldValue
                    );
                }
            }
        }

        return convertedFields;

    }

    private Object setExpressionField(Map<String, Object> fieldValues, CustomFieldTemplate cft, Map<String, Object> convertedFields) throws ELException {

        Object evaluatedExpression = MeveoValueExpressionWrapper.evaluateExpression(cft.getDefaultValue(), fieldValues.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)), String.class);

        if (evaluatedExpression != null) {

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
        } else {
            return null;
        }
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
        Response response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/transaction/flush", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), statement);
        String result = response.readEntity(String.class);
        log.info("mergeNodes OutGoingRelStatement result={}", result);

        String resolvedInGoingRelStatement = sub.replace(Neo4JRequests.mergeInGoingRelStatement);
        log.info("mergeNodes resolvedOutGoingRelStatement:{}", resolvedInGoingRelStatement);
        String inGoingsReltatement = "{\"statements\":[{\"statement\":\"" + resolvedInGoingRelStatement + "\",\"resultDataContents\":[\"row\"]}]}";
        Response inGoingsRelResponse = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/transaction/flush", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), inGoingsReltatement);
        String inGoingsRelResult = inGoingsRelResponse.readEntity(String.class);
        log.info("mergeNodes InGoingRelStatement result={}", inGoingsRelResult);

    }

    public String getNeo4jData(String query, boolean getOnlyFirstElement) {
        StringBuffer result;
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
        return iepSearchResultDTO != null && iepSearchResultDTO.getData() != null && !iepSearchResultDTO.getData().isEmpty() ? iepSearchResultDTO.getData().get(0) : new ArrayList<>();
    }

    public String getNeo4jRowData(String jsonResult) {
        log.info("getNeo4jRowData jsonResult={}", jsonResult);
        StringBuilder result = new StringBuilder();
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
