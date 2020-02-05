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

/**
 * 
 * @author clement.bareth
 * @since 6.8.0
 * @version 6.8.0
 */
@SessionScoped
@Named
public class CurrentRepositoryProvider implements Serializable {
	
	@Inject
	private RepositoryService repositoryService;
	
	private Repository repository;

	@Produces @SessionScoped
	public Repository getRepository() {
		return repository != null ? repository : repositoryService.findDefaultRepository();
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	
	
}
