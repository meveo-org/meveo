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

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.IllegalTransitionException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.elresolver.ELException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.CustomEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.impl.SQLStorageImpl;
import org.meveo.persistence.neo4j.service.Neo4jService;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.security.PasswordUtils;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomTableRelationService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.storage.FileSystemService;
import org.meveo.util.PersistenceUtils;
import org.slf4j.Logger;


/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author clement.bareth
 * @version 6.11.0
 */
public class CrossStorageService implements CustomPersistenceService {

	@Inject
	private CrossStorageTransaction transaction;

	@Inject
	private CustomFieldsCacheContainerProvider cache;

	@Inject
	private CustomTableService customTableService;

	@Inject
	private CustomEntityInstanceService customEntityInstanceService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	@Inject
	private FileSystemService fileSystemService;

	@Inject
	private CustomFieldInstanceService customFieldInstanceService;

	@Inject
	private Logger log;
	
	@Inject
	private CustomEntityTemplateService customEntityTemplateService;
	
	@Inject
	private Neo4jService neo4jService;
	
    @Inject
    @Updated
    private Event<CustomEntityInstance> customEntityInstanceUpdate;
    
    @Inject
    @Created
    private Event<CustomEntityInstance> customEntityInstanceCreate;
    
    @Inject
    @Removed
    private Event<CustomEntityInstance> customEntityInstanceDelete;
    
	@Inject
	private CustomTableRelationService customTableRelationService;
	
	@Inject
	private StorageImplProvider provider;
	
	@Inject
	private SQLStorageImpl sqlStorageImpl;

	/**
	 * Retrieves one entity instance
	 *
	 * @param repository     Repository code
	 * @param cet            Template of the entities to retrieve
	 * @param uuid           UUID of the entity
	 * @param withReferences Whether to fetch entity references
	 * @return list of matching entities
	 * @throws EntityDoesNotExistsException if entity can't be found
	 */
	public Map<String, Object> find(Repository repository, CustomEntityTemplate cet, String uuid, boolean withReferences) throws EntityDoesNotExistsException {
		return findById(repository, cet, uuid, null, new HashMap<>(), true);
	}
	
	public Map<String, Object> find(Repository repository, CustomEntityTemplate cet, String uuid, Collection<String> fetchFields, boolean withEntityReferences) throws EntityDoesNotExistsException {
		return findById(repository, cet, uuid, fetchFields, new HashMap<>(), withEntityReferences);
	}

	/**
	 * Retrieves one entity instance
	 *
	 * @param repository           Repository code
	 * @param cet                  Template of the entities to retrieve
	 * @param uuid                 UUID of the entity
	 * @param fetchFields          Fields to select
	 * @param withEntityReferences Whether to fetch entity references
	 * @return list of matching entities
	 * @throws EntityDoesNotExistsException if entity does not exist
	 */
	public Map<String, Object> findById(Repository repository, CustomEntityTemplate cet, String uuid, Collection<String> fetchFields, Map<String, Set<String>> subFields, boolean withEntityReferences) throws EntityDoesNotExistsException {
		if (uuid == null) {
			throw new IllegalArgumentException("Cannot retrieve entity by uuid without uuid");
		}

		if (cet == null) {
			throw new IllegalArgumentException("CET should be provided");
		}

		List<String> selectFields;
		Map<String, Object> values = new HashMap<>();
		values.put("uuid", uuid);
		boolean foudEntity=false;
		
		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.getCftsWithInheritedFields(cet);

		// Retrieve only asked fields
		if (fetchFields != null && !fetchFields.isEmpty()) {
			selectFields = new ArrayList<>(fetchFields);

		// No restrictions about fields - retrieve all fields
		} else {
			selectFields = cfts.values().stream().map(CustomFieldTemplate::getCode).collect(Collectors.toList());
		}
		
		for (var storage : cet.getAvailableStorages()) {
			Map<String, Object> storageValues = provider.findImplementation(storage) 
					.findById(repository, cet, uuid, cfts, selectFields, withEntityReferences);
			
			if (storageValues != null) {
				foudEntity = true;
				values.putAll(storageValues);
			}
			
			// Don't retrieve the fields we already fetched
			selectFields.removeAll(values.keySet());
		}

		if(!foudEntity){
			throw new EntityDoesNotExistsException(cet.getCode(),uuid);
		}
		
		// Remove null values
		values.values().removeIf(Objects::isNull);

		// Fetch entity references
		fetchEntityReferences(repository, cet, values, subFields);

		values = deserializeData(values, cfts.values());
		
		return values;
	}

	/**
	 * Completed the data spread across multiple storage
	 * 
	 * @param data the actual data
	 * @param repository the repository to get data from
	 * @param cet data type
	 * @param uuid id of the record
	 * @param selectFields the field to fetch
	 * @return the data completed
	 * @throws EntityDoesNotExistsException if data with given id can't be found
	 */
	public Map<String, Object> getMissingData(Map<String, Object> data, Repository repository, CustomEntityTemplate cet, String uuid, Collection<String> selectFields, Map<String, Set<String>> subFields) throws EntityDoesNotExistsException {
		List<String> actualFetchField = new ArrayList<>(selectFields);
		actualFetchField.removeIf(entry -> data.keySet().contains(entry) && !subFields.keySet().contains(entry));

		// Every field has been already retrieved
		if (actualFetchField.isEmpty()) {
			return new HashMap<>();
		}

		// Retrieve the missing fields
		return findById(repository, cet, uuid, actualFetchField, subFields, false);
	}

	/**
	 * Retrieves entity instances
	 *
	 * @param repository              Repository code
	 * @param cet                     Template of the entities to retrieve
	 * @param paginationConfiguration Pagination and filters
	 * @return list of matching entities
	 * @throws EntityDoesNotExistsException if data with given id can't be found
	 */
	public List<Map<String, Object>> find(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) throws EntityDoesNotExistsException {
		final Map<String, CustomFieldTemplate> fields = customFieldTemplateService.getCftsWithInheritedFields(cet);

		final Set<String> actualFetchFields;
		// If no pagination nor fetch fields are defined, we consider that we must fetch everything
		boolean fetchAllFields = paginationConfiguration == null || paginationConfiguration.getFetchFields() == null || paginationConfiguration.getFetchFields().isEmpty();
		if(fetchAllFields) {
			actualFetchFields = new HashSet<>(fields.keySet());
		} else {
			actualFetchFields = new HashSet<>(paginationConfiguration.getFetchFields());
		}
		
		final Map<String, Set<String>> subFields = PersistenceUtils.extractSubFields(actualFetchFields);
		
		final Map<String, Object> filters = paginationConfiguration == null ? null : paginationConfiguration.getFilters();

		final List<Map<String, Object>> valuesList = new ArrayList<>();

		StorageQuery query = new StorageQuery();
		query.setCet(cet);
		query.setFetchFields(actualFetchFields);
		query.setFilters(filters);
		query.setPaginationConfiguration(paginationConfiguration);
		query.setRepository(repository);
		query.setSubFields(subFields);
		query.setFetchAllFields(fetchAllFields);
		
		// Make sure the filters matches the fields
		if(filters != null) {
			filters.keySet()
				.forEach(key -> {
					String[] fieldInfo = key.split(" ");
					String fieldName = fieldInfo.length == 1 ? fieldInfo[0] : fieldInfo[1];
					if(fields.get(fieldName) == null && !"uuid".equals(fieldName) && !("code".equals(fieldName) && cet.getAvailableStorages().contains(DBStorageType.SQL) && !cet.getSqlStorageConfiguration().isStoreAsTable())) {
						throw new IllegalArgumentException("Filter " + key + " does not match fields of " + cet.getCode());
					}
				});
		}
		
		for (var storage : cet.getAvailableStorages()) {
			List<Map<String, Object>> values = provider.findImplementation(storage)
					.find(query);
			
			if (values != null) {
				values.forEach(resultMap -> mergeData(valuesList, resultMap));
			}
		}
		
		// Complete missing data
		for (Map<String, Object> data : valuesList) {
			String uuid = (String) (data.get("uuid") != null ? data.get("uuid") : data.get("meveo_uuid"));

			final Map<String, Object> missingData = getMissingData(data, repository, cet, uuid, actualFetchFields, subFields);
			if(missingData != null) {
				data.putAll(missingData);
			}
		}
		
		return valuesList.stream()
				.map(values -> deserializeData(values, fields.values()))
				.collect(Collectors.toList());
	}

	/**
	 * Count the number of record for a given pagination
	 * 
	 * @param repository              the repository
	 * @param cet                     the data type
	 * @param paginationConfiguration the pagination
	 * @return the number of record
	 */
	public int count(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {
		for (var storage : cet.getAvailableStorages()) {
			var count = provider.findImplementation(storage)
					.count(repository, cet, paginationConfiguration);
			if (count != null) {
				return count;
			}
		}

		return 0;
	}

	/**
	 * Insert / update an entity with the sourceValues. The target entity is retrieved from the target values. <br>
	 * If the target entity does not exist, create the source node. <br>
	 * If the target entity and the relation exists, update the source entity with the source values. <br>
	 * If the target entity exists but not the relation, create the relation along with the source entity.
	 *
	 * @param repository   Code of the repository / configuration to store the data. <br>
	 *                     NOTE : only available for NEO4J at the moment.
	 * @param relationCode Code of the relation to create
	 * @param sourceValues Values to insert
	 * @param targetValues Filters on target entity
	 */
	@Override
	public PersistenceActionResult addSourceEntityUniqueCrt(Repository repository, String relationCode, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(relationCode);
		var cfts = cache.getCustomFieldTemplates(crt.getAppliesTo());

		if (!crt.isUnique()) {
			throw new IllegalArgumentException("CRT must be unique !");
		}

		final CustomEntityTemplate endNode = crt.getEndNode();
		final CustomEntityTemplate startNode = crt.getStartNode();

		// Everything is stored in Neo4J
		if (isEverythingStoredInNeo4J(crt)) {
			return neo4jService.addSourceNodeUniqueCrt(
					repository.getNeo4jConfiguration().getCode(),
					relationCode,
					PersistenceUtils.filterValues(cfts, sourceValues, crt, DBStorageType.NEO4J),
					PersistenceUtils.filterValues(cfts, targetValues, crt, DBStorageType.NEO4J));
		}

		String targetUUUID = findEntityId(repository, targetValues, endNode);

		// Target does not exists. We create the source
		if (targetUUUID == null) {
			CustomEntityInstance cei = new CustomEntityInstance();
			cei.setCetCode(startNode.getCode());
			cei.setRepository(repository);
			customFieldInstanceService.setCfValues(cei, startNode.getCode(), sourceValues);

			return createOrUpdate(repository, cei);

		} else {
			// Target exists. Let's check if the relation exist.
			final String relationUUID = findUniqueRelationByTargetUuid(repository, targetUUUID, crt);

			CustomEntityInstance cei = new CustomEntityInstance();
			cei.setRepository(repository);
			cei.setCetCode(startNode.getCode());
			cei.setCet(cache.getCustomEntityTemplate(startNode.getCode()));
			customFieldInstanceService.setCfValues(cei, startNode.getCode(), sourceValues);

			// Relation does not exists. We create the source.
			if (relationUUID == null) {
				return createOrUpdate(repository, cei);

			} else {
				// Relation exists. We update the source node.
				String sourceUUID = findIdOfSourceEntityByRelationId(repository, relationUUID, crt);
				cei.setUuid(sourceUUID);
				update(repository, cei);

				return new PersistenceActionResult(sourceUUID);
			}
		}
	}

	private boolean isEverythingStoredInNeo4J(CustomRelationshipTemplate crt) {
		return crt.getAvailableStorages().contains(DBStorageType.NEO4J) && crt.getStartNode().getAvailableStorages().contains(DBStorageType.NEO4J) && crt.getEndNode().getAvailableStorages().contains(DBStorageType.NEO4J);
	}
	
	public boolean exists(Repository repository, CustomEntityTemplate cet, String uuid) {
		for (var storage : cet.getAvailableStorages()) {
			if (provider.findImplementation(storage).exists(repository, cet, uuid)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create or update an entity and enventyally entity references that it holds
	 *
	 * @param repository Code of the repository / configuration to store the data. <br>
	 *                   NOTE : only available for NEO4J at the moment.
	 * @param ceiToSave  the {@link CustomEntityInstance}
	 * @return the persisted entites
	 */
	@Override
	public PersistenceActionResult createOrUpdate(Repository repository, CustomEntityInstance ceiToSave) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		if (repository == null) {
			throw new IllegalArgumentException("Repository should be provided");
		}
		
		// Retrieve corresponding CET
		CustomEntityTemplate cet  = customEntityTemplateService.findByCode(ceiToSave.getCetCode(), List.of("availableStorages"));
		ceiToSave.setCet(cet);
		if(cet == null) {
			throw new IllegalArgumentException("CET with code " + ceiToSave.getCetCode() + " does not exist");
		}
		
		Set<EntityRef> persistedEntities = new HashSet<>();

		// Initialize the CEI that will be manipulated
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCetCode(ceiToSave.getCetCode());
		cei.setCode(ceiToSave.getCode());
		cei.setDescription(ceiToSave.getDescription());
		cei.setCfValuesOld(ceiToSave.getCfValuesOld());
		cei.setRepository(repository);
		
		if (ceiToSave.getCfValuesOld() != null && !ceiToSave.getCfValuesOld().getValuesByCode().isEmpty()) {
			try {
				checkBeforeUpdate(repository, ceiToSave);
			} catch (EntityDoesNotExistsException | ELException e) {
				throw new BusinessException(e);
			}
		}
		
		if(ceiToSave.getUuid() != null) {
			cei.setUuid(ceiToSave.getUuid());
		}
				
		final Map<String, CustomFieldTemplate> customFieldTemplates =  (cet.getSuperTemplate() == null ? cache.getCustomFieldTemplates(cet.getAppliesTo()) : customFieldTemplateService.getCftsWithInheritedFields(cet));
		cei.setCet(cet);
		cei.setFieldTemplates(customFieldTemplates);

		// Create referenced entities and set UUIDs in the values
		Map<String, Object> tmpValues = ceiToSave.getCfValuesAsValues() != null ? new HashMap<>(ceiToSave.getCfValuesAsValues()) : new HashMap<>();
		Map<String, Object> entityValues = createEntityReferences(repository, tmpValues, cet);
		customFieldInstanceService.setCfValues(cei, cet.getCode(), entityValues);

		String uuid = null;

		// First check if data exist, in order to synchronize UUID across all storages
		String foundId;
		
		List<String> secretFields = customFieldTemplates.values()
			.stream()
			.filter(cft -> cft.getFieldType() == CustomFieldTypeEnum.SECRET)
			.filter(cft -> cei.get(cft.getCode()) != null)
			.map(CustomFieldTemplate::getCode)
			.collect(Collectors.toList());
		
		try {
			if (ceiToSave.getUuid() != null && exists(repository, cet, ceiToSave.getUuid())) {
				foundId = ceiToSave.getUuid();
			} else {
				foundId = findEntityId(repository, cei);
			}
		} catch (IllegalArgumentException e) {
			// It's no problem if we can't retrieve record using values - we consider it does not exist
			foundId = null;
		}
		
		if (foundId != null) {
			cei.setUuid(foundId);
			// Handle secret fields
			if(!secretFields.isEmpty()) {
				var foundCei = CEIUtils.pojoToCei(find(repository, cet, foundId, false));
				String oldCeiHash = CEIUtils.getHash(foundCei, customFieldTemplates);
				secretFields.forEach(secretField -> {
					// Secret field has not changed, decrypt it so it will be correctly re-encrypted
					if(cei.get(secretField).equals(foundCei.get(secretField))) {
						String decryptedValue = PasswordUtils.decryptNoSecret(oldCeiHash, cei.get(secretField));
						cei.getCfValues().setValue(secretField, decryptedValue);
						entityValues.put(secretField, decryptedValue);
					}
				});
			}
		}
		
		// Encrypt secret fields
		if(!secretFields.isEmpty()) {
			String ceiHash = CEIUtils.getHash(cei, customFieldTemplates);
			secretFields.forEach(secretField -> {
				String secretValue = cei.get(secretField);
				if(!secretValue.startsWith("🔒")) {	// Value is already encrypted
					String encryptedValue = PasswordUtils.encryptNoSecret(ceiHash, secretValue);
					cei.getCfValues().setValue(secretField, encryptedValue);
					entityValues.put(secretField, encryptedValue);
				}
			});
		}
		
		var listener = customEntityTemplateService.loadCrudEventListener(cei.getCet());
		CustomEntity cetClassInstance = null;
		
		if(listener != null) {
			var cetClass =  listener.getEntityClass();
			cetClassInstance = CEIUtils.ceiToPojo(cei, cetClass);
		}
		
		CustomEntityInstance ceiAfterPreEvents = cei;
		
		try {
			transaction.beginTransaction(repository, cet.getAvailableStorages());
			
			if(cetClassInstance != null) {
				if(foundId != null) {
					listener.preUpdate(cetClassInstance);
				} else {
					listener.prePersist(cetClassInstance);
				}
				
				ceiAfterPreEvents = CEIUtils.pojoToCei(cetClassInstance);	// Handle case where entity was modified
				ceiAfterPreEvents.setCet(cei.getCet());
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		for (var storage : cet.getAvailableStorages()) {
			var results = provider.findImplementation(storage)
					.createOrUpdate(repository, ceiAfterPreEvents, customFieldTemplates, foundId);
			uuid = results.getBaseEntityUuid();
			if (foundId == null) {
				ceiAfterPreEvents.setUuid(uuid);
			}
		}

		try {
			
			if(cetClassInstance != null) {
				if(foundId != null) {
					listener.postUpdate(cetClassInstance);
				} else {
					listener.postPersist(cetClassInstance);
				}
			}
			
			transaction.commitTransaction(repository, cet.getAvailableStorages());
			
		} catch (Exception e) {
			
			log.error("Can't create or update data", e);
			
			transaction.rollbackTransaction(e, cet.getAvailableStorages());
			
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
		
		if(uuid == null) {
			throw new PersistenceException("Failed to save " + ceiAfterPreEvents);
		}
		
		return new PersistenceActionResult(persistedEntities, uuid);
	}

	/**
	 * Update an entity instance
	 *
	 * @param repository Repository code
	 * @param ceiToUpdate the instance to be updated
	 * @throws BusinessException if error occures
	 * @throws IOException  if a binary can't be updated
	 * @throws BusinessApiException  if error occures
	 * @throws EntityDoesNotExistsException if the data does not exist yet
	 */
	public void update(Repository repository, CustomEntityInstance ceiToUpdate) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		ceiToUpdate.setRepository(repository);
		CustomEntityTemplate cet = ceiToUpdate.getCet();
		
		Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.getCftsWithInheritedFields(cet);
		ceiToUpdate.setFieldTemplates(customFieldTemplates);

		if (ceiToUpdate.getCfValuesOld() != null && !ceiToUpdate.getCfValuesOld().getValuesByCode().isEmpty()) {
			try {
				checkBeforeUpdate(repository, ceiToUpdate);
			} catch (EntityDoesNotExistsException | ELException e) {
				throw new BusinessException(e);
			}
		}
		
		for (var storage : cet.getAvailableStorages()) {
			provider.findImplementation(storage).update(repository, ceiToUpdate);
		}
	}

	@Override
	public PersistenceActionResult addCRTByValues(Repository repository, String relationCode, Map<String, Object> relationValues, Map<String, Object> sourceValues, Map<String, Object> targetValues) throws ELException, BusinessException {
		CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(relationCode);

		final CustomEntityTemplate endNode = crt.getEndNode();
		final CustomEntityTemplate startNode = crt.getStartNode();
		
		var cfts = cache.getCustomFieldTemplates(crt.getAppliesTo());

		// All Neo4j storage
		if (crt.getAvailableStorages().contains(DBStorageType.NEO4J) && startNode.getAvailableStorages().contains(DBStorageType.NEO4J) && endNode.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			return neo4jService.addCRTByNodeValues(
					repository.getNeo4jConfiguration().getCode(), 
					relationCode, 
					PersistenceUtils.filterValues(cfts, relationValues, crt, DBStorageType.NEO4J),
					PersistenceUtils.filterValues(cfts, sourceValues, crt, DBStorageType.NEO4J),
					PersistenceUtils.filterValues(cfts, targetValues, crt, DBStorageType.NEO4J));
		}

		String sourceUUID = findEntityId(repository, sourceValues, startNode);
		String targetUUUID = findEntityId(repository, targetValues, endNode);
		
		return addCRTByUuids(repository, relationCode, relationValues, sourceUUID, targetUUUID);
	}

	@Override
	public PersistenceActionResult addCRTByUuids(Repository repository, String relationCode, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws ELException, BusinessException {
		CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(relationCode);
		var cfts = cache.getCustomFieldTemplates(crt.getAppliesTo());

		// All neo4j storage
		if (isEverythingStoredInNeo4J(crt)) {
			return neo4jService.addCRTByNodeIds(
					repository.getNeo4jConfiguration().getCode(), 
					relationCode, 
					PersistenceUtils.filterValues(cfts, relationValues, crt, DBStorageType.NEO4J), sourceUuid, targetUuid);
		}
		
		for (var storage : crt.getAvailableStorages()) {
			var result = provider.findImplementation(storage)
					.addCRTByUuids(repository, crt, PersistenceUtils.filterValues(cfts, relationValues, crt, storage), sourceUuid, targetUuid);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	/**
	 * Retrieve the uuid of an entity
	 * 
	 * @param repository repository where the entity is stored
	 * @param cei the instance holding values to get id from
	 * @return the id corresponding to the values
	 */
	public String findEntityId(Repository repository, CustomEntityInstance cei) {
		cei.setRepository(repository);
		
		String uuid = null;
		CustomEntityTemplate cet = cei.getCet();
		
		for (var storage : cet.getAvailableStorages()) {
			if (uuid != null && provider.findImplementation(storage).exists(repository, cet, uuid)) {
				break;
			}
			uuid = provider.findImplementation(storage).findEntityIdByValues(repository, cei);
		}

		return uuid;
	}

	/**
	 * Find an entity instance UUID
	 *
	 * @param repository    Repository to search in
	 * @param valuesFilters Filter on entity's values
	 * @param cet           Template of the entity
	 * @return UUID of the entity or nul if it was'nt found
	 * @throws BusinessException if error happens
	 */
	public String findEntityId(Repository repository, Map<String, Object> valuesFilters, CustomEntityTemplate cet) throws BusinessException {
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCet(cet);
		cei.setCetCode(cet.getCode());
		cei.setCode((String) valuesFilters.get("code"));
		cei.setRepository(repository);
		
		customFieldInstanceService.setCfValues(cei, cet.getCode(), valuesFilters);
		return findEntityId(repository, cei);
	}

	/**
	 * Remove an entity from database
	 *
	 * @param repository Repository
	 * @param cet        Template of the entity
	 * @param uuid       UUID of the entity
	 * @throws BusinessException if error happens
	 */
	public void remove(Repository repository, CustomEntityTemplate cet, String uuid) throws BusinessException {
		if (uuid == null) {
			throw new IllegalArgumentException("Cannot remove entity by UUID without uuid");
		}
		
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCet(cet);
		cei.setCetCode(cet.getCode());
		cei.setUuid(uuid);
		cei.setRepository(repository);
		
		var listener = customEntityTemplateService.loadCrudEventListener(cei.getCet());
		CustomEntity cetClassInstance = null;
		
		transaction.beginTransaction(repository, cet.getAvailableStorages());
		
		try {
			if(listener != null) {
				var cetClass =  listener.getEntityClass();
				try {
					var values = find(repository, cet, uuid, false);
					cei = CEIUtils.fromMap(values, cet);
					cetClassInstance = CEIUtils.ceiToPojo(cei, cetClass);
					listener.preRemove(cetClassInstance);
				} catch (EntityDoesNotExistsException e) {
					e.printStackTrace();
				}
			}
			
			for (var storage : cet.getAvailableStorages()) {
				provider.findImplementation(storage).remove(repository, cet, uuid);
			}
	
			fileSystemService.delete(repository, cet, uuid);
			
			if (!(cet.getAvailableStorages().contains(DBStorageType.SQL) && !cet.getSqlStorageConfiguration().isStoreAsTable())) {
				customEntityInstanceDelete.fire(cei);
			}
			
			if(cetClassInstance != null) {
				listener.postRemove(cetClassInstance);
			}
			
			// Delete binaries
			fileSystemService.delete(repository, cet, uuid);
			
			transaction.commitTransaction(repository, cet.getAvailableStorages());
		
		} catch(Exception e) {
			transaction.rollbackTransaction(e, cet.getAvailableStorages());
			throw e;
		}
		
	}

	/**
	 * Replace the files references by the one in input
	 * 
	 * @param repository Repository where data is stored
	 * @param cet        Template of the data
	 * @param cft        Concerned field
	 * @param uuid       UUID of the entity
	 * @param binaries   New binaries
	 * @throws BusinessException if update fails
	 */
	public void setBinaries(Repository repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<File> binaries) throws BusinessException {
		for (var storage : cft.getStoragesNullSafe()) {
			provider.findImplementation(storage).setBinaries(repository, cet, cft, uuid, binaries);
		}
	}

	private Map<String, Object> createEntityReferences(Repository repository, Map<String, Object> entityValues, CustomEntityTemplate cet) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		Map<String, Object> updatedValues = new HashMap<>(entityValues);

		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.getCftsWithInheritedFields(cet);
		List<CustomFieldTemplate> cetFields = updatedValues.keySet()
				.stream()
				.map(cfts::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		for (CustomFieldTemplate customFieldTemplate : cetFields) {
			if (CustomFieldTypeEnum.ENTITY.equals(customFieldTemplate.getFieldType())) {
				final CustomEntityTemplate referencedCet = customEntityTemplateService.findByCode(customFieldTemplate.getEntityClazzCetCode(), List.of("availableStorages"));
				if(referencedCet != null) {
					createCetReference(repository, updatedValues, customFieldTemplate, referencedCet);
				
				}
			}
		}

		return updatedValues;
	}

	@SuppressWarnings("unchecked")
	private void createCetReference(Repository repository, Map<String, Object> updatedValues, CustomFieldTemplate customFieldTemplate, CustomEntityTemplate referencedCet) throws BusinessException, BusinessApiException, EntityDoesNotExistsException, IOException {
		List<Object> entitiesToCreate = new ArrayList<>();
		final Object fieldValue = updatedValues.get(customFieldTemplate.getCode());
		
		// Don't save empty nodes
		if(fieldValue == null) {
			return;
		}
		
		final Set<EntityRef> createdEntityReferences = new HashSet<>();

		if (fieldValue instanceof Collection && customFieldTemplate.getStorageType() != CustomFieldStorageTypeEnum.LIST) {
			Collection<?> collectionValue = (Collection<? extends Map<String, Object>>) fieldValue;
			if (!collectionValue.isEmpty()) {
				entitiesToCreate.add(collectionValue.iterator().next());
			}
		
		} else if (fieldValue instanceof Collection && customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
			Collection<EntityReferenceWrapper> fieldValueCol = (Collection<EntityReferenceWrapper>) fieldValue;
			if(!fieldValueCol.isEmpty()) {
				// Only references are passed so the entities are already created
				if(fieldValueCol.iterator().next() instanceof EntityReferenceWrapper) {
					Collection<EntityReferenceWrapper> entityReferences = (Collection<EntityReferenceWrapper>) fieldValueCol;
					List<EntityRef> entityRefs = entityReferences.stream()
							.map(EntityRef::new)
							.collect(Collectors.toList());
					createdEntityReferences.addAll(entityRefs);
				} else {
					entitiesToCreate.addAll((Collection<? extends Map<String, Object>>) fieldValue);
				}
			}
		
		} else if (fieldValue instanceof Map) {
			
			// Create references stored in map then update it
			if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
				Map<String, Object> referenceMap = new HashMap<>();
				for (var entry : ((Map<String, Object>) fieldValue).entrySet()) {
					if (entry.getValue() instanceof Map) {
						var map = (Map<String, Object>) entry.getValue();
						if (map.get("uuid") != null) {
							try {
								var existingData = find(repository, referencedCet, (String) map.get("uuid"), false);
								if (existingData != null) {
									referenceMap.put(entry.getKey(), map.get("uuid"));
									continue;
								}
							} catch (EntityDoesNotExistsException e) {
								//NOOP
							}
						}
						String uuid = createReferencedEntity(repository, customFieldTemplate, map).getBaseEntityUuid();
						referenceMap.put(entry.getKey(), uuid);
					} else if (entry.getValue() instanceof EntityReferenceWrapper) {
						referenceMap.put(entry.getKey(), ((EntityReferenceWrapper) entry.getValue()).getUuid());
					} else if (entry.getValue() instanceof String) {
						referenceMap.put(entry.getKey(), (String) entry.getValue());
					} else if (entry.getValue() instanceof CustomEntity) {
						var ce = (CustomEntity) entry.getValue();
						if (ce.getUuid() != null) {
							try {
								var existingData = find(repository, referencedCet, ce.getUuid(), false);
								if (existingData != null) {
									referenceMap.put(entry.getKey(), ce.getUuid());
									continue;
								}
							} catch (EntityDoesNotExistsException e) {
								//NOOP
							}
						}
						var cei = CEIUtils.pojoToCei(ce);
						cei.setRepository(repository);
						
						String uuid = createReferencedEntity(repository, customFieldTemplate, cei.getCfValuesAsValues()).getBaseEntityUuid();
						referenceMap.put(entry.getKey(), uuid);
					}
				}
				updatedValues.put(customFieldTemplate.getCode(), referenceMap);
			} else {
				entitiesToCreate.add(fieldValue);
			}
		
		} else if (referencedCet.getNeo4JStorageConfiguration() != null && referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
			entitiesToCreate.add(Collections.singletonMap("value", fieldValue));
		
		} else if (fieldValue instanceof EntityReferenceWrapper) {
			EntityReferenceWrapper entityReferenceWrapper = (EntityReferenceWrapper) fieldValue;
			updatedValues.put(customFieldTemplate.getCode(), entityReferenceWrapper.getUuid());
			createdEntityReferences.add(new EntityRef(entityReferenceWrapper));
		} else if (fieldValue instanceof CustomEntityInstance) {
			EntityRef entityRef = new EntityRef(((CustomEntityInstance) fieldValue).getUuid(), referencedCet.getCode());
			createdEntityReferences.add(entityRef);
		}

		for (Object e : entitiesToCreate) {
			if (e instanceof Map) {
				final Set<EntityRef> createdEntities = createReferencedEntity(repository, customFieldTemplate, (Map<String, Object>) e)
						.getPersistedEntities();
				if (createdEntities.isEmpty()) {
					log.error("Failed to create reference for {} ", e, new Exception());
				}
				createdEntityReferences.addAll(createdEntities);

			} else if (e instanceof String) {
				// If entity reference is a string, then it means it refers to an existing UUID
				EntityRef entityRef = new EntityRef((String) e, referencedCet.getCode());
				entityRef.setTrustScore(100);
				createdEntityReferences.add(entityRef);
			}
		}

		List<String> uuids = getTrustedUuids(createdEntityReferences);

		// Replace with entity reference's UUID only when target is not primitive
		if (referencedCet.getNeo4JStorageConfiguration() == null || !referencedCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {

			if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
				updatedValues.put(customFieldTemplate.getCode(), uuids);

			} else if (customFieldTemplate.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && !uuids.isEmpty()) {
				if (customFieldTemplate.getFieldType() == CustomFieldTypeEnum.ENTITY && referencedCet.getSqlStorageConfiguration() != null) {
					if (!referencedCet.isStoreAsTable()) {
						CustomEntityInstance customEntityInstance = customEntityInstanceService.findByUuid(referencedCet.getCode(), uuids.get(0));
						updatedValues.put(customFieldTemplate.getCode(), customEntityInstance);
					} else {
						Map<String, Object> map = customTableService.findById(repository.getSqlConfigurationCode(), referencedCet.getCode(), uuids.get(0));
						if (map != null && !map.isEmpty()) {
							if (map.get("code") == null) {
								map.put("code", uuids.get(0));
							}
							CustomEntityInstance cei = customEntityInstanceService.fromMap(referencedCet, map);
							cei.setRepository(repository);
							updatedValues.put(customFieldTemplate.getCode(), cei);
						}
					}
				}else {
					updatedValues.put(customFieldTemplate.getCode(), uuids.get(0));
				}
			}

		} else {
			// If entity reference is primitive, place the UUID in the map along with the
			// value
			updatedValues.put(customFieldTemplate.getCode() + "UUID", uuids.get(0));
		}

	}

	/**
	 * @param repository
	 * @param customFieldTemplate
	 * @param e
	 * @param map
	 * @return
	 * @throws BusinessException
	 * @throws IOException
	 * @throws BusinessApiException
	 * @throws EntityDoesNotExistsException
	 */
	protected PersistenceActionResult createReferencedEntity(Repository repository, CustomFieldTemplate customFieldTemplate, Map<String, Object> values) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCetCode(customFieldTemplate.getEntityClazzCetCode());
		cei.setCode((String) values.get("code"));
		cei.setRepository(repository);
		String uuid = (String) values.get("uuid");
		if (uuid != null) {
			cei.setUuid(uuid);
		}

		customFieldInstanceService.setCfValues(cei, customFieldTemplate.getEntityClazzCetCode(), values);

		return createOrUpdate(repository, cei);
	}

	//TODO: Migrate in StorageService
	private String findUniqueRelationByTargetUuid(Repository repository, String targetUuid, CustomRelationshipTemplate crt) {
		if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
			return customTableRelationService.findIdOfUniqueRelationByTargetId(crt, targetUuid);
		}

		if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			return neo4jService.findIdOfUniqueRelationByTargetId(repository.getNeo4jConfiguration().getCode(), crt, targetUuid);
		}

		return null;
	}

	//TODO: Migrate in StorageImpl
	private String findIdOfSourceEntityByRelationId(Repository repository, String relationUuid, CustomRelationshipTemplate crt) {
		if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
			return customTableRelationService.findIdOfSourceEntityByRelationId(crt, relationUuid);
		}

		if (crt.getAvailableStorages().contains(DBStorageType.NEO4J)) {
			return neo4jService.findIdOfUniqueRelationByTargetId(repository.getNeo4jConfiguration().getCode(), crt, relationUuid);
		}

		return null;
	}

	public void fetchEntityReferences(Repository repository, CustomModelObject customModelObject, Map<String, Object> values, Map<String, Set<String>> subFields) throws EntityDoesNotExistsException {
		for (Map.Entry<String, Object> entry : new HashSet<>(values.entrySet())) {
			CustomFieldTemplate cft = cache.getCustomFieldTemplate(entry.getKey(), customModelObject.getAppliesTo());
			if (cft != null && cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
				CustomEntityTemplate cet = cache.getCustomEntityTemplate(cft.getEntityClazzCetCode());
				
				// Check if target is not JPA entity
				if (cet == null) {
					try {
						var session = sqlStorageImpl.getHibernateSession("default");
						Class<?> clazz = Class.forName(cft.getEntityClazzCetCode());
						values.put(
							entry.getKey(), 
							session.find(clazz, entry.getValue())
						);
						continue;
						
					} catch (ClassNotFoundException e) {
						//NOOP
					
					} catch(Exception e) {
						log.error("Cannot find referenced entity {}", e.getMessage());
						throw new RuntimeException(e);
					}
				}
				
				var nConf = cet.getNeo4JStorageConfiguration();
				
				// Don't fetch primitive entities
				if(nConf != null && nConf.isPrimitiveEntity()) {
					continue;
				}
				
				Set<String> fetchFields = subFields.get(cft.getCode());
				if(fetchFields == null) {
					continue;
				}
				fetchFields = new HashSet<>(fetchFields);
				
				Map<String, Set<String>> subSubFields = PersistenceUtils.extractSubFields(fetchFields);

				if (fetchFields.contains("*")) {
					fetchFields = cache.getCustomFieldTemplates(cet.getAppliesTo()).keySet();
				}
				
				if(cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE) {
					if(entry.getValue() instanceof String) {
						Map<String, Object> refValues = findById(repository, cet, (String) entry.getValue(), fetchFields, subSubFields, false);
						values.put(cft.getCode(), refValues);
					} else if(entry.getValue() instanceof Map) {
						values.put(cft.getCode(), entry.getValue());
					}
				} else if(cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
					if(entry.getValue() instanceof Collection) {
						Collection<?> collection = (Collection<?>) entry.getValue();
						if(!collection.isEmpty()) {
							var firstItem = collection.iterator().next();
							if(firstItem instanceof String) {
								// List list = find(repository, cet, null);
								//TODO
							}
						}
					}
				}

			}
		}
	}
	
	private Map<String, Object> deserializeData(Map<String, Object> values, Collection<CustomFieldTemplate> cfts) {
		Map<String, Object> map = new HashMap<>(values);
		
		cfts.forEach(cft -> {
			Object value = values.get(cft.getCode());
			if(value instanceof String && cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
				map.put(cft.getCode(), JacksonUtil.fromString((String) value, Map.class));
			}
		});
		 
		return map;
	}
	
	public void checkBeforeUpdate(Repository repository, CustomEntityInstance entity) throws EntityDoesNotExistsException, ELException, IllegalTransitionException {
		entity.setRepository(repository);
		
		Map<String, Set<String>> map = customEntityInstanceService.getValueCetCodeAndWfTypeFromWF();
		Map<String, Object> values = entity.getCfValuesAsValues();
		if (values != null) {
			if (map.isEmpty()) {
				return;
			}
			
			for (String key : values.keySet()) {
				if (map.keySet().contains(entity.getCetCode()) && map.get(entity.getCetCode()).contains(key)) {
					
					// Skip if value doesn't changes
					if(Objects.equals(entity.getCfValuesOld().getValue(key), entity.getCfValues().getValue(key))) {
						continue;
					}
					
					if (customEntityInstanceService.transitionsFromPreviousState(key, entity)) {
						continue;
					} else {
						throw new IllegalTransitionException((String) entity.getCfValuesOldNullSafe().getValue(key), 
								(String) values.get(key), 
								key);
					}
				}
			}
		}
	}
	
	/**
	 * Add or merge a map of data to a list.
	 * 
	 * @param valuesList list of map of values
	 * @param resultMap  map to merge or add
	 */
	private void mergeData(List<Map<String, Object>> valuesList, Map<String, Object> resultMap) {

		String uuid = (String) (resultMap.get("uuid") != null ? resultMap.get("uuid") : resultMap.get("meveo_uuid"));

		boolean found = false;
		for (Map<String, Object> mapOfValues : valuesList) {
			String uuid2 = (String) (mapOfValues.get("uuid") != null ? mapOfValues.get("uuid") : mapOfValues.get("meveo_uuid"));
			if (uuid.equals(uuid2)) {
				mapOfValues.putAll(resultMap);
				found = true;
				break;
			}
		}

		if (!found) {
			valuesList.add(resultMap);
		}
	}

}
