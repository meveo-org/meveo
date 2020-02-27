package org.meveo.api.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiOperation;

import java.io.Serializable;
import org.meveo.api.message.exception.InvalidDTOException;
/**
 * A base class for all API DTO classes
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@JsonInclude(Include.NON_NULL)
public abstract class BaseDto implements Serializable {
    private static final long serialVersionUID = 4456089256601996946L;
    /**
     * Validate DTO
     *
     * @throws InvalidDTOException Validation exception
     */
    @ApiOperation("Validates the dto")
    public void validate() throws InvalidDTOException {
    }
}