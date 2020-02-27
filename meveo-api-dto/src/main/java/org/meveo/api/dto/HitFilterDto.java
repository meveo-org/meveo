package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Phu Bach
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "hitFilter")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class HitFilterDto extends BaseEntityDto {

	private static final long serialVersionUID = -763450889692487278L;

	/**
	 * Operator
	 */
	@XmlAttribute
	@ApiModelProperty("Operator")
	protected String operator;

	/**
	 * Fieldname to operate
	 */
	@XmlAttribute
	@ApiModelProperty("Fieldname to operate")
	protected String fieldName;

	/**
	 * Type of field
	 */
	@XmlAttribute
	@ApiModelProperty("Type of field")
	private String fieldType;

	/**
	 * Value of the filter
	 */
	@XmlAttribute
	@ApiModelProperty("Value of the filter")
	private String value;

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
