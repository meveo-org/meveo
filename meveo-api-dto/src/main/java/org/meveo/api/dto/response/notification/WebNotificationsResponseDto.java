package org.meveo.api.dto.response.notification;

import java.util.List;

import org.meveo.api.dto.notification.WebNotificationDto;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
public class WebNotificationsResponseDto extends NotificationResponsesDto<WebNotificationDto> {

	private static final long serialVersionUID = 6962210886073503543L;
	private List<WebNotificationDto> webNotifications;

	public List<WebNotificationDto> getWebNotifications() {
		return webNotifications;
	}

	public void setWebNotifications(List<WebNotificationDto> webNotifications) {
		this.webNotifications = webNotifications;
	}
}
