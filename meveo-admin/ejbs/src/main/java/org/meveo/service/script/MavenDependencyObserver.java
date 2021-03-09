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
import org.meveo.event.qualifier.RemovedAfterTx;
import org.meveo.model.scripts.MavenDependency;
import org.slf4j.Logger;

/**
 * @author Tony Alejandro | tonysviews@gmail.com
 * @version 6.14.0
 */
@Singleton
@Startup
@LoggedEvent
@Lock(LockType.READ)
public class MavenDependencyObserver {

	@Inject
	private Logger log;

	@Inject
	private MavenDependencyService mavenDependencyService;

	/**
	 * Triggered after a maven dependency is deleted
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onAfterDelete(@Observes(during = AFTER_COMPLETION) @RemovedAfterTx MavenDependency mavenDependency) {
		log.debug("[CDI event]  Trigger onAfterDelete maven dependency={}", mavenDependency.getCoordinates());
		mavenDependencyService.removeDependencyFromClassLoader(mavenDependency);
	}
}
