package org.meveo.service.admin.impl;

import javax.ejb.Stateless;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.service.base.PersistenceService;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
@Stateless
public class MeveoModuleItemService extends PersistenceService<MeveoModuleItem> {

	@Override
	public void create(MeveoModuleItem entity) throws BusinessException {
		super.create(entity);
	}
	
	public MeveoModuleItem findByBusinessEntity(BusinessEntity entity) {
    	if (!entity.getClass().isAnnotationPresent(ModuleItem.class)) {
    		return null;
    	}
    	
    	String appliesTo = null;
    	if (entity instanceof CustomFieldTemplate) {
    		appliesTo = ((CustomFieldTemplate) entity).getAppliesTo();
    	} else if (entity instanceof EntityCustomAction) {
    		appliesTo = ((EntityCustomAction) entity).getAppliesTo();
    	} else if (entity instanceof CustomEntityInstance) {
    		appliesTo = ((CustomEntityInstance) entity).getCetCode();
    	}
    	
    	String code;
    	if (entity instanceof CustomEntityInstance) {
    		code = ((CustomEntityInstance) entity).getUuid();
    	} else {
    		code = entity.getCode();
    	}
    	
    	
    	MeveoModuleItem item = null;
    	if (entity != null) {
			Session session = this.getEntityManager().unwrap(Session.class);
			String query = "FROM MeveoModuleItem mi "
    				+ "	WHERE mi.itemCode = :code "
    				+ "	AND mi.itemClass = :itemClass "
    				+ (appliesTo != null ? "	AND mi.appliesTo = :appliesTo" : "");
			
    		Query<MeveoModuleItem> q = session.createQuery(
    				query,
    				MeveoModuleItem.class);
    		q.setParameter("code", code);
    		q.setParameter("itemClass", entity.getClass().getName());
    		if (appliesTo != null) q.setParameter("appliesTo", appliesTo);
    		item = q.getResultStream().findFirst().orElse(null);
		}
    	
    	return item;
	}
}
