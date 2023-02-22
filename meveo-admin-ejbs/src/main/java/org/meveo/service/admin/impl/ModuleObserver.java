/**
 * 
 */
package org.meveo.service.admin.impl;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.exception.InvalidScriptException;
import org.meveo.event.qualifier.Created;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ModulePostPull;
import org.meveo.model.ModulePrePull;
import org.meveo.model.admin.MvCredential;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.storage.Repository;
import org.meveo.model.storage.StorageConfiguration;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.script.module.ModuleScriptInterface;
import org.meveo.service.script.module.ModuleScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class ModuleObserver {

    @PersistenceUnit(unitName = "MeveoAdmin")
	private EntityManagerFactory emf;
	
	private static Logger log = LoggerFactory.getLogger(ModuleObserver.class);
	
	@Inject
	private MeveoModuleService moduleService;
	
	@Inject
	@CurrentUser
	private MeveoUser currentUser;
	
	@Inject
	private ModuleInstallationContext moduleInstallationContext;
	
	@Inject
	private ModuleScriptService moduleScriptService;
	
	public void addItemToCurrentUserModule(@Observes @Created BusinessEntity itemEntity) {
		{
			Class<?>[] ignoredClasses = { 
					MeveoModule.class, 
					CustomEntityInstance.class, 
					MvCredential.class,
					GitRepository.class,
					Repository.class,
					StorageConfiguration.class
			};
			
			for (Class<?> ignoredClass : ignoredClasses) {
				if (ignoredClass.isInstance(itemEntity)) {
					return;
				}
			}
		}
		
		if (!moduleInstallationContext.isActive() && currentUser != null && currentUser.getCurrentModule() != null && !currentUser.getCurrentModule().equals("Meveo")) {
			MeveoModule module = moduleService.findByCode(currentUser.getCurrentModule());
            MeveoModuleItem item = new MeveoModuleItem(itemEntity);
            
            String appliesTo = null;
        	if (itemEntity instanceof CustomFieldTemplate) {
        		appliesTo = ((CustomFieldTemplate) itemEntity).getAppliesTo();
        	} else if (itemEntity instanceof EntityCustomAction) {
        		appliesTo = ((EntityCustomAction) itemEntity).getAppliesTo();
        	}
        	item.setAppliesTo(appliesTo);
            
        	try {
        		moduleService.addModuleItem(item, module);
        	} catch (BusinessException e) {
        		log.error("Entity cannot be add to the module", e);
        	}

		}
	}
	
	public void prePull(@Observes @ModulePrePull MeveoModule module) throws ElementNotFoundException, InvalidScriptException, BusinessException {
		if (module.getScript() != null) {
		    moduleScriptService.prePull(module.getScript().getCode(), module);;
		}
	}
	
	public void postPull(@Observes @ModulePostPull MeveoModule module) throws ElementNotFoundException, InvalidScriptException, BusinessException {
    	if (module.getScript() != null) {
    		moduleScriptService.postPull(module.getScript().getCode(), module);
    	}
	}
		
}
