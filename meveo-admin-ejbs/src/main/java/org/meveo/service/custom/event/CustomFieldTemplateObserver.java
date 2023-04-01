package org.meveo.service.custom.event;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.event.logging.LoggedEvent;
import org.meveo.event.qualifier.Removed;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.service.admin.impl.MeveoModuleItemService;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.BusinessServiceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
@Startup
@LoggedEvent
@Lock(LockType.READ)
public class CustomFieldTemplateObserver {

	private static Logger log = LoggerFactory.getLogger(CustomFieldTemplateObserver.class);

    @Inject
    private BusinessServiceFinder businessServiceFinder;
	
	@Inject
	private MeveoModuleItemService meveoModuleItemService;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomFieldTemplate cft) throws BusinessException {

		log.debug("CFT onRemoved observer={}", cft);
		BusinessService businessService = businessServiceFinder.find(cft);
		MeveoModule module = businessService.findModuleOf(cft);
    	try {
    		if (module != null) {
    			businessService.removeFilesFromModule(cft, module);
				MeveoModuleItem item = meveoModuleItemService.findByBusinessEntity(cft);
				if (item != null) {
					module.removeItem(item);
				}
    		}
		} catch (BusinessException e) {
			throw new BusinessException("CFT: " + cft.getCode() + " cannot be removed");
		}

	}



}
