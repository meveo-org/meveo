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

package org.meveo.api.rest.technicalservice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.meveo.api.rest.technicalservice.impl.EndpointRequest;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;

public class EndpointExecutionBuilder {
    private Map<String, Object> parameters = new HashMap<>();
    private HttpServletResponse resp;
    private String pathInfo;
    private boolean keep;
    private boolean wait;
    private EndpointHttpMethod method;
    private String persistenceContextId;
    private String persistenceContext;
    private String budgetUnit;
    private Double bugetMax;
    private TimeUnit delayUnit;
    private Long delayValue;
    private EndpointRequest request;
    private Endpoint endpoint;
    
    public EndpointExecutionBuilder setEndpoint(Endpoint endpoint) {
    	this.endpoint = endpoint;
    	return this;
    }

    public EndpointExecutionBuilder setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public EndpointExecutionBuilder setResponse(HttpServletResponse resp) {
        this.resp = resp;
        return this;
    }

    public EndpointExecutionBuilder setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
        return this;
    }

    public EndpointExecutionBuilder setKeep(boolean keep) {
        this.keep = keep;
        return this;
    }

    public EndpointExecutionBuilder setWait(boolean wait) {
        this.wait = wait;
        return this;
    }

    public EndpointExecutionBuilder setMethod(EndpointHttpMethod method) {
        this.method = method;
        return this;
    }

    public EndpointExecution createEndpointExecution() {
        return new EndpointExecution(parameters, resp, pathInfo, keep, wait, method, persistenceContextId, persistenceContext, budgetUnit, bugetMax, delayUnit, delayValue, request, endpoint);
    }

    public EndpointExecutionBuilder setResp(HttpServletResponse resp) {
        this.resp = resp;
        return this;
    }

    public EndpointExecutionBuilder setPersistenceContextId(String persist) {
        this.persistenceContextId = persist;
        return this;
    }

    public EndpointExecutionBuilder setPersistenceContext(String persistenceContext) {
        this.persistenceContext = persistenceContext;
        return this;
    }

    public EndpointExecutionBuilder setBudgetUnit(String budgetUnit) {
        this.budgetUnit = budgetUnit;
        return this;
    }

    public EndpointExecutionBuilder setBugetMax(Double bugetMax) {
        this.bugetMax = bugetMax;
        return this;
    }

    public EndpointExecutionBuilder setDelayUnit(TimeUnit delayUnit) {
        this.delayUnit = delayUnit;
        return this;
    }

    public EndpointExecutionBuilder setDelayValue(Long delayValue) {
        this.delayValue = delayValue;
        return this;
    }

    public EndpointExecutionBuilder setRequest(EndpointRequest request) {
        this.request = request;
        return this;
    }
}