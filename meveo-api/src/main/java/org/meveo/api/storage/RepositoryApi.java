package org.meveo.api.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.utils.DtoUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.admin.User;
import org.meveo.model.hierarchy.UserHierarchyLevel;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.BinaryStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.admin.impl.UserService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.hierarchy.impl.UserHierarchyLevelService;
import org.meveo.service.neo4j.Neo4jConfigurationService;
import org.meveo.service.storage.BinaryStorageConfigurationService;
import org.meveo.service.storage.RepositoryService;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.13
 */
@Stateless
public class RepositoryApi extends BaseCrudApi<Repository, RepositoryDto> {

	public RepositoryApi() {
		super(Repository.class, RepositoryDto.class);
	}

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private BinaryStorageConfigurationService binaryStorageConfigurationService;

	@Inject
	private Neo4jConfigurationService neo4jConfigurationService;

	@Inject
	private SqlConfigurationService sqlConfigurationService;
    
    @Inject
    private UserHierarchyLevelService userHierarchyLevelService;
    
    @Inject
    private UserService userService;

	@Override
	public RepositoryDto toDto(Repository entity) {
		return new RepositoryDto(entity);
	}

	@Override
	public Repository fromDto(RepositoryDto dto) throws MeveoApiException {

		try {
			return toRepository(dto, null);

		} catch (EntityDoesNotExistsException e) {
			throw new MeveoApiException(e.getMessage());
		}
	}

	@Override
	public IPersistenceService<Repository> getPersistenceService() {
		return repositoryService;
	}

	public Repository toRepository(RepositoryDto source, Repository target) throws EntityDoesNotExistsException {

		if (target == null) {
			target = new Repository();
		}

		DtoUtils.toBusinessEntity(source, target);

		if (source.getParentCode() != null) {
			if (!StringUtils.isBlank(source.getParentCode())) {
				Repository parentRepo = repositoryService.findByCode(source.getParentCode());
				if (parentRepo != null) {
					target.setParentRepository(parentRepo);

				} else {
					throw new EntityDoesNotExistsException(Repository.class, source.getParentCode());
				}

			} else {
				target.setParentRepository(null);
			}
		}

		if (source.getBinaryStorageConfigurationCode() != null) {
			if (!StringUtils.isBlank(source.getBinaryStorageConfigurationCode())) {
				BinaryStorageConfiguration binaryStorageConfiguration = binaryStorageConfigurationService
						.findByCode(source.getBinaryStorageConfigurationCode());
				if (binaryStorageConfiguration != null) {
					target.setBinaryStorageConfiguration(binaryStorageConfiguration);

				} else {
					throw new EntityDoesNotExistsException(BinaryStorageConfiguration.class,
							source.getBinaryStorageConfigurationCode());
				}

			} else {
				target.setBinaryStorageConfiguration(null);
			}
		}

		if (source.getNeo4jConfigurationCode() != null) {
			if (!StringUtils.isBlank(source.getNeo4jConfigurationCode())) {
				Neo4JConfiguration neo4jConfiguration = neo4jConfigurationService
						.findByCode(source.getNeo4jConfigurationCode());
				if (neo4jConfiguration != null) {
					target.setNeo4jConfiguration(neo4jConfiguration);

				} else {
					throw new EntityDoesNotExistsException(Neo4JConfiguration.class,
							source.getNeo4jConfigurationCode());
				}

			} else {
				target.setNeo4jConfiguration(null);
			}
		}

		if (source.getSqlConfigurationCode() != null) {
			if (!StringUtils.isBlank(source.getSqlConfigurationCode())) {
				SqlConfiguration sqlConfiguration = sqlConfigurationService
						.findByCode(source.getSqlConfigurationCode());
				if (sqlConfiguration != null) {
					target.setSqlConfiguration(sqlConfiguration);

				} else {
					throw new EntityDoesNotExistsException(SqlConfiguration.class, source.getSqlConfigurationCode());
				}

			} else {
				target.setSqlConfiguration(null);
			}
		}

		if (source.getDataSeparationType() != null && !StringUtils.isBlank(source.getDataSeparationType())) {
			target.setDataSeparationType(source.getDataSeparationType());
		}
		
		if (!StringUtils.isBlank(source.getUserHierarchyLevelCode())) {
			UserHierarchyLevel userLevel = userHierarchyLevelService.findByCode(source.getUserHierarchyLevelCode());
			if (userLevel != null) {
				target.setUserHierarchyLevel(userLevel);
			}
		}

		return target;
	}

	public Repository create(RepositoryDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		if (repositoryService.findByCode(postData.getCode()) != null) {
			throw new EntityAlreadyExistsException(Repository.class, postData.getCode());
		}

		Repository entity = toRepository(postData, null);
		repositoryService.create(entity);

		return entity;
	}

	public Repository update(RepositoryDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		Repository entity = repositoryService.findByCode(postData.getCode());
		if (entity == null) {
			throw new EntityDoesNotExistsException(Repository.class, postData.getCode());
		}

		entity = toRepository(postData, entity);

		return repositoryService.update(entity);
	}

	public Repository createOrUpdate(RepositoryDto postData) throws BusinessException, MeveoApiException {
		Repository entity = repositoryService.findByCode(postData.getCode());
		if (entity == null) {
			return create(postData);

		} else {
			return update(postData);
		}
	}

	public RepositoryDto find(String code) throws EntityDoesNotExistsException {
		Repository entity = repositoryService.findByCode(code);

		if (entity == null) {
			throw new EntityDoesNotExistsException(Repository.class, code);
		}

		return new RepositoryDto(entity);
	}

	public List<RepositoryDto> findAll() {
		List<Repository> entities = repositoryService.list();

		return entities != null ? entities.stream().map(RepositoryDto::new).collect(Collectors.toList())
				: new ArrayList<>();
	}

	public void remove(String code, Boolean forceDelete) throws BusinessException {
		Repository entity = repositoryService.findByCode(code);

		if (entity == null) {
			throw new EntityDoesNotExistsException(Repository.class, code);
		}

		repositoryService.remove(entity, forceDelete);
	}

	@Override
	public boolean exists(RepositoryDto dto) {
		return repositoryService.findByCode(dto.getCode()) != null;
	}

	public void removeHierarchy(String code) throws BusinessException {

		Repository entity = repositoryService.findByCode(code);
		if (entity == null) {
			throw new EntityDoesNotExistsException(Repository.class, code);
		}

		repositoryService.removeHierarchy(entity);
	}
	
	@Override
	public void remove(RepositoryDto dto) throws MeveoApiException, BusinessException {
		var entity = repositoryService.findByCode(dto.getCode());
		if(entity != null) {
			repositoryService.remove(entity);
		}
	}

	public List<RepositoryDto> filterByUser() {

		List<RepositoryDto> result = new ArrayList<>();
		User user = userService.findByUsername(currentUser.getUserName());
		if (user.getUserLevel() != null) {
			List<Repository> userRepos = userService.findAssignedRepositories(user.getUserLevel());
			if (userRepos != null) {
				userRepos.stream().forEach(e -> result.add(new RepositoryDto(e)));
			}

		} else {
			result.add(new RepositoryDto(repositoryService.findDefaultRepository()));
		}

		return result;
	}
}
