/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.api.dto.response;

import org.meveo.api.dto.TechnicalServicesDto;
import org.meveo.api.dto.technicalservice.TechnicalServiceDto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * The Class ListConnectorResponse.
 *
 * @author Clément Bareth
 */
@XmlRootElement(name = "ListConnectorResponse")
public class ListTechnicalServiceResponse extends BaseResponse {

    private static final long serialVersionUID = 8305118549760963932L;

    private TechnicalServicesDto technicalServices = new TechnicalServicesDto();

    /**
     * @return the technical services retrieved
     */
    public List<TechnicalServiceDto> getTechnicalServices() {
        return technicalServices.geTechnicalServiceDtos();
    }

    /**
     * @param services the new technical services
     */
    public void setConnectors(TechnicalServicesDto services) {
        this.technicalServices = services;
    }

    @Override
    public String toString() {
        return "ListTechnicalServiceResponse [technicalservices=" + technicalServices
                + ", toString()=" + super.toString() + "]";
    }
}
