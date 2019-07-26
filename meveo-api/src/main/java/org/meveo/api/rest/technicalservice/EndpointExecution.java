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

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.meveo.api.rest.technicalservice.impl.EndpointRequest;
import org.meveo.api.rest.technicalservice.impl.EndpointResponse;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;

public class EndpointExecution {
    private final Map<String, Object> parameters;
    private final HttpServletResponse resp;
    private final PrintWriter writer;
    private final String[] pathInfo;
    private final String firstUriPart;
    private final boolean keep;
    private final boolean wait;
    private final EndpointHttpMethod method;
    private final String persistenceContextId;
    private final String persistenceContext;
    private final String budgetUnit;
    private final Double bugetMax;
    private final TimeUnit delayUnit;
    private final Long delayValue;
    private final EndpointRequest request;
    private final EndpointResponse response;
    private final Endpoint endpoint;

    public EndpointExecution(Map<String, Object> parameters, HttpServletResponse resp, PrintWriter writer, String[] pathInfo, String firstUriPart, boolean keep, boolean wait, EndpointHttpMethod method, String persistenceContextId, String persistenceContext, String budgetUnit, Double bugetMax, TimeUnit delayUnit, Long delayValue, EndpointRequest request, Endpoint endpoint) {
        this.parameters = parameters;
        this.resp = resp;
        this.writer = writer;
        this.pathInfo = pathInfo;
        this.firstUriPart = firstUriPart;
        this.keep = keep;
        this.wait = wait;
        this.method = method;
        this.persistenceContextId = persistenceContextId;
        this.persistenceContext = persistenceContext;
        this.budgetUnit = budgetUnit;
        this.bugetMax = bugetMax;
        this.delayUnit = delayUnit;
        this.delayValue = delayValue;
        this.request = request;
        this.endpoint = endpoint;
        this.response = new EndpointResponse(resp);
    }
    
    public EndpointResponse getResponse() {
		return response;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public EndpointRequest getRequest() {
        return request;
    }

    public TimeUnit getDelayUnit() {
        return delayUnit;
    }

    public Long getDelayValue() {
        return delayValue;
    }

    public String getBudgetUnit() {
        return budgetUnit;
    }

    public Double getBugetMax() {
        return bugetMax;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public String[] getPathInfo() {
        return pathInfo;
    }

    public String getFirstUriPart() {
        return firstUriPart;
    }

    public boolean isKeep() {
        return keep;
    }

    public boolean isWait() {
        return wait;
    }

    public EndpointHttpMethod getMethod() {
        return method;
    }

    public String getPersistenceContextId() {
        return persistenceContextId;
    }

    public String getPersistenceContext() {
        return persistenceContext;
    }
}
