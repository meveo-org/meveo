package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class LanguagesDto.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "Languages")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class LanguagesDto extends BaseEntityDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4455041168159380792L;

    /** The language. */
    @ApiModelProperty("List of language information")
    private List<LanguageDto> language;

    /**
     * Gets the language.
     *
     * @return the language
     */
    public List<LanguageDto> getLanguage() {
        if (language == null)
            language = new ArrayList<LanguageDto>();
        return language;
    }

    /**
     * Sets the language.
     *
     * @param language the new language
     */
    public void setLanguage(List<LanguageDto> language) {
        this.language = language;
    }

}
