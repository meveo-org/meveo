package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.meveo.model.billing.Country;


/**
 * The Class CountryIsoDto.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since Oct 4, 2013
 */
@XmlRootElement(name = "CountryIso")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class CountryIsoDto extends BaseEntityDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4175660113940481232L;

    /** The country code. */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "Code of the country")
    private String countryCode;

    /** The description. */
    @XmlAttribute()
    @ApiModelProperty("Description of the country")
    private String description;

    /** The nationality. */
    @XmlAttribute()
    @ApiModelProperty("The nationality")
    private String nationality;

    /** The language descriptions. */
    @ApiModelProperty("List of language descriptions information")
    private List<LanguageDescriptionDto> languageDescriptions;

    /** The currency code. */
    @XmlElement(required = true)
    @ApiModelProperty(required = true, value = "Code of the currency")
    private String currencyCode;

    /** The language code. */
    @XmlElement(required = true)
    @ApiModelProperty(required = true, value = "Code of the language")
    private String languageCode;

    /**
     * Instantiates a new country iso dto.
     */
    public CountryIsoDto() {

    }

    /**
     * Instantiates a new country iso dto.
     *
     * @param e the country entity
     */
    public CountryIsoDto(Country e) {
        countryCode = e.getCode();
        description = e.getDescription();
        nationality = e.getNationality();
        currencyCode = e.getCurrency().getCurrencyCode();
        languageDescriptions = new ArrayList<>();

        if (e.getLanguage() != null) {
            languageCode = e.getLanguage().getCode();
        }
    }

    /**
     * Gets the country code.
     *
     * @return the country code
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the country code.
     *
     * @param countryCode the new country code
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the nationality.
     *
     * @return the nationality
     */
    public String getNationality() {
        return nationality;
    }

    /**
     * Sets the nationality.
     *
     * @param nationality the nationality to set
     */
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    /**
     * Gets the language descriptions.
     *
     * @return the languageDescriptions
     */
    public List<LanguageDescriptionDto> getLanguageDescriptions() {
        return languageDescriptions;
    }

    /**
     * Sets the language descriptions.
     *
     * @param languageDescriptions the languageDescriptions to set
     */
    public void setLanguageDescriptions(List<LanguageDescriptionDto> languageDescriptions) {
        this.languageDescriptions = languageDescriptions;
    }

    /**
     * Gets the currency code.
     *
     * @return the currency code
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Sets the currency code.
     *
     * @param currencyCode the new currency code
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

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

}
