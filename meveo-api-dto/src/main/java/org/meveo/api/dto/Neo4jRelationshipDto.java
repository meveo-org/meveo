package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Rachid AITYAAZZA
 **/
@XmlRootElement(name = "Neo4jRelationshipDto")
@XmlAccessorType(XmlAccessType.FIELD)
public class Neo4jRelationshipDto implements Serializable {

    private static final long serialVersionUID = 9156372453581362595L;

    @XmlAttribute(required = true)
    private String crtCode;
    
    private CustomFieldsDto crtCustomFields = new CustomFieldsDto();
    
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