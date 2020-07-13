package org.meveo.api.rest.notification.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.notification.WebNotificationDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.notification.WebNotificationResponseDto;
import org.meveo.api.dto.response.notification.WebNotificationsResponseDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.notification.WebNotificationApi;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.notification.WebNotificationRs;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class WebNotificationRsImpl extends BaseRs implements WebNotificationRs {

	@Inject
	private WebNotificationApi webNotificationApi;

	@Override
	public ActionStatus create(WebNotificationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			webNotificationApi.create(postData);
		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

	@Override
	public ActionStatus update(WebNotificationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			webNotificationApi.update(postData);
		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

	@Override
	public WebNotificationResponseDto find(String notificationCode) {

		WebNotificationResponseDto result = new WebNotificationResponseDto();

		try {
			result.setWebNotification(webNotificationApi.find(notificationCode));

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}

		return result;
	}

	@Override
	public ActionStatus remove(String notificationCode) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			webNotificationApi.remove(notificationCode);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public ActionStatus createOrUpdate(WebNotificationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			webNotificationApi.createOrUpdate(postData);

		} catch (Exception e) {
			processException(e, result);
		}
		return result;
	}

	@Override
	public WebNotificationsResponseDto list(PagingAndFiltering pagingAndFiltering) {

		WebNotificationsResponseDto result = new WebNotificationsResponseDto();

		try {
			result = webNotificationApi.list(pagingAndFiltering);

		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}
		return result;
	}

}
