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

import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

public class EndpointExecutionBuilder {
    private Map<String, Object> parameters;
    private HttpServletResponse resp;
    private PrintWriter writer;
    private String[] pathInfo;
    private String firstUriPart;
    private boolean keep;
    private boolean wait;
    private EndpointHttpMethod method;
    private String persistenceContextId;
    private String persistenceContext;

    public EndpointExecutionBuilder setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    public EndpointExecutionBuilder setResponse(HttpServletResponse resp) {
        this.resp = resp;
        return this;
    }

    public EndpointExecutionBuilder setWriter(PrintWriter writer) {
        this.writer = writer;
        return this;
    }

    public EndpointExecutionBuilder setPathInfo(String[] pathInfo) {
        this.pathInfo = pathInfo;
        return this;
    }

    public EndpointExecutionBuilder setFirstUriPart(String firstUriPart) {
        this.firstUriPart = firstUriPart;
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
        return new EndpointExecution(parameters, resp, writer, pathInfo, firstUriPart, keep, wait, method, persistenceContextId, persistenceContext);
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
}