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
package org.meveo.api.rest.technicalservice.impl;

import org.meveo.api.TechnicalServiceApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.ListTechnicalServiceResponse;
import org.meveo.api.dto.response.TechnicalServiceResponse;
import org.meveo.api.dto.technicalservice.TechnicalServiceDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.technicalservice.TechnicalServiceRs;
import org.meveo.model.technicalservice.TechnicalService;
import org.meveo.service.technicalservice.TechnicalServiceService;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;

/**
 * @author Clément Bareth
 */
@RequestScoped
@Interceptors({WsRestApiInterceptor.class})
public abstract class AbstractTechnicalServiceRsImpl<T extends TechnicalService, S extends TechnicalServiceService<T>> extends BaseRs implements TechnicalServiceRs {

    protected abstract TechnicalServiceApi<T, S> technicalServiceApi();

    @Override
    public ActionStatus index() {
        return null;
    }

    @Override
    public ActionStatus create(TechnicalServiceDto postData) {
        ActionStatus result = new ActionStatus();
        try {
            technicalServiceApi().create(postData);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus update(TechnicalServiceDto postData) {
        ActionStatus result = new ActionStatus();
        try {
            technicalServiceApi().update(postData);
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
    public ActionStatus createOrUpdate(TechnicalServiceDto postData) {
        return null;
    }

    @Override
    public TechnicalServiceResponse findByNameAndVersionOrLatest(String connectorName, String version) {
        TechnicalServiceResponse result = new TechnicalServiceResponse();
        try {
            result.setTechnicalServiceDto(technicalServiceApi().findByNameAndVersionOrLatest(connectorName, version != null ? Integer.parseInt(version) : null));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

    @Override
    public ListTechnicalServiceResponse list(String connectorName) {
        ListTechnicalServiceResponse result = new ListTechnicalServiceResponse();
        try {
            result.setConnectors(technicalServiceApi().list(connectorName));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

    @Override
    public ListTechnicalServiceResponse listByName(String connectorName) {
        ListTechnicalServiceResponse result = new ListTechnicalServiceResponse();
        try {
            result.setConnectors(technicalServiceApi().listByName(connectorName));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

}
