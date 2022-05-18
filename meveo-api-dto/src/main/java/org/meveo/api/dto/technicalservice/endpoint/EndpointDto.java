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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.EndpointPool;
import org.meveo.validation.constraint.nointersection.NoIntersectionBetween;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Configuration of an endpoint allowing to use a technical service.
 *
 * @author clement.bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 * @since 01.02.2019
 */
@XmlRootElement(name = "Endpoint")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("EndpointDto")
@NoIntersectionBetween(firstCollection = "parameterMappings.serviceParameter", secondCollection = "pathParameters")
public class EndpointDto extends BusinessEntityDto implements Serializable {

	private static final long serialVersionUID = 3419481525817374645L;

	/** Whether endpoint is accessible without logging */
	@ApiModelProperty("Whether endpoint is accessible without logging")
	@JsonProperty(defaultValue = "true")
	private boolean secured = true;
	
	/** Whether to check exact match of path parameters number */
	@ApiModelProperty("Whether to check exact match of path parameters number")
	private boolean checkPathParams = true;
	
	/**
     * Code of the technical service to update or create
     */
    @JsonProperty(required = true) @NotNull
    @ApiModelProperty(required = true, value = "Code of the technical service")
    private String serviceCode;

    /**
     * Whether the endpoint should be synchronous
     */
    @JsonProperty(required = true) @NotNull
    @ApiModelProperty(required = true, value = "synchronous")
    private boolean synchronous;

    /**
     * Method to use to access the endpoint
     */
    @JsonProperty(required = true) @NotNull
    @ApiModelProperty(required = true, value = "Http method of the endpoint")
    private EndpointHttpMethod method;

    /**
     * Mapping for the technical service parameters that are not defined as path parameter
     */
    @JsonProperty(required = true) @NotNull
    @ApiModelProperty(required = true, value = "Parameter mappings information")
    private List<TSParameterMappingDto> parameterMappings;

    /**
     * Ordered list of parameters that will construct endpoint path
     */
    @JsonProperty
    @ApiModelProperty("List of endpoint path parameters")
    private List<String> pathParameters = new ArrayList<>();

    @JsonProperty
    @ApiModelProperty("Roles")
    private List<String> roles = new ArrayList<>();

    /**
     * JSONata query used to transform the result
     */
    @JsonProperty
    @ApiModelProperty("JSONata query to transform the serialized result")
    private String jsonataTransformer;
    
    /**
     * Context variable to be returned by the endpoint
     */
    @JsonProperty
    @ApiModelProperty("Name of the returned context variable")
    private String returnedVariableName;

    /**
     * Whether to serialize the result of the endpoint if a returned variable has been specified
     */
    @JsonProperty
    @ApiModelProperty("Whether to serialize the result of the endpoint")
    private boolean serializeResult;
    
    /**
     * Content type of the response
     */
    @JsonProperty
    @ApiModelProperty("Content type of the response")
    private String contentType;


    /**
     * Content type of the response
     */
    @JsonProperty
    @ApiModelProperty("endpoint base path, built from service code if not set")
    private String basePath;


    /**
     * Content type of the response
     */
    @JsonProperty
    @ApiModelProperty("endpoint path , built from path parameters if not set")
    private String path;
    
    @JsonProperty
    private EndpointPool pool;
    
    public EndpointDto() {
    	super();
    }
    
    public String getContentType() {
		return contentType;
	}
    
	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public boolean isSerializeResult() {
        return serializeResult;
    }

    public void setSerializeResult(boolean serializeResult) {
        this.serializeResult = serializeResult;
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

    public List<String> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(List<String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public List<TSParameterMappingDto> getParameterMappings() {
        return parameterMappings;
    }

    public void setParameterMappings(List<TSParameterMappingDto> parameterMappings) {
        this.parameterMappings = parameterMappings;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

	public boolean isCheckPathParams() {
		return checkPathParams;
	}

	public void setCheckPathParams(boolean checkPathParams) {
		this.checkPathParams = checkPathParams;
	}

    public String getBasePath() {return basePath;}

    public void setBasePath(String basePath) {this.basePath = basePath;}

    public String getPath() {return path;}

    public void setPath(String path) {this.path = path;}

	/**
	 * @return the {@link #pool}
	 */
	public EndpointPool getPool() {
		return pool;
	}

	/**
	 * @param pool the pool to set
	 */
	public void setPool(EndpointPool pool) {
		this.pool = pool;
	}
}
