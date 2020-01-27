package org.meveo.api.dto.notification;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseEntityDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Wrapper class for a list of inbound request dto.
 *
 * @see InboundRequestDto
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "InboutRequests")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel("InboundRequestsDto")
public class InboundRequestsDto extends BaseEntityDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4968458684592803252L;

	/** The inbound request. */
	@ApiModelProperty("List of inbound requests")
	private List<InboundRequestDto> inboundRequest;

	/**
	 * Gets the inbound request.
	 *
	 * @return the inbound request
	 */
	public List<InboundRequestDto> getInboundRequest() {
		if (inboundRequest == null)
			inboundRequest = new ArrayList<InboundRequestDto>();
		return inboundRequest;
	}

	/**
	 * Sets the inbound request.
	 *
	 * @param inboundRequest the new inbound request
	 */
	public void setInboundRequest(List<InboundRequestDto> inboundRequest) {
		this.inboundRequest = inboundRequest;
	}

	@Override
	public String toString() {
		return "InboutRequestsDto [inboundRequest=" + inboundRequest + ", toString()=" + super.toString() + "]";
	}

}
