/**
 * 
 */
package org.meveo.admin.action.storage;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.action.BaseCrudBean;
import org.meveo.admin.action.admin.custom.CustomFieldDataEntryBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.storage.StorageConfigurationApi;
import org.meveo.api.storage.StorageConfigurationDto;
import org.meveo.elresolver.ELException;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.StorageConfiguration;
import org.meveo.persistence.DBStorageTypeService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.storage.StorageConfigurationService;

@Named
@ViewScoped
public class StorageConfigurationBean extends BaseCrudBean<StorageConfiguration, StorageConfigurationDto> {

	private static final long serialVersionUID = 6594956459968308089L;
	
    @Inject
    protected CustomFieldDataEntryBean customFieldDataEntryBean;

	@Inject
	private transient StorageConfigurationService service;
	
	@Inject
	private transient StorageConfigurationApi api;
	
	@Inject
	private transient DBStorageTypeService dbStorageTypeService;
	
    public StorageConfigurationBean() {
    	super(StorageConfiguration.class);
    }
    
    public List<DBStorageType> getDbStorageTypes() {
    	return dbStorageTypeService.list();
    }
	
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

	@Override
	public String saveOrUpdate() throws BusinessException, ELException {
		customFieldDataEntryBean.saveCustomFieldsToEntity((ICustomFieldEntity) entity, entity.isTransient());
		return super.saveOrUpdate();
	}

}
