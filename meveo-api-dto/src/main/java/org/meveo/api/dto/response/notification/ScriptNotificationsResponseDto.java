package org.meveo.api.dto.response.notification;

import java.util.List;

import org.meveo.api.dto.notification.ScriptNotificationDto;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
public class ScriptNotificationsResponseDto extends NotificationResponsesDto<ScriptNotificationDto> {

	private static final long serialVersionUID = 7317939487581376735L;
	private List<ScriptNotificationDto> scriptNotifications;

	public List<ScriptNotificationDto> getScriptNotifications() {
		return scriptNotifications;
	}

	public void setScriptNotifications(List<ScriptNotificationDto> scriptNotifications) {
		this.scriptNotifications = scriptNotifications;
	}
}
