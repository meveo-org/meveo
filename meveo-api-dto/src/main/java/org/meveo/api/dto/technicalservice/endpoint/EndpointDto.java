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
import org.meveo.api.dto.BusinessDto;
import org.meveo.api.dto.technicalservice.InputPropertyDto;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.validation.constraint.nointersection.NoIntersectionBetween;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of an endpoint allowing to use a technical service.
 *
 * @author clement.bareth
 * @since 01.02.2019
 */
@NoIntersectionBetween(firstCollection = "parameterMappings.serviceParameter", secondCollection = "pathParameters")
public class EndpointDto extends BusinessDto implements Serializable {

    /**
     * Code of the technical service to update or create
     */
    @JsonProperty(required = true) @NotNull
    private String serviceCode;

    /**
     * Whether the endpoint should be synchronous
     */
    @JsonProperty(required = true) @NotNull
    private boolean synchronous;

    /**
     * Method to use to access the endpoint
     */
    @JsonProperty(required = true) @NotNull
    private EndpointHttpMethod method;

    /**
     * Mapping for the technical service parameters that are not defined as path parameter
     */
    @JsonProperty(required = true) @NotNull
    private List<TSParameterMappingDto> parameterMappings;

    /**
     * Ordered list of parameters that will construct endpoint path
     */
    @JsonProperty
    private List<InputPropertyDto> pathParameters = new ArrayList<>();


    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public EndpointHttpMethod getMethod() {
        return method;
    }

    public void setMethod(EndpointHttpMethod method) {
        this.method = method;
    }

    public List<InputPropertyDto> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(List<InputPropertyDto> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public List<TSParameterMappingDto> getParameterMappings() {
        return parameterMappings;
    }

    public void setParameterMappings(List<TSParameterMappingDto> parameterMappings) {
        this.parameterMappings = parameterMappings;
    }
}
