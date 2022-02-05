/**
 * 
 */
package org.meveo.persistence.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageTransaction;
import org.meveo.persistence.PersistenceActionResult;
import org.meveo.persistence.StorageImpl;
import org.meveo.persistence.StorageQuery;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.storage.FileSystemService;
import org.meveo.util.PersistenceUtils;
import org.slf4j.Logger;

/**
 * 
 * @author heros
 * @since 
 * @version
 */
public class SQLStorageImpl implements StorageImpl {
	
	@Inject
	private CrossStorageTransaction transaction;
	
	@Inject
	private CustomTableService customTableService;
	
	@Inject
	private CustomEntityInstanceService customEntityInstanceService;
	
	@Inject
	private CustomFieldsCacheContainerProvider cache;
	
	@Inject
	private CustomFieldTemplateService customFieldTemplateService;
	
	@Inject
	private CustomFieldInstanceService customFieldInstanceService;
	
	@Inject
	private Logger log;
	
	@Inject
	private FileSystemService fileSystemService;
	
    @Inject
    @Updated
    private Event<CustomEntityInstance> customEntityInstanceUpdate;
    
    @Inject
    @Created
    private Event<CustomEntityInstance> customEntityInstanceCreate;

	@Override
	public boolean exists(Repository repository, CustomEntityTemplate cet, String uuid) {
		if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
			return customTableService.findById(repository.getSqlConfigurationCode(), cet, uuid) != null;
		} else {
			return customEntityInstanceService.findByUuid(cet.getCode(), uuid) != null;
		}
	}

	@Override
	public String findEntityIdByValues(Repository repository, CustomEntityInstance cei) {
		String uuid = null;
		CustomEntityTemplate cet = cei.getCet();
		Map<String, Object> valuesFilters = cei.getValuesNullSafe();
		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.getCftsWithInheritedFields(cet);
		
		if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
			if(cei.getUuid() != null) {
				Map<String, Object> values = customTableService.findById(repository.getSqlConfigurationCode(), cet, cei.getUuid());
				if(values != null) {
					uuid = cei.getUuid();
				}
			}
			
			if(uuid == null) {
				List<CustomFieldTemplate> uniqueCfts = cfts.values().stream()
					.filter(CustomFieldTemplate::isUnique)
					.filter(cft -> cft.getStoragesNullSafe().contains(DBStorageType.SQL))
					.collect(Collectors.toList());

				if(uniqueCfts.isEmpty()) {
					throw new IllegalArgumentException("Can't retrieve SQL record by values if no unique fields are defined");
				}
				
				Map<String, Object> uniqueValues = new HashMap<>();
				uniqueCfts.forEach(cft -> {
					var value = valuesFilters.get(cft.getCode());
					if(value != null) {
						uniqueValues.put(cft.getCode(), value);
					}
				});
				
				if(uniqueValues.isEmpty()) {
					throw new IllegalArgumentException("No unique values provided");
				}
				
				uuid = customTableService.findIdByUniqueValues(repository.getSqlConfigurationCode(), cet, uniqueValues, cfts.values());
			}
			
		} else {
			if(cei.getUuid() != null) {
				CustomEntityInstance existingCei = customEntityInstanceService.findByUuid(cet.getCode(), cei.getUuid());
				if (existingCei != null) {
					uuid = existingCei.getUuid();
				}
			}
			
			if(uuid == null){
				final CustomEntityInstance customEntityInstance = getCustomEntityInstance(cet.getCode(), cei.getCode(), valuesFilters);
				if (customEntityInstance != null) {
					uuid = customEntityInstance.getUuid();
				}
			}
		}
		
		return null;
	}

	@Override
	public Map<String, Object> findById(Repository repository, CustomEntityTemplate cet, String uuid, Map<String, CustomFieldTemplate> cfts, Collection<String> fetchFields, boolean withEntityReferences) {
		boolean foundEntity = false;
		Map<String, Object> values = new HashMap<>();
		
		try {
			transaction.beginTransaction(repository);

			if (cet.getAvailableStorages().contains(DBStorageType.SQL)) {
				List<String> sqlFields = PersistenceUtils.filterFields(fetchFields, cfts, DBStorageType.SQL);
				if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
					final Map<String, Object> customTableValue = customTableService.findById(repository.getSqlConfigurationCode(), cet, uuid, sqlFields);
					replaceKeys(cet, sqlFields, customTableValue);
					if(customTableValue != null) {
						foundEntity = true;
						values.putAll(customTableValue);
					}
				} else {
					final CustomEntityInstance cei = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
					if (cei == null) {
						return null;
					}

					values.put("code", cei.getCode());
					values.put("description", cei.getDescription());
					foundEntity = true;
					if (sqlFields != null) {
						for (String field : sqlFields) {
							if (cei.getCfValues() != null && cei.getCfValues().getCfValue(field) != null) {
								values.putIfAbsent(field, cei.getCfValues().getCfValue(field).getValue());
							}
						}
					} else {
						if (cei.getCfValuesAsValues() != null) {
							values.putAll(cei.getCfValuesAsValues());
						}
					}
				}
			}

			transaction.commitTransaction(repository);
		} catch (Exception e) {
			if(e instanceof EntityDoesNotExistsException) {
				return null;
			}
			
			transaction.rollbackTransaction(e);
			throw new RuntimeException(e);
		}
		
		if (foundEntity) {
			return values;
		} else {
			return null;
		}
	}

	@Override
	public List<Map<String, Object>> find(StorageQuery query) throws EntityDoesNotExistsException {
		PaginationConfiguration sqlPaginationConfiguration = new PaginationConfiguration(query.getPaginationConfiguration());
		sqlPaginationConfiguration.setFetchFields(List.copyOf(query.getFetchFields()));
		
		if (query.getCet().getSqlStorageConfiguration().isStoreAsTable()) {
			final List<Map<String, Object>> values = customTableService.list(query.getRepository().getSqlConfigurationCode(), query.getCet(), sqlPaginationConfiguration);
			values.forEach(v -> replaceKeys(query.getCet(), query.getFetchFields(), v));
			return values;

		} else {
			final List<CustomEntityInstance> ceis = customEntityInstanceService.list(query.getCet().getCode(), query.getCet().isStoreAsTable(), query.getFilters(), sqlPaginationConfiguration);
			final List<Map<String, Object>> values = new ArrayList<>();

			for (CustomEntityInstance cei : ceis) {
				Map<String, Object> cfValuesAsValues = cei.getCfValuesAsValues();
				final Map<String, Object> map = cfValuesAsValues == null ? new HashMap<>() : new HashMap<>(cfValuesAsValues);
				map.put("uuid", cei.getUuid());
				map.put("code", cei.getCode());
				map.put("description", cei.getDescription());

				if (!query.isFetchAllFields()) {
					for (String k : cei.getCfValuesAsValues().keySet()) {
						if (!query.getFetchFields().contains(k)) {
							map.remove(k);
						}
					}
				}
				values.add(map);
			}

			return values;
		}
	}

	@Override
	public PersistenceActionResult createOrUpdate(Repository repository, CustomEntityInstance cei, Map<String, CustomFieldTemplate> customFieldTemplates, String foundUuid) throws BusinessException {
		Map<String, Object> sqlValues = PersistenceUtils.filterValues(customFieldTemplates, cei.getCfValuesAsValues(), cei.getCet(), DBStorageType.SQL, false);
		String uuid = null;
		Set<EntityRef> persistedEntities = new HashSet<>();
		
		if (!sqlValues.isEmpty() || !cei.getCet().getSqlStorageConfiguration().isStoreAsTable()) {
			CustomEntityInstance sqlCei = new CustomEntityInstance();
			sqlCei.setCet(cei.getCet());
			sqlCei.setUuid(cei.getUuid());
			sqlCei.setCode(cei.getCode());
			sqlCei.setCetCode(cei.getCetCode());
			sqlCei.setDescription(cei.getDescription());
			customFieldInstanceService.setCfValues(sqlCei, cei.getCet().getCode(), sqlValues);

			// Update binaries stored in SQL
			List<CustomFieldTemplate> binariesInSql = customFieldTemplates.values().stream().filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY)).filter(f -> f.getStoragesNullSafe().contains(DBStorageType.SQL)).collect(Collectors.toList());

			try {
				if (cei.getCet().getSqlStorageConfiguration().isStoreAsTable()) {
					uuid = createOrUpdateSQL(repository, sqlCei, binariesInSql, customFieldTemplates);
	
				} else {
					uuid = createOrUpdateCei(repository, sqlCei, binariesInSql);
				}
			} catch (Exception e) {
				throw new BusinessException(e);
			}

			persistedEntities.add(new EntityRef(uuid, cei.getCet().getCode()));
		}
		
		return new PersistenceActionResult(persistedEntities, uuid);
	}

	@Override
	public void update(Repository repository, CustomEntityInstance ceiToUpdate) throws BusinessException {
		try {
			Map<String, CustomFieldTemplate> customFieldTemplates = ceiToUpdate.getFieldTemplates();
			List<CustomFieldTemplate> binariesInSql = customFieldTemplates.values().stream().filter(f -> f.getFieldType().equals(CustomFieldTypeEnum.BINARY)).filter(f -> f.getStoragesNullSafe().contains(DBStorageType.SQL)).collect(Collectors.toList());
			Map<String, Object> values = ceiToUpdate.getCfValuesAsValues();
			
			// Custom table
			if (ceiToUpdate.getCet().getSqlStorageConfiguration().isStoreAsTable()) {
				Map<String, Object> sqlValues = PersistenceUtils.filterValues(customFieldTemplates, values, ceiToUpdate.getCet(), DBStorageType.SQL);
	
				if (!CollectionUtils.isEmpty(binariesInSql)) {
					List<String> binariesFieldsToFetch = binariesInSql.stream().map(CustomFieldTemplate::getCode).collect(Collectors.toList());
	
					final Map<String, Object> existingBinariesFields = customTableService.findById(repository.getSqlConfigurationCode(), ceiToUpdate.getCet(), ceiToUpdate.getUuid(), binariesFieldsToFetch);
					fileSystemService.updateBinaries(repository, ceiToUpdate.getUuid(), ceiToUpdate.getCet(), binariesInSql, sqlValues, existingBinariesFields);
				}
	
				customTableService.update(repository.getSqlConfigurationCode(), ceiToUpdate.getCet(), ceiToUpdate);
			} else {
				// CEI storage
				final CustomEntityInstance cei = customEntityInstanceService.findByUuid(ceiToUpdate.getCet().getCode(), ceiToUpdate.getUuid());
	
				// Update binaries
				if (CollectionUtils.isNotEmpty(binariesInSql)) {
					final Map<String, Object> existingValues = cei.getCfValuesAsValues();
					fileSystemService.updateBinaries(repository, cei.getUuid(), ceiToUpdate.getCet(), binariesInSql, values, existingValues);
				}
	
				CustomFieldValues customFieldValues = new CustomFieldValues();
				values.forEach(customFieldValues::setValue);
				cei.setCfValues(customFieldValues);
				customEntityInstanceService.update(cei);
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}

	}

	@Override
	public void setBinaries(Repository repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<File> binaries) throws BusinessException {
		Object valueToSave = binaries;
		List<String> paths = binaries.stream().map(File::getPath).collect(Collectors.toList());

		if (cft.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE)) {
			valueToSave = paths.isEmpty() ? null : paths.get(0);
		}

		if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
			customTableService.updateValue(repository.getSqlConfigurationCode(), SQLStorageConfiguration.getDbTablename(cet), uuid, cft.getDbFieldname(), valueToSave);
		} else {
			CustomEntityInstance cei = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
			CustomFieldValues cfValues = cei.getCfValues();
			cfValues.setValue(cft.getCode(), valueToSave);
			customEntityInstanceService.update(cei);
		}
	}

	@Override
	public void remove(Repository repository, CustomEntityTemplate cet, String uuid) throws BusinessException {
		if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
			customTableService.remove(repository.getSqlConfigurationCode(), cet, uuid);
		} else {
			final CustomEntityInstance customEntityInstance = customEntityInstanceService.findByUuid(cet.getCode(), uuid);
			customEntityInstanceService.remove(customEntityInstance);
		}
	}

	@Override
	public DBStorageType getStorageType() {
		return DBStorageType.SQL;
	}
	
	
	@Override
	public int count(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {
		final String dbTablename = SQLStorageConfiguration.getDbTablename(cet);

		if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
			return (int) customTableService.count(repository.getSqlConfigurationCode(), dbTablename, paginationConfiguration);

		} else {
			return (int) customEntityInstanceService.count(cet.getCode(), paginationConfiguration);
		}
	}

	public void replaceKeys(CustomEntityTemplate cet, Collection<String> sqlFields, Map<String, Object> customTableValue) {
		if (sqlFields != null && !sqlFields.isEmpty()) {
			List<CustomFieldTemplate> cfts = sqlFields.stream().map(field -> cache.getCustomFieldTemplate(field, cet.getAppliesTo())).collect(Collectors.toList());

			customTableService.replaceKeys(cfts, customTableValue);
		} else {
			final Collection<CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo()).values();
			customTableService.replaceKeys(cfts, customTableValue);
		}
	}
	
	private CustomEntityInstance getCustomEntityInstance(String cetCode, String code, Map<String, Object> values) {
		if (code != null) {
			return customEntityInstanceService.findByCodeByCet(cetCode, code);
		}

		final List<CustomEntityInstance> list = customEntityInstanceService.list(cetCode, values);

		if (list.isEmpty()) {
			return null;
		} else if (list.size() > 1) {
			throw new NonUniqueResultException(list.size() + " results found for CEI for CET " + cetCode + " with values " + values);
		} else {
			return list.get(0);
		}

	}

	private String createOrUpdateCei(Repository repository, CustomEntityInstance ceiToSave, Collection<CustomFieldTemplate> binariesInSql) throws BusinessException, IOException, BusinessApiException {
		CustomEntityTemplate cet = ceiToSave.getCet();
		Map<String, Object> values = ceiToSave.getCfValuesAsValues();

		CustomEntityInstance cei = getCustomEntityInstance(ceiToSave.getCetCode(), ceiToSave.getCode(), values);		
		
		Map<CustomFieldTemplate, Object> persistedBinaries = new HashMap<>();

		if (cei == null) {
			cei = ceiToSave;

			if (CollectionUtils.isNotEmpty(binariesInSql)) {
				persistedBinaries = fileSystemService.updateBinaries(repository, cei.getUuid(), cet, binariesInSql, values, new HashMap<>());
			}

			for (Map.Entry<CustomFieldTemplate, Object> entry : persistedBinaries.entrySet()) {
				cei.getCfValues().setValue(entry.getKey().getCode(), entry.getValue());
			}

			customEntityInstanceService.create(cei);

		} else {
			if (CollectionUtils.isNotEmpty(binariesInSql)) {
				final Map<String, Object> existingValues = cei.getCfValuesAsValues();
				persistedBinaries = fileSystemService.updateBinaries(repository, cei.getUuid(), cet, binariesInSql, values, existingValues);
			}

			cei.setCfValuesOld(cei.getCfValues());
			cei.setCfValues(ceiToSave.getCfValues());
			cei.setDescription(ceiToSave.getDescription());
			for (Map.Entry<CustomFieldTemplate, Object> entry : persistedBinaries.entrySet()) {
				cei.getCfValues().setValue(entry.getKey().getCode(), entry.getValue());
			}

			customEntityInstanceService.update(cei);
		}

		return cei.getUuid();
	}

	

	private String createOrUpdateSQL(Repository repository, CustomEntityInstance cei, Collection<CustomFieldTemplate> binariesInSql, Map<String, CustomFieldTemplate> cfts) throws BusinessException, IOException, BusinessApiException, EntityDoesNotExistsException {
		String sqlUUID = null;
		
		Map<String, Object> oldCfValues = new HashMap<>();
		
		if(cei.getUuid() != null) {
			oldCfValues = customTableService.findById(repository.getSqlConfigurationCode(), cei.getCet(), cei.getUuid());
			if(oldCfValues != null) {
				sqlUUID = cei.getUuid();
				oldCfValues.remove("uuid");
			}
		}
		
		if(sqlUUID == null) {
			Map<String, Object> sqlValues = PersistenceUtils.filterValues(cfts, cei.getCfValuesAsValues(), cei.getCet(), DBStorageType.SQL, false);
			sqlUUID = customTableService.findIdByUniqueValues(repository.getSqlConfigurationCode(), cei.getCet(), sqlValues, cfts.values());
		}
		
		if (sqlUUID != null) {
			
			cei.setUuid(sqlUUID);
			
			CustomEntityInstance tempCei = new CustomEntityInstance();
			tempCei.setCetCode(cei.getCetCode());
			customFieldInstanceService.setCfValues(tempCei, cei.getCetCode(), oldCfValues);
			cei.setCfValuesOld(tempCei.getCfValues());
			
			// Update binaries
			if (CollectionUtils.isNotEmpty(binariesInSql)) {
				List<String> binariesFieldsToFetch = binariesInSql.stream().map(CustomFieldTemplate::getCode).collect(Collectors.toList());

				Map<String, Object> existingBinariesField = customTableService.findById(repository.getSqlConfigurationCode(), cei.getCet(), sqlUUID, binariesFieldsToFetch);
				fileSystemService.updateBinaries(repository, 
						cei.getUuid(), 
						cei.getCet(), 
						binariesInSql, 
						cei.getCfValuesAsValues(), 
						existingBinariesField);

			}
			
			customTableService.update(repository.getSqlConfigurationCode(), cei.getCet(), cei);
			customEntityInstanceUpdate.fire(cei);

		} else {
			String uuid = customTableService.create(repository.getSqlConfigurationCode(), cei.getCet(), cei);
			cei.setUuid(uuid);

			// Save binaries
			if (CollectionUtils.isNotEmpty(binariesInSql)) {

				final Map<CustomFieldTemplate, Object> binariesPaths = fileSystemService.updateBinaries(repository, uuid, cei.getCet(), binariesInSql, cei.getCfValuesAsValues(), null);

				for (Map.Entry<CustomFieldTemplate, Object> binary : binariesPaths.entrySet()) {
					customTableService.updateValue(repository.getSqlConfigurationCode(), cei.getTableName(), uuid, binary.getKey().getDbFieldname(), binary.getValue());
				}
			}
			
			customEntityInstanceCreate.fire(cei);
		}

		return cei.getUuid();
	}



}
