package org.meveo.api.dto.billing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.model.order.OrderItemActionEnum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class ActivateServicesRequestDto.
 *
 * @author Edward P. Legaspi
 * @version 6.7.0
 */
@XmlRootElement(name = "ActivateServicesRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties({ "orderNumber", "orderItemId", "orderItemAction" })
@ApiModel("ActivateServicesRequestDto")
public class ActivateServicesRequestDto extends BaseEntityDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1150993171011072506L;

	/** The subscription. */
	@XmlElement(required = true)
	@ApiModelProperty(required = true, value = "Code of the subscription where the list of services in this request will be activated")
	private String subscription;

	/** The services to activate. */
	@XmlElement
	@ApiModelProperty("Wrapper to a list of services that will be activated")
	private ServicesToActivateDto servicesToActivate = new ServicesToActivateDto();

	/** The order number. */
	@ApiModelProperty("Order number to which this request is attached")
	private String orderNumber;

	/** The order item id. */
	@ApiModelProperty("Order item id to which this request is attached")
	private Long orderItemId;

	/** The order item action. */
	@ApiModelProperty("The type of order action")
	private OrderItemActionEnum orderItemAction;

	/**
	 * Gets the subscription.
	 *
	 * @return the subscription
	 */
	public String getSubscription() {
		return subscription;
	}

	/**
	 * Sets the subscription.
	 *
	 * @param subscription the new subscription
	 */
	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	/**
	 * Gets the services to activate dto.
	 *
	 * @return the services to activate dto
	 */
	public ServicesToActivateDto getServicesToActivateDto() {
		return servicesToActivate;
	}

	/**
	 * Sets the services to activate dto.
	 *
	 * @param servicesToActivate the new services to activate dto
	 */
	public void setServicesToActivateDto(ServicesToActivateDto servicesToActivate) {
		this.servicesToActivate = servicesToActivate;
	}

	/**
	 * Gets the order number.
	 *
	 * @return the order number
	 */
	public String getOrderNumber() {
		return orderNumber;
	}

	/**
	 * Sets the order number.
	 *
	 * @param orderNumber the new order number
	 */
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	/**
	 * Gets the order item id.
	 *
	 * @return the order item id
	 */
	public Long getOrderItemId() {
		return orderItemId;
	}

	/**
	 * Sets the order item id.
	 *
	 * @param orderItemId the new order item id
	 */
	public void setOrderItemId(Long orderItemId) {
		this.orderItemId = orderItemId;
	}

	/**
	 * Gets the order item action.
	 *
	 * @return the order item action
	 */
	public OrderItemActionEnum getOrderItemAction() {
		return orderItemAction;
	}

	/**
	 * Sets the order item action.
	 *
	 * @param action the new order item action
	 */
	public void setOrderItemAction(OrderItemActionEnum action) {
		this.orderItemAction = action;
	}

	@Override
	public String toString() {
		return "ActivateServicesRequestDto [subscription=" + subscription + ", servicesToActivate=" + servicesToActivate + ", orderNumber=" + orderNumber + "]";
	}
}