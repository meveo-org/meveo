package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class CountriesDto.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "Countries")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class CountriesDto extends BaseEntityDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8690397660261914992L;

    /** The country. */
	@ApiModelProperty("List of coutry information")
	@SuppressWarnings("deprecation")
    private List<CountryDto> country;

    /**
     * Gets the country.
     *
     * @return the country
     */
	@SuppressWarnings("deprecation")
    public List<CountryDto> getCountry() {
        if (country == null)
            country = new ArrayList<CountryDto>();
        return country;
    }

    /**
     * Sets the country.
     *
     * @param country the new country
     */
	@SuppressWarnings("deprecation")
    public void setCountry(List<CountryDto> country) {
        this.country = country;
    }

}
