package org.meveo.api.rest.sql.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.response.sql.SqlConfigurationResponseDto;
import org.meveo.api.dto.response.sql.SqlConfigurationsResponseDto;
import org.meveo.api.dto.sql.SqlConfigurationDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseCrudRs;
import org.meveo.api.rest.sql.SqlConfigurationRs;
import org.meveo.api.sql.SqlConfigurationApi;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.persistence.sql.SqlConfigurationService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 * @since 6.6.0
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class SqlConfigurationRsImpl extends BaseCrudRs<SqlConfiguration, SqlConfigurationDto> implements SqlConfigurationRs {

	@Inject
	private SqlConfigurationApi sqlConfigurationApi;
	
	@Override
	public ActionStatus create(SqlConfigurationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			sqlConfigurationApi.create(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public ActionStatus update(SqlConfigurationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			sqlConfigurationApi.update(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public ActionStatus createOrUpdate(SqlConfigurationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			sqlConfigurationApi.createOrUpdate(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public SqlConfigurationResponseDto find(String code) {

		SqlConfigurationResponseDto result = new SqlConfigurationResponseDto();
		try {
			result.setSqlConfiguration(sqlConfigurationApi.find(code));

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}

		return result;
	}

	@Override
	public SqlConfigurationsResponseDto list() {

		SqlConfigurationsResponseDto result = new SqlConfigurationsResponseDto();
		try {
			result.setSqlConfigurations(sqlConfigurationApi.findAll());

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}

		return result;
	}

	@Override
	public ActionStatus remove(String code) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			sqlConfigurationApi.remove(code);

		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

	@Override
	public BaseCrudApi<SqlConfiguration, SqlConfigurationDto> getBaseCrudApi() {
		return sqlConfigurationApi;
	}

	@Override
	public void initialize(String code) {
		sqlConfigurationApi.initialize(code);
	}
}
