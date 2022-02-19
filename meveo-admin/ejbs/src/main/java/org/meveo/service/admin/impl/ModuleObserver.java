/**
 * 
 */
package org.meveo.service.admin.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.meveo.admin.exception.BusinessException;
import org.meveo.event.qualifier.PostRemoved;
import org.meveo.event.qualifier.Removed;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.BusinessServiceFinder;
import org.slf4j.Logger;

@RequestScoped
public class ModuleObserver {

    @PersistenceUnit(unitName = "MeveoAdmin")
	private EntityManagerFactory emf;
	
	@Inject
	private Logger log;
		
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Transactional(value = TxType.REQUIRES_NEW)
	public void onItemDelete(@Observes(during = TransactionPhase.AFTER_SUCCESS) @PostRemoved MeveoModuleItem item, BusinessServiceFinder bsf, MeveoModuleService moduleService) {
		if (item.getMeveoModule() == null) {
			return;
		}
		
		MeveoModule meveoModule = moduleService.findById(item.getMeveoModule().getId());
		// Module has been deleted, so as the git repository and it's items so we don't need to remove them
		if (meveoModule == null) {
			return;
		}
		
		try {
			moduleService.loadModuleItem(item);
		} catch (BusinessException e1) {
			log.info("Can't load entity for item {}", e1);
		}
		
		BusinessEntity entity = item.getItemEntity();
		
		if (entity != null) {
			MeveoModule mainModule = moduleService.findByCode("Meveo");
			BusinessService businessService = bsf.find(entity);
			
			try {
				businessService.moveFilesToModule(entity, item.getMeveoModule(), mainModule);
			} catch (BusinessException | IOException e) {
				log.info("Fail to move files to main module", e);
			}
		}

	}
}
