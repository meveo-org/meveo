package org.meveo.service.catalog.impl;

import org.apache.commons.lang3.StringUtils;
import org.meveo.api.dto.BusinessDto;
import org.meveo.api.dto.CustomFieldsDto;
import org.meveo.model.catalog.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class ServiceTemplateDto.
 *
 * @author Edward P. Legaspi
 * @author akadid abdelmounaim
 * @lastModifiedVersion 5.0.1
 */
@XmlRootElement(name = "ServiceTemplate")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceTemplateDto extends BusinessDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6794700715161690227L;

    /** The long description. */
    private String longDescription;

    /** The invoicing calendar. */
    private String invoicingCalendar;

    /** The custom fields. */
    private CustomFieldsDto customFields;

    /** The mandatory. */
    @Deprecated
    private boolean mandatory;

    /**
     * BusinessServiceModel code.
     */
    private String somCode;

    /** The image path. */
    private String imagePath;

    /** The image base 64. */
    private String imageBase64;

    /** The minimum amount El. */
    private String minimumAmountEl;

    /** The minimum label El. */   
    private String minimumLabelEl;

    /**
     * Instantiates a new service template dto.
     */
    public ServiceTemplateDto() {
    }

    /**
     * Instantiates a new service template dto.
     *
     * @param serviceTemplate the service template
     * @param customFieldInstances the custom field instances
     */
    public ServiceTemplateDto(ServiceTemplate serviceTemplate, CustomFieldsDto customFieldInstances) {
        super(serviceTemplate);

        longDescription = serviceTemplate.getLongDescription();
        invoicingCalendar = serviceTemplate.getInvoicingCalendar() == null ? null : serviceTemplate.getInvoicingCalendar().getCode();
        imagePath = serviceTemplate.getImagePath();
        minimumAmountEl = serviceTemplate.getMinimumAmountEl();
        minimumLabelEl = serviceTemplate.getMinimumLabelEl();

        if (serviceTemplate.getBusinessServiceModel() != null) {
            somCode = serviceTemplate.getBusinessServiceModel().getCode();
        }


        customFields = customFieldInstances;
    }

    /**
     * Instantiates a new service template dto.
     *
     * @param serviceTemplate the service template
     */
    public ServiceTemplateDto(ServiceTemplate serviceTemplate) {
        super(serviceTemplate);
    }

    /**
     * Gets the invoicing calendar.
     *
     * @return the invoicing calendar
     */
    public String getInvoicingCalendar() {
        return invoicingCalendar;
    }

    /**
     * Sets the invoicing calendar.
     *
     * @param invoicingCalendar the new invoicing calendar
     */
    public void setInvoicingCalendar(String invoicingCalendar) {
        this.invoicingCalendar = invoicingCalendar;
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
     * Checks if is mandatory.
     *
     * @return true, if is mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Sets the mandatory.
     *
     * @param mandatory the new mandatory
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Gets the som code.
     *
     * @return the som code
     */
    public String getSomCode() {
        return somCode;
    }

    /**
     * Sets the som code.
     *
     * @param somCode the new som code
     */
    public void setSomCode(String somCode) {
        this.somCode = somCode;
    }

    /**
     * Checks if is code only.
     *
     * @return true, if is code only
     */
    public boolean isCodeOnly() {
        return StringUtils.isBlank(getDescription()) && StringUtils.isBlank(invoicingCalendar) && StringUtils.isBlank(somCode)
                && (customFields == null || customFields.isEmpty());
    }

    /**
     * Gets the long description.
     *
     * @return the long description
     */
    public String getLongDescription() {
        return longDescription;
    }

    /**
     * Sets the long description.
     *
     * @param longDescription the new long description
     */
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    /**
     * Gets the image path.
     *
     * @return the image path
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Sets the image path.
     *
     * @param imagePath the new image path
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Gets the image base 64.
     *
     * @return the image base 64
     */
    public String getImageBase64() {
        return imageBase64;
    }

    /**
     * Sets the image base 64.
     *
     * @param imageBase64 the new image base 64
     */
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    
    @Override
    public String toString() {
        return "ServiceTemplateDto [code=" + getCode() + ", description=" + getDescription() + ", longDescription=" + longDescription + ", invoicingCalendar=" + invoicingCalendar
                +"customFields=" + customFields + ", mandatory=" + mandatory + ", somCode=" + somCode + ", imagePath=" + imagePath + "]";
    }

    /**
     * Get the minimum amount EL.
     *
     * @return the minimum amount EL
     */
    public String getMinimumAmountEl() {
        return minimumAmountEl;
    }

    /**
     * Sets the minimum amount EL.
     *
     * @param minimumAmountEl the minimum amount EL
     */
    public void setMinimumAmountEl(String minimumAmountEl) {
        this.minimumAmountEl = minimumAmountEl;
    }

    /**
     * Get the minimum label EL.
     *
     * @return the minimum label EL
     */
    public String getMinimumLabelEl() {
        return minimumLabelEl;
    }

    /**
     * Sets the minimum label EL.
     *
     * @param minimumLabelEl the minimum label EL
     */
    public void setMinimumLabelEl(String minimumLabelEl) {
        this.minimumLabelEl = minimumLabelEl;
    }
}