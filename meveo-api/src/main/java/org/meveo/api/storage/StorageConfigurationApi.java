/**
 * 
 */
package org.meveo.api.storage;

import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.model.storage.StorageConfiguration;
import org.meveo.persistence.DBStorageTypeService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.storage.StorageConfigurationService;

@Named("StorageConfigurationApi")
public class StorageConfigurationApi extends BaseCrudApi<StorageConfiguration, StorageConfigurationDto> {

	@Inject
	private StorageConfigurationService configurationService;
	
	@Inject
	private DBStorageTypeService dbStorageTypeService;
	
	public StorageConfigurationApi() {
		super(StorageConfiguration.class, StorageConfigurationDto.class);
	}
	
	@Override
	public StorageConfiguration fromDto(StorageConfigurationDto dto, StorageConfiguration entity) throws MeveoApiException, BusinessException {
		entity = super.fromDto(dto, entity);
		entity.setDbStorageType(dbStorageTypeService.find(dto.getDbStorageType()));
		return entity;
	}
	
	@Override
	public IPersistenceService<StorageConfiguration> getPersistenceService() {
		return configurationService;
	}
}
