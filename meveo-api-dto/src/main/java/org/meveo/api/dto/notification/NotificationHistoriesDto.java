package org.meveo.api.dto.notification;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseEntityDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Wrapper class for a list of notification history.
 *
 * @see NotificationHistoryDto
 * @author Edward P. Legaspi
 */
@XmlRootElement(name = "NotificationHistories")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel("NotificationHistoriesDto")
public class NotificationHistoriesDto extends BaseEntityDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4179758713839676597L;

	/** The notification history. */
	@ApiModelProperty("List of notification histories")
	private List<NotificationHistoryDto> notificationHistory;

	/**
	 * Gets the notification history.
	 *
	 * @return the notification history
	 */
	public List<NotificationHistoryDto> getNotificationHistory() {
		if (notificationHistory == null)
			notificationHistory = new ArrayList<NotificationHistoryDto>();
		return notificationHistory;
	}

	/**
	 * Sets the notification history.
	 *
	 * @param notificationHistory the new notification history
	 */
	public void setNotificationHistory(List<NotificationHistoryDto> notificationHistory) {
		this.notificationHistory = notificationHistory;
	}

	@Override
	public String toString() {
		return "NotificationHistoriesDto [notificationHistory=" + notificationHistory + ", toString()=" + super.toString() + "]";
	}
}