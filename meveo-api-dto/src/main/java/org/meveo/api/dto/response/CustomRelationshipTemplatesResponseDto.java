package org.meveo.api.dto.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.CustomRelationshipTemplateDto;

/**
 * @author Rachid AITYAAZZA
 **/
@XmlRootElement(name = "CustomRelationshipTemplatesResponseDto")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomRelationshipTemplatesResponseDto extends BaseResponse {

    private static final long serialVersionUID = 2198425912826143580L;

    @XmlElementWrapper(name = "customRelationshipTemplates")
    @XmlElement(name = "customRelationshipTemplate")
    private List<CustomRelationshipTemplateDto> customRelationshipTemplates = new ArrayList<CustomRelationshipTemplateDto>();

	public List<CustomRelationshipTemplateDto> getCustomRelationshipTemplates() {
		return customRelationshipTemplates;
	}

	public void setCustomRelationshipTemplates(
			List<CustomRelationshipTemplateDto> customRelationshipTemplates) {
		this.customRelationshipTemplates = customRelationshipTemplates;
	}

   
    
    
}