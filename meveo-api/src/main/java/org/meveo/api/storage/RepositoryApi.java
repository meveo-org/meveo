package org.meveo.api.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseApi;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.utils.DtoUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.storage.BinaryStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.BinaryStorageConfigurationService;
import org.meveo.service.storage.RepositoryService;

/**
 * @author Edward P. Legaspi
 */
@Stateless
public class RepositoryApi extends BaseApi {

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private BinaryStorageConfigurationService binaryStorageConfigurationService;

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
				BinaryStorageConfiguration binaryStorageConfiguration = binaryStorageConfigurationService.findByCode(source.getBinaryStorageConfigurationCode());
				if (binaryStorageConfiguration != null) {
					target.setBinaryStorageConfiguration(binaryStorageConfiguration);

				} else {
					throw new EntityDoesNotExistsException(BinaryStorageConfiguration.class, source.getBinaryStorageConfigurationCode());
				}
			} else {
				target.setBinaryStorageConfiguration(null);
			}
		}

//		if(source.getNeo4jConfigurationCode() !=null) {
//			if(!StringUtils.isBlank(source.getNeo4jConfigurationCode())) {
//				Neo4JConfiguration neo4jConfiguration
//			} else {
//				target.setNeo4jConfiguration(null);
//			}
//		}

		if (source.getDataSeparationType() != null && !StringUtils.isBlank(source.getDataSeparationType())) {
			target.setDataSeparationType(source.getDataSeparationType());
		}

		return target;
	}

	public void create(RepositoryDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		if (repositoryService.findByCode(postData.getCode()) != null) {
			throw new EntityAlreadyExistsException(Repository.class, postData.getCode());
		}

		Repository entity = toRepository(postData, null);

		repositoryService.create(entity);
	}

	public void update(RepositoryDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		Repository entity = repositoryService.findByCode(postData.getCode());
		if (entity == null) {
			throw new EntityDoesNotExistsException(Repository.class, postData.getCode());
		}

		entity = toRepository(postData, entity);

		repositoryService.update(entity);
	}

	public void createOrUpdate(RepositoryDto postData) throws BusinessException, MeveoApiException {
		Repository entity = repositoryService.findByCode(postData.getCode());
		if (entity == null) {
			create(postData);

		} else {
			update(postData);
		}

	}

	public RepositoryDto find(String code) throws EntityDoesNotExistsException {
		Repository entity = repositoryService.findByCode(code);

		if (entity == null) {
			throw new EntityDoesNotExistsException(Repository.class, code);
		}

		RepositoryDto dto = new RepositoryDto(entity);

		return dto;
	}

	public List<RepositoryDto> findAll() {
		List<Repository> entities = repositoryService.list();

		return entities != null ? entities.stream().map(e -> new RepositoryDto(e)).collect(Collectors.toList()) : new ArrayList<RepositoryDto>();
	}

	public void remove(String code) throws BusinessException {
		Repository entity = repositoryService.findByCode(code);
		if (entity == null) {
			throw new EntityDoesNotExistsException(Repository.class, code);
		}

		repositoryService.remove(entity);
	}
}
