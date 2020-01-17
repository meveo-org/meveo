package org.meveo.api.dto.billing;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.CustomFieldsDto;
import org.meveo.model.catalog.ServiceTemplate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

/**
 * Service information that will be activated. Code is use to identity the
 * {@link ServiceTemplate}.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "ServiceToUpdate")
@XmlAccessorType(XmlAccessType.FIELD)
@Api("ServiceToUpdateDto")
public class ServiceToUpdateDto implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3815026205495621916L;

	/** Service instance ID. Read-only */
	@XmlAttribute()
	@ApiModelProperty("Id of the service. Read-only")
	private Long id;

	/**
	 * Service instance code. Note: not a unique identifier as service can be
	 * activated mnultiple times
	 */
	@XmlAttribute()
	@ApiModelProperty("Service instance code. Note: not a unique identifier as service can be activated mnultiple times")
	private String code;

	/** Description. */
	@XmlAttribute()
	@ApiModelProperty("Description of the service. Mainly use for display.")
	private String description;

	/** The number of service to process when update is reactivation. */
	@XmlElement(required = false)
	@ApiModelProperty("The number of service to process when update is reactivation")
	private BigDecimal quantity;

	/**
	 * Service suspension or reactivation date - used in service suspension or
	 * reactivation API only.
	 */
	@ApiModelProperty("Service suspension or reactivation date - used in service suspension or reactivation API only.")
	private Date actionDate;

	/** Date when this service is suspended. */
	@ApiModelProperty("Date when this service is suspended.")
	private Date endAgreementDate;

	/** Custom fields. */
	@ApiModelProperty("The custom fields for the services processed")
	private CustomFieldsDto customFields;

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * Gets the action date.
	 *
	 * @return the action date
	 */
	public Date getActionDate() {
		return actionDate;
	}

	/**
	 * Sets the action date.
	 *
	 * @param actionDate the new action date
	 */
	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
	}

	/**
	 * Gets the end agreement date.
	 *
	 * @return the end agreement date
	 */
	public Date getEndAgreementDate() {
		return endAgreementDate;
	}

	/**
	 * Sets the end agreement date.
	 *
	 * @param endAgreementDate the new end agreement date
	 */
	public void setEndAgreementDate(Date endAgreementDate) {
		this.endAgreementDate = endAgreementDate;
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

	@Override
	public String toString() {
		return "ServiceToSuspendDto [code=" + code + ", actionDate=" + actionDate + "]";
	}
}