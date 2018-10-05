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
package org.meveo.api.rest.connector.impl;

import org.meveo.api.ConnectorApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ConnectorDto;
import org.meveo.api.dto.response.GetConnectorResponse;
import org.meveo.api.dto.response.ListConnectorResponse;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.connector.ConnectorRs;
import org.meveo.api.rest.impl.BaseRs;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

/**
 * @author Clément Bareth
 */
@RequestScoped
@Interceptors({WsRestApiInterceptor.class})
public class ConnectorRsImpl extends BaseRs implements ConnectorRs {

    @Inject
    private ConnectorApi connectorApi;

    @Override
    public ActionStatus index() {
        return null;
    }

    @Override
    public ActionStatus create(ConnectorDto postData) {
        ActionStatus result = new ActionStatus();
        try {
            connectorApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus update(ConnectorDto postData) {
        ActionStatus result = new ActionStatus();
        try {
            connectorApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus remove(String connectorName, String version) {
        return null;
    }

    @Override
    public ActionStatus createOrUpdate(ConnectorDto postData) {
        return null;
    }

    @Override
    public GetConnectorResponse findByNameAndVersionOrLatest(String connectorName, String version) {
        GetConnectorResponse result = new GetConnectorResponse();
        try {
            result.setConnectorDto(connectorApi.findByNameAndVersionOrLatest(connectorName, version != null ? Integer.parseInt(version) : null));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

    @Override
    public ListConnectorResponse list(String connectorName) {
        ListConnectorResponse result = new ListConnectorResponse();
        try {
            result.setConnectors(connectorApi.list(connectorName));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

    @Override
    public ListConnectorResponse listByName(String connectorName) {
        ListConnectorResponse result = new ListConnectorResponse();
        try {
            result.setConnectors(connectorApi.listByName(connectorName));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

}
