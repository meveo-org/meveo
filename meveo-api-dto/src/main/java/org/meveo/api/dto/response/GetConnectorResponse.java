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
package org.meveo.api.dto.response;

import org.meveo.api.dto.ConnectorDto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class GetConnectorResponse.
 *
 * @author Clément Bareth
 */
@XmlRootElement(name = "GetConnectorResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetConnectorResponse extends BaseResponse {

    private ConnectorDto connectorDto;

    /**
     * @return the calendar
     */
    public ConnectorDto getConnectorDto() {
        return connectorDto;
    }

    /**
     * @param connectorDto the new connector
     */
    public void setConnectorDto(ConnectorDto connectorDto) {
        this.connectorDto = connectorDto;
    }

    @Override
    public String toString() {
        return "GetCalendarResponse [connector=" + connectorDto + ", toString()=" + super.toString() + "]";
    }

}
