package org.meveo.service.script;

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
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.UpdatedAfterTx;
import org.meveo.model.scripts.ScriptInstance;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
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
	 * Remove orphan maven dependencies
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onScriptUpdated(@Observes(during = TransactionPhase.AFTER_COMPLETION) @UpdatedAfterTx ScriptInstance si) {
		log.debug("[CDI event]  Trigger onScriptUpdated script instance with id={}", si.getId());
		mavenDependencyService.removeOrphans();
	}

	/**
	 * Remove orphan maven dependencies
	 */
	public void onScriptDeleted(@Observes @Removed ScriptInstance si) {
		mavenDependencyService.removeOrphans();
	}
}
