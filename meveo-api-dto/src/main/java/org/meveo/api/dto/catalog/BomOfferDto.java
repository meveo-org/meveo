package org.meveo.api.dto.catalog;

import java.util.ArrayList;
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
import org.meveo.model.catalog.LifeCycleStatusEnum;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

/**
 * A template for creating an offer.
 * 
 * @author anasseh
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "BomOffer")
@XmlAccessorType(XmlAccessType.FIELD)
@Api("BomOfferDto")
public class BomOfferDto extends BaseEntityDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4557706201829891403L;

	/** The bom code. */
	@NotNull
	@XmlAttribute(required = true)
	@ApiModelProperty("Code of the bom")
	private String bomCode;

	/** The code. */
	@NotNull
	@XmlAttribute(required = true)
	@ApiModelProperty("Code of the offer")
	private String code;

	/** The name. */
	@NotNull
	@XmlAttribute(required = true)
	@ApiModelProperty("Name of the offer")
	private String name;

	/** The description. */
	@XmlAttribute
	@ApiModelProperty("Description of the offer")
	private String description;

	/** The custom fields. */
	@XmlElementWrapper(name = "parameters")
	@XmlElement(name = "parameter")
	@ApiModelProperty("Custom fields of the offer")
	private List<CustomFieldDto> customFields;

	/** The prefix. */
	@Deprecated
	private String prefix;

	/** The services to activate. */
	@XmlElementWrapper(name = "servicesToActivate")
	@XmlElement(name = "serviceToActivate")
	@ApiModelProperty("List of services to activate")
	private List<ServiceConfigurationDto> servicesToActivate = new ArrayList<>();

	/** The products to activate. */
	@XmlElementWrapper(name = "productsToActivate")
	@XmlElement(name = "productToActivate")
	@ApiModelProperty("List of products to activate")
	private List<ServiceConfigurationDto> productsToActivate = new ArrayList<>();

	/** The business service models. See BusinessServiceModel. */
	@XmlElementWrapper(name = "businessServiceModels")
	@XmlElement(name = "businessServiceModel")
	@ApiModelProperty("List of servicess models template use to activate the services.")
	private List<BSMConfigurationDto> businessServiceModels = new ArrayList<>();

	/** The life cycle status enum. */
	@ApiModelProperty("Life cycle value set to the offer after initialization. Should be ACTIVE to be use.")
	private LifeCycleStatusEnum lifeCycleStatusEnum;

	/**
	 * Gets the bom code.
	 *
	 * @return the bom code
	 */
	public String getBomCode() {
		return bomCode;
	}

	/**
	 * Sets the bom code.
	 *
	 * @param bomCode the new bom code
	 */
	public void setBomCode(String bomCode) {
		this.bomCode = bomCode;
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
	 * Gets the services to activate.
	 *
	 * @return the services to activate
	 */
	public List<ServiceConfigurationDto> getServicesToActivate() {
		return servicesToActivate;
	}

	/**
	 * Sets the services to activate.
	 *
	 * @param servicesToActivate the new services to activate
	 */
	public void setServicesToActivate(List<ServiceConfigurationDto> servicesToActivate) {
		this.servicesToActivate = servicesToActivate;
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
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the products to activate.
	 *
	 * @return the products to activate
	 */
	public List<ServiceConfigurationDto> getProductsToActivate() {
		return productsToActivate;
	}

	/**
	 * Sets the products to activate.
	 *
	 * @param productsToActivate the new products to activate
	 */
	public void setProductsToActivate(List<ServiceConfigurationDto> productsToActivate) {
		this.productsToActivate = productsToActivate;
	}

	/**
	 * Gets the business service models.
	 *
	 * @return the business service models
	 */
	public List<BSMConfigurationDto> getBusinessServiceModels() {
		return businessServiceModels;
	}

	/**
	 * Sets the business service models.
	 *
	 * @param businessServiceModels the new business service models
	 */
	public void setBusinessServiceModels(List<BSMConfigurationDto> businessServiceModels) {
		this.businessServiceModels = businessServiceModels;
	}

	/**
	 * Gets the life cycle status enum.
	 *
	 * @return the lifeCycleStatusEnum
	 */
	public LifeCycleStatusEnum getLifeCycleStatusEnum() {
		return lifeCycleStatusEnum;
	}

	/**
	 * Sets the life cycle status enum.
	 *
	 * @param lifeCycleStatusEnum the lifeCycleStatusEnum to set
	 */
	public void setLifeCycleStatusEnum(LifeCycleStatusEnum lifeCycleStatusEnum) {
		this.lifeCycleStatusEnum = lifeCycleStatusEnum;
	}

	@Override
	public String toString() {
		return "BomOfferDto [bomCode=" + bomCode + ", code=" + code + ", name=" + name + ", description=" + description + ", customFields=" + customFields + ", prefix=" + prefix
				+ ", servicesToActivate=" + servicesToActivate + ", productsToActivate=" + productsToActivate + ", businessServiceModels=" + businessServiceModels
				+ ", lifeCycleStatusEnum=" + lifeCycleStatusEnum + "]";
	}

}