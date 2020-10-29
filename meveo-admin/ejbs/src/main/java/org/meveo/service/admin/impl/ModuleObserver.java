/**
 * 
 */
package org.meveo.service.admin.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.event.qualifier.Removed;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.module.MeveoModuleItem;
import org.slf4j.Logger;

@RequestScoped
public class ModuleObserver {

    @PersistenceUnit(unitName = "MeveoAdmin")
	private EntityManagerFactory emf;
	
	@Inject
	private Logger log;
	
	private List<BusinessEntity> entities = new ArrayList<>();
	
	@PreDestroy
	private void onDestroy() {
		var em = emf.createEntityManager();
		
		try {
			for(var be : entities) {
				if (be.getClass().isAnnotationPresent(ModuleItem.class)) {
					QueryBuilder qb = new QueryBuilder(MeveoModuleItem.class, "i");
					qb = qb.addCriterion("itemCode", "=", be.getCode(), true);
					qb = qb.addCriterion("itemClass", "=", be.getClass().getName(), true);
		
					try {
						
						List<MeveoModuleItem> items = (List<MeveoModuleItem>) qb.find(em);
						items.forEach(em::remove);
					} catch (NoResultException e) {
		
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to remove module items : ", e);
		} finally {
			em.close();
		}
		
	}
	
	/**
	 * Observer when an entity that extends a BusinessEntity is deleted which is
	 * annotated by MeveoModuleItem.
	 *
	 * @param be BusinessEntity
	 * @throws BusinessException
	 */
	public void onMeveoModuleItemDelete(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed BusinessEntity be) throws BusinessException {
		if (be.getClass().isAnnotationPresent(ModuleItem.class)) {
			entities.add(be);
		}
	}
}
