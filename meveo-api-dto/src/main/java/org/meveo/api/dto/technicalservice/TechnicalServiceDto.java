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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.function.FunctionDto;
import org.meveo.model.persistence.JacksonUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.type.TypeReference;


/**
 * Data transfer object for connector.
 *
 * @author Clément Bareth
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnicalServiceDto extends FunctionDto {

    private static final long serialVersionUID = 5579910176536059520L;

    /**
     * Description of the inputs and outputs of the technical service
     */
    private Map<String, InputOutputDescription> descriptions = new HashMap<>();

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

    /** Whether the entity is disabled. */
    protected boolean disabled;
    
    private Set<String> extendedServices;

    /**
	 * Instantiates a new technical service dto.
	 */
    public TechnicalServiceDto() {

    }

    /**
	 * Gets the extended services.
	 *
	 * @return the extended services
	 */
    public Set<String> getExtendedServices() {
		return extendedServices;
	}

	/**
	 * Sets the extended services.
	 *
	 * @param extendedServices the new extended services
	 */
	public void setExtendedServices(Set<String> extendedServices) {
		this.extendedServices = extendedServices;
	}

	@Override
    public String getCode() {
        return name + "." + version;
    }

    /**
	 * Gets the description of the inputs and outputs of the technical service.
	 *
	 * @return the description of the inputs and outputs of the technical service
	 */
    public Map<String, InputOutputDescription> getDescriptions() {
        return descriptions;
    }
    
    @JsonSetter
    private void setDescriptions(Object descriptions) {
    	if(descriptions instanceof Map) {
    		TypeReference<Map<String, InputOutputDescription>> typeref = new TypeReference<Map<String, InputOutputDescription>>(){};
			Map<String, InputOutputDescription> desc = JacksonUtil.convert(descriptions, typeref);
    		setDescriptions(desc);
    		
    	} else {
    		TypeReference<List<InputOutputDescription>> typeref = new TypeReference<List<InputOutputDescription>>(){};
    		List<InputOutputDescription> desc = JacksonUtil.convert(descriptions, typeref);
    		setDescriptions(desc);
    	}
    }

    /**
	 * Sets the description of the inputs and outputs of the technical service.
	 *
	 * @param dto the new description of the inputs and outputs of the technical service
	 */
    public void setDescriptions(Map<String, InputOutputDescription> dto) {
        this.descriptions = dto;
    }
    
    /**
	 * Sets the description of the inputs and outputs of the technical service.
	 *
	 * @param dto the new description of the inputs and outputs of the technical service
	 */
    public void setDescriptions(List<InputOutputDescription> dto) {
        dto.forEach(d -> this.descriptions.put(d.getName(), d));
    }

    /**
	 * Gets the name of the technical service.
	 *
	 * @return the name of the technical service
	 */
    public String getName() {
        return name;
    }

    /**
	 * Sets the name of the technical service.
	 *
	 * @param name the new name of the technical service
	 */
    public void setName(String name) {
        this.name = name;
    }

    /**
	 * Gets the version of the technical service.
	 *
	 * @return the version of the technical service
	 */
    public Integer getVersion() {
        return version;
    }

    /**
	 * Sets the version of the technical service.
	 *
	 * @param version the new version of the technical service
	 */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
	 * Gets the name of the service type defined by the administrator.
	 *
	 * @return the name of the service type defined by the administrator
	 */
    public String getServiceType() {
        return serviceType;
    }

    /**
	 * Sets the name of the service type defined by the administrator.
	 *
	 * @param serviceType the new name of the service type defined by the administrator
	 */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public Boolean isDisabled() {
        return disabled;
    }

    /**
	 * Sets the whether the entity is disabled.
	 *
	 * @param disabled the new whether the entity is disabled
	 */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}