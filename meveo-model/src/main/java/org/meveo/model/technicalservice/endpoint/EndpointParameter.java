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

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Embeddable representation of an endpoint parameter.
 *
 * @author clement.bareth
 * @since 01.02.2019
 */
@Embeddable
public class EndpointParameter implements Serializable {

	private static final long serialVersionUID = 4736053515328740887L;

	/**
	 * Endpoint associated to the parameter
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "endpoint_id")
	private Endpoint endpoint;

	/**
	 * Input property of the technical service described by the endpoint
	 */
	@Column(name = "parameter_id")
	private String parameter;

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * @return Mapped input of the function described by the endpoint
	 */
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Override
	public String toString() {
		return parameter;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		EndpointParameter that = (EndpointParameter) o;
		return Objects.equals(getEndpoint(), that.getEndpoint()) && Objects.equals(getParameter(), that.getParameter());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getEndpoint(), getParameter());
	}
}
