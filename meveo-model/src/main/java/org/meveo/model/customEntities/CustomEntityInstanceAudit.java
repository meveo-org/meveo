package org.meveo.model.customEntities;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
public class CustomEntityInstanceAudit {

	public enum CustomEntityInstanceAuditType {
		CREATED, UPDATED, REMOVED
	}

	private String uuid;
	private String ceiUuid;
	private String user;
	private LocalDateTime eventDate;
	private CustomEntityInstanceAuditType action;
	private String field;
	private Object oldValue;
	private Object newValue;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getCeiUuid() {
		return ceiUuid;
	}

	public void setCeiUuid(String ceiUuid) {
		this.ceiUuid = ceiUuid;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public LocalDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public CustomEntityInstanceAuditType getAction() {
		return action;
	}

	public void setAction(CustomEntityInstanceAuditType action) {
		this.action = action;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	@Override
	public String toString() {
		return "CustomEntityInstanceAudit [uuid=" + uuid + ", ceiUuid=" + ceiUuid + ", user=" + user + ", eventDate=" + eventDate + ", action=" + action + ", field=" + field
				+ ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
	}

}
