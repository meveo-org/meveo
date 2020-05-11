/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.api.dto;

import org.meveo.api.dto.technicalservice.TechnicalServiceDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Data transfer object for connector.
 *
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class TechnicalServicesDto extends BusinessEntityDto {

    private static final long serialVersionUID = -5952076128269784073L;

    @ApiModelProperty("List of technical services information")
    private List<TechnicalServiceDto> technicalServices = new ArrayList<>();

    public TechnicalServicesDto() {
    }

    /**
     * @param services Technical services DTO
     */
    public TechnicalServicesDto(List<TechnicalServiceDto> services) {
        this.technicalServices = services;
    }

    /**
     * @return Technical services DTO
     */
    public List<TechnicalServiceDto> geTechnicalServiceDtos() {
        return technicalServices;
    }

    /**
     * @param services Technical services DTO
     */
    public void setTechnicalServices(List<TechnicalServiceDto> services) {
        this.technicalServices = services;
    }

}