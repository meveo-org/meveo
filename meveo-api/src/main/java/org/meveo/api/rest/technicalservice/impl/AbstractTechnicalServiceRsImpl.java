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

import org.jboss.resteasy.core.ServerResponse;
import org.meveo.api.TechnicalServiceApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.ListTechnicalServiceResponse;
import org.meveo.api.dto.response.TechnicalServiceResponse;
import org.meveo.api.dto.technicalservice.TechnicalServiceDto;
import org.meveo.api.dto.technicalservice.TechnicalServiceFilters;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.technicalservice.TechnicalServiceRs;
import org.meveo.model.technicalservice.TechnicalService;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Clément Bareth
 */
@RequestScoped
@Interceptors({WsRestApiInterceptor.class})
public abstract class AbstractTechnicalServiceRsImpl<T extends TechnicalService> extends BaseRs implements TechnicalServiceRs {

    protected abstract TechnicalServiceApi<T> technicalServiceApi();

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
    public ActionStatus remove(String connectorName, Integer version) {
        ActionStatus result = new ActionStatus();
        try {
            technicalServiceApi().remove(connectorName, version);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus createOrUpdate(TechnicalServiceDto postData) {
        ActionStatus result = new ActionStatus();
        try {
            technicalServiceApi().createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public TechnicalServiceResponse findByNameAndVersionOrLatest(String connectorName, Integer version) {
        TechnicalServiceResponse result = new TechnicalServiceResponse();
        try {
            result.setTechnicalService(technicalServiceApi().findByNameAndVersionOrLatest(connectorName, version));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

    @Override
    public ListTechnicalServiceResponse list(TechnicalServiceFilters filters) {
        ListTechnicalServiceResponse result = new ListTechnicalServiceResponse();
        try {
            result.setConnectors(technicalServiceApi().list(filters));
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

    public Response names(){
        ServerResponse response = new ServerResponse();
        try {
            List<String> names = technicalServiceApi().names();
            response.setStatus(200);
            response.setEntity(names);
        } catch (Exception e) {
            response.setStatus(400);
            response.setEntity(e.getMessage());
        }
        return response;
    }

    public Response versions(@PathParam("name") String name){
        ServerResponse response = new ServerResponse();
        try {
            List<Integer> names = technicalServiceApi().versions(name);
            response.setStatus(200);
            response.setEntity(names);
        } catch (Exception e) {
            response.setStatus(400);
            response.setEntity(e.getMessage());
        }
        return response;
    }

    @Override
    public Response exists(String name, Integer version) {

        ServerResponse response = new ServerResponse();
        try {
            boolean exists = technicalServiceApi().exists(name, version);
            if(exists){
                response.setStatus(200);
            }else{
                response.setStatus(404);
            }
        } catch (Exception e) {
            response.setStatus(400);
            response.setEntity(e.getMessage());
        }
        return response;
    }

    @Override
    public Response count(TechnicalServiceFilters filters) {
        ServerResponse response = new ServerResponse();
        try {
            long count = technicalServiceApi().count(filters);
            response.setStatus(200);
            response.setEntity(count);
        } catch (Exception e) {
            response.setStatus(400);
            response.setEntity(e.getMessage());
        }
        return response;
    }

    @Override
    public ActionStatus rename(String oldName, String newName) {
        ActionStatus result = new ActionStatus();
        try {
            technicalServiceApi().rename(oldName, newName);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus renameVersion(String name, Integer oldVersion, Integer newVersion) {
        ActionStatus result = new ActionStatus();
        try {
            technicalServiceApi().renameVersion(name, oldVersion, newVersion);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }
}
