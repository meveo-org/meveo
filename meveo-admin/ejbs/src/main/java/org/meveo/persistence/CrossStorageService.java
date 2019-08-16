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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.BusinessApiException;
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
import org.meveo.model.storage.Repository;
import org.meveo.persistence.neo4j.base.Neo4jDao;
import org.meveo.persistence.neo4j.service.Neo4jService;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomTableRelationService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.storage.FileSystemService;

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

    @Inject
    private FileSystemService fileSystemService;

    /**
     * Retrieves one entity instance
     *
     * @param repository Repository code
     * @param cet        Template of the entities to retrieve
     * @param uuid       UUID of the entity
     * @return list of matching entities
     */
    public Map<String, Object> find(Repository repository, CustomEntityTemplate cet, String uuid) {
        return find(repository, cet, uuid, null);
    }

    /**
     * Retrieves one entity instance
     *
     * @param repository  Repository code
     * @param cet         Template of the entities to retrieve
     * @param uuid        UUID of the entity
     * @param fetchFields Fields to select
     * @return list of matching entities
     */
    //TODO : add "fetchSubEntities" parameter
    public Map<String, Object> find(Repository repository, CustomEntityTemplate cet, String uuid, List<String> fetchFields) {
        if (uuid == null) {
            throw new NullPointerException("Cannot retrieve entity by uuid without uuid");
        }

        List<String> selectFields;
        Map<String, Object> values = new HashMap<>();

        // Retrieve only asked fields
        if(fetchFields != null){
            selectFields = new ArrayList<>(fetchFields);

        // No restrictions about fields - retrieve all fields
        } else {
            selectFields = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo())
                    .values()
                    .stream()
                    .map(CustomFieldTemplate::getCode)
                    .collect(Collectors.toList());
        }

        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            List<String> neo4jFields = filterFields(selectFields, cet, DBStorageType.NEO4J);
            if (!neo4jFields.isEmpty()) {
                values.putAll(neo4jDao.findNodeById(repository.getNeo4jConfiguration().getCode(), cet.getCode(), uuid, neo4jFields));
            }
        }

        // Don't retrieve the fields we already fetched
        selectFields.removeAll(values.keySet());

        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            List<String> sqlFields = filterFields(selectFields, cet, DBStorageType.SQL);
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                final Map<String, Object> customTableValue = customTableService.findById(cet, uuid, sqlFields);
                replaceKeys(cet, sqlFields, customTableValue);
                values.putAll(customTableValue);
            } else {
                final CustomEntityInstance cei = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
                if(cei == null) {
                	return null;
                }

                values.put("code", cei.getCode());
                values.put("description", cei.getDescription());
                if (sqlFields != null) {
                    for (String field : sqlFields) {
                        if (cei.getCfValues() != null && cei.getCfValues().getCfValue(field) != null) {
                            values.put(field, cei.getCfValues().getCfValue(field).getValue());
                        }
                    }
                } else {
                    if (cei.getCfValuesAsValues() != null) {
                        values.putAll(cei.getCfValuesAsValues());
                    }
                }

            }
        }

        // Remove null values
        values.values().removeIf(Objects::isNull);

        // Fetch entity references
        fetchEntityReferences(repository, cet, values);

        return values;
    }

    public Map<String, Object> getMissingData(Map<String, Object> data, Repository repository, CustomEntityTemplate cet, String uuid, Collection<String> selectFields) {
        List<String> actualFetchField = new ArrayList<>(selectFields);
        actualFetchField.removeAll(data.keySet());

        // Every field has been already retrieved
        if (actualFetchField.isEmpty()) {
            return new HashMap<>();
        }

        // Retrieve the missing fields
        return find(repository, cet, uuid, actualFetchField);
    }

    /**
     * Retrieves entity instances
     *
     * @param repository              Repository code
     * @param cet                     Template of the entities to retrieve
     * @param paginationConfiguration Pagination and filters
     * @return list of matching entities
     */
    public List<Map<String, Object>> find(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {

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
        if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.SQL) && !dontFetchSql && (fetchAllFields || hasSqlFetchField || hasSqlFilter)) {
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                final List<Map<String, Object>> values = customTableService.list(cet, paginationConfiguration);
                values.forEach(v -> replaceKeys(cet, actualFetchFields, v));
                valuesList.addAll(values);

            } else {
                final List<CustomEntityInstance> ceis = customEntityInstanceService.list(cet.getCode(), filters);
                final List<Map<String, Object>> values = new ArrayList<>();

                for (CustomEntityInstance cei : ceis) {
                    Map<String, Object> cfValuesAsValues = cei.getCfValuesAsValues();
                    final HashMap<String, Object> map = cfValuesAsValues == null ? new HashMap<>() : new HashMap<>(cfValuesAsValues);
                    map.put("uuid", cei.getUuid());
                    if (!fetchAllFields) {
                        for (String k : cei.getCfValuesAsValues().keySet()) {
                            if (!actualFetchFields.contains(k)) {
                                map.remove(k);
                            }
                        }
                    }
                    values.add(map);
                }

                valuesList.addAll(values);
            }
        }

        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {

            // Find by graphql if query provided
            if (paginationConfiguration != null && paginationConfiguration.getGraphQlQuery() != null) {

                String graphQlQuery = paginationConfiguration.getGraphQlQuery()
                        .replaceAll("([\\w)]\\s*\\{)(\\s*\\w*)", "$1meveo_uuid,$2");

                final Map<String, Object> result = neo4jDao.executeGraphQLQuery(
                        repository.getNeo4jConfiguration().getCode(),
                        graphQlQuery,
                        null,
                        null
                );

                final List<Map<String, Object>> values = (List<Map<String, Object>>) result.get(cet.getCode());
                values.forEach(map -> {
                    final HashMap<String, Object> resultMap = new HashMap<>(map);
                    map.entrySet().forEach(entry -> {
                        if (!entry.getKey().equals("uuid") && !entry.getKey().equals("meveo_uuid") && actualFetchFields != null && !actualFetchFields.contains(entry)) {
                            resultMap.remove(entry);
                        }

                        // Flatten primitive types and Binary values (singleton maps with only "value" attribute)
                        if(entry.getValue() instanceof Map && ((Map) entry.getValue()).size() == 1 && ((Map) entry.getValue()).containsKey("value")){
                            Object value = ((Map) entry.getValue()).get("value");
                            map.put(entry.getKey(), value);
                        }
                    });

                    valuesList.add(resultMap);
                });
            } else {
            	//TODO: retrieve every records ?
            }

        }

        // Complete missing data
        valuesList.forEach(data -> {
            String uuid = (String) (data.get("uuid") != null ? data.get("uuid") : data.get("meveo_uuid"));

            Collection<String> fetchFields = actualFetchFields != null
                    ? actualFetchFields
                    : customFieldTemplateService.findByAppliesTo(cet.getAppliesTo()).keySet();

            final Map<String, Object> missingData = getMissingData(data, repository, cet, uuid, fetchFields);
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
     * @param repository   Code of the repository / configuration to store the data.
     *                     <br> NOTE : only available for NEO4J at the moment.
     * @param relationCode Code of the relation to create
     * @param sourceValues Values to insert
     * @param targetValues Filters on target entity
     */
    @Override
    public void addSourceEntityUniqueCrt(Repository repository, String relationCode, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException, IOException, BusinessApiException {
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
                    repository.getNeo4jConfiguration().getCode(),
                    relationCode,
                    filterValues(sourceValues, crt, DBStorageType.NEO4J),
                    filterValues(targetValues, crt, DBStorageType.NEO4J)
            );

            return;
        }

        String targetUUUID = findEntityId(repository, targetValues, endNode);

        // Target does not exists. We create the source
        if (targetUUUID == null) {
            createOrUpdate(repository, startNode.getCode(), sourceValues);

        } else {
            // Target exists. Let's check if the relation exist.
            final String relationUUID = findUniqueRelationByTargetUuid(repository, targetUUUID, crt);

            // Relation does not exists. We create the source.
            if (relationUUID == null) {
                createOrUpdate(repository, startNode.getCode(), sourceValues);

            } else {
                // Relation exists. We update the source node.
                String sourceUUID = findIdOfSourceEntityByRelationId(repository, relationUUID, crt);
                update(repository, startNode, sourceValues, sourceUUID);
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
     * @param repository Code of the repository / configuration to store the data.
     *                   <br> NOTE : only available for NEO4J at the moment.
     * @param entityCode Code of the entity to create
     * @param values     Values of the entity
     * @return the persisted entites
     */
    @Override
    public PersistenceActionResult createOrUpdate(Repository repository, String entityCode, Map<String, Object> values) throws BusinessException, IOException, BusinessApiException {

        Map<String, Object> entityValues = new HashMap<>(values);

        CustomEntityTemplate cet = customFieldsCacheContainerProvider.getCustomEntityTemplate(entityCode);
        final Map<String, CustomFieldTemplate> customFieldTemplates = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo());

        createEntityReferences(repository, entityValues, cet);

        Set<EntityRef> persistedEntities = new HashSet<>();

        String uuid = createOrUpdateNeo4J(repository, entityValues, cet, customFieldTemplates, persistedEntities);

        // SQL Storage
        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            Map<String, Object> sqlValues = filterValues(entityValues, cet, DBStorageType.SQL);
            if (!sqlValues.isEmpty()) {
                // Update binaries stored in SQL
                List<CustomFieldTemplate> binariesInSql = customFieldTemplates.values().stream()
                        .filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY))
        	            .filter(f -> f.getStorages().contains(DBStorageType.SQL))
                        .collect(Collectors.toList());

                if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                    uuid = createOrUpdateSQL(repository, cet, binariesInSql, uuid, sqlValues);
                } else {
                    uuid = createOrUpdateCei(repository, cet, sqlValues, (String) entityValues.get("code"), uuid, binariesInSql);
                }

                persistedEntities.add(new EntityRef(uuid, cet.getCode()));
            }
        }

        return new PersistenceActionResult(persistedEntities, uuid);
    }

    private String createOrUpdateCei(Repository repository, CustomEntityTemplate cet, Map<String, Object> values, String code, String uuid, Collection<CustomFieldTemplate> binariesInSql) throws BusinessException, IOException, BusinessApiException {
        CustomEntityInstance cei = getCustomEntityInstance(cet.getCode(), values);

        if (cei == null) {
            cei = new CustomEntityInstance();
            cei.setCetCode(cet.getCode());
            cei.setCode(code);

            if (uuid != null) {
                cei.setUuid(uuid);
            }

            if (CollectionUtils.isNotEmpty(binariesInSql)) {
            	fileSystemService.updateBinaries(repository, cei.getUuid(), cet, binariesInSql, values, Collections.EMPTY_MAP);
            }

            CustomFieldValues customFieldValues = new CustomFieldValues();
            values.forEach(customFieldValues::setValue);
            cei.setCfValues(customFieldValues);

            customEntityInstanceService.create(cei);


        } else {

            if (CollectionUtils.isNotEmpty(binariesInSql)) {
                final Map<String, Object> existingValues = cei.getCfValuesAsValues();
                fileSystemService.updateBinaries(repository, cei.getUuid(), cet, binariesInSql, values, existingValues);
            }

            CustomFieldValues customFieldValues = new CustomFieldValues();
            values.forEach(customFieldValues::setValue);
            cei.setCfValues(customFieldValues);

            customEntityInstanceService.update(cei);
        }

        uuid = cei.getUuid();
        return uuid;
    }

    private String createOrUpdateNeo4J(Repository repository, Map<String, Object> entityValues, CustomEntityTemplate cet, Map<String, CustomFieldTemplate> customFieldTemplates, Set<EntityRef> persistedEntities) throws IOException, BusinessException, BusinessApiException {
        String uuid = null;

        // Neo4j storage
        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {

            Map<String, Object> neo4jValues = filterValues(entityValues, cet, DBStorageType.NEO4J);
            if (!neo4jValues.isEmpty()) {
                final Set<EntityRef> entityRefs = neo4jService.addCetNode(repository.getNeo4jConfiguration().getCode(), cet, neo4jValues);
                //TODO: check if created or updated
                uuid = getTrustedUuids(entityRefs).get(0);
                if (uuid == null) {
                    throw new NullPointerException("Generated UUID from Neo4J cannot be null");
                }

                // Update binaries stored in Neo4j
                updateNeo4jBinaries(repository, cet, customFieldTemplates, uuid, neo4jValues);

                persistedEntities.addAll(entityRefs);
            }
        }

        return uuid;
    }

	/**
	 * TODO: Document
	 */
	public void updateNeo4jBinaries(Repository repository, CustomEntityTemplate cet, Map<String, CustomFieldTemplate> customFieldTemplates, String uuid, Map<String, Object> neo4jValues) throws IOException, BusinessException, BusinessApiException {
        List<CustomFieldTemplate> binariesInNeo4J = customFieldTemplates.values().stream()
	            .filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY))
	            .filter(f -> f.getStorages().contains(DBStorageType.NEO4J))
	            .collect(Collectors.toList());

        String neo4JCode = repository.getNeo4jConfiguration().getCode();

		if(!CollectionUtils.isEmpty(binariesInNeo4J)) {

            Map<String, Object> existingBinaries = new HashMap<>();

			for(CustomFieldTemplate neo4jField : binariesInNeo4J) {
		    	// Retrieve binaries
		    	List<String> binaries = neo4jService.findBinaries(
		    			uuid,
		    			repository.getNeo4jConfiguration().getCode(),
		    			cet,
		    			neo4jField
				);

		    	if(CollectionUtils.isEmpty(binaries)) {
		    		continue;
		    	}

		    	if(neo4jField.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE)) {
		    		existingBinaries.put(neo4jField.getCode(), binaries.get(0));
		    	} else if(neo4jField.getStorageType().equals(CustomFieldStorageTypeEnum.LIST)) {
		    		existingBinaries.put(neo4jField.getCode(), binaries);
		    	}

			}

			// Persist binaries in file system
		    final Map<CustomFieldTemplate, Object> binariesByCft = fileSystemService.updateBinaries(
		            repository,
		            uuid,
		            cet,
		            customFieldTemplates.values(),
		            neo4jValues,
		            existingBinaries
		    );

		    // Handle binaries references stored in Neo4J
		    for (Map.Entry<CustomFieldTemplate, Object> binary : binariesByCft.entrySet()) {
		        if (binary.getValue() instanceof String) {
		            neo4jService.updateBinary(uuid,neo4JCode,cet,binary.getKey(), (String) binary.getValue());

		        } else if (binary.getValue() instanceof Collection) {
		        	// Delete binaries present in previous values and not in persisted values
		        	List<String> previousBinaries = (List<String>) existingBinaries.get(binary.getKey().getCode());
		        	if(previousBinaries != null) {
		        		for(String previousBinary : previousBinaries) {
		        			// Check if existing files were deleted and remove them from neo4j if they were
			        		if(!new File(previousBinary).exists()) {
			        			neo4jService.removeBinary(uuid, neo4JCode, cet, binary.getKey(), previousBinary);
			        		}
		        		}
		        	}


		        	// Add or update remaining binaries
		            neo4jService.addBinaries(uuid, neo4JCode, cet, binary.getKey(), (Collection<String>) binary.getValue());

		        // All binaries were deleted
		        } else if(binary.getValue() == null) {
		        	neo4jService.removeBinaries(uuid, repository.getNeo4jConfiguration().getCode(), cet, binary.getKey());
		        }
		    }

		}
	}

    private String createOrUpdateSQL(Repository repository, CustomEntityTemplate cet, Collection<CustomFieldTemplate> binariesInSql, String uuid, Map<String, Object> sqlValues) throws BusinessException, IOException, BusinessApiException {
        String tableName = SQLStorageConfiguration.getDbTablename(cet);

        //TODO: Find by UUID if updated in Neo4J and throw error if not found
        String sqlUUID = customTableService.findIdByValues(tableName, sqlValues);

        if (sqlUUID != null) {
            sqlValues.put("uuid", sqlUUID);

            // Update binaries
            if (CollectionUtils.isNotEmpty(binariesInSql)) {
                List<String> binariesFieldsToFetch = binariesInSql.stream()
                        .map(CustomFieldTemplate::getCode)
                        .collect(Collectors.toList());

                Map<String, Object> existingBinariesField = customTableService.findById(cet, sqlUUID, binariesFieldsToFetch);
                fileSystemService.updateBinaries(
                        repository,
                        uuid,
                        cet,
                        binariesInSql,
                        sqlValues,
                        existingBinariesField
                );

            }

            customTableService.update(cet, sqlValues);
            uuid = sqlUUID;

        } else {
            if (uuid != null) {
                sqlValues.put("uuid", uuid);
            }

            uuid = customTableService.create(cet, sqlValues);

            // Save binaries
            if (CollectionUtils.isNotEmpty(binariesInSql)) {

                final Map<CustomFieldTemplate, Object> binariesPaths = fileSystemService.updateBinaries(
                        repository,
                        uuid,
                        cet,
                        binariesInSql,
                        sqlValues,
                        null
                );

                for (Map.Entry<CustomFieldTemplate, Object> binary : binariesPaths.entrySet()) {
                    customTableService.updateValue(
                            SQLStorageConfiguration.getDbTablename(cet),
                            uuid,
                            binary.getKey().getDbFieldname(),
                            binary.getValue()
                    );
                }
            }

        }

        return uuid;
    }

    /**
     * Update an entity instance
     *
     * @param repository Repository code
     * @param cet        Template of the entity to update
     * @param values     New values to assign to the entity
     * @param uuid       UUID identifying the entity to update
     */
    public void update(Repository repository, CustomEntityTemplate cet, Map<String, Object> values, String uuid) throws BusinessException, IOException, BusinessApiException {

    	Map<String, CustomFieldTemplate> customFieldTemplates = customFieldsCacheContainerProvider.getCustomFieldTemplates(cet.getAppliesTo());

        // Neo4j storage
        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            Map<String, Object> neo4jValues = filterValues(values, cet, DBStorageType.NEO4J);

            final List<String> cetLabels = cet.getNeo4JStorageConfiguration().getLabels() != null ? cet.getNeo4JStorageConfiguration().getLabels() : new ArrayList<>();
            List<String> labels = new ArrayList<>(cetLabels);
            labels.add(cet.getCode());

            updateNeo4jBinaries(repository, cet, customFieldTemplates, uuid, neo4jValues);

            neo4jDao.updateNodeByNodeId(repository.getNeo4jConfiguration().getCode(), uuid, cet.getCode(), neo4jValues, labels);

            //TODO: Delete binaries that were removed
        }

        // SQL Storage
        if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
        	List<CustomFieldTemplate> binariesInSql = customFieldTemplates.values().stream()
	            .filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY))
	            .filter(f -> f.getStorages().contains(DBStorageType.SQL))
	            .collect(Collectors.toList());

            // Custom table
            if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
                Map<String, Object> sqlValues = filterValues(values, cet, DBStorageType.SQL);

                if(!CollectionUtils.isEmpty(binariesInSql)) {
                    List<String> binariesFieldsToFetch = binariesInSql.stream()
                            .map(CustomFieldTemplate::getCode)
                            .collect(Collectors.toList());

                    final Map<String, Object> existingBinariesFields = customTableService.findById(cet, uuid, binariesFieldsToFetch);
                    fileSystemService.updateBinaries(
                            repository,
                            uuid,
                            cet,
                            binariesInSql,
                            sqlValues,
                            existingBinariesFields
                    );
                }


                sqlValues.put("uuid", uuid);
                customTableService.update(cet, sqlValues);
            } else {
                // CEI storage
                final CustomEntityInstance cei = customEntityInstanceService.findByUuid(cet.getCode(), uuid);

                // Update binaries
                if (CollectionUtils.isNotEmpty(binariesInSql)) {
                    final Map<String, Object> existingValues = cei.getCfValuesAsValues();
                    fileSystemService.updateBinaries(repository, cei.getUuid(), cet, binariesInSql, values, existingValues);
                }

                CustomFieldValues customFieldValues = new CustomFieldValues();
                values.forEach(customFieldValues::setValue);
                cei.setCfValues(customFieldValues);
                customEntityInstanceService.update(cei);
            }
        }
    }

    @Override
    public void addCRTByValues(Repository repository, String relationCode, Map<String, Object> relationValues, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException {
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
                    repository.getNeo4jConfiguration().getCode(),
                    relationCode,
                    filterValues(relationValues, crt, DBStorageType.NEO4J),
                    filterValues(sourceValues, crt, DBStorageType.NEO4J),
                    filterValues(targetValues, crt, DBStorageType.NEO4J));
        }

        String sourceUUID = findEntityId(repository, sourceValues, startNode);
        String targetUUUID = findEntityId(repository, sourceValues, endNode);

        // SQL Storage
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            //TODO: Check if entity references placed in values maps does not interfer ...
            customTableRelationService.createRelation(crt, sourceUUID, targetUUUID, relationValues);
        }

        // Neo4J Storage
        if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jService.addCRTByNodeIds(repository.getNeo4jConfiguration().getCode(), crt.getCode(), relationValues, sourceUUID, targetUUUID);
        }

    }

    @Override
    public void addCRTByUuids(Repository repository, String relationCode, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws ELException, BusinessException {
        CustomRelationshipTemplate crt = customFieldsCacheContainerProvider.getCustomRelationshipTemplate(relationCode);

        // All neo4j storage
        if (isEverythingStoredInNeo4J(crt)) {
            neo4jService.addCRTByNodeIds(
                    repository.getNeo4jConfiguration().getCode(),
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
            neo4jService.addCRTByNodeIds(repository.getNeo4jConfiguration().getCode(), crt.getCode(), relationValues, sourceUuid, targetUuid);
        }
    }

    /**
     * Find an entity instance UUID
     *
     * @param repository    Repository to search in
     * @param valuesFilters Filter on entity's values
     * @param cet           Template of the entity
     * @return UUID of the entity or nul if it was'nt found
     */
    public String findEntityId(Repository repository, Map<String, Object> valuesFilters, CustomEntityTemplate cet) {
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
            uuid = neo4jDao.findNodeId(repository.getNeo4jConfiguration().getCode(), cet.getCode(), filterValues(valuesFilters, cet, DBStorageType.NEO4J));
        }

        return uuid;
    }

    /**
     * Remove an entity from database
     *
     * @param repositoryCode Repository
     * @param cet            Template of the entity
     * @param uuid           UUID of the entity
     */
    public void remove(String repositoryCode, CustomEntityTemplate cet, String uuid) throws BusinessException {
        if (uuid == null) {
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

//        if (cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
//            neo4jDao.removeNode(repositoryCode, cet.getCode(), uuid);
//        }
    }

    /**
     * Remove a relation from database
     *
     * @param repositoryCode Repository
     * @param crt            Template of the relation
     * @param uuid           UUID of the relation
     */
    public void remove(String repositoryCode, CustomRelationshipTemplate crt, String uuid) throws BusinessException {
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            final String dbTablename = SQLStorageConfiguration.getDbTablename(crt);
            customTableRelationService.remove(dbTablename, uuid);
        }

        if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jDao.removeRelation(repositoryCode, crt.getCode(), uuid);
        }
    }

    private Map<String, Object> filterValues(Map<String, Object> values, CustomModelObject cet, DBStorageType storageType) {
    	Map<String, Object> filteredValues = new HashMap<>();

        values.entrySet()
	        .stream()
	        .filter(entry -> {
	            // Always include UUID
	            if (entry.getKey().equals("uuid")) {
	                return true;
	            }

	            // For CEI storage, always include code
	            if (cet instanceof CustomEntityTemplate && entry.getKey().equals("code")) {
	                if (storageType == DBStorageType.SQL && !((CustomEntityTemplate) cet).getSqlStorageConfiguration().isStoreAsTable()) {
	                    return true;
	                }
	            }

	            CustomFieldTemplate cft = customFieldsCacheContainerProvider.getCustomFieldTemplate(entry.getKey(), cet.getAppliesTo());

	            if (cft == null) {
	                return false;
	            }

	            return cft.getStorages().contains(storageType);
	        }).forEach(v -> filteredValues.put(v.getKey(), v.getValue()));

        return filteredValues;
    }

    private List<String> filterFields(List<String> fields, CustomModelObject cet, DBStorageType storageType) {
    	// If fields are null return all avaiblable fields for the given storage
        if (fields == null) {
            return new ArrayList<>();
        }

        return fields.stream()
                .filter(entry -> {
                    CustomFieldTemplate cft = customFieldsCacheContainerProvider.getCustomFieldTemplate(entry, cet.getAppliesTo());
                    if(cft == null) {
                    	return false;
                    }

                    return cft.getStorages().contains(storageType);
                }).collect(Collectors.toList());
    }

    private void createEntityReferences(Repository repository, Map<String, Object> entityValues, CustomEntityTemplate cet) throws BusinessException, IOException, BusinessApiException {
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
                    final Set<EntityRef> createdEntities = createOrUpdate(repository, customFieldTemplate.getEntityClazzCetCode(), e).getPersistedEntities();
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

    private String findUniqueRelationByTargetUuid(Repository repository, String targetUuid, CustomRelationshipTemplate crt) {
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            return customTableRelationService.findIdOfUniqueRelationByTargetId(crt, targetUuid);
        }

        if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            return neo4jService.findIdOfUniqueRelationByTargetId(repository.getNeo4jConfiguration().getCode(), crt, targetUuid);
        }

        return null;
    }

    private String findIdOfSourceEntityByRelationId(Repository repository, String relationUuid, CustomRelationshipTemplate crt) {
        if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
            return customTableRelationService.findIdOfSourceEntityByRelationId(crt, relationUuid);
        }

        if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            return neo4jService.findIdOfUniqueRelationByTargetId(repository.getNeo4jConfiguration().getCode(), crt, relationUuid);
        }

        return null;
    }

    private void fetchEntityReferences(Repository repository, CustomModelObject customModelObject, Map<String, Object> values) {
        // TODO: extract sub-entities fetch fields. Ex : "a.x". Fetch level by default is one.
        new HashSet<>(values.entrySet()).forEach(entry -> {
            CustomFieldTemplate cft = customFieldsCacheContainerProvider.getCustomFieldTemplate(entry.getKey(), customModelObject.getAppliesTo());
            if (cft != null && cft.getFieldType() == CustomFieldTypeEnum.ENTITY && cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE) {
                CustomEntityTemplate cet = customFieldsCacheContainerProvider.getCustomEntityTemplate(cft.getEntityClazzCetCode());
                Map<String, Object> refValues = find(repository, cet, (String) entry.getValue());
                values.put(cft.getCode(), refValues);
            }
            //TODO: fetch list of referefences
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

    private CustomEntityInstance getCustomEntityInstance(String cetCode, Map<String, Object> values) {
        String code = (String) values.get("code");

        if (code != null) {
            return customEntityInstanceService.findByCodeByCet(cetCode, code);
        }

        final List<CustomEntityInstance> list = customEntityInstanceService.list(cetCode, values);

        if (list.isEmpty()) {
            return null;
        } else {
            throw new NonUniqueResultException(list.size() + " results found for CEI for CET " + cetCode + " with values " + values);
        }

    }



}
