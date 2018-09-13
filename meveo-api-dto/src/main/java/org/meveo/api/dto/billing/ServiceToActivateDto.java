package org.meveo.api.dto.billing;

import org.meveo.api.dto.CustomFieldsDto;
import org.meveo.model.catalog.ServiceTemplate;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * The Class ServiceToActivateDto.
 *
 * @author Edward P. Legaspi
 */
@XmlRootElement(name = "ServiceToActivate")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceToActivateDto implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3815026205495621916L;

    /** The code. */
    @XmlAttribute(required = true)
    private String code;

    /** The description. */
    @XmlAttribute
    private String description;

    /** The quantity. */
    @XmlElement(required = false)
    private BigDecimal quantity;

    /** The subscription date. */
    private Date subscriptionDate;

    /** The custom fields. */
    private CustomFieldsDto customFields;

    /** The service template. */
    @XmlTransient
    // @ApiModelProperty(hidden = true)
    private ServiceTemplate serviceTemplate;

    /** The rate until date. */
    private Date rateUntilDate;

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
     * Gets the quantity.
     *
     * @return the quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity.
     *
     * @param quantity the new quantity
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the subscription date.
     *
     * @return the subscription date
     */
    public Date getSubscriptionDate() {
        return subscriptionDate;
    }

    /**
     * Sets the subscription date.
     *
     * @param subscriptionDate the new subscription date
     */
    public void setSubscriptionDate(Date subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    /**
     * Gets the service template.
     *
     * @return the service template
     */
    public ServiceTemplate getServiceTemplate() {
        return serviceTemplate;
    }

    /**
     * Sets the service template.
     *
     * @param serviceTemplate the new service template
     */
    public void setServiceTemplate(ServiceTemplate serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
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
     * Gets the rate until date.
     *
     * @return the rate until date
     */
    public Date getRateUntilDate() {
        return rateUntilDate;
    }

    /**
     * Sets the rate until date.
     *
     * @param rateUtilDate the new rate until date
     */
    public void setRateUntilDate(Date rateUtilDate) {
        this.rateUntilDate = rateUtilDate;
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
    
    @Override
    public String toString() {
        return String.format("ServiceToActivateDto [code=%s, quantity=%s, subscriptionDate=%s, chargeInstanceOverrides=%s, customFields=%s]", code, quantity, subscriptionDate,
             customFields);
    }
}