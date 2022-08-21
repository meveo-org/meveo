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

import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.ObservableEntity;
import org.meveo.model.annotation.ImportOrder;

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
public class WSEndpoint extends Websocket {

	private static final long serialVersionUID = 6561905332917884614L;
	
	public static final String ENDPOINT_INTERFACE_JS = "WSEndpointInterface";

	public static final Pattern basePathPattern = Pattern.compile("[a-zA-Z0-9_\\-.]+");
	
}
