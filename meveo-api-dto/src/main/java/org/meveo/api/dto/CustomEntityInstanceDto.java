package org.meveo.api.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.customEntities.CustomEntityInstance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The Class CustomEntityInstanceDto.
 *
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@XmlRootElement(name = "CustomEntityInstance")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel
public class CustomEntityInstanceDto extends BaseEntityDto implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 9156372453581362595L;

    /** The code. */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "Code of the custom entity instance")
    private String code;

    /** The description. */
    @XmlAttribute()
    @ApiModelProperty("Description of the custom entity instance")
    private String description;

    /** The cet code. */
    @XmlAttribute(required = true)
    @ApiModelProperty(required = true, value = "Code of the custom entity template")
    private String cetCode;

    /** The disabled. */
    @ApiModelProperty("Whether to disable")
    private boolean disabled;

    /** The custom fields. */
    @ApiModelProperty("Custom fields information")
    private CustomFieldsDto customFields;
    
    private String uuid;

    /**
     * Instantiates a new custom entity instance dto.
     */
    public CustomEntityInstanceDto() {
    	super();
    }
    
    /**
	 * @return the {@link #uuid}
	 */
	public String getUuid() {
		return uuid;
	}



	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
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

    /**
     * Checks if is disabled.
     *
     * @return true, if is disabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Sets the disabled.
     *
     * @param disabled the new disabled
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Gets the cet code.
     *
     * @return the cet code
     */
    public String getCetCode() {
        return cetCode;
    }

    /**
     * Sets the cet code.
     *
     * @param cetCode the new cet code
     */
    public void setCetCode(String cetCode) {
        this.cetCode = cetCode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("CustomEntityInstanceDto [code=%s, description=%s, cetCode=%s, disabled=%s, customFields=%s]", code, description, cetCode, disabled, customFields);
    }

    /**
     * Gets the custom fields.
     *
     * @return the custom fields
     */
    public CustomFieldsDto getCustomFields() {
        return customFields;
    }

    /**
     * Sets the custom fields.
     *
     * @param customFields the new custom fields
     */
    public void setCustomFields(CustomFieldsDto customFields) {
        this.customFields = customFields;
    }

    /**
     * Convert CustomEntityInstance entity to CustomEntityInstanceDto object including custom field values.
     * 
     * @param cei CustomEntityInstance entity to convert
     * @param customFieldInstances custom field instances.
     * @return CustomEntityInstanceDto object
     */
    public static CustomEntityInstanceDto toDTO(CustomEntityInstance cei, CustomFieldsDto customFieldInstances) {
        CustomEntityInstanceDto dto = new CustomEntityInstanceDto();

        dto.setCode(cei.getCode());
        dto.setCetCode(cei.getCetCode());
        dto.setDescription(cei.getDescription());
        dto.setDisabled(cei.isDisabled());

        dto.setCustomFields(customFieldInstances);

        return dto;
    }

    /**
     * Convert CustomEntityInstanceDto object to CustomEntityInstance object. Note: does not convert custom field values
     * 
     * @param dto CustomEntityInstanceDto to convert
     * @param ceiToUpdate CustomEntityInstance to update with values from dto, or if null create a new one
     * @return A new or updated CustomEntityInstance instance
     */
    public static CustomEntityInstance fromDTO(CustomEntityInstanceDto dto, CustomEntityInstance ceiToUpdate) {

        CustomEntityInstance cei = new CustomEntityInstance();
        if (ceiToUpdate != null) {
            cei = ceiToUpdate;
        }
        cei.setCode(dto.getCode());
        cei.setCetCode(dto.getCetCode());
        cei.setDescription(dto.getDescription());

        return cei;
    }
}