/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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
package org.meveo.model.technicalservice.wsendpoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.ObservableEntity;
import org.meveo.model.annotation.ImportOrder;
import org.meveo.model.scripts.Function;

/**
 * Configuration of a websocket endpoint allowing to use a technical service.
 *
 */
@Entity
@Table(name = "service_wsendpoint")
@GenericGenerator(name = "ID_GENERATOR", strategy = "increment")
@NamedQueries({@NamedQuery(name = "WSEndpoint.deleteByService", query = "DELETE from WSEndpoint e WHERE e.service.id=:serviceId"),
		@NamedQuery(name = "WSEndpoint.deleteById", query = "DELETE from WSEndpoint e WHERE e.id=:endpointId")})
@ImportOrder(5)
@ExportIdentifier({ "code" })
@ModuleItem(value = "WSEndpoint", path = "wsendpoints")
@ModuleItemOrder(80)
@ObservableEntity
public class WSEndpoint extends BusinessEntity {

	private static final long serialVersionUID = 6561905332917884614L;
	
	public static final String ENDPOINT_INTERFACE_JS = "WSEndpointInterface";

	public static final Pattern basePathPattern = Pattern.compile("[a-zA-Z0-9_\\-.]+");


	/** Whether websocket endpoint is accessible without logging */
	@Column(name = "secured", nullable = false)
	@Type(type = "numeric_boolean")
	private boolean secured = true;

	/**
	 * Technical service associated to the endpoint
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id", updatable = false, nullable = false)
	private Function service;

	public void setCode(String code){
		Matcher matcher = basePathPattern.matcher(code);
		if(matcher.matches()) {
			this.code = code;
		} else {
			throw new RuntimeException("invalid code");
		}
	}

	public Function getService() {
		return service;
	}

	public void setService(Function service) {
		this.service = service;
	}

	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}
	
}
