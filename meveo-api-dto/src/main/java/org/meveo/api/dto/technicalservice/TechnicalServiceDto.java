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
package org.meveo.api.dto.technicalservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.meveo.api.dto.BusinessEntityDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Data transfer object for connector.
 *
 * @author Clément Bareth
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnicalServiceDto extends BusinessEntityDto {

    private static final long serialVersionUID = 5579910176536059520L;

    /**
     * Description of the inputs and outputs of the technical service
     */
    private List<InputOutputDescription> descriptions = new ArrayList<>();

    /**
     * Name of the technical service
     */
    @NotNull(message = "The technical service name must be provided")
    private String name;

    /**
     * Version of the technical service
     */
    @Min(value = 0, message = "Technical version cannot be lower than {value}")
    private Integer version;

    /**
     * Name of the service type defined by the administrator
     */
    private String serviceType;

    /**
     * Whether the entity is disabled
     */
    protected boolean disabled;

    public TechnicalServiceDto() {

    }

    @Override
    public String getCode() {
        return name + "." + version;
    }

    public List<InputOutputDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<InputOutputDescription> dto) {
        this.descriptions = dto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}