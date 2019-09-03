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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.annotation.ImportOrder;
import org.meveo.model.scripts.Function;
import org.meveo.validation.constraint.nointersection.NoIntersectionBetween;

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
@NamedQuery(
        name = "findByParameterName",
        query = "SELECT e FROM Endpoint e " +
                "INNER JOIN e.service as service " +
                "LEFT JOIN e.pathParameters as pathParameter " +
                "LEFT JOIN e.parametersMapping as parameterMapping " +
                "WHERE service.code = :serviceCode " +
                "AND (pathParameter.endpointParameter.parameter = :propertyName OR parameterMapping.endpointParameter.parameter = :propertyName)"
)
@ImportOrder(5)
@ExportIdentifier({ "code" })
@ModuleItem
public class Endpoint extends BusinessEntity {

	private static final long serialVersionUID = 6561905332917884613L;

	/**
     * Technical service associated to the endpoint
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", updatable = false, nullable = false)
    private Function service;

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
    @OneToMany(mappedBy = "endpointParameter.endpoint", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "position")
    private List<EndpointPathParameter> pathParameters = new ArrayList<>();

    /**
     * Mapping of the parameters that are not defined as path parameters
     */
    @OneToMany(mappedBy = "endpointParameter.endpoint", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TSParameterMapping> parametersMapping = new ArrayList<>();
    
    /**
     * JSONata query used to transform the result
     */
    @Column(name = "jsonata_transformer")
    private String jsonataTransformer;
    
    /**
     * Context variable to be returned by the endpoint
     */
    @Column(name = "returned_variable_name")
    private String returnedVariableName;

    /**
     * Context variable to be returned by the endpoint
     */
    @Type(type = "numeric_boolean")
    @Column(name = "serialize_result", nullable = false)
    private boolean serializeResult;
    
    /**
     * Content type of the response
     */
    @Column(name = "content_type")
    private String contentType;
    
    public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setSerializeResult(boolean serializeResult) {
        this.serializeResult = serializeResult;
    }

    public boolean isSerializeResult() {
        return serializeResult;
    }

    public String getReturnedVariableName() {
		return returnedVariableName;
	}

	public void setReturnedVariableName(String returnedVariableName) {
		this.returnedVariableName = returnedVariableName;
	}

	public String getJsonataTransformer() {
        return jsonataTransformer;
    }

    public void setJsonataTransformer(String jsonataTransformer) {
        this.jsonataTransformer = jsonataTransformer;
    }

    public Function getService() {
        return service;
    }

    public void setService(Function service) {
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
        final StringBuilder endpointUrl = new StringBuilder("/rest/"+code);
        pathParameters.forEach(endpointPathParameter -> endpointUrl.append("/{").append(endpointPathParameter).append("}"));
        return endpointUrl.toString();
    }

}
