package org.meveo.service.script;

import static javax.enterprise.event.TransactionPhase.AFTER_COMPLETION;
import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.meveo.event.logging.LoggedEvent;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.RemovedAfterTx;
import org.meveo.event.qualifier.UpdatedAfterTx;
import org.meveo.model.scripts.MavenDependency;
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
    public void onScriptUpdated(@Observes(during = AFTER_COMPLETION) @UpdatedAfterTx ScriptInstance scriptInstance) {
	log.debug("[CDI event]  Trigger onScriptUpdated script instance with id={}", scriptInstance.getId());
	mavenDependencyService.removeOrphans();
    }

    /**
     * Remove orphan maven dependencies
     */
    public void onScriptDeleted(@Observes @Removed ScriptInstance scriptInstance) {
	log.debug("onScriptDeleted: {}", scriptInstance);
	// force the retrieval of the dependencies here, otherwise, it won't be available
	// for lazy load later on AFTER_COMPLETION
	List<MavenDependency> dependenciesToRemove = mavenDependencyService
		.findScriptRelatedDependencies(scriptInstance);
	log.debug("dependenciesToRemove: {}", dependenciesToRemove);
    }

    /**
     * Remove orphan maven dependencies
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onAfterScriptDeleted(@Observes(during = AFTER_COMPLETION) @RemovedAfterTx ScriptInstance scriptInstance) {
	log.debug("onAfterScriptDeleted: {}", scriptInstance);
	long scriptId = scriptInstance.getId();
	List<MavenDependency> dependenciesToRemove = mavenDependencyService.findScriptRelatedDependencies(scriptInstance);
	log.debug("scriptId: {}", scriptId);
	log.debug("dependenciesToRemove: {}", dependenciesToRemove);
	mavenDependencyService.removeScriptRelatedDependencies(dependenciesToRemove);
	mavenDependencyService.removeDependenciesFromClassLoader(dependenciesToRemove);
    }
}
