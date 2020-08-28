package org.meveo.api.dto.response.notification;

import org.meveo.api.dto.notification.WebNotificationDto;
import org.meveo.api.dto.response.BaseResponse;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
public class WebNotificationResponseDto extends BaseResponse {

	private static final long serialVersionUID = 4507363989284033922L;

	private WebNotificationDto webNotification;

	public WebNotificationDto getWebNotification() {
		return webNotification;
	}

	public void setWebNotification(WebNotificationDto webNotification) {
		this.webNotification = webNotification;
	}
}
