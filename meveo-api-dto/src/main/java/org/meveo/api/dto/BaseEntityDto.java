package org.meveo.api.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.meveo.api.message.exception.InvalidDTOException;
import org.meveo.model.BaseEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

/**
 * A base class for all API DTO classes
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 **/
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(Include.NON_NULL)
public class BaseEntityDto implements Serializable {

	private static final long serialVersionUID = 4456089256601996946L;

	/** The id. */
	@JsonIgnore
	@ApiModelProperty("Entity id")
	protected Long id;

	public BaseEntityDto() {

	}

	public BaseEntityDto(BaseEntity e) {
		id = e.getId();
	}
	
	public String getCode() {
		return null;
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