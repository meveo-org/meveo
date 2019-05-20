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
     * <br> If the target entity and the relation does not exist, create them along with the source entity.
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

        // Neo4j storage
        if (
                crt.getAvailableStorages().contains(DBStorageType.NEO4J)
                        && crt.getStartNode().getAvailableStorages().contains(DBStorageType.NEO4J)
                        && crt.getEndNode().getAvailableStorages().contains(DBStorageType.NEO4J)
        ) {
            neo4jService.addSourceNodeUniqueCrt(
                    configurationCode,
                    relationCode,
                    filterValues(sourceValues, crt, DBStorageType.NEO4J),
                    filterValues(targetValues, crt, DBStorageType.NEO4J));
        }

        // SQL Storage
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            //TODO: Check if entity references placed in values maps does not interfer ...
            final String dbTablename = SQLStorageConfiguration.getDbTablename(crt);
            final String sourceUUID = customTableRelationService.findIdByValues(dbTablename, sourceValues);
            final String targetUUUID = customTableRelationService.findIdByValues(dbTablename, targetValues);
//            customTableRelationService.createRelation(crt, sourceUUID, targetUUUID, relationValues);
        }


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
    public Set<EntityRef> createOrUpdate(String configurationCode, String entityCode, Map<String, Object> values) throws BusinessException {

        Map<String, Object> entityValues = new HashMap<>(values);

        CustomEntityTemplate cet = customFieldsCacheContainerProvider.getCustomEntityTemplate(entityCode);
        createEntityReferences(configurationCode, entityValues, cet);

        Set<EntityRef> persistedEntities = new HashSet<>();

        // Neo4j storage
        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            Map<String, Object> neo4jValues = filterValues(entityValues, cet, DBStorageType.NEO4J);
            persistedEntities.addAll(neo4jService.addCetNode(configurationCode, cet, neo4jValues));
        }

        // SQL Storage
        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            Map<String, Object> sqlValues = filterValues(entityValues, cet, DBStorageType.SQL);
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                String tableName = SQLStorageConfiguration.getDbTablename(cet);
                String uuid = customTableService.findIdByValues(tableName, sqlValues);
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
            }
        }

        return persistedEntities;


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

        // Neo4j storage
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


        // SQL Storage
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            //TODO: Check if entity references placed in values maps does not interfer ...
            String sourceUUID = null, targetUUUID = null;

            // Look in SQL
            if(startNode.getAvailableStorages().contains(DBStorageType.SQL)){
                // Custom table
                if(startNode.getSqlStorageConfiguration().isStoreAsTable()){
                    final String dbTablename = SQLStorageConfiguration.getDbTablename(startNode);
                    sourceUUID = customTableRelationService.findIdByValues(dbTablename, filterValues(sourceValues, crt, DBStorageType.SQL));
                }else{
                    final CustomEntityInstance customEntityInstance = getCustomEntityInstance(startNode.getCode(), sourceValues);
                    if(customEntityInstance != null){
                        sourceUUID = customEntityInstance.getUuid();
                    }
                }
            }

            if(sourceUUID == null && startNode.getAvailableStorages().contains(DBStorageType.NEO4J)){
                sourceUUID = neo4jDao.findNodeId(configurationCode, startNode.getCode(), filterValues(sourceValues, crt, DBStorageType.NEO4J));
            }

            if(endNode.getAvailableStorages().contains(DBStorageType.SQL)){
                if(endNode.getSqlStorageConfiguration().isStoreAsTable()) {
                    final String dbTablename = SQLStorageConfiguration.getDbTablename(endNode);
                    targetUUUID = customTableRelationService.findIdByValues(dbTablename, filterValues(sourceValues, crt, DBStorageType.SQL));
                }else{
                    final CustomEntityInstance customEntityInstance = getCustomEntityInstance(endNode.getCode(), sourceValues);
                    if(customEntityInstance != null){
                        sourceUUID = customEntityInstance.getUuid();
                    }
                }

                //TODO: in CEI
            }

            if(targetUUUID == null && endNode.getAvailableStorages().contains(DBStorageType.NEO4J)){
                targetUUUID = neo4jDao.findNodeId(configurationCode, endNode.getCode(), filterValues(sourceValues, crt, DBStorageType.NEO4J));
            }

            customTableRelationService.createRelation(crt, sourceUUID, targetUUUID, relationValues);
        }


    }

    public void addCRTByUuids(String configurationCode, String relationCode, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws ELException, BusinessException {
        CustomRelationshipTemplate crt = customFieldsCacheContainerProvider.getCustomRelationshipTemplate(relationCode);

        // All neo4j storage
        if (
                crt.getAvailableStorages().contains(DBStorageType.NEO4J)
                        && crt.getStartNode().getAvailableStorages().contains(DBStorageType.NEO4J)
                        && crt.getEndNode().getAvailableStorages().contains(DBStorageType.NEO4J)
        ) {
            neo4jService.addCRTByNodeIds(
                    configurationCode,
                    relationCode,
                    filterValues(relationValues, crt, DBStorageType.NEO4J),
                    sourceUuid,
                    targetUuid);
        }

        // SQL Storage
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            //TODO: Check if entity references placed in values maps does not interfer ...
            customTableRelationService.createRelation(crt, sourceUuid, targetUuid, relationValues);
        }

        // TODO: Neo4j storage
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
                    final Set<EntityRef> createdEntities = createOrUpdate(configurationCode, customFieldTemplate.getEntityClazzCetCode(), e);
                    createdEntities.stream()
                            .filter(EntityRef::isTrusted)
                            .findFirst()
                            .ifPresent(createdEntityReferences::add);
                }

                List<String> uuids = createdEntityReferences.stream()
                        .map(EntityRef::getUuid)
                        .collect(Collectors.toList());

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

}
