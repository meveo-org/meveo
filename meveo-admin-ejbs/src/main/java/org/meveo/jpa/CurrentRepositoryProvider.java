/**
 * 
 */
package org.meveo.jpa;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;

/**
 * Provider used to set current user repository for a given session
 * Mainly used in GUI.
 * 
 * @author clement.bareth
 * @since 6.8.0
 * @version 6.9.0
 */
@SessionScoped
@Named
public class CurrentRepositoryProvider implements Serializable {
	
	private static final long serialVersionUID = -1161560465567580602L;

	@Inject
	private transient RepositoryService repositoryService;

	@Inject
	private transient Logger log;
	
	private Repository repository;

	@Produces @SessionScoped
	public Repository getRepository() {
		return repository != null ? repository : repositoryService.findDefaultRepository();
	}

	public void setRepository(Repository repository) {
		if(repository == null) {
			this.repository = repositoryService.findDefaultRepository();
		} else {
			this.repository = repository;
		}
		
		log.info("Setting current repository to {}", this.repository.getCode());
	}
	
}
