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
import org.meveo.api.services.CountryUtils;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.exceptions.InvalidCustomFieldException;
import org.meveo.export.RemoteAuthenticationException;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.admin.User;
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
import org.meveo.neo4j.base.Neo4jConnectionProvider;
import org.meveo.service.base.ValueExpressionWrapper;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.util.ApplicationProvider;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Rachid AITYAAZZA
 */
@Stateless
public class Neo4jService {

    private static final Logger log = LoggerFactory.getLogger(Neo4jService.class);

    @Inject
    private Neo4jConnectionProvider neo4jSessionFactory;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    CountryUtils countryUtils;

    @Inject
    @ApplicationProvider
    protected Provider appProvider;

    private final static StringBuffer crtStatement = new StringBuffer("MATCH (${startAlias}:${startNode} ${starNodeKeys})")
            .append(" MATCH (${endAlias}:${endNode} ${endNodeKeys})")
            .append(" MERGE (${startAlias})-[:${relationType}${fields}]->(${endAlias}) set ${startAlias}.internal_updateDate=${updateDate}, ${endAlias}.internal_updateDate=${updateDate}");

    private final static String cetStatement = "Merge (n:${cetCode}${fieldKeys}) "
            + "ON CREATE SET n = ${fields}"
            + "ON MATCH SET n += ${fields} return ID(n)";

    private final static StringBuffer findStartNodeId = new StringBuffer()
            .append(" MATCH (startNode:${cetCode})-[:${crtCode}]->(:${endCetcode} ${uniqueFields})")
            .append(" MERGE (startNode)")
            .append(" RETURN ID(startNode)");

    private final static StringBuffer updateNodeWithId = new StringBuffer()
            .append(" MATCH (startNode) WHERE ID(startNode) = ${id}")
            .append(" SET startNode += ${fields}");

    private final static String mergeOutGoingRelStatement = "MATCH (a:${cetCode})-[r]->(c) where ID(a) =${originNodeId} "
            + "MATCH (b:${cetCode})where ID(b) =${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(b, relType, {}, c) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";
    private final static String mergeInGoingRelStatement = "MATCH (a:${cetCode})<-[r]-(c) where ID(a) =${originNodeId} "
            + "MATCH (b:${cetCode})where ID(b) =${targetNodeId} "
            + "WITH a, b,c,r, COLLECT(TYPE(r)) AS relTypes "
            + "UNWIND relTypes AS relType "
            + "CALL apoc.create.relationship(c, relType, {}, b) YIELD rel "
            + "DELETE r  "
            + "SET a.internal_active=FALSE "
            + "RETURN rel";

    public final static String START_NODE_ALIAS = "start";
    public final static String END_NODE_ALIAS = "end";

    /**
     * @param cetCode
     * @param fieldValues
     * @param user
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Long addCetNode(String cetCode, Map<Object, Object> fieldValues, User user) {
        return addCetNode(cetCode, fieldValues, false, user);
    }

    public Long addCetNodeInSameTransaction(String cetCode, Map<Object, Object> fieldValues, User user) throws BusinessException {
        return addCetNode(cetCode, fieldValues, false,user);
    }

    public void addCRTinSameTransaction(String crtCode, Map<Object, Object> fieldValues, User user) throws BusinessException {
        addCRT(crtCode, fieldValues, false, user);
    }

    /**
     * @param cetCode
     * @param fieldValues
     * @param user
     * @throws BusinessException
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Long addCetNode(String cetCode, Map<Object, Object> fieldValues, boolean isTemporaryCET, User user) {

        Long nodeId = null;
        boolean isMerged = false;
        log.info("addCetNode cetCode={},isTemporaryCET={},fieldValues={}", cetCode, isTemporaryCET, fieldValues);
        try {
            String excludedPrefixes = ParamBean.getInstance().getProperty("Email.excludedPrefixes", "contact@,info@");
            CustomEntityTemplate cetEntity = customEntityTemplateService
                    .findByCode(cetCode);
            if (cetEntity == null) {
                throw new ElementNotFoundException(cetCode, CustomEntityTemplate.class.getName());
            }
            Map<String, CustomFieldTemplate> cetFields = customFieldTemplateService
                    .findByAppliesTo(cetEntity.getAppliesTo());
            if ("Email".equals(cetCode)) {
                String email = (String) fieldValues.get("email");
                if (!StringUtils.isBlank(email)) {
                    String prefixAt = email.substring(0, email.indexOf("@") + 1);
                    if (!excludedPrefixes.contains(prefixAt)) {
                        String prefix = email.substring(0, email.indexOf("@"));
                        String domainName = email.substring(email.indexOf("@") + 1);
                        fieldValues.put("emailPrefix", prefix);
                        fieldValues.put("emailDomain", domainName);
                    }
                }
            }
            log.info("cetFields:" + cetFields);
            Map<Object, Object> uniqueFields = new HashMap<>();
            Map<Object, Object> fields = validateAndConvertCustomFields(cetFields, fieldValues, uniqueFields, true, user);
            fields.put(CETConstants.CET_ACTIVE_FIELD, "TRUE");
            uniqueFields.put(CETConstants.CET_ACTIVE_FIELD, "TRUE");
            fields.put(CETConstants.CET_UPDATE_DATE_FIELD, isTemporaryCET ? -1 : System.currentTimeMillis());
            if (CETConstants.QUERY_CATEGORY_CODE.equals(cetCode)) {
                String isSuperCategory = (String) fields.get("isSuperCategory");
                if (isSuperCategory != null && Boolean.valueOf(isSuperCategory)) {
                    fields.put(CETConstants.CET_ACTIVE_FIELD, "FALSE");
                    uniqueFields.remove(CETConstants.CET_ACTIVE_FIELD);
                }
            }
            if (!isMerged && fields != null) {
                String results = null;
                Response response = null;
                String rowData = null;
                Map<String, Object> valuesMap = new HashMap<>();
                valuesMap.put("cetCode", cetCode);
                valuesMap.put("fieldKeys", Values.value(uniqueFields));
                valuesMap.put("fields", Values.value(fields));
                StrSubstitutor sub = new StrSubstitutor(valuesMap);
                String resolvedStatement = sub.replace(cetStatement);
                resolvedStatement = resolvedStatement.replace('"', '\'');
                log.info("addCetNode resolvedStatement:{}", resolvedStatement);

                response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/transaction/commit", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), "{\"statements\":[{\"statement\":\"" + resolvedStatement + "\"}]}");
                results = response.readEntity(String.class);
                log.info("addCetNode results={}", results);
                rowData = getNeo4jRowData(results);
                if (!StringUtils.isBlank(rowData) && CETUtils.isParsableAsLong(rowData)) {
                    nodeId = Long.valueOf(rowData);
                }

            }
        } catch (BusinessException e) {
            log.error("addCetNode cetCode={}, errorMsg={}", cetCode, e.getMessage(), e);
        }
        log.info("addCetNode cetCode={},nodeId={},isMerged={}", cetCode, nodeId, isMerged);
        return nodeId;

    }

    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addCRT(String crtCode, Map<Object, Object> fieldValues, User user) throws BusinessException {
        addCRT(crtCode, fieldValues, false, user);
    }

    /**
     * @param crtCode
     * @param fieldValues
     * @param user
     * @throws BusinessException
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addCRT(String crtCode, Map<Object, Object> fieldValues, boolean isTemporaryCET, User user) throws BusinessException {
        log.info("crtCode={}", crtCode);
        CustomRelationshipTemplate customRelationshipTemplate = customRelationshipTemplateService.findByCode(crtCode);

        if (customRelationshipTemplate == null) {
            throw new ElementNotFoundException(crtCode, CustomRelationshipTemplate.class.getName());
        }

        Map<String, CustomFieldTemplate> crtCustomFields = customFieldTemplateService
                .findByAppliesTo(customRelationshipTemplate.getAppliesTo());

        log.info("crtCustomFields={}", crtCustomFields);
        log.info("addCRT fieldValues size:" + fieldValues.size());
        log.info("addCRT fieldValues:" + fieldValues);
        Map<String, CustomFieldTemplate> startNodeFields = customFieldTemplateService
                .findByAppliesTo(customRelationshipTemplate.getStartNode().getAppliesTo());
        Map<Object, Object> startNodeFieldValues = validateAndConvertCustomFields(startNodeFields, fieldValues, null, true, user);
        log.info("addCRT startNodeFieldValues:" + startNodeFieldValues);

        Map<Object, Object> startNodeKeysMap = getNodeKeys(customRelationshipTemplate.getStartNode().getAppliesTo(),
                startNodeFieldValues);

        Map<String, CustomFieldTemplate> endNodeFields = customFieldTemplateService
                .findByAppliesTo(customRelationshipTemplate.getEndNode().getAppliesTo());
        Map<Object, Object> endNodeFieldValues = validateAndConvertCustomFields(endNodeFields, fieldValues, null, true, user);

        Map<Object, Object> endNodeKeysMap = getNodeKeys(customRelationshipTemplate.getEndNode().getAppliesTo(),
                endNodeFieldValues);

        log.info("startNodeKeysMap:" + startNodeKeysMap);
        log.info("endNodeKeysMap:" + endNodeKeysMap);
        if (startNodeKeysMap.size() > 0 && endNodeKeysMap.size() > 0) {
            Map<Object, Object> crtFields = validateAndConvertCustomFields(crtCustomFields, fieldValues, null, true, user);
            // crtFields.put(CETConstants.CET_UPDATE_DATE_FIELD, System.currentTimeMillis());
            saveCRT2Neo4j(customRelationshipTemplate, startNodeKeysMap, endNodeKeysMap, crtFields, isTemporaryCET);
        }
    }

    /**
     * Persist an instance of {@link CustomRelationshipTemplate}
     *
     * @param crtCode          Code of the CustomRelationshipTemplate instance
     * @param crtValues        Properties of the link
     * @param startFieldValues Filters on start node
     * @param endFieldValues   Filters on end node
     * @param user             User executing the query
     * @throws BusinessException If error happens
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addCRT(String crtCode, Map<Object, Object> crtValues, Map<Object, Object> startFieldValues, Map<Object, Object> endFieldValues, User user)
            throws BusinessException {

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
        Map<Object, Object> startNodeFieldValues = validateAndConvertCustomFields(startNodeFields, startFieldValues, null, true, user);
        log.info("Filters on start node :" + startNodeFieldValues);
        Map<Object, Object> startNodeKeysMap = getNodeKeys(customRelationshipTemplate.getStartNode().getAppliesTo(), startNodeFieldValues);

        /* Recuperation of the custom fields of the target node */
        Map<String, CustomFieldTemplate> endNodeFields = customFieldTemplateService.findByAppliesTo(customRelationshipTemplate.getEndNode().getAppliesTo());
        Map<Object, Object> endNodeFieldValues = validateAndConvertCustomFields(endNodeFields, endFieldValues, null, true, user);
        log.info("Filters on end node : " + startNodeFieldValues);
        Map<Object, Object> endNodeKeysMap = getNodeKeys(customRelationshipTemplate.getEndNode().getAppliesTo(), endNodeFieldValues);

        log.info("startNodeKeysMap:" + startNodeKeysMap);
        log.info("endNodeKeysMap:" + endNodeKeysMap);

        /* If matching source and target exists, persist the link */
        if (startNodeKeysMap.size() > 0 && endNodeKeysMap.size() > 0) {
            Map<Object, Object> crtFields = validateAndConvertCustomFields(crtCustomFields, crtValues, null, true, user);
            saveCRT2Neo4j(customRelationshipTemplate, startNodeKeysMap, endNodeKeysMap, crtFields, false);
        }

    }

    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addCRT(String crtCode, Map<Object, Object> startFieldValues, Map<Object, Object> endFieldValues, User user) throws BusinessException {
        Map<Object, Object> crtValues = new HashMap<>();
        crtValues.putAll(startFieldValues);
        crtValues.putAll(endFieldValues);
        addCRT(crtCode, crtValues, startFieldValues, endFieldValues, user);
    }

    /**
     * Save CRT to Neo4j
     *  @param customRelationshipTemplate
     * @param startNodeKeysMap
     * @param endNodeKeysMap
     * @param crtFields
     */
    private void saveCRT2Neo4j(CustomRelationshipTemplate customRelationshipTemplate, Map<Object, Object> startNodeKeysMap,
                               Map<Object, Object> endNodeKeysMap, Map<Object, Object> crtFields, boolean isTemporaryCET) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("startAlias", START_NODE_ALIAS);
        valuesMap.put("endAlias", END_NODE_ALIAS);
        valuesMap.put("startNode", customRelationshipTemplate.getStartNode().getCode());
        valuesMap.put("endNode", customRelationshipTemplate.getEndNode().getCode());
        valuesMap.put("relationType", customRelationshipTemplate.getName());
        valuesMap.put("starNodeKeys", Values.value(startNodeKeysMap));
        valuesMap.put("endNodeKeys", Values.value(endNodeKeysMap));
        valuesMap.put("updateDate", isTemporaryCET ? -1 : System.currentTimeMillis());
        valuesMap.put("fields", Values.value(crtFields));
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String result = callNeo4jWithStatement(crtStatement, valuesMap);
        log.info("addCRT result={}", result);
    }

    /**
     * Persist a source node of an unique relationship.
     * If a relationship that targets the target node exists, then we merge the fields of the start in parameter to
     * the fields of the source node of the relationship.
     * If such a relation does not exists, we create the source node with it fields.
     *
     * @param crtCode
     * @param endNodeValues
     */
    @JpaAmpNewTx
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addSourceNodeUniqueCrt(String crtCode, Map<Object, Object> startNodeValues, Map<Object, Object> endNodeValues){

        /* Get relationship template */
        final CustomRelationshipTemplate customRelationshipTemplate = customRelationshipTemplateService.findByCode(crtCode);

        /* Extract unique fields values for the start node */
        final Map<Object, Object> endNodeUniqueFields = getNodeKeys(customRelationshipTemplate.getStartNode().getAppliesTo(), endNodeValues);

        /* Map the variables declared in the statement */
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("cetCode", customRelationshipTemplate.getStartNode().getCode());
        valuesMap.put("endCetCode", customRelationshipTemplate.getEndNode().getCode());
        valuesMap.put("uniqueFields", Values.value(endNodeUniqueFields));
        valuesMap.put("sourceNodeFields", Values.value(startNodeValues));

        /* Save the source node to Neo4j */
//        String result = callNeo4jWithStatement(uniqueCrtSourceNodeStatement, valuesMap);
//        log.info("addSourceNodeUniqueCrt result= {} ", result);
    }

    public String callNeo4jWithStatement(StringBuffer statement, Map<String, Object> values){
        StrSubstitutor sub = new StrSubstitutor(values);
        String resolvedStatement = sub.replace(statement);
        log.info("resolvedStatement : {}", resolvedStatement);
        resolvedStatement = resolvedStatement.replace('"', '\'');
        Response response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/transaction/commit", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), "{\"statements\":[{\"statement\":\"" + resolvedStatement + "\"}]}");
        return response.readEntity(String.class);
    }

    /**
     * @param appliesTo
     * @param convertedFieldValues
     * @return
     */

    private Map<Object, Object> getNodeKeys(String appliesTo, Map<Object, Object> convertedFieldValues) {
        Map<Object, Object> nodeKeysMap = new HashMap<Object, Object>();
        List<CustomFieldTemplate> retrievedCft = customFieldTemplateService.findCftUniqueFieldsByApplies(appliesTo);
        for (CustomFieldTemplate cf : retrievedCft) {
            if (!StringUtils.isBlank(convertedFieldValues.get(cf.getDescription()))) {
                if (cf.getIndexType() == CustomFieldIndexTypeEnum.INDEX_NEO4J) {
                        nodeKeysMap.put(cf.getCode() + "_IDX", convertedFieldValues.get(cf.getCode() + "_IDX"));
                } else {
                    nodeKeysMap.put(cf.getDescription(), convertedFieldValues.get(cf.getDescription()));
                }
            }
        }
        return nodeKeysMap;
    }


    public Response callNeo4jRest(String baseurl, String url, String username, String password, String body) {
        try {
            ResteasyClient client = new ResteasyClientBuilder().build();
            ResteasyWebTarget target = client.target(baseurl + url);
            log.info("callNeo4jRest {} with body:{}", baseurl + url, body);
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
    public Map<Object, Object> validateAndConvertCustomFields(Map<String, CustomFieldTemplate> customFieldTemplates, Map<Object, Object> fieldValues, Map<Object, Object> uniqueFields, boolean checkCustomFields, User currentUser) throws BusinessException {
        Map<Object, Object> convertedFields = new HashMap<Object, Object>();
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
                throw new InvalidCustomFieldException("CFT with code " + cft.getCode() + " does not exist");
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
                log.info("validateAndConvertCustomFields fieldValue0={}", fieldValue);
                for (Object valueToCheck : valuesToCheck) {
                    if (cft.getFieldType() == CustomFieldTypeEnum.STRING && !"null".equals(valueToCheck)) {
                        String stringValue = null;
                        if (valueToCheck instanceof Integer) {
                            stringValue = ((Integer) valueToCheck).toString();
                        } else if (valueToCheck instanceof Map) {
                            GsonBuilder builder = new GsonBuilder();
                            Gson gson = builder.create();
                            String mapToJson = gson.toJson(valueToCheck).replaceAll("'", "’").replaceAll("\"", "");
                            log.info("validateAndConvertCustomFields mapToJson={}", mapToJson);
                            convertedFields.put(cft.getCode(), mapToJson);
                            continue;
                        } else {
                            stringValue = (String) valueToCheck;
                        }
                        stringValue = stringValue.trim().replaceAll("'", "’").replaceAll("\"", "");
                        stringValue = stringValue.replaceAll("\n", " ");

                        if (cft.getIndexType() == CustomFieldIndexTypeEnum.INDEX_NEO4J) {
                            convertedFields.put(cft.getCode() + "_IDX", CETUtils.stripAndFormatFields(stringValue));
                        }
                        if (cft.getCode().equals("country")) {
                            String countryCode = countryUtils.getCountryIsoCode(Locale.US, stringValue.toLowerCase());
                            if (countryCode == null) {
                                countryCode = countryUtils.getCountryIsoCode(Locale.FRANCE, stringValue.toLowerCase());
                            }
                            convertedFields.put("countryCode", countryCode);

                        } else if (cft.getCode().equals("countryCode")) {
                            String countryName = countryUtils.getCountryName(Locale.US, stringValue.toLowerCase());
                            if (countryName == null) {
                                countryName = countryUtils.getCountryName(Locale.FRANCE, stringValue.toLowerCase());
                            }
                            convertedFields.put("country", countryName);
                        }
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
                            if (cft.getCode().equals("city")) {
                                fieldValue = ((String) fieldValue).replaceAll("[0-9]", "");
                            }
                        }

                    } else if (cft.getFieldType() == CustomFieldTypeEnum.LONG) {
                        Long longValue = null;
                        if (valueToCheck instanceof Integer) {
                            longValue = ((Integer) valueToCheck).longValue();
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
                        Double doubleValue = null;
                        if (valueToCheck instanceof Integer) {
                            doubleValue = ((Integer) valueToCheck).doubleValue();
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
                        uniqueFields.put(cft.getDescription(), fieldValue);
                    }
                    convertedFields.put(cft.getDescription(), fieldValue);
                }
            }
            if (cft.getFieldType() == CustomFieldTypeEnum.EXPRESSION) {
                fieldValue = setExpressionField(fieldValues, cft, convertedFields);
                convertedFields.put(cft.getDescription(), fieldValue);
                if (cft.isUnique() && uniqueFields != null) {
                    uniqueFields.put(cft.getDescription(), fieldValue);
                }
            }
        }
        //loop CFT a second time to set expression fields that are composed by other expressionFields
        for (CustomFieldTemplate cft : expressionFields) {
            Object fieldValue = setExpressionField(fieldValues, cft, convertedFields);
            convertedFields.put(cft.getDescription(), fieldValue);
            if (cft.isUnique() && uniqueFields != null) {
                uniqueFields.put(cft.getDescription(), fieldValue);
            }
        }
        return convertedFields;
    }

    private Object setExpressionField(Map<Object, Object> fieldValues, CustomFieldTemplate cft, Map<Object, Object> convertedFields) throws BusinessException {

        Object evaluatedExpression = ValueExpressionWrapper.evaluateExpression(cft.getDefaultValue(), fieldValues,
                String.class);
        //fieldValues.put(cft.getCode(), fieldValue);
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

        String resolvedOutGoingRelStatement = sub.replace(mergeOutGoingRelStatement);
        log.info("mergeNodes resolvedOutGoingRelStatement:{}", resolvedOutGoingRelStatement);
        String statement = "{\"statements\":[{\"statement\":\"" + resolvedOutGoingRelStatement + "\",\"resultDataContents\":[\"row\"]}]}";
        Response response = callNeo4jRest(neo4jSessionFactory.getRestUrl(), "/db/data/transaction/commit", neo4jSessionFactory.getNeo4jLogin(), neo4jSessionFactory.getNeo4jPassword(), statement);
        String result = response.readEntity(String.class);
        log.info("mergeNodes OutGoingRelStatement result={}", result);

        String resolvedInGoingRelStatement = sub.replace(mergeInGoingRelStatement);
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
