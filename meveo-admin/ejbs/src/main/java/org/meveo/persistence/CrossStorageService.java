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
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomTableRelationService;
import org.meveo.service.custom.CustomTableService;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class CrossStorageService {

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
    public PersistenceActionResult createOrUpdate(String configurationCode, String entityCode, Map<String, Object> values) throws BusinessException {

        Map<String, Object> entityValues = new HashMap<>(values);

        CustomEntityTemplate cet = customFieldsCacheContainerProvider.getCustomEntityTemplate(entityCode);
        createEntityReferences(configurationCode, entityValues, cet);

        Set<EntityRef> persistedEntities = new HashSet<>();

        String uuid = null;

        // Neo4j storage
        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            Map<String, Object> neo4jValues = filterValues(entityValues, cet, DBStorageType.NEO4J);
            final Set<EntityRef> entityRefs = neo4jService.addCetNode(configurationCode, cet, neo4jValues);
            uuid = getTrustedUuids(entityRefs).get(0);
            persistedEntities.addAll(entityRefs);
        }

        // SQL Storage
        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            Map<String, Object> sqlValues = filterValues(entityValues, cet, DBStorageType.SQL);
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                String tableName = SQLStorageConfiguration.getDbTablename(cet);
                uuid = customTableService.findIdByValues(tableName, sqlValues);
                if (uuid != null) {
                    sqlValues.put("uuid", uuid);
                    customTableService.update(tableName, sqlValues);
                } else {
                    uuid = customTableService.create(tableName, sqlValues);
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

                    customEntityInstanceService.create(cei);
                } else {
                    cei.setCfValues(customFieldValues);
                    customEntityInstanceService.update(cei);
                }

                persistedEntities.add(new EntityRef(cei.getUuid()));
                uuid = cei.getUuid();
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
            neo4jDao.updateNodeByNodeId(configurationCode, uuid, neo4jValues);
        }

        // SQL Storage
        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            Map<String, Object> sqlValues = filterValues(values, cet, DBStorageType.SQL);
            sqlValues.put("uuid", uuid);
            customTableService.update(
                    SQLStorageConfiguration.getDbTablename(cet),
                    sqlValues
            );
        }
    }

    private CustomEntityInstance getCustomEntityInstance(String entityCode, Map<String, Object> values) throws BusinessException {
        String code = (String) values.get("code");
        if (code == null) {
            throw new BusinessException("Code is missing so we can't find CEI with values " + values);
        }

        return customEntityInstanceService.findByCodeByCet(entityCode, code);
    }

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

            //TODO: Case where target or source is not in neo4j => create method findNodeId
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
    public String findEntityId(String configurationCode, Map<String, Object> valuesFilters, CustomEntityTemplate cet) throws BusinessException {
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
     * @param cet   Template of the entity
     * @param uuid UUID of the entity
     */
    public void remove(String configurationCode, CustomEntityTemplate cet, String uuid) throws BusinessException {
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
     * @param crt   Template of the relation
     * @param uuid UUID of the relation
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
                    CustomFieldTemplate cft = customFieldsCacheContainerProvider.getCustomFieldTemplate(entry.getKey(), cet.getAppliesTo());
                    return cft.getStorages().contains(storageType);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
                if (!referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {

                    if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
                        entityValues.put(customFieldTemplate.getCode(), uuids);
                    } else if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.SINGLE) {
                        entityValues.put(customFieldTemplate.getCode(), uuids.get(0));
                    }

                    // If entity reference is primitive, place the UUID in the map
                } else {
                    entityValues.put(customFieldTemplate.getCode() + "UUID", uuids.get(0));
                }
            }
        }
    }

    private List<String> getTrustedUuids(Set<EntityRef> createdEntityReferences) {
        return createdEntityReferences.stream()
                .filter(EntityRef::isTrusted)
                .map(EntityRef::getUuid)
                .collect(Collectors.toList());
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

}
