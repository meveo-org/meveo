package org.meveo.api.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.notification.ScriptNotificationDto;
import org.meveo.api.dto.sql.SqlConfigurationDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.base.local.IPersistenceService;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 * @since 6.6.0
 */
@Stateless
public class SqlConfigurationApi extends BaseCrudApi<SqlConfiguration, SqlConfigurationDto> {

	@Inject
	private SqlConfigurationService sqlConfigurationService;

	public SqlConfigurationApi() {
		super(SqlConfiguration.class, SqlConfigurationDto.class);
	}

	@Override
	public SqlConfigurationDto find(String code) throws EntityDoesNotExistsException {

		SqlConfiguration entity = sqlConfigurationService.findByCode(code);

		if (entity == null) {
			throw new EntityDoesNotExistsException(SqlConfiguration.class, code);
		}

		return new SqlConfigurationDto(entity);
	}

	@Override
	public SqlConfiguration createOrUpdate(SqlConfigurationDto postData) throws MeveoApiException, BusinessException {

		SqlConfiguration entity = sqlConfigurationService.findByCode(postData.getCode());
		if (entity == null) {
			return create(postData);

		} else {
			return update(postData);
		}
	}

	public SqlConfiguration update(SqlConfigurationDto postData) throws BusinessException, MeveoApiException {

		validate(postData);

		SqlConfiguration entity = sqlConfigurationService.findByCode(postData.getCode());
		if (entity == null) {
			throw new EntityDoesNotExistsException(SqlConfiguration.class, postData.getCode());
		}

		toSqlConfiguration(postData, entity);

		return sqlConfigurationService.update(entity);
	}

	public SqlConfiguration create(SqlConfigurationDto postData) throws BusinessException, MeveoApiException {

		validate(postData);

		if (sqlConfigurationService.findByCode(postData.getCode()) != null) {
			throw new EntityAlreadyExistsException(SqlConfiguration.class, postData.getCode());
		}

		SqlConfiguration entity = toSqlConfiguration(postData, null);
		sqlConfigurationService.create(entity);

		return entity;
	}

	public List<SqlConfigurationDto> findAll() {

		List<SqlConfiguration> entities = sqlConfigurationService.list();

		return entities != null ? entities.stream().map(SqlConfigurationDto::new).collect(Collectors.toList()) : new ArrayList<>();
	}

	public void remove(String code) throws EntityDoesNotExistsException, BusinessException {

		SqlConfiguration entity = sqlConfigurationService.findByCode(code);

		if (entity == null) {
			throw new EntityDoesNotExistsException(SqlConfiguration.class, code);
		}

		sqlConfigurationService.remove(entity);
	}

	private SqlConfiguration toSqlConfiguration(SqlConfigurationDto source, SqlConfiguration target) {

		if (target == null) {
			target = new SqlConfiguration();
		}
		target.setCode(source.getCode());
		target.setDescription(source.getDescription());
		target.setDialect(source.getDialect());
		target.setDriverClass(source.getDriverClass());
		target.setClearPassword(source.getPassword());
		target.setUrl(source.getUrl());
		target.setUsername(source.getUsername());

		return target;
	}

	@Override
	public SqlConfigurationDto toDto(SqlConfiguration entity) {
		return new SqlConfigurationDto(entity);
	}

	@Override
	public SqlConfiguration fromDto(SqlConfigurationDto dto) throws MeveoApiException {
		return toSqlConfiguration(dto, null);
	}

	@Override
	public IPersistenceService<SqlConfiguration> getPersistenceService() {
		return sqlConfigurationService;
	}

	@Override
	public boolean exists(SqlConfigurationDto dto) {
		return false;
	}
	
	/**
	 * Initializes custom tables for the given configuration
	 * 
	 * @param code Code of the configuration
	 */
	public void initialize(String code) {
		SqlConfiguration conf = sqlConfigurationService.findByCode(code);
		if(conf == null) {
			throw new IllegalArgumentException("SQL Configuration " + code + " does not exists");
		}
		
		sqlConfigurationService.initializeCet(conf);		
	}
	
	@Override
	public void remove(SqlConfigurationDto dto) throws MeveoApiException, BusinessException {
		var entity = sqlConfigurationService.findByCode(dto.getCode());
		if(entity != null) {
			sqlConfigurationService.remove(entity);
		}
	}

}
