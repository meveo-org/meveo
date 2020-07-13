package org.meveo.api.notification;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.api.dto.notification.ScriptNotificationDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.notification.ScriptNotificationsResponseDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.model.notification.ScriptNotification;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.notification.ScriptNotificationService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
@Stateless
public class ScriptNotificationApi extends NotificationApi<ScriptNotification, ScriptNotificationDto> {

	@Inject
	private ScriptNotificationService scriptNotificationService;

	public ScriptNotificationApi() {
		super(ScriptNotification.class, ScriptNotificationDto.class);
	}

	@Override
	public IPersistenceService<ScriptNotification> getPersistenceService() {
		return scriptNotificationService;
	}

	public ScriptNotificationsResponseDto list(PagingAndFiltering pagingAndFiltering) throws MeveoApiException {

		ScriptNotificationsResponseDto result = new ScriptNotificationsResponseDto();
		result.setScriptNotifications(listByPage(result, pagingAndFiltering));

		return result;
	}

}
