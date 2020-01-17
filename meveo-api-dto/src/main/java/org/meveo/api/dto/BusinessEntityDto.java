package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.meveo.model.BusinessEntity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The dto for business entities.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since Oct 4, 2013
 **/
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class BusinessEntityDto extends EnableEntityDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4451119256601996946L;

	/** The code. */
	// @Pattern(regexp = "^[@A-Za-z0-9_\\.-]+$")
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "Business entity's code")
	protected String code;

	/** The description. */
	@XmlAttribute()
	@ApiModelProperty("Description of the business entity")
	protected String description;

	/** The updated code. */
	@ApiModelProperty("Use when updating the code of the business entity")
	protected String updatedCode;

	/**
	 * Instantiates a new business dto.
	 */
	public BusinessEntityDto() {
		super();
	}

	/**
	 * Instantiates a new business dto.
	 *
	 * @param e the BusinessEntity entity
	 */
	public BusinessEntityDto(BusinessEntity e) {
		super(e);
		
		if (e != null) {
			code = e.getCode();
			description = e.getDescription();
		}
	}

	/**
	 * Gets the updated code.
	 *
	 * @return the updated code
	 */
	public String getUpdatedCode() {
		return updatedCode;
	}

	/**
	 * Sets the updated code.
	 *
	 * @param updatedCode the new updated code
	 */
	public void setUpdatedCode(String updatedCode) {
		this.updatedCode = updatedCode;
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code the new code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
