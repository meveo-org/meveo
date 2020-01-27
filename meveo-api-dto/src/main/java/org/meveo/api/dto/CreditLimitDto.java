package org.meveo.api.dto;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * The Class CreditLimitDto.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since Nov 13, 2013
 */
@XmlRootElement(name = "CreditLimit")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class CreditLimitDto extends BaseEntityDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7319728721914566412L;

    /** The organization id. */
    @ApiModelProperty("Id of organization")
    private String organizationId;
    
    /** The credit limit. */
    @ApiModelProperty("Credit limit of an organization")
    private BigDecimal creditLimit;

    /**
     * Gets the organization id.
     *
     * @return the organization id
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the organization id.
     *
     * @param organizationId the new organization id
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Gets the credit limit.
     *
     * @return the credit limit
     */
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    /**
     * Sets the credit limit.
     *
     * @param creditLimit the new credit limit
     */
    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

}
