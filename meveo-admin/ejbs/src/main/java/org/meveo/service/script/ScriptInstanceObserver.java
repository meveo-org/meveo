package org.meveo.service.script;

import static javax.enterprise.event.TransactionPhase.AFTER_COMPLETION;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.meveo.event.logging.LoggedEvent;
import org.meveo.event.qualifier.CreatedAfterTx;
import org.meveo.event.qualifier.RemovedAfterTx;
import org.meveo.event.qualifier.UpdatedAfterTx;
import org.meveo.model.scripts.ScriptInstance;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author Tony Alejandro | tonysviews@gmail.com
 * @version 6.11.0
 */
@Singleton
@Startup
@LoggedEvent
@Lock(LockType.READ)
public class ScriptInstanceObserver {

	@Inject
	private Logger log;

	@Inject
	private MavenDependencyService mavenDependencyService;

	/**
	 * Triggered after a script is created
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onAfterCreate(@Observes(during = AFTER_COMPLETION) @CreatedAfterTx ScriptInstance scriptInstance) {
		log.debug("[CDI event]  Trigger onAfterCreate script instance with id={}", scriptInstance.getId());
		mavenDependencyService.removeOrphans();
	}

	/**
	 * Triggered after a script is updated
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onAfterUpdate(@Observes(during = AFTER_COMPLETION) @UpdatedAfterTx ScriptInstance scriptInstance) {
		log.debug("[CDI event]  Trigger onAfterUpdate script instance with id={}", scriptInstance.getId());
		mavenDependencyService.removeOrphans();
	}

	/**
	 * Triggered after a script instance is deleted
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onAfterDelete(@Observes(during = AFTER_COMPLETION) @RemovedAfterTx ScriptInstance scriptInstance) {
		log.debug("[CDI event]  Trigger onAfterDelete script instance with id={}", scriptInstance.getId());
		mavenDependencyService.removeOrphans();
	}
}
