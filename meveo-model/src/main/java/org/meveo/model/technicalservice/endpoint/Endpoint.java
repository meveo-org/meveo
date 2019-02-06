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
package org.meveo.model.technicalservice.endpoint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.TechnicalService;
import org.meveo.validation.constraint.nointersection.NoIntersectionBetween;
import org.meveo.validation.constraint.subtypeof.SubTypeOf;

import javax.persistence.*;
import java.util.List;

/**
 * Configuration of an endpoint allowing to use a technical service.
 *
 * @author clement.bareth
 * @since 01.02.2019
 */
@Entity
@Table(name = "service_endpoint")
@GenericGenerator(name = "ID_GENERATOR", strategy = "increment")
@NoIntersectionBetween(
        firstCollection = "pathParameters.endpointParameter.parameter",
        secondCollection = "parametersMapping.endpointParameter.parameter"
)
public class Endpoint extends BusinessEntity {

    /**
     * Technical service associated to the endpoint
     */
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Function.class)
    @JoinColumn(name = "service_id", updatable = false, nullable = false)
    private TechnicalService service;

    /**
     * Whether the execution of the service will be syncrhonous.
     * If asynchronous, and id of execution will be returned to the user.
     */
    @Type(type = "numeric_boolean")
    @Column(name = "synchronous", nullable = false)
    private boolean synchronous;

    /**
     * Method used to access the endpoint.
     * Conditionates the input format of the endpoint.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private EndpointHttpMethod method;

    /**
     * Parameters that will be exposed in the endpoint path
     */
    @OneToMany(mappedBy = "endpointParameter.endpoint", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderColumn(name = "position")
    private List<EndpointPathParameter> pathParameters;

    /**
     * Mapping of the parameters that are not defined as path parameters
     */
    @OneToMany(mappedBy = "endpointParameter.endpoint", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TSParameterMapping> parametersMapping;

    @Transient
    private String endpointUrl;

    @PostLoad
    private void postLoad() {
        endpointUrl = "/rest"+code;
        pathParameters.forEach(endpointPathParameter -> endpointUrl += "/{"+endpointPathParameter+"}");
    }

    public TechnicalService getService() {
        return service;
    }

    public void setService(TechnicalService service) {
        this.service = service;
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

    public List<EndpointPathParameter> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(List<EndpointPathParameter> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public List<TSParameterMapping> getParametersMapping() {
        return parametersMapping;
    }

    public void setParametersMapping(List<TSParameterMapping> parametersMapping) {
        this.parametersMapping = parametersMapping;
    }

    @Transient
    public String getEndpointUrl() {
        return endpointUrl;
    }

}
