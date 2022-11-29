package org.meveo.admin.action;

import org.meveo.model.IEntity;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.service.admin.impl.MeveoModuleFilters;
import org.meveo.service.admin.impl.MeveoModuleService;

@Named
public abstract class ModuleItemBaseBean<T extends IEntity> extends BaseBean<T>{

    private String workingModule;

    @Inject
    private MeveoModuleService meveoModuleService;

    protected ModuleItemBaseBean() {
        super();
    }
    
    protected ModuleItemBaseBean(Class<T> clazz) {
    	super(clazz);
    }

    @PostConstruct
	@Override
    public void init() {
		super.init();
        this.workingModule = super.getUserCurrentModule();
        this.filters.put("moduleBelonging", workingModule);
    }

    @Override
	public void search() {		
		this.filters.put("moduleBelonging", workingModule);
        super.search();
	}

    public List<String> getMeveoModulesCodes() {
		var filters = new MeveoModuleFilters();
		filters.setIsInDraft(true);
		return meveoModuleService.listCodesOnly(filters);
	}

    public String getWorkingModule() {
        return this.workingModule;
    }

    public void setWorkingModule(String workingModule) {
        this.workingModule = workingModule;
    }
}
