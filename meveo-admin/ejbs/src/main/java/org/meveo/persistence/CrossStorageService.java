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

package org.meveo.persistence;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.persistence.neo4j.base.Neo4jDao;
import org.meveo.persistence.neo4j.service.Neo4jService;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomTableRelationService;
import org.meveo.service.custom.CustomTableService;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import java.util.*;
import java.util.stream.Collectors;

@Default
public class CrossStorageService implements CustomPersistenceService {

    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCacheContainerProvider;

    @Inject
    private Neo4jService neo4jService;

    @Inject
    private Neo4jDao neo4jDao;

    @Inject
    private CustomTableService customTableService;

    @Inject
    private CustomEntityInstanceService customEntityInstanceService;

    @Inject
    private CustomTableRelationService customTableRelationService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    /**
     * Retrieves one entity instance
     *
     * @param configurationCode Repository code
     * @param cet               Template of the entities to retrieve
     * @param uuid              UUID of the entity
     * @return list of matching entities
     */
    public Map<String, Object> find(String configurationCode, CustomEntityTemplate cet, String uuid) {
        return find(configurationCode, cet, uuid, null);
    }

    /**
     * Retrieves one entity instance
     *
     * @param configurationCode Repository code
     * @param cet               Template of the entities to retrieve
     * @param uuid              UUID of the entity
     * @param fetchFields      Fields to select
     * @return list of matching entities
     */
    //TODO : add "fetchSubEntities" parameter
    public Map<String, Object> find(String configurationCode, CustomEntityTemplate cet, String uuid, List<String> fetchFields) {
        if(uuid == null){
            throw new NullPointerException("Cannot retrieve entity by uuid without uuid");
        }

        List<String> selectFields = fetchFields == null ? new ArrayList<>() : new ArrayList<>(fetchFields);
        Map<String, Object> values = new HashMap<>();

        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            List<String> neo4jFields = filterFields(selectFields, cet, DBStorageType.NEO4J);
            if(!neo4jFields.isEmpty()) {
                values.putAll(neo4jDao.findNodeById(configurationCode, cet.getCode(), uuid, neo4jFields));
            }
        }

        // Don't retrieve the fields we already fetched
        selectFields.removeAll(values.keySet());

        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            List<String> sqlFields = filterFields(selectFields, cet, DBStorageType.SQL);
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                final Map<String, Object> customTableValue = customTableService.findById(SQLStorageConfiguration.getDbTablename(cet), uuid, sqlFields);
                replaceKeys(cet, sqlFields, customTableValue);
                values.putAll(customTableValue);
            } else {
                final CustomEntityInstance cei = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
                if (sqlFields != null) {
                    for (String field : sqlFields) {
                        values.put(field, cei.getCfValues().getCfValue(field).getValue());
                    }
                } else {
                    values.putAll(cei.getCfValuesAsValues());
                }

            }
        }

        // Remove null values
        values.values().removeIf(Objects::isNull);

        // Fetch entity references
        fetchEntityReferences(configurationCode, cet, values);

        return values;
    }

    public Map<String, Object> getMissingData(Map<String, Object> data, String configurationCode, CustomEntityTemplate cet, String uuid, Collection<String> selectFields) {
        List<String> actualFetchField = new ArrayList<>(selectFields);
        actualFetchField.removeAll(data.keySet());

        // Every field has been already retrieved
        if (actualFetchField.isEmpty()) {
            return new HashMap<>();
        }

        // Retrieve the missing fields
        return find(configurationCode, cet, uuid, actualFetchField);
    }

    /**
     * Retrieves entity instances
     *
     * @param configurationCode       Repository code
     * @param cet                     Template of the entities to retrieve
     * @param paginationConfiguration Pagination and filters
     * @return list of matching entities
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> find(String configurationCode, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {

        final List<String> actualFetchFields = paginationConfiguration == null ? null : paginationConfiguration.getFetchFields();

        final Map<String, Object> filters = paginationConfiguration == null ? null : paginationConfiguration.getFilters();

        List<Map<String, Object>> valuesList = new ArrayList<>();

        // If no pagination nor fetch fields are defined, we consider that we must fetch everything
        boolean fetchAllFields = paginationConfiguration == null || actualFetchFields == null;

        // In case where only graphql query is passed, we will only use it so we won't fetch sql
        boolean dontFetchSql = paginationConfiguration != null && paginationConfiguration.getFilters() == null && paginationConfiguration.getGraphQlQuery() != null;

        boolean hasSqlFetchField = paginationConfiguration != null && actualFetchFields != null && actualFetchFields.stream()
                .anyMatch(s -> customTableService.sqlCftFilter(cet, s));

        boolean hasSqlFilter = paginationConfiguration != null && paginationConfiguration.getFilters() != null && filters.keySet().stream()
                .anyMatch(s -> customTableService.sqlCftFilter(cet, s));

        // Collect initial data
        if (cet.getAvailableStorages().contains(DBStorageType.SQL) && !dontFetchSql && (fetchAllFields || hasSqlFetchField || hasSqlFilter)) {
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                final List<Map<String, Object>> values = customTableService.list(cet, paginationConfiguration);
                values.forEach(v -> replaceKeys(cet, actualFetchFields, v));
                valuesList.addAll(values);
            } else {
                final List<CustomEntityInstance> ceis = customEntityInstanceService.list(cet.getCode(), filters);
                final List<Map<String, Object>> values = ceis.stream()
                        .map(cei -> {
                            final HashMap<String, Object> map = new HashMap<>(cei.getCfValuesAsValues());
                            map.put("uuid", cei.getUuid());
                            if(!fetchAllFields) {
                                for (String k : cei.getCfValuesAsValues().keySet()) {
                                    if (!actualFetchFields.contains(k)) {
                                        map.remove(k);
                                    }
                                }
                            }
                            return map;
                        }).collect(Collectors.toList());

                valuesList.addAll(values);
            }
        }

        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {

            // Find by graphql if query provided
            if(paginationConfiguration != null && paginationConfiguration.getGraphQlQuery() != null){

                String graphQlQuery = paginationConfiguration.getGraphQlQuery()
                        .replaceAll("([\\w)]\\s*\\{)(\\s*\\w*)", "$1meveo_uuid,$2");

                final Map<String, Object> result = neo4jDao.executeGraphQLQuery(
                        configurationCode,
                        graphQlQuery,
                        null,
                        null
                );

                final List<Map<String, Object>> values = (List<Map<String, Object>>) result.get(cet.getCode());
                values.forEach(map -> {
                    final HashMap<String, Object> resultMap = new HashMap<>(map);
                    map.keySet().forEach(key -> {
                        if(!key.equals("uuid") && !key.equals("meveo_uuid") && actualFetchFields != null && !actualFetchFields.contains(key)){
                            resultMap.remove(key);
                        }
                    });
                    valuesList.add(resultMap);
                });
            }

        }

        // Complete missing data
        valuesList.forEach(data -> {
            String uuid = (String) (data.get("uuid") != null ? data.get("uuid") : data.get("meveo_uuid"));

            Collection<String> fetchFields = actualFetchFields != null
                    ? actualFetchFields
                    : customFieldTemplateService.findByAppliesTo(cet.getAppliesTo()).keySet();

            final Map<String, Object> missingData = getMissingData(data, configurationCode, cet, uuid, fetchFields);
            data.putAll(missingData);
        });

        return valuesList;
    }

    /**
     * Insert / update an entity with the sourceValues. The target entity is retrieved from the target values.
     * <br> If the target entity does not exist, create the source node.
     * <br> If the target entity and the relation exists, update the source entity with the source values.
     * <br> If the target entity exists but not the relation, create the relation along with the source entity.
     *
     * @param configurationCode Code of the repository / configuration to store the data.
     *                          <br> NOTE : only available for NEO4J at the moment.
     * @param relationCode      Code of the relation to create
     * @param sourceValues      Values to insert
     * @param targetValues      Filters on target entity
     */
    @Override
    public void addSourceEntityUniqueCrt(String configurationCode, String relationCode, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException {
        CustomRelationshipTemplate crt = customFieldsCacheContainerProvider.getCustomRelationshipTemplate(relationCode);

        if (!crt.isUnique()) {
            throw new IllegalArgumentException("CRT must be unique !");
        }

        final CustomEntityTemplate endNode = crt.getEndNode();
        final CustomEntityTemplate startNode = crt.getStartNode();

        // Everything is stored in Neo4J
        if (
                isEverythingStoredInNeo4J(crt)
        ) {
            neo4jService.addSourceNodeUniqueCrt(
                    configurationCode,
                    relationCode,
                    filterValues(sourceValues, crt, DBStorageType.NEO4J),
                    filterValues(targetValues, crt, DBStorageType.NEO4J)
            );

            return;
        }

        String targetUUUID = findEntityId(configurationCode, targetValues, endNode);

        // Target does not exists. We create the source
        if (targetUUUID == null) {
            createOrUpdate(configurationCode, startNode.getCode(), sourceValues);

        } else {
            // Target exists. Let's check if the relation exist.
            final String relationUUID = findUniqueRelationByTargetUuid(configurationCode, targetUUUID, crt);

            // Relation does not exists. We create the source.
            if (relationUUID == null) {
                createOrUpdate(configurationCode, startNode.getCode(), sourceValues);

            } else {
                // Relation exists. We update the source node.
                String sourceUUID = findIdOfSourceEntityByRelationId(configurationCode, relationUUID, crt);
                update(configurationCode, startNode, sourceValues, sourceUUID);
            }
        }

    }

    private boolean isEverythingStoredInNeo4J(CustomRelationshipTemplate crt) {
        return crt.getAvailableStorages().contains(DBStorageType.NEO4J) &&
                crt.getStartNode().getAvailableStorages().contains(DBStorageType.NEO4J) &&
                crt.getEndNode().getAvailableStorages().contains(DBStorageType.NEO4J);
    }

    /**
     * Create or update an entity and enventyally entity references that it holds
     *
     * @param configurationCode Code of the repository / configuration to store the data.
     *                          <br> NOTE : only available for NEO4J at the moment.
     * @param entityCode        Code of the entity to create
     * @param values            Values of the entity
     * @return the persisted entites
     */
    @Override
    public PersistenceActionResult createOrUpdate(String configurationCode, String entityCode, Map<String, Object> values) throws BusinessException {

        Map<String, Object> entityValues = new HashMap<>(values);

        CustomEntityTemplate cet = customFieldsCacheContainerProvider.getCustomEntityTemplate(entityCode);
        createEntityReferences(configurationCode, entityValues, cet);

        Set<EntityRef> persistedEntities = new HashSet<>();

        String uuid = null;

        // Neo4j storage
        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            Map<String, Object> neo4jValues = filterValues(entityValues, cet, DBStorageType.NEO4J);
            if(!neo4jValues.isEmpty()) {
                final Set<EntityRef> entityRefs = neo4jService.addCetNode(configurationCode, cet, neo4jValues);
                //TODO: check if created or updated
                uuid = getTrustedUuids(entityRefs).get(0);
                if(uuid == null){
                    throw new NullPointerException("Generated UUID from Neo4J cannot be null");
                }
                persistedEntities.addAll(entityRefs);
            }
        }

        // SQL Storage
        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            Map<String, Object> sqlValues = filterValues(entityValues, cet, DBStorageType.SQL);
            if(!sqlValues.isEmpty()) {
                if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                    String tableName = SQLStorageConfiguration.getDbTablename(cet);
                    //TODO: Find by UUID if updated in Neo4J and throw error if not found
                    String sqlUUID = customTableService.findIdByValues(tableName, sqlValues);
                    if (sqlUUID != null) {
                        sqlValues.put("uuid", sqlUUID);
                        customTableService.update(cet, sqlValues);
                        uuid = sqlUUID;
                    } else {
                        if (uuid != null) {
                            sqlValues.put("uuid", uuid);
                        }

                        uuid = customTableService.create(cet, sqlValues);
                    }
                    persistedEntities.add(new EntityRef(uuid));
                } else {
                    final String code = (String) entityValues.get("code");
                    CustomEntityInstance cei = getCustomEntityInstance(entityCode, values);

                    CustomFieldValues customFieldValues = new CustomFieldValues();
                    values.forEach(customFieldValues::setValue);

                    if (cei == null) {
                        cei = new CustomEntityInstance();
                        cei.setCetCode(entityCode);
                        cei.setCode(code);
                        cei.setCfValues(customFieldValues);

                        if (uuid != null) {
                            cei.setUuid(uuid);
                        }

                        customEntityInstanceService.create(cei);
                    } else {
                        cei.setCfValues(customFieldValues);
                        customEntityInstanceService.update(cei);
                    }

                    persistedEntities.add(new EntityRef(cei.getUuid()));
                    uuid = cei.getUuid();
                }
            }
        }

        return new PersistenceActionResult(persistedEntities, uuid);
    }

    /**
     * Update an entity instance
     *
     * @param configurationCode Repository code
     * @param cet               Template of the entity to update
     * @param values            New values to assign to the entity
     * @param uuid              UUID identifying the entity to update
     */
    public void update(String configurationCode, CustomEntityTemplate cet, Map<String, Object> values, String uuid) throws BusinessException {
        // Neo4j storage
        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            Map<String, Object> neo4jValues = filterValues(values, cet, DBStorageType.NEO4J);
            final List<String> cetLabels = cet.getNeo4JStorageConfiguration().getLabels() != null ? cet.getNeo4JStorageConfiguration().getLabels() : new ArrayList<>();
            List<String> labels = new ArrayList<>(cetLabels);
            labels.add(cet.getCode());
            neo4jDao.updateNodeByNodeId(configurationCode, uuid, neo4jValues, labels);
        }

        // SQL Storage
        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            // Custom table
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                Map<String, Object> sqlValues = filterValues(values, cet, DBStorageType.SQL);
                sqlValues.put("uuid", uuid);
                customTableService.update(cet, sqlValues);
            } else {
                // CEI storage
                final CustomEntityInstance cei = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
                CustomFieldValues customFieldValues = new CustomFieldValues();
                values.forEach(customFieldValues::setValue);
                cei.setCfValues(customFieldValues);
                customEntityInstanceService.update(cei);
            }
        }
    }

    @Override
    public void addCRTByValues(String configurationCode, String relationCode, Map<String, Object> relationValues, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException {
        CustomRelationshipTemplate crt = customFieldsCacheContainerProvider.getCustomRelationshipTemplate(relationCode);

        final CustomEntityTemplate endNode = crt.getEndNode();
        final CustomEntityTemplate startNode = crt.getStartNode();

        // All Neo4j storage
        if (
                crt.getAvailableStorages().contains(DBStorageType.NEO4J)
                        && startNode.getAvailableStorages().contains(DBStorageType.NEO4J)
                        && endNode.getAvailableStorages().contains(DBStorageType.NEO4J)
        ) {
            neo4jService.addCRTByNodeValues(
                    configurationCode,
                    relationCode,
                    filterValues(relationValues, crt, DBStorageType.NEO4J),
                    filterValues(sourceValues, crt, DBStorageType.NEO4J),
                    filterValues(targetValues, crt, DBStorageType.NEO4J));
        }

        String sourceUUID = findEntityId(configurationCode, sourceValues, startNode);
        String targetUUUID = findEntityId(configurationCode, sourceValues, endNode);

        // SQL Storage
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            //TODO: Check if entity references placed in values maps does not interfer ...
            customTableRelationService.createRelation(crt, sourceUUID, targetUUUID, relationValues);
        }

        // Neo4J Storage
        if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jService.addCRTByNodeIds(configurationCode, crt.getCode(), relationValues, sourceUUID, targetUUUID);
        }

    }

    @Override
    public void addCRTByUuids(String configurationCode, String relationCode, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws ELException, BusinessException {
        CustomRelationshipTemplate crt = customFieldsCacheContainerProvider.getCustomRelationshipTemplate(relationCode);

        // All neo4j storage
        if (isEverythingStoredInNeo4J(crt)) {
            neo4jService.addCRTByNodeIds(
                    configurationCode,
                    relationCode,
                    filterValues(relationValues, crt, DBStorageType.NEO4J),
                    sourceUuid,
                    targetUuid);

            return;
        }

        // SQL Storage
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            //TODO: Check if entity references placed in values maps does not interfer ...
            customTableRelationService.createRelation(crt, sourceUuid, targetUuid, relationValues);
        }

        // Neo4J Storage
        if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jService.addCRTByNodeIds(configurationCode, crt.getCode(), relationValues, sourceUuid, targetUuid);
        }
    }

    /**
     * Find an entity instance UUID
     *
     * @param configurationCode Repository to search in
     * @param valuesFilters     Filter on entity's values
     * @param cet               Template of the entity
     * @return UUID of the entity or nul if it was'nt found
     */
    public String findEntityId(String configurationCode, Map<String, Object> valuesFilters, CustomEntityTemplate cet) {
        String uuid = null;

        // SQL
        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            // Custom table
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                final String dbTablename = SQLStorageConfiguration.getDbTablename(cet);
                uuid = customTableRelationService.findIdByValues(dbTablename, filterValues(valuesFilters, cet, DBStorageType.SQL));
            } else {
                final CustomEntityInstance customEntityInstance = getCustomEntityInstance(cet.getCode(), valuesFilters);
                if (customEntityInstance != null) {
                    uuid = customEntityInstance.getUuid();
                }
            }
        }

        // Neo4J
        if (uuid == null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            uuid = neo4jDao.findNodeId(configurationCode, cet.getCode(), filterValues(valuesFilters, cet, DBStorageType.NEO4J));
        }

        return uuid;
    }

    /**
     * Remove an entity from database
     *
     * @param configurationCode Repository
     * @param cet               Template of the entity
     * @param uuid              UUID of the entity
     */
    public void remove(String configurationCode, CustomEntityTemplate cet, String uuid) throws BusinessException {
        if(uuid == null) {
            throw new IllegalArgumentException("Cannot remove entity by UUID without uuid");
        }

        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                final String dbTablename = SQLStorageConfiguration.getDbTablename(cet);
                customTableService.remove(dbTablename, uuid);
            } else {
                final CustomEntityInstance customEntityInstance = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
                customEntityInstanceService.remove(customEntityInstance);
            }
        }

        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jDao.removeNode(configurationCode, cet.getCode(), uuid);
        }
    }

    /**
     * Remove a relation from database
     *
     * @param configurationCode Repository
     * @param crt               Template of the relation
     * @param uuid              UUID of the relation
     */
    public void remove(String configurationCode, CustomRelationshipTemplate crt, String uuid) throws BusinessException {
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            final String dbTablename = SQLStorageConfiguration.getDbTablename(crt);
            customTableRelationService.remove(dbTablename, uuid);
        }

        if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jDao.removeRelation(configurationCode, crt.getCode(), uuid);
        }
    }

    private Map<String, Object> filterValues(Map<String, Object> values, CustomModelObject cet, DBStorageType storageType) {
        return values.entrySet()
                .stream()
                .filter(entry -> {
                    // Always include UUID
                    if(entry.getKey().equals("uuid")){
                        return true;
                    }

                    // For CEI storage, always include code
                    if(cet instanceof CustomEntityTemplate && entry.getKey().equals("code")){
                        if(storageType == DBStorageType.SQL && !((CustomEntityTemplate) cet).getSqlStorageConfiguration().isStoreAsTable()){
                            return true;
                        }
                    }

                    CustomFieldTemplate cft = customFieldsCacheContainerProvider.getCustomFieldTemplate(entry.getKey(), cet.getAppliesTo());

                    if(cft == null){
                        return false;
                    }

                    return cft.getStorages().contains(storageType);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<String> filterFields(List<String> fields, CustomModelObject cet, DBStorageType storageType) {
        if (fields == null) {
            return new ArrayList<>();
        }

        return fields.stream()
                .filter(entry -> {
                    CustomFieldTemplate cft = customFieldsCacheContainerProvider.getCustomFieldTemplate(entry, cet.getAppliesTo());
                    return cft.getStorages().contains(storageType);
                }).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private void createEntityReferences(String configurationCode, Map<String, Object> entityValues, CustomEntityTemplate cet) throws BusinessException {
        // Extract entities references
        for (String fieldName : entityValues.keySet()) {
            customFieldsCacheContainerProvider.getCustomFieldTemplate(fieldName, cet.getAppliesTo());
        }

        List<CustomFieldTemplate> cetFields = entityValues.keySet()
                .stream()
                .map(fieldName -> customFieldsCacheContainerProvider.getCustomFieldTemplate(fieldName, cet.getAppliesTo()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (CustomFieldTemplate customFieldTemplate : cetFields) {
            if (CustomFieldTypeEnum.ENTITY.equals(customFieldTemplate.getFieldType())) {
                final CustomEntityTemplate referencedCet = customFieldsCacheContainerProvider.getCustomEntityTemplate(customFieldTemplate.getEntityClazzCetCode());

                List<Map<String, Object>> entitiesToCreate = new ArrayList<>();
                final Object fieldValue = entityValues.get(customFieldTemplate.getCode());

                if (fieldValue instanceof Collection && customFieldTemplate.getStorageType() != CustomFieldStorageTypeEnum.LIST) {
                    Collection collectionValue = (Collection<? extends Map<String, Object>>) fieldValue;
                    if (!collectionValue.isEmpty()) {
                        entitiesToCreate.add((Map<String, Object>) collectionValue.iterator().next());
                    }
                } else if (fieldValue instanceof Collection && customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
                    entitiesToCreate.addAll((Collection<? extends Map<String, Object>>) fieldValue);
                } else if (fieldValue instanceof Map) {
                    entitiesToCreate.add((Map<String, Object>) fieldValue);
                } else if (referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
                    entitiesToCreate.add(Collections.singletonMap("value", fieldValue));
                }

                final Set<EntityRef> createdEntityReferences = new HashSet<>();

                for (Map<String, Object> e : entitiesToCreate) {
                    final Set<EntityRef> createdEntities = createOrUpdate(configurationCode, customFieldTemplate.getEntityClazzCetCode(), e).getPersistedEntities();
                    createdEntityReferences.addAll(createdEntities);
                }

                List<String> uuids = getTrustedUuids(createdEntityReferences);

                // Replace with entity reference's UUID only when target is not primitive
                if (referencedCet.getNeo4JStorageConfiguration() == null || !referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {

                    if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
                        entityValues.put(customFieldTemplate.getCode(), uuids);
                    } else if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.SINGLE) {
                        entityValues.put(customFieldTemplate.getCode(), uuids.get(0));
                    }

                } else {
                    // If entity reference is primitive, place the UUID in the map along with the value
                    entityValues.put(customFieldTemplate.getCode() + "UUID", uuids.get(0));
                }
            }
        }
    }

    private String findUniqueRelationByTargetUuid(String configurationCode, String targetUuid, CustomRelationshipTemplate crt) {
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            return customTableRelationService.findIdOfUniqueRelationByTargetId(crt, targetUuid);
        }

        if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            return neo4jService.findIdOfUniqueRelationByTargetId(configurationCode, crt, targetUuid);
        }

        return null;
    }

    private String findIdOfSourceEntityByRelationId(String configurationCode, String relationUuid, CustomRelationshipTemplate crt) {
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            return customTableRelationService.findIdOfSourceEntityByRelationId(crt, relationUuid);
        }

        if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            return neo4jService.findIdOfUniqueRelationByTargetId(configurationCode, crt, relationUuid);
        }

        return null;
    }

    private void fetchEntityReferences(String configurationCode, CustomModelObject customModelObject, Map<String, Object> values) {
        // TODO: extract sub-entities fetch fields. Ex : "a.x". Fetch level by default is one.
        new HashSet<>(values.entrySet()).forEach(entry -> {
            CustomFieldTemplate cft = customFieldsCacheContainerProvider.getCustomFieldTemplate(entry.getKey(), customModelObject.getAppliesTo());
            if (cft != null && cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
                CustomEntityTemplate cet = customFieldsCacheContainerProvider.getCustomEntityTemplate(cft.getEntityClazzCetCode());
                Map<String, Object> refValues = find(configurationCode, cet, (String) entry.getValue());
                values.put(cft.getCode(), refValues);
            }
        });
    }

    private void replaceKeys(CustomEntityTemplate cet, List<String> sqlFields, Map<String, Object> customTableValue) {
        if (sqlFields != null && !sqlFields.isEmpty()) {
            List<CustomFieldTemplate> cfts = sqlFields.stream()
                    .map(field -> customFieldsCacheContainerProvider.getCustomFieldTemplate(field, cet.getAppliesTo()))
                    .collect(Collectors.toList());

            customTableService.replaceKeys(cfts, customTableValue);
        } else {
            final Collection<CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo()).values();
            customTableService.replaceKeys(cfts, customTableValue);
        }
    }

    private CustomEntityInstance getCustomEntityInstance(String entityCode, Map<String, Object> values) {
        String code = (String) values.get("code");

        if (code != null) {
            return customEntityInstanceService.findByCodeByCet(entityCode, code);
        }

        final List<CustomEntityInstance> list = customEntityInstanceService.list(entityCode, values);

        if(list.isEmpty()){
            return null;
        }else {
            throw new NonUniqueResultException(list.size() + " results found for CEI for CET " + entityCode + " with values " + values);
        }

    }

}
