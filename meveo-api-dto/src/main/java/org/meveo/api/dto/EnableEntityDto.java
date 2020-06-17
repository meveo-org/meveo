package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.meveo.model.EnableEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class EnableEntityDto extends AuditableEntityDto {

	private static final long serialVersionUID = 3544475652777380893L;

	/**
	 * Whether this entity is disabled
	 */
	@ApiModelProperty("Whether this entity is disabled")
	private Boolean disabled;
	
	/**
	 * Whether this entity is active
	 */
	@ApiModelProperty("Whether this entity is active")
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

	@JsonIgnore
	public Boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public Boolean isActive() {
		if(active == null) {
			return disabled == null ? null : !disabled;
		}

		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
		if (active != null) {
			setDisabled(!active);
		}
	}
}
