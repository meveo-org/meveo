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

import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.NonUniqueResultException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

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
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageTransaction;
import org.meveo.persistence.PersistenceActionResult;
import org.meveo.persistence.StorageImpl;
import org.meveo.persistence.StorageQuery;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.persistence.sql.SQLConnectionProvider;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomTableCreatorService;
import org.meveo.service.custom.CustomTableRelationService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.storage.FileSystemService;
import org.meveo.util.PersistenceUtils;
import org.slf4j.Logger;

import liquibase.pro.packaged.S;

@RequestScoped
public class SQLStorageImpl implements StorageImpl {
	
	private UserTransaction userTx;
	
	private Map<String, org.hibernate.Session> hibernateSessions = new HashMap<>();
	
	@Inject
	private SQLConnectionProvider sqlConnectionProvider;
	
	@Inject
	private Logger log;
	
	@Inject
	private CustomTableRelationService customTableRelationService;
	
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
	private FileSystemService fileSystemService;
	
    @Inject
    @Updated
    private Event<CustomEntityInstance> customEntityInstanceUpdate;
    
    @Inject
    @Created
    private Event<CustomEntityInstance> customEntityInstanceCreate;
    
    @Inject
    private CustomTableCreatorService customTableCreatorService;
    
    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCache;

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
		cei.setRepository(repository);
		
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

				if(!uniqueCfts.isEmpty()) {
					Map<String, Object> uniqueValues = new HashMap<>();
					uniqueCfts.forEach(cft -> {
						var value = valuesFilters.get(cft.getCode());
						if(value != null) {
							uniqueValues.put(cft.getCode(), value);
						}
					});
				
					if(!uniqueValues.isEmpty()) {
						uuid = customTableService.findIdByUniqueValues(repository.getSqlConfigurationCode(), cet, uniqueValues, cfts.values());
					}
				}
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
		
		return uuid;
	}

	@Override
	public Map<String, Object> findById(Repository repository, CustomEntityTemplate cet, String uuid, Map<String, CustomFieldTemplate> cfts, Collection<String> fetchFields, boolean withEntityReferences) {
		boolean foundEntity = false;
		Map<String, Object> values = new HashMap<>();
		
		try {
			transaction.beginTransaction(repository, List.of(getStorageType()));

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

			transaction.commitTransaction(repository, List.of(getStorageType()));
		} catch (Exception e) {
			if(e instanceof EntityDoesNotExistsException) {
				return null;
			}
			
			transaction.rollbackTransaction(e, List.of(getStorageType()));
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
		PaginationConfiguration paginationConfiguration = query.getPaginationConfiguration();
		CustomEntityTemplate cet = query.getCet();
		var filters = query.getFilters();
		var actualFetchFields = query.getFetchFields();
		
		// In case where only graphql query is passed, we will only use it so we won't
		// fetch sql
		boolean dontFetchSql = paginationConfiguration != null && paginationConfiguration.getFilters() == null && paginationConfiguration.getGraphQlQuery() != null;

		boolean hasSqlFetchField = paginationConfiguration != null && actualFetchFields != null && actualFetchFields.stream().anyMatch(s -> customTableService.sqlCftFilter(cet, s));

		boolean hasSqlFilter = paginationConfiguration != null && paginationConfiguration.getFilters() != null && filters.keySet().stream().anyMatch(s -> customTableService.sqlCftFilter(cet, s));
		
		if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.SQL) && !dontFetchSql && (query.isFetchAllFields() || hasSqlFetchField || hasSqlFilter)) {

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
		
		return null;
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
				cei.setRepository(repository);
				
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

	public DBStorageType getStorageType() {
		return DBStorageType.SQL;
	}
	
	
	@Override
	public Integer count(Repository repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {
		final List<String> actualFetchFields = paginationConfiguration == null ? null : paginationConfiguration.getFetchFields();

		final Map<String, Object> filters = paginationConfiguration == null ? null : paginationConfiguration.getFilters();

		
		// If no pagination nor fetch fields are defined, we consider that we must fetch
		// everything
		boolean fetchAllFields = paginationConfiguration == null || actualFetchFields == null;

		// In case where only graphql query is passed, we will only use it so we won't
		// fetch sql
		boolean dontFetchSql = paginationConfiguration != null && paginationConfiguration.getFilters() == null && paginationConfiguration.getGraphQlQuery() != null;

		boolean hasSqlFetchField = paginationConfiguration != null && actualFetchFields != null && actualFetchFields.stream().anyMatch(s -> customTableService.sqlCftFilter(cet, s));

		boolean hasSqlFilter = paginationConfiguration != null && paginationConfiguration.getFilters() != null && filters.keySet().stream().anyMatch(s -> customTableService.sqlCftFilter(cet, s));

		if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.SQL) && !dontFetchSql && (fetchAllFields || hasSqlFetchField || hasSqlFilter)) {
			final String dbTablename = SQLStorageConfiguration.getDbTablename(cet);

			if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
				return (int) customTableService.count(repository.getSqlConfigurationCode(), dbTablename, paginationConfiguration);

			} else {
				return (int) customEntityInstanceService.count(cet.getCode(), paginationConfiguration);
			}
		}
		
		return null;
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
		ceiToSave.setRepository(repository);
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
			cei.setRepository(repository);
			
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
		cei.setRepository(repository);
		
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
			if (sqlUUID != null) {
				oldCfValues = customTableService.findById(repository.getSqlConfigurationCode(), cei.getCet(), sqlUUID);
			}
		}
		
		if (sqlUUID != null) {
			
			cei.setUuid(sqlUUID);
			
			CustomEntityInstance tempCei = new CustomEntityInstance();
			tempCei.setCetCode(cei.getCetCode());
			tempCei.setRepository(repository);
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

	@Override
	public void cetCreated(CustomEntityTemplate cet) {
        if (cet.getSqlStorageConfiguration() != null && cet.getSqlStorageConfiguration().isStoreAsTable()) {
	        customTableCreatorService.createTable(cet);
        }
	}

	@Override
	public void crtCreated(CustomRelationshipTemplate crt) throws BusinessException {
        if(crt.getAvailableStorages().contains(DBStorageType.SQL)) {
        	customTableCreatorService.createCrtTable(crt);
        }
	}

	@Override
	public void cftCreated(CustomModelObject template, CustomFieldTemplate cft) {
		if (template.getAvailableStorages().contains(DBStorageType.SQL)) {
			if (template instanceof CustomEntityTemplate) {
				CustomEntityTemplate cet = (CustomEntityTemplate) template;
				if (cet.getSqlStorageConfiguration().isStoreAsTable()) {
					customTableCreatorService.addField(template, cft);
				}
			} else {
				customTableCreatorService.addField(template, cft);
			}
		}

		
	}

	@Override
	public void removeCft(CustomModelObject template, CustomFieldTemplate cft) {
		if (template.getAvailableStorages().contains(DBStorageType.SQL)) {
			if (template instanceof CustomEntityTemplate) {
				CustomEntityTemplate cet = (CustomEntityTemplate) template;
				 if (cet.getSqlStorageConfiguration() != null && cet.getSqlStorageConfiguration().isStoreAsTable()) {
		            customTableCreatorService.removeField(cet, cft);
				}
			} else {
	            customTableCreatorService.removeField(template, cft);
			}
		}
		
	}

	@Override
	public void removeCet(CustomEntityTemplate cet) {
		if (cet.getSqlStorageConfiguration() != null && cet.getSqlStorageConfiguration().isStoreAsTable()) {
            customTableCreatorService.removeTable(cet);
	    } else if (cet.getSqlStorageConfiguration() != null) {
	        customEntityInstanceService.removeByCet(cet.getCode());
	    }
	}

	@Override
	public void removeCrt(CustomRelationshipTemplate crt) {
        if(crt.getAvailableStorages().contains(DBStorageType.SQL)) {
        	for (Repository r : crt.getRepositories()) {
                customTableCreatorService.removeTable(r.getSqlConfigurationCode(), SQLStorageConfiguration.getDbTablename(crt));
        	}
        }
	}

	@Override
	public void cetUpdated(CustomEntityTemplate oldCet, CustomEntityTemplate cet) {
        var sqlConfs = cet.getRepositories();
        
        // Handle SQL inheritance
        if(cet.storedIn(DBStorageType.SQL)) {
        	if(oldCet.getSuperTemplate() != null && cet.getSuperTemplate() == null) {
        		// Inheritance removed
        		sqlConfs.forEach(sc -> customTableCreatorService.removeInheritance(sc.getCode(), cet));
        	} else if(oldCet.getSuperTemplate() == null && cet.getSuperTemplate() != null) {
        		// Inheritance added
        		sqlConfs.forEach(sc -> customTableCreatorService.addInheritance(sc.getCode(), cet));
        	}
        }
	}

	@Override
	public void crtUpdated(CustomRelationshipTemplate crt) throws BusinessException {
        // SQL Storage logic
        if(crt.getAvailableStorages().contains(DBStorageType.SQL)) {
        	boolean created = customTableCreatorService.createCrtTable(crt);
        	// Create the custom fields for the table if the table has been created
        	if(created) {
        		for(CustomFieldTemplate cft : customFieldTemplateService.findByAppliesTo(crt.getAppliesTo()).values()) {
    				customTableCreatorService.addField(crt, cft);
        		}
        	}
        }else {
            // Remove table if storage previously contained SQL
            if(customFieldsCache.getCustomRelationshipTemplate(crt.getCode()).getAvailableStorages().contains(DBStorageType.SQL)) {
                customTableCreatorService.removeTable(crt);
            }
        }
	}

	@Override
	public void cftUpdated(CustomModelObject template, CustomFieldTemplate oldCft, CustomFieldTemplate cft) {
		if (template instanceof CustomEntityTemplate) {
			CustomEntityTemplate cet = (CustomEntityTemplate) template;
			if (cet.getSqlStorageConfiguration() != null && cet.getSqlStorageConfiguration().isStoreAsTable() && cet.getAvailableStorages().contains(DBStorageType.SQL)) {
	            customTableCreatorService.updateField(cet, cft);
			
			} else if(cet.getAvailableStorages() != null && !cet.getAvailableStorages().contains(DBStorageType.SQL) && oldCft != null && oldCft.getStoragesNullSafe() != null && oldCft.getStoragesNullSafe().contains(DBStorageType.SQL)) {
				customTableCreatorService.removeField(cet, cft);
			}
		} else {
			if (template.getAvailableStorages().contains(DBStorageType.SQL)) {
	            customTableCreatorService.updateField(template, cft);
			} else if(!template.getAvailableStorages().contains(DBStorageType.SQL) && oldCft.getStoragesNullSafe()!= null && oldCft.getStoragesNullSafe().contains(DBStorageType.SQL)) {
				customTableCreatorService.removeField(template, cft);
			}
		}
		
	}


	@Override
	public PersistenceActionResult addCRTByUuids(Repository repository, CustomRelationshipTemplate crt, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws BusinessException {
		// SQL Storage
		if (crt.getAvailableStorages().contains(DBStorageType.SQL)) {
			String relationUuid = customTableRelationService.createOrUpdateRelation(repository, crt, sourceUuid, targetUuid, relationValues);
			return new PersistenceActionResult(relationUuid);
		}
		
		return null;
	}

	@Override
	public void init() {
		// User transaction might be managed by container instead of bean
		try {
			userTx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		} catch (Exception e) {
			// NOOP
		}
	}

	@Override
	public <T> T beginTransaction(Repository repository, int stackedCalls) {
		try {
			if(userTx != null && userTx.getStatus() == Status.STATUS_NO_TRANSACTION && stackedCalls == 0) {
				userTx.begin();
			}
			
			if(repository.getSqlConfiguration() != null) {
				return (T) getHibernateSession(repository.getSqlConfigurationCode());
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return null;
	}

	@Override
	public void commitTransaction(Repository repository) {
		try {
			if(userTx != null) {
				userTx.commit();
			}
		} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void rollbackTransaction(int stackedCalls) {
		try {
			if(userTx != null && userTx.getStatus() == Status.STATUS_ACTIVE) {
				if(stackedCalls == 0) {
					userTx.rollback();
				} else {
					userTx.setRollbackOnly();
				}
			}
		} catch (SecurityException | IllegalStateException | SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@PreDestroy
	public void destroy() {
		try {
			hibernateSessions.values().forEach(s -> {
				if (s.isOpen()) {
					s.close();
				}
			});
			hibernateSessions.clear();
			if(userTx != null && userTx.getStatus() == Status.STATUS_ACTIVE) {
				userTx.commit();
			}
		} catch (Exception e) {
			log.error("Error destroying {}", this, e);
		}
	}
	
	public org.hibernate.Session getHibernateSession(String repository) {
		try {
			if(userTx != null && userTx.getStatus() == Status.STATUS_NO_TRANSACTION) {
				userTx.begin();
			}
			return hibernateSessions.computeIfAbsent(repository, sqlConnectionProvider::getSession);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
