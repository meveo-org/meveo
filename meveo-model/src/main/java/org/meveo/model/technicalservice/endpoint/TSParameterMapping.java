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

import javax.persistence.*;

import org.hibernate.annotations.Type;

/**
 * Configuration of an endpoint allowing to use a technical service.
 *
 * @author clement.bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 * @since 01.02.2019
 */
@Entity
@Table(name = "service_parameter_mapping")
@NamedQuery(name = "TSParameterMapping.deleteByEndpoint", query = "DELETE from TSParameterMapping m WHERE m.endpointParameter.endpoint.id=:endpointId")
public class TSParameterMapping {

	@EmbeddedId
	private EndpointParameter endpointParameter;

	/**
	 * Exposed name of the parameter
	 */
	@Column(name = "parameter_name")
	private String parameterName;

	@Column(name = "multivalued")
	@Type(type = "numeric_boolean")
	private boolean multivalued;

	/**
	 * Default value of the parameter
	 */
	@Column(name = "default_value")
	private String defaultValue;

	/**
	 * When this value is set to true, the min length of string and array objects
	 * must be set to 1 by default or they should not be null.
	 */
	@Type(type = "numeric_boolean")
	@Column(name = "value_required")
	private boolean valueRequired;

	public EndpointParameter getEndpointParameter() {
		return endpointParameter;
	}

	public void setEndpointParameter(EndpointParameter endpointParameter) {
		this.endpointParameter = endpointParameter;
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

	public boolean isMultivalued() {
		return multivalued;
	}

	public void setMultivalued(boolean multivalued) {
		this.multivalued = multivalued;
	}

	/**
	 * Retrieves the boolean value of whether this parameter is required.
	 * 
	 * @return true if this parameter is required
	 */
	public boolean isValueRequired() {
		return valueRequired;
	}

	/**
	 * Sets whether this parameter is required or not.
	 * 
	 * @param valueRequired boolean value
	 */
	public void setValueRequired(boolean valueRequired) {
		this.valueRequired = valueRequired;
	}
}
