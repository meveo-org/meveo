package org.meveo.api.rest.storage.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.response.storage.BinaryStorageConfigurationResponseDto;
import org.meveo.api.dto.response.storage.BinaryStorageConfigurationsResponseDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseCrudRs;
import org.meveo.api.rest.storage.BinaryStorageConfigurationRs;
import org.meveo.api.storage.BinaryStorageConfigurationApi;
import org.meveo.api.storage.BinaryStorageConfigurationDto;
import org.meveo.model.storage.BinaryStorageConfiguration;

/**
 * @author Edward P. Legaspi
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class BinaryStorageConfigurationRsImpl extends BaseCrudRs<BinaryStorageConfiguration, BinaryStorageConfigurationDto> implements BinaryStorageConfigurationRs {

	@Inject
	private BinaryStorageConfigurationApi binaryStorageConfigurationApi;

	@Override
	public ActionStatus create(BinaryStorageConfigurationDto postData) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			binaryStorageConfigurationApi.create(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public ActionStatus update(BinaryStorageConfigurationDto postData) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			binaryStorageConfigurationApi.update(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public ActionStatus createOrUpdate(BinaryStorageConfigurationDto postData) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			binaryStorageConfigurationApi.createOrUpdate(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public BinaryStorageConfigurationResponseDto find(String code) {
		BinaryStorageConfigurationResponseDto result = new BinaryStorageConfigurationResponseDto();
		try {
			result.setBinaryStorageConfiguration(binaryStorageConfigurationApi.find(code));

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}

		return result;
	}

	@Override
	public BinaryStorageConfigurationsResponseDto list() {
		BinaryStorageConfigurationsResponseDto result = new BinaryStorageConfigurationsResponseDto();
		try {
			result.setBinaryStorageConfigurations(binaryStorageConfigurationApi.findAll());

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}

		return result;
	}

	@Override
	public ActionStatus remove(String code) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			binaryStorageConfigurationApi.remove(code);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public BaseCrudApi<BinaryStorageConfiguration, BinaryStorageConfigurationDto> getBaseCrudApi() {
		return binaryStorageConfigurationApi;
	}

}