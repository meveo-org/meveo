package org.meveo.api.dto.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.CustomRelationshipTemplateDto;

/**
 * @author Rachid AITYAAZZA
 **/
@XmlRootElement(name = "CustomRelationShipTemplateResponseDto")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomRelationshipTemplateResponseDto extends BaseResponse {

    private static final long serialVersionUID = -1871967200014440842L;

    private CustomRelationshipTemplateDto customRelationshipTemplate;

	public CustomRelationshipTemplateDto getCustomRelationshipTemplate() {
		return customRelationshipTemplate;
	}

	public void setCustomRelationshipTemplate(
			CustomRelationshipTemplateDto customRelationshipTemplate) {
		this.customRelationshipTemplate = customRelationshipTemplate;
	}

  
}