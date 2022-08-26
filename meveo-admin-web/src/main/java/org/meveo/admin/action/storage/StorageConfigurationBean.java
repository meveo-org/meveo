/**
 * 
 */
package org.meveo.admin.action.storage;

import javax.inject.Inject;

import org.meveo.admin.action.BaseCrudBean;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.storage.StorageConfigurationApi;
import org.meveo.api.storage.StorageConfigurationDto;
import org.meveo.model.storage.StorageConfiguration;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.storage.StorageConfigurationService;

public class StorageConfigurationBean extends BaseCrudBean<StorageConfiguration, StorageConfigurationDto> {

	private static final long serialVersionUID = 6594956459968308089L;

	@Inject
	private transient StorageConfigurationService service;
	
	@Inject
	private transient StorageConfigurationApi api;
	
	@Override
	public String getEditViewName() {
		return "storageConfigurationDetail";
	}
	
	@Override
	protected String getListViewName() {
		return "storageConfigurations";
	}
	
	@Override
	public BaseCrudApi<StorageConfiguration, StorageConfigurationDto> getBaseCrudApi() {
		return api;
	}

	@Override
	protected IPersistenceService<StorageConfiguration> getPersistenceService() {
		return service;
	}

}
