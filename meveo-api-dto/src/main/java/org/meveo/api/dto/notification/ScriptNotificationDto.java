package org.meveo.api.dto.notification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
@XmlRootElement(name = "ScriptNotification")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("ScriptNotificationDto")
public class ScriptNotificationDto extends NotificationDto {

	private static final long serialVersionUID = -5577716554189393392L;

}
