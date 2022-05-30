package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.meveo.model.billing.Language;

/**
 * The Class LanguageDto.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "Language")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class LanguageDto extends BaseEntityDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 725968016559888810L;

    /** The code. */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "Code of the language")
    private String code;

    /** The description. */
    @ApiModelProperty("Description of the language")
    private String description;

    /**
     * Instantiates a new language dto.
     */
    public LanguageDto() {

    }

    /**
     * Instantiates a new language dto.
     *
     * @param language the language
     */
    public LanguageDto(Language language) {
        code = language.getCode();
        description = language.getDescription();
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LanguageDto [code=" + code + ", description=" + description + "]";
    }

}
