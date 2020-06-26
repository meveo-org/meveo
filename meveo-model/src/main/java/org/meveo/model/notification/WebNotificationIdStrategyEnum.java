package org.meveo.model.notification;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
public enum WebNotificationIdStrategyEnum {
	UUID, TIMESTAMP;

	public String getLabel() {
		return this.getClass().getSimpleName() + "." + this.name();
	}
}
