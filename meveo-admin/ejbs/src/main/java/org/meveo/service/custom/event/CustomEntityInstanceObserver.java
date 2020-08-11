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

import org.meveo.event.logging.LoggedEvent;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.customEntities.CustomEntityInstance;
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

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created CustomEntityInstance cei) {

		log.debug("onCreated={}", cei);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomEntityInstance cei) {

		log.debug("onUpdated={}", cei);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomEntityInstance cei) {

		log.debug("onRemoved={}", cei);
	}
}
