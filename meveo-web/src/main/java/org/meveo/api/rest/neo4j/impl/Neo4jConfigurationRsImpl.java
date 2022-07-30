package org.meveo.api.rest.neo4j.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.neo4j.Neo4jConfigurationDto;
import org.meveo.api.dto.response.neo4j.Neo4jConfigurationResponseDto;
import org.meveo.api.dto.response.neo4j.Neo4jConfigurationsResponseDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.neo4j.Neo4jConfigurationApi;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.neo4j.Neo4jConfigurationRs;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class Neo4jConfigurationRsImpl extends BaseRs implements Neo4jConfigurationRs {

	@Inject
	private Neo4jConfigurationApi neo4jConfigurationApi;

	@Override
	public ActionStatus create(Neo4jConfigurationDto postData) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			neo4jConfigurationApi.create(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public ActionStatus update(Neo4jConfigurationDto postData) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			neo4jConfigurationApi.update(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public ActionStatus createOrUpdate(Neo4jConfigurationDto postData) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			neo4jConfigurationApi.createOrUpdate(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public Neo4jConfigurationResponseDto find(String code) {
		Neo4jConfigurationResponseDto result = new Neo4jConfigurationResponseDto();
		try {
			result.setNeo4jConfiguration(neo4jConfigurationApi.find(code));

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}

		return result;
	}

	@Override
	public Neo4jConfigurationsResponseDto list() {
		Neo4jConfigurationsResponseDto result = new Neo4jConfigurationsResponseDto();
		try {
			result.setNeo4jConfigurations(neo4jConfigurationApi.findAll());

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}

		return result;
	}

	@Override
	public ActionStatus remove(String code) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			neo4jConfigurationApi.remove(code);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

}
