package org.meveo.service.storage;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.storage.Repository;
import org.meveo.service.base.BusinessService;

/**
 * Persistence layer for {@link Repository.}
 * 
 * @author Edward P. Legaspi
 */
@Stateless
public class RepositoryService extends BusinessService<Repository> {

	@Inject
	protected ResourceBundle resourceMessages;

	@SuppressWarnings("unchecked")
	public List<Repository> findByParent(Repository parentEntity) {
		QueryBuilder qb = new QueryBuilder(Repository.class, "r");
		qb.addCriterionEntity("parentRepository", parentEntity);

		return qb.getQuery(getEntityManager()).getResultList();
	}

	@Override
	public void create(Repository entity) throws BusinessException {
		super.create(entity);
		entity.setPath(computePath(entity));
	}

	@Override
	public Repository update(Repository entity) throws BusinessException {
		entity = super.update(entity);
		entity.setPath(computePath(entity));
		return entity;
	}

	public String computePath(Repository entity) {
		if (entity.getParentRepository() == null) {
			return String.valueOf(entity.getId());

		} else {
			Repository parentRepository = retrieveIfNotManaged(entity.getParentRepository());
			return String.format("%s.%d", computePath(parentRepository), entity.getId());
		}
	}

	@Override
	public void remove(Repository entity) throws BusinessException {
		List<Repository> result = findByParent(entity);
		if (result != null && !result.isEmpty()) {
			throw new BusinessException(resourceMessages.getString("repository.error.delete.parent"));
		}

		super.remove(entity);
	}
}
