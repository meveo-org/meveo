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
import org.meveo.api.dto.BusinessDto;

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
public class TechnicalServiceDto extends BusinessDto {

    private static final long serialVersionUID = 5579910176536059520L;

    private List<InputOutputDescription> descriptions = new ArrayList<>();

    @NotNull(message = "The technical service name must be provided")
    private String name;

    @Min(value = 0, message = "Technical version cannot be lower than {value}")
    private Integer version;

    private String serviceType;

    public TechnicalServiceDto() {

    }

    @Override
    public String getCode() {
        return name + "." + version;
    }

    /**
     * Description of the inputs and outputs of the connector
     */
    public List<InputOutputDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<InputOutputDescription> dto) {
        this.descriptions = dto;
    }

    /**
     * Name of the connector
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Name of the connector
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Version of the technical service
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param version Version of the technical service
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * @return Name of the service type defined by the administrator
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * @param serviceType Name of the service type defined by the administrator
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}