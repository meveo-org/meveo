/**
 * 
 */
package org.meveo.admin.patches;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.meveo.admin.listener.MeveoInitializer;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.patch.PatchExecution;
import org.slf4j.Logger;

/**
 * Service to execute patches
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
public class PatchExecutionService implements MeveoInitializer {

	@Inject
	@MeveoJpa
	private EntityManagerWrapper emWrapper;
	
	@Inject
	@Any
	private Instance<Patch> patches;
	
	@Inject
	private Logger log;
	
	/**
	 * Execute every non-executed patch
	 */
	public void init() {
		for(Patch patch : getPatchesToExecute()) {
			try {
				log.info("Running patch {}", patch.name());
				patch.execute();
				
				PatchExecution patchExecution = new PatchExecution();
				patchExecution.setName(patch.name());
				patchExecution.setRan(true);
				emWrapper.getEntityManager().merge(patchExecution);
				log.info("Successfully ran patch {}", patch.name());

			} catch (Exception e) {
				log.error("Failed to execute patch {}", patch.name(),e);
				break;
			}
		}
	}
	
	private Collection<Patch> getPatchesToExecute() {
		List<String> executedPatches = emWrapper.getEntityManager()
			.createQuery("SELECT name FROM PatchExecution WHERE ran = true", String.class)
			.getResultList();
		
		return patches.stream().filter(patch -> !executedPatches.contains(patch.name()))
				.sorted((patch1, patch2) -> patch1.order() - patch2.order())
				.collect(Collectors.toList());
	}
}
