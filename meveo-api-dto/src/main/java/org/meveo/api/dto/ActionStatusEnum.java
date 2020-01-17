package org.meveo.api.dto;

import io.swagger.annotations.ApiModel;

/**
 * Tells whether the request is successful or not.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@ApiModel
public enum ActionStatusEnum {
    /**
     * Request is ok. No error found.
     */
    SUCCESS,

    /**
     * Request failed. See ActionStatus.errorCode for an error code.
     */
    FAIL
}
