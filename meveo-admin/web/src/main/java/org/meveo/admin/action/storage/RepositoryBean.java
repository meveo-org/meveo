package org.meveo.admin.action.storage;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseCrudBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.storage.RepositoryApi;
import org.meveo.api.storage.RepositoryDto;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.storage.RepositoryService;

/**
 * Controller for managing {@link Repository} CRUD operations.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 **/
@Named
@ViewScoped
public class RepositoryBean extends BaseCrudBean<Repository, RepositoryDto> {

	private static final long serialVersionUID = 8661265102557481231L;

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private RepositoryApi repositoryApi;

	private Boolean forceDelete;

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
	public BaseCrudApi<Repository, RepositoryDto> getBaseCrudApi() {
		return repositoryApi;
	}

	public Boolean getForceDelete() {
		return forceDelete;
	}

	public void setForceDelete(Boolean forceDelete) {
		this.forceDelete = forceDelete;
	}

	@ActionMethod
	public String saveOrUpdate() throws BusinessException {
		String message = entity.isTransient() ? "save.successful" : "update.successful";

		if (entity.isTransient()) {
			repositoryService.create(entity);

		} else {
			repositoryService.update(entity);
		}

		messages.info(new BundleKey("messages", message));
		return back();
	}

	/**
	 * Deletes a given repository and it's children when value of forceDelete is
	 * true.
	 * 
	 * @param entity      record to be deleted
	 * @param forceDelete if true, delete the children
	 * @throws BusinessException failed entity deletion
	 */
	@ActionMethod
	public void delete(Repository entity, Boolean forceDelete) throws BusinessException {
		repositoryService.remove(entity, forceDelete);
	}

	@ActionMethod
	public String deleteAndNavigate() throws BusinessException {
		repositoryService.remove(entity);
		return getListViewName();
	}

	public List<Repository> listWithSqlConnection() {
		return repositoryService.listWithSqlConnection();
	}

}
