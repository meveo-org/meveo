package org.meveo.api.dto;

import org.meveo.api.dto.BaseDto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Phu Bach
 *
 */
@XmlRootElement(name = "hitFilter")
@XmlAccessorType(XmlAccessType.FIELD)
public class HitFilterDto extends BaseDto {

	private static final long serialVersionUID = -763450889692487278L;

    @XmlAttribute
    protected String operator;
    @XmlAttribute
    protected String fieldName;
    @XmlAttribute
    private String fieldType;
    @XmlAttribute
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
