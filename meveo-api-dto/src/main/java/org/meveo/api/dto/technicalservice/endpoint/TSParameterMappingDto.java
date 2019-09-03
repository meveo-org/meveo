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
package org.meveo.api.dto.technicalservice.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.meveo.api.dto.technicalservice.InputPropertyDto;

import java.io.Serializable;

/**
 * Configration of how a GET or POST parameter will be exposed
 */
public class TSParameterMappingDto implements Serializable {

    /**
     * Technical service's parameter to map
     */
    @JsonProperty(required = true)
    private String serviceParameter;

    /**
     * Whether the parameter is multivalued
     */
    @JsonProperty(required = true)
    private String multivalued;

    /**
     * Name of the parameter as exposed by the endpoint
     */
    @JsonProperty
    private String parameterName;

    /**
     * Default value to give to the parameter
     */
    @JsonProperty(required = true)
    private String defaultValue;

    public String getServiceParameter() {
        return serviceParameter;
    }

    public void setServiceParameter(String serviceParameter) {
        this.serviceParameter = serviceParameter;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getMultivalued() {
        return multivalued;
    }

    public void setMultivalued(String multivalued) {
        this.multivalued = multivalued;
    }
}
