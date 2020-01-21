package org.meveo.api.dto.catalog;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomFieldDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

/**
 * This class contains the information to process a BusinessProductModelDto.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since 28 Nov 2017
 */
@XmlRootElement(name = "BpmProduct")
@XmlAccessorType(XmlAccessType.FIELD)
@Api("BpmProductDto")
public class BpmProductDto extends BaseEntityDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5612754939639937822L;

    /** The bpm code. */
    @NotNull
    @XmlAttribute(required = true)
    @ApiModelProperty("Code of the business product model")
    private String bpmCode;

    /**
     * Will be the code of the newly created ProductTemplate.
     */
    @NotNull
    @XmlElement(required = true)
    @ApiModelProperty("Prefix that will be use when instantiating a ProductTemplate")
    private String prefix;

    /** The custom fields. */
    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    @ApiModelProperty("Custom fields to set")
    private List<CustomFieldDto> customFields;

    /**
     * Gets the bpm code.
     *
     * @return the bpm code
     */
    public String getBpmCode() {
        return bpmCode;
    }

    /**
     * Sets the bpm code.
     *
     * @param bpmCode the new bpm code
     */
    public void setBpmCode(String bpmCode) {
        this.bpmCode = bpmCode;
    }

    /**
     * Gets the custom fields.
     *
     * @return the custom fields
     */
    public List<CustomFieldDto> getCustomFields() {
        return customFields;
    }

    /**
     * Sets the custom fields.
     *
     * @param customFields the new custom fields
     */
    public void setCustomFields(List<CustomFieldDto> customFields) {
        this.customFields = customFields;
    }

    /**
     * Gets the prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix.
     *
     * @param prefix the new prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}