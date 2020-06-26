package org.meveo.api.notification;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.api.dto.notification.WebNotificationDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.notification.WebNotificationsResponseDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.model.notification.WebNotification;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.notification.WebNotificationService;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@Stateless
public class WebNotificationApi extends NotificationApi<WebNotification, WebNotificationDto> {

	@Inject
	WebNotificationService webNotificationService;

	public WebNotificationApi() {
		super(WebNotification.class, WebNotificationDto.class);
	}

	@Override
	public IPersistenceService<WebNotification> getPersistenceService() {
		return webNotificationService;
	}

	public WebNotificationsResponseDto list(PagingAndFiltering pagingAndFiltering) throws MeveoApiException {

		WebNotificationsResponseDto result = new WebNotificationsResponseDto();
		result.setWebNotifications(listByPage(result, pagingAndFiltering));

		return result;
	}

}
