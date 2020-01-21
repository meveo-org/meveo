package org.meveo.api.dto.response.dwh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModelProperty;

import org.meveo.api.dto.dwh.MeasurableQuantityDto;
import org.meveo.api.dto.response.BaseResponse;

/**
 * The Class GetMeasurableQuantityResponse.
 * 
 * @author anasseh
 */
@XmlRootElement(name = "GetMeasurableQuantityResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetMeasurableQuantityResponse extends BaseResponse {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The measurable quantity dto. */
    @ApiModelProperty("Measurable quantity information")
    private MeasurableQuantityDto measurableQuantityDto;

    /**
     * Gets the measurable quantity dto.
     *
     * @return the measurable quantity dto
     */
    public MeasurableQuantityDto getMeasurableQuantityDto() {
        return measurableQuantityDto;
    }

    /**
     * Sets the measurable quantity dto.
     *
     * @param measurableQuantityDto the new measurable quantity dto
     */
    public void setMeasurableQuantityDto(MeasurableQuantityDto measurableQuantityDto) {
        this.measurableQuantityDto = measurableQuantityDto;
    }


    @Override
    public String toString() {
        return "GetMeasurableQuantityResponse [measurableQuantityDto=" + (measurableQuantityDto == null ? null : measurableQuantityDto) + "]";
    }
}