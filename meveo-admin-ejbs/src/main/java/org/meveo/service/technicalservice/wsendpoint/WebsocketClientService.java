/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.technicalservice.wsendpoint;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.wsendpoint.WebsocketClient;
import org.meveo.service.base.BusinessService;

@Stateless
public class WebsocketClientService extends BusinessService<WebsocketClient> {

	public List<WebsocketClient> findByServiceCode(String code) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<WebsocketClient> query = cb.createQuery(WebsocketClient.class);
		Root<WebsocketClient> root = query.from(WebsocketClient.class);
		final Join<WebsocketClient, Function> service = root.join("service");
		query.where(cb.equal(service.get("code"), code));
		return getEntityManager().createQuery(query).getResultList();
	}
}
