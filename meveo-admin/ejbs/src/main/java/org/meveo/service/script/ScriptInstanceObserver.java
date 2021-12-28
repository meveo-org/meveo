package org.meveo.service.script;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.meveo.event.logging.LoggedEvent;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.PostRemoved;
import org.meveo.event.qualifier.UpdatedAfterTx;
import org.meveo.model.scripts.ScriptInstance;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
@LoggedEvent
@Lock(LockType.READ)
public class ScriptInstanceObserver {

	@Inject
	private transient Logger log;

	@Inject
	private transient MavenDependencyService mavenDependencyService;
	
	@Inject
	private transient ScriptInstanceService scriptInstanceService;
	
	/**
	 * Remove orphan maven dependencies
	 */
	@Transactional(TxType.REQUIRES_NEW)
	public void onScriptUpdated(@Observes(during = TransactionPhase.AFTER_COMPLETION) @UpdatedAfterTx ScriptInstance si) {
		log.debug("[CDI event]  Trigger onScriptUpdated script instance with id={}", si.getId());
		mavenDependencyService.removeOrphans(si);
	}

	/**
	 * Remove orphan maven dependencies
	 */
	@Transactional
	public void onScriptDeleted(@Observes @PostRemoved ScriptInstance si) {
		mavenDependencyService.removeOrphans(si);
	}
	
	public void removeScriptFileOnFailure(@Observes(during = TransactionPhase.AFTER_FAILURE) @Created ScriptInstance scriptInstance) {
		scriptInstanceService.removeScriptFile(scriptInstance);
	}
}
