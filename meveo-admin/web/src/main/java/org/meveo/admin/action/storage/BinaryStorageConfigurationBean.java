package org.meveo.admin.action.storage;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.action.BaseCrudBean;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.storage.BinaryStorageConfigurationApi;
import org.meveo.api.storage.BinaryStorageConfigurationDto;
import org.meveo.model.storage.BinaryStorageConfiguration;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.storage.BinaryStorageConfigurationService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 */
@Named
@ViewScoped
public class BinaryStorageConfigurationBean extends BaseCrudBean<BinaryStorageConfiguration, BinaryStorageConfigurationDto> {

	private static final long serialVersionUID = -5910309600957099205L;

	@Inject
	private BinaryStorageConfigurationService binaryStorageConfigurationService;
	
	@Inject
	private BinaryStorageConfigurationApi api;

	public BinaryStorageConfigurationBean() {
		super(BinaryStorageConfiguration.class);
	}

	@Override
	protected IPersistenceService<BinaryStorageConfiguration> getPersistenceService() {
		return binaryStorageConfigurationService;
	}

	@Override
	public BaseCrudApi<BinaryStorageConfiguration, BinaryStorageConfigurationDto> getBaseCrudApi() {
		return api;
	}
}
