package org.meveo.service.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;

import org.eclipse.aether.RepositoryException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.admin.User;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.hierarchy.UserHierarchyLevel;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;
import org.meveo.service.admin.impl.UserService;
import org.meveo.service.base.BusinessService;
import org.meveo.service.hierarchy.impl.UserHierarchyLevelService;

/**
 * Persistence layer for {@link Repository}
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.13
 * @since 6.4.0
 */
@Stateless
public class RepositoryService extends BusinessService<Repository> {

	@Inject
	protected ResourceBundle resourceMessages;

	@Inject
	private UserService userService;

	@Inject
	private UserHierarchyLevelService userHierarchyLevelService;

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

	public void remove(Repository entity, Boolean forceDelete) throws BusinessException {
		if (forceDelete == null || !forceDelete) {
			remove(entity);

		} else {
			removeHierarchy(entity);
		}
	}

	/**
	 * Removes a {@linkplain Repository} hierarchy.
	 * 
	 * @param entity the parent {@link RepositoryException}
	 * @throws BusinessException fail removing the entity hierarchy
	 */
	public void removeHierarchy(Repository entity) throws BusinessException {

		List<Repository> childRepositories = findByParent(entity);
		if (childRepositories != null && !childRepositories.isEmpty()) {
			for (Repository r : childRepositories) {
				removeHierarchy(r);
			}
		}

		super.remove(entity);
	}

	@SuppressWarnings("unchecked")
	public List<Repository> listWithSqlConnection() {

		QueryBuilder qb = new QueryBuilder(Repository.class, "r");
		qb.addSql("r.sqlConfiguration IS NOT NULL");

		return qb.getQuery(getEntityManager()).getResultList();
	}

	public Repository findDefaultRepository() {
		return findByCode("default");
	}

	public List<Repository> listByCet(CustomEntityTemplate cet) {
		return listByCet(cet, null);
	}

	public List<Repository> listByCetByUserLevel(CustomEntityTemplate cet) {

		User user = userService.findByUsername(currentUser.getUserName());
		return listByCet(cet, user.getUserLevel());
	}

	/**
	 * Retrieves a filtered list of repositories base on
	 * {@linkplain CustomEntityTemplate} storage type.
	 * 
	 * @param cet instance of custom entity template
	 * @return filtered list of repositories
	 */
	@SuppressWarnings("unchecked")
	public List<Repository> listByCet(CustomEntityTemplate cet, UserHierarchyLevel userLevel) {

		List<DBStorageType> storageTypes = cet.getAvailableStorages();

		boolean withCustomTable = false;
		withCustomTable = storageTypes.contains(DBStorageType.SQL) && cet.getSqlStorageConfigurationNullSafe().isStoreAsTable();

		boolean withSql = false;
		withSql = storageTypes.contains(DBStorageType.SQL);

		boolean withNeo4j = false;
		withNeo4j = storageTypes.contains(DBStorageType.NEO4J);

		QueryBuilder qb = new QueryBuilder(Repository.class, "r");

		if (withSql) {
			qb.addSql("r.sqlConfiguration IS NOT NULL");
			if (!withCustomTable) {
				qb.addSql("r.sqlConfiguration.code = 'default'");
			}
		}

		if (withNeo4j) {
			qb.addSql("r.neo4jConfiguration IS NOT NULL");
		}

		if (userLevel != null) {
			List<UserHierarchyLevel> userLevels = userHierarchyLevelService.buildHierarchy(userLevel);
			qb.startOrClause();
			for (UserHierarchyLevel ul : userLevels) {
				qb.addCriterionEntity("userHierarchyLevel", ul);
			}
			qb.addSql("r.sqlConfiguration.code = 'default'");
			qb.endOrClause();
		}

		return qb.getQuery(getEntityManager()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Repository> findByUserHierarchyLevels(List<UserHierarchyLevel> userLevels) {

		List<Repository> result = new ArrayList<>();
		QueryBuilder qb = new QueryBuilder(Repository.class, "r");
		if (userLevels != null) {
			List<Long> userLevelIds = userLevels.stream().map(UserHierarchyLevel::getId).collect(Collectors.toList());
			qb.setSqlString("FROM " + Repository.class.getSimpleName() + " r WHERE r.userHierarchyLevel.id IN :userLevelIds");
			Query q = qb.getQuery(getEntityManager()).setParameter("userLevelIds", userLevelIds);
			result.addAll((List<Repository>) q.getResultList());
		}

		return result;
	}

}
