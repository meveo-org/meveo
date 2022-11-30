package org.meveo.api.rest.notification.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.notification.ScriptNotificationDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.notification.GetNotificationResponseDto;
import org.meveo.api.dto.response.notification.InboundRequestsResponseDto;
import org.meveo.api.dto.response.notification.NotificationHistoriesResponseDto;
import org.meveo.api.dto.response.notification.ScriptNotificationsResponseDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.notification.ScriptNotificationApi;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.notification.ScriptNotificationRs;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 **/
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class ScriptNotificationRsImpl extends BaseRs implements ScriptNotificationRs {

	@Inject
	private ScriptNotificationApi scriptNotificationApi;

	@Override
	public ActionStatus create(ScriptNotificationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			scriptNotificationApi.create(postData);
		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

	@Override
	public ActionStatus update(ScriptNotificationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			scriptNotificationApi.update(postData);
		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

	@Override
	public GetNotificationResponseDto find(String notificationCode) {

		GetNotificationResponseDto result = new GetNotificationResponseDto();

		try {
			result.setNotificationDto(scriptNotificationApi.find(notificationCode));

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}

		return result;
	}

	@Override
	public ActionStatus remove(String notificationCode) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			scriptNotificationApi.remove(notificationCode);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public NotificationHistoriesResponseDto listNotificationHistory() {

		NotificationHistoriesResponseDto result = new NotificationHistoriesResponseDto();

		try {
			result.setNotificationHistories(scriptNotificationApi.listNotificationHistory());

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}
		return result;
	}

	@Override
	public InboundRequestsResponseDto listInboundRequest() {

		InboundRequestsResponseDto result = new InboundRequestsResponseDto();

		try {
			result.setInboundRequests(scriptNotificationApi.listInboundRequest());

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}
		return result;
	}

	@Override
	public ActionStatus createOrUpdate(ScriptNotificationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			scriptNotificationApi.createOrUpdate(postData);

		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

	@Override
	public ScriptNotificationsResponseDto list(PagingAndFiltering pagingAndFiltering) {

		ScriptNotificationsResponseDto result = new ScriptNotificationsResponseDto();

		try {
			result = scriptNotificationApi.list(pagingAndFiltering);

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}
		return result;
	}
}
