package org.meveo.api.dto.billing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

/**
 * A wrapper class to a list of service information for activation.
 *
 * @see ServiceToActivateDto
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "ServicesToActivate")
@XmlAccessorType(XmlAccessType.FIELD)
@Api("ServicesToActivateDto")
public class ServicesToActivateDto implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6088111478916521480L;

	/** The service. */
	@XmlElement(required = true)
	@ApiModelProperty("List of services information to activate")
	private List<ServiceToActivateDto> service;

	/**
	 * Gets the service.
	 *
	 * @return the service
	 */
	public List<ServiceToActivateDto> getService() {
		return service;
	}

	/**
	 * Sets the service.
	 *
	 * @param services the new service
	 */
	public void setService(List<ServiceToActivateDto> services) {
		this.service = services;
	}

	/**
	 * Adds the service.
	 *
	 * @param serviceToActivate the service to activate
	 */
	public void addService(ServiceToActivateDto serviceToActivate) {
		if (service == null) {
			service = new ArrayList<>();
		}
		service.add(serviceToActivate);
	}

	@Override
	public String toString() {
		return "ServicesToActivateDto [service=" + service + "]";
	}
}