package org.meveo.api.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import org.meveo.api.message.exception.InvalidDTOException;
import org.meveo.model.BaseEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A base class for all API DTO classes
 * 
 * @author Edward P. Legaspi
 * @since Oct 4, 2013
 **/
@JsonInclude(Include.NON_NULL)
public abstract class BaseEntityDto implements Serializable {

	private static final long serialVersionUID = 4456089256601996946L;

	/** The id. */
	@JsonIgnore
	protected Long id;

	public BaseEntityDto() {

	}

	public BaseEntityDto(BaseEntity e) {
		id = e.getId();
	}

	/**
	 * Validate DTO
	 * 
	 * @throws InvalidDTOException Validation exception
	 */
	public void validate() throws InvalidDTOException {

	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(Long id) {
		this.id = id;
	}
}