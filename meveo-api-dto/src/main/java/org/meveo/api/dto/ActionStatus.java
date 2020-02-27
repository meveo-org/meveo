package org.meveo.api.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.MeveoApiErrorCodeEnum;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Determine the status of the MEVEO API web service response.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@ApiModel
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(Include.NON_NULL)
public class ActionStatus {

    /**
     * Tells whether the instance of this <code>ActionStatus</code> object ok or not.
     */
    @XmlElement(required = true)
    @ApiModelProperty("Status of the api response")
    private ActionStatusEnum status;

    /**
     * An error code.
     */
    @ApiModelProperty("Error code return by an api request")
    private MeveoApiErrorCodeEnum errorCode;

    /**
     * A detailed error message if applicable, can contain the entity id that was created.
     */
    @XmlElement(required = true)
    @ApiModelProperty("Message return by an api request")
    private String message;

    public ActionStatus() {
        status = ActionStatusEnum.SUCCESS;
        this.message = "";
    }

    /**
     * Sets status and message.
     * 
     * @param status action status
     * @param message message
     */
    public ActionStatus(ActionStatusEnum status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Sets status, error code and message.
     * 
     * @param status action status
     * @param errorCode error code
     * @param message message.
     */
    public ActionStatus(ActionStatusEnum status, MeveoApiErrorCodeEnum errorCode, String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ActionStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ActionStatusEnum status) {
        this.status = status;
    }

    /**
     * Error code.
     * 
     * @return Error code
     */
    public MeveoApiErrorCodeEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(MeveoApiErrorCodeEnum errorCode) {
        this.errorCode = errorCode;
    }

    public String getjson() {
        return "{\"status\":\"" + status + "\",\"errorCode\": \"" + errorCode + "\",\"message\": \"" + message + "\"}";
    }

    @Override
    public String toString() {
        return "ActionStatus [status=" + status + ", errorCode=" + errorCode + ", message=" + message + "]";
    }
}
