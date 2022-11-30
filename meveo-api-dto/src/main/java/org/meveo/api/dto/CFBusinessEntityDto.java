/**
 * 
 */
package org.meveo.api.dto;

import io.swagger.annotations.ApiModelProperty;

public class CFBusinessEntityDto extends BusinessEntityDto {

    /** Custom fields. */
    @ApiModelProperty("Custom fields information")
    protected CustomFieldsDto customFields;
    
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
}
