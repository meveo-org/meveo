package org.meveo.admin.action.storage;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.action.BaseBean;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.storage.RepositoryApi;
import org.meveo.model.storage.Repository;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.storage.RepositoryService;

/**
 * Controller for managing {@link Repository} CRUD operations.
 * 
 * @author Edward P. Legaspi
 */
@Named
@ViewScoped
public class RepositoryBean extends BaseBean<Repository> {

	private static final long serialVersionUID = 8661265102557481231L;

	@Inject
	private RepositoryService repositoryService;
	
	@Inject
	private RepositoryApi repositoryApi;
	
	public RepositoryBean() {
		super(Repository.class);
	}

	@Override
	public String getEditViewName() {
		return "repositoryDetail";
	}

	@Override
	protected String getListViewName() {
		return "repositories";
	}

	@Override
	protected IPersistenceService<Repository> getPersistenceService() {
		return repositoryService;
	}

	@Override
	public BaseCrudApi<Repository, ?> getBaseCrudApi() {
		return repositoryApi;
	}
	
	
}
