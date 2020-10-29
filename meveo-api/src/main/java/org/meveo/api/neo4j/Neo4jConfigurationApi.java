package org.meveo.api.neo4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.neo4j.Neo4jConfigurationDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.utils.DtoUtils;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.service.neo4j.Neo4jConfigurationService;;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
@Stateless
public class Neo4jConfigurationApi extends BaseApi {

	@Inject
	private Neo4jConfigurationService neo4jConfigurationService;

	private Neo4JConfiguration toNeo4jConfiguration(Neo4jConfigurationDto source, Neo4JConfiguration target) {
		if (target == null) {
			target = new Neo4JConfiguration();
		}
		DtoUtils.toBusinessEntity(source, target);

		target.setNeo4jUrl(source.getNeo4jUrl());
		target.setNeo4jLogin(source.getNeo4jLogin());
		target.setClearPassword(source.getNeo4jPassword());

		return target;
	}

	public void create(Neo4jConfigurationDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		if (neo4jConfigurationService.findByCode(postData.getCode()) != null) {
			throw new EntityAlreadyExistsException(Neo4JConfiguration.class, postData.getCode());
		}

		Neo4JConfiguration entity = toNeo4jConfiguration(postData, null);

		neo4jConfigurationService.create(entity);
	}

	public void update(Neo4jConfigurationDto postData) throws BusinessException, MeveoApiException {
		validate(postData);

		Neo4JConfiguration entity = neo4jConfigurationService.findByCode(postData.getCode());
		if (entity == null) {
			throw new EntityDoesNotExistsException(Neo4JConfiguration.class, postData.getCode());
		}

		toNeo4jConfiguration(postData, entity);

		neo4jConfigurationService.update(entity);
	}

	public void createOrUpdate(Neo4jConfigurationDto postData) throws BusinessException, MeveoApiException {
		Neo4JConfiguration entity = neo4jConfigurationService.findByCode(postData.getCode());
		if (entity == null) {
			create(postData);

		} else {
			update(postData);
		}

	}

	public Neo4jConfigurationDto find(String code) throws EntityDoesNotExistsException {
		Neo4JConfiguration entity = neo4jConfigurationService.findByCode(code);

		if (entity == null) {
			throw new EntityDoesNotExistsException(Neo4JConfiguration.class, code);
		}

		Neo4jConfigurationDto dto = new Neo4jConfigurationDto(entity);

		return dto;
	}

	public List<Neo4jConfigurationDto> findAll() {
		List<Neo4JConfiguration> entities = neo4jConfigurationService.list();

		return entities != null ? entities.stream().map(e -> new Neo4jConfigurationDto(e)).collect(Collectors.toList()) : new ArrayList<Neo4jConfigurationDto>();
	}

	public void remove(String code) throws BusinessException {
		Neo4JConfiguration entity = neo4jConfigurationService.findByCode(code);
		if (entity == null) {
			throw new EntityDoesNotExistsException(Neo4JConfiguration.class, code);
		}

		neo4jConfigurationService.remove(entity);
	}
}
