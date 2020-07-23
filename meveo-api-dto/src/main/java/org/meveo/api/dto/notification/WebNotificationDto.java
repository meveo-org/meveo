package org.meveo.api.dto.notification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.notification.Notification;
import org.meveo.model.notification.WebNotification;
import org.meveo.model.notification.WebNotificationIdStrategyEnum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@XmlRootElement(name = "WebNotification")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("WebNotificationDto")
public class WebNotificationDto extends NotificationDto {

	private static final long serialVersionUID = -4777484091248210796L;

	@ApiModelProperty(required = true, value = "The id strategy")
	private WebNotificationIdStrategyEnum idStrategy = WebNotificationIdStrategyEnum.UUID;

	@ApiModelProperty("The notification data EL")
	private String dataEL;

	@ApiModelProperty("Flag indicating if publication is allowed to the web notification channel")
	private boolean publicationAllowed;

	@ApiModelProperty("Flag indicating if all messages are persisted")
	private boolean persistHistory;

	public WebNotificationDto() {
		super();
	}

	public WebNotificationDto(WebNotification webNotif) {
		super((Notification) webNotif);
		this.setIdStrategy(webNotif.getIdStrategy());
		this.setDataEL(webNotif.getDataEL());
		this.setPublicationAllowed(webNotif.isPublicationAllowed());
		this.setPersistHistory(webNotif.isPersistHistory());
	}

	@Override
	public String toString() {
		return "WebNotificationDto [idStrategy=" + getIdStrategy() + ", dataEL=" + getDataEL() + ", publicationAllowed="
				+ isPublicationAllowed() + ", persistHistory=" + isPersistHistory() + "]";
	}

	/** The id strategy */
	public WebNotificationIdStrategyEnum getIdStrategy() {
		return idStrategy;
	}

	public void setIdStrategy(WebNotificationIdStrategyEnum idStrategy) {
		this.idStrategy = idStrategy;
	}

	/** The notification data */
	public String getDataEL() {
		return dataEL;
	}

	public void setDataEL(String dataEL) {
		this.dataEL = dataEL;
	}

	public boolean isPublicationAllowed() {
		return publicationAllowed;
	}

	public void setPublicationAllowed(boolean publicationAllowed) {
		this.publicationAllowed = publicationAllowed;
	}

	public boolean isPersistHistory() {
		return persistHistory;
	}

	public void setPersistHistory(boolean persistHistory) {
		this.persistHistory = persistHistory;
	}
}
