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

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.core.ServerResponse;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.TechnicalServiceApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.TechnicalServicesDto;
import org.meveo.api.dto.response.ListTechnicalServiceResponse;
import org.meveo.api.dto.response.TechnicalServiceResponse;
import org.meveo.api.dto.technicalservice.InputOutputDescription;
import org.meveo.api.dto.technicalservice.ProcessDescriptionsDto;
import org.meveo.api.dto.technicalservice.TechnicalServiceDto;
import org.meveo.api.dto.technicalservice.TechnicalServiceFilters;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.rest.technicalservice.TechnicalServiceRs;
import org.meveo.model.technicalservice.TechnicalService;

/**
 * @author Clément Bareth
 */
@RequestScoped
@Interceptors({WsRestApiInterceptor.class})
public abstract class AbstractTechnicalServiceRsImpl<T extends TechnicalService, D extends TechnicalServiceDto> extends BaseRs implements TechnicalServiceRs<D> {

    private TechnicalServiceApi<T, D> tsApi;

    protected abstract TechnicalServiceApi<T, D> technicalServiceApi();

    @PostConstruct
    private void init(){
        tsApi = technicalServiceApi();
    }

    @Override
    public ActionStatus index() {
        return null;
    }

    @Override
    public ActionStatus create(D postData) {
        ActionStatus result = new ActionStatus();
        try {
            tsApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus update(D postData) {
        ActionStatus result = new ActionStatus();
        try {
            tsApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus remove(String connectorName, Integer version) {
        ActionStatus result = new ActionStatus();
        try {
            tsApi.remove(connectorName, version);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus createOrUpdate(D postData) {
        ActionStatus result = new ActionStatus();
        try {
            tsApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public TechnicalServiceResponse findByNameAndVersionOrLatest(String connectorName, Integer version) {
        TechnicalServiceResponse result = new TechnicalServiceResponse();
        try {
            result.setTechnicalService(tsApi.findByNameAndVersionOrLatest(connectorName, version));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

    @Override
    public ListTechnicalServiceResponse list(TechnicalServiceFilters filters, Date sinceDate) {
    	
    	ListTechnicalServiceResponse response = new ListTechnicalServiceResponse();
        TechnicalServicesDto technicalServicesDto;
        if (sinceDate == null) {
            technicalServicesDto = tsApi.list(filters);
        } else {
            technicalServicesDto = tsApi.findByNewerThan(filters, sinceDate);
        }
        
        response.setConnectors(technicalServicesDto);
        
        return response;
    }

    @Override
    public ListTechnicalServiceResponse listByName(String connectorName) {
        ListTechnicalServiceResponse result = new ListTechnicalServiceResponse();
        try {
            result.setConnectors(tsApi.listByName(connectorName));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

    @Override
    public Response updateDescription(@PathParam("name") String name, @QueryParam("version") Integer version, ProcessDescriptionsDto dtos){
        ServerResponse response = new ServerResponse();
        try {
            tsApi.updateDescription(name, version, dtos);
            response.setStatus(200);
        } catch (Exception e) {
            response.setStatus(500);
            response.setEntity(e.getMessage());
        }
        return response;
    }

    @Override
    public Response names(){
        ServerResponse response = new ServerResponse();
        try {
            List<String> names = tsApi.names();
            response.setStatus(200);
            response.setEntity(names);
        } catch (Exception e) {
            response.setStatus(400);
            response.setEntity(e.getMessage());
        }
        return response;
    }

    @Override
    public Response versions(String name){
        ServerResponse response = new ServerResponse();
        try {
            List<Integer> names = tsApi.versions(name);
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
            boolean exists = tsApi.exists(name, version);
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
            long count = tsApi.count(filters);
            response.setStatus(200);
            response.setEntity(count);
        } catch (Exception e) {
            response.setStatus(500);
            response.setEntity(e.getMessage());
        }
        return response;
    }

    @Override
    public Response description(String name, Integer version){
        ServerResponse response = new ServerResponse();
        try {
            List<InputOutputDescription> desc = tsApi.description(name, version);
            response.setStatus(200);
            response.setEntity(desc);
        } catch(EntityDoesNotExistsException e){
            response.setStatus(404);
        } catch (Exception e) {
            response.setStatus(500);
            response.setEntity(e.getMessage());
        }
        return response;
    }

    @Override
    public void enable(String name, Integer version) throws EntityDoesNotExistsException, BusinessException {
        tsApi.disable(name, version, false);
    }

    @Override
    public void disable(String name, Integer version) throws EntityDoesNotExistsException, BusinessException {
        tsApi.disable(name, version, true);
    }
}
