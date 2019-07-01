package org.meveo.api.dto;

import java.util.Date;

import org.meveo.model.AuditableEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Contain the entity creation and modification dates.
 * 
 * 
 * @author Edward P. Legaspi
 * 
 */
public class AuditableEntityDto extends BaseEntityDto {

	/**
	 * serial versuion uid.
	 */
	private static final long serialVersionUID = 1040133977061424749L;

	/**
	 * created date.
	 */
	private Date created;

	@JsonIgnore
	private Date updated;
	
	public AuditableEntityDto() {
		super();
	}

	public AuditableEntityDto(AuditableEntity e) {
		super(e);

		if (e.getAuditable() != null) {
			created = e.getAuditable().getCreated();
			updated = e.getAuditable().getUpdated();
		}
	}

	/**
	 * @return created date.
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @param created created date
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}
}
