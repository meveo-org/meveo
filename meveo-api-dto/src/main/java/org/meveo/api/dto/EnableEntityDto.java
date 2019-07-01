package org.meveo.api.dto;

import org.meveo.model.EnableEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Edward P. Legaspi
 */
public class EnableEntityDto extends AuditableEntityDto {

	private static final long serialVersionUID = 3544475652777380893L;

	private Boolean disabled;
	private Boolean active;

	public EnableEntityDto() {
		super();
	}

	public EnableEntityDto(EnableEntity e) {
		super((EnableEntity) e);

		if (e != null) {
			setDisabled(e.isDisabled());
		}
	}

	public Boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	@JsonIgnore
	public Boolean isActive() {
		return disabled == null ? null : !disabled;
	}

	public void setActive(Boolean active) {
		setDisabled(!active);
	}
}
