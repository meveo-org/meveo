package org.meveo.api.dto;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class ServiceToTerminateDto.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since Nov 13, 2013
 */
@XmlRootElement(name = "ServiceToTerminate")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class ServiceToTerminateDto implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3267838736094614395L;

    /** The service id. */
    @ApiModelProperty("Id of this entity")
    private String serviceId;
    
    /** The termination date. */
    @ApiModelProperty("Date when this service will be terminated")
    private Date terminationDate;

    /**
     * Gets the service id.
     *
     * @return the service id
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the service id.
     *
     * @param serviceId the new service id
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Gets the termination date.
     *
     * @return the termination date
     */
    public Date getTerminationDate() {
        return terminationDate;
    }

    /**
     * Sets the termination date.
     *
     * @param terminationDate the new termination date
     */
    public void setTerminationDate(Date terminationDate) {
        this.terminationDate = terminationDate;
    }

}
