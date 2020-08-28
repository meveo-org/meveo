package org.meveo.model.customEntities;

import org.meveo.model.crm.custom.CustomFieldValues;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
public class CustomEntityInstanceAuditParameter {

	private String code;
	private String description;
	private String cetCode;
	private String ceiUuid;
	private CustomFieldValues oldValues;
	private CustomFieldValues newValues;
	private String appliesTo;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCetCode() {
		return cetCode;
	}

	public void setCetCode(String cetCode) {
		this.cetCode = cetCode;
	}

	public CustomFieldValues getOldValues() {
		return oldValues;
	}

	public void setOldValues(CustomFieldValues oldValues) {
		this.oldValues = oldValues;
	}

	public CustomFieldValues getNewValues() {
		return newValues;
	}

	public void setNewValues(CustomFieldValues newValues) {
		this.newValues = newValues;
	}

	public String getCeiUuid() {
		return ceiUuid;
	}

	public void setCeiUuid(String ceiUuid) {
		this.ceiUuid = ceiUuid;
	}

	public String getAppliesTo() {
		return appliesTo;
	}

	public void setAppliesTo(String appliesTo) {
		this.appliesTo = appliesTo;
	}
}
