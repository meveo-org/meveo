package org.meveo.api.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModelProperty;

/**
 * Dto for {@link Neo4jRelationship}
 * 
 * @author Rachid AITYAAZZA
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@XmlRootElement(name = "Neo4jRelationshipDto")
@XmlAccessorType(XmlAccessType.FIELD)
public class Neo4jRelationshipDto implements Serializable {

	private static final long serialVersionUID = 9156372453581362595L;

	/**
	 * The crt code
	 */
	@XmlAttribute(required = true)
	@ApiModelProperty("The crt code")
	private String crtCode;

	/**
	 * The ctr custom fields
	 */
	@ApiModelProperty("The crt custom fields")
	private CustomFieldsDto crtCustomFields = new CustomFieldsDto();

	/**
	 * The target custom fields
	 */
	@ApiModelProperty("The target custom fields")
	private CustomFieldsDto targetCustomFields = new CustomFieldsDto();

	public Neo4jRelationshipDto() {

	}

	public String getCrtCode() {
		return crtCode;
	}

	public void setCrtCode(String crtCode) {
		this.crtCode = crtCode;
	}

	public CustomFieldsDto getCrtCustomFields() {
		return crtCustomFields;
	}

	public void setCrtCustomFields(CustomFieldsDto crtCustomFields) {
		this.crtCustomFields = crtCustomFields;
	}

	public CustomFieldsDto getTargetCustomFields() {
		return targetCustomFields;
	}

	public void setTargetCustomFields(CustomFieldsDto targetCustomFields) {
		this.targetCustomFields = targetCustomFields;
	}

}