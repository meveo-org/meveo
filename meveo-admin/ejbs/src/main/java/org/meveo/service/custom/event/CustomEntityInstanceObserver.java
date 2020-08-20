package org.meveo.service.custom.event;

import java.io.IOException;

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
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.event.logging.LoggedEvent;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityInstanceAuditParameter;
import org.meveo.service.custom.CustomEntityInstanceAuditService;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
@Singleton
@Startup
@LoggedEvent
@Lock(LockType.READ)
public class CustomEntityInstanceObserver {

	@Inject
	private Logger log;

	@Inject
	private CustomEntityInstanceAuditService customEntityInstanceAuditService;
	
	@Inject
	private CustomFieldsCacheContainerProvider cache;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created CustomEntityInstance cei) {

		// log.debug("onCreated={}, cfValuesOld={}, cfValues={}",
		// cei.getCfValuesOldNullSafe(), cei.getCfValues());
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomEntityInstance cei)
			throws BusinessException, BusinessApiException, EntityDoesNotExistsException, IOException {

		if(cei.getCet() == null) {
			cei.setCet(cache.getCustomEntityTemplate(cei.getCetCode()));
		}
		
		if (cei.getCet().isAudited()) {
			log.debug("onUpdated cfValuesOld={}, cfValues={}", cei.getCfValuesOldNullSafe().getValues(), cei.getCfValues().getValues());
			CustomEntityInstanceAuditParameter param = new CustomEntityInstanceAuditParameter();
			param.setCode(cei.getCode());
			param.setDescription(cei.getDescription());
			param.setCetCode(cei.getCetCode());
			param.setOldValues(cei.getCfValuesOldNullSafe());
			param.setNewValues(cei.getCfValues());
			param.setCeiUuid(cei.getUuid());

			customEntityInstanceAuditService.auditChanges(param);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomEntityInstance cei) {

		log.debug("onRemoved={}", cei);
	}
}
