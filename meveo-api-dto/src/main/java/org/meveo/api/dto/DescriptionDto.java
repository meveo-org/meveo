package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class DescriptionDto.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since Oct 16, 2013
 */
@XmlRootElement(name = "Description")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class DescriptionDto {

    /** The language code. */
	@ApiModelProperty("The language code")
    private String languageCode;
    
    /** The description. */
	@ApiModelProperty("The description")
    private String description;

    /**
     * Gets the language code.
     *
     * @return the language code
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * Sets the language code.
     *
     * @param languageCode the new language code
     */
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
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
