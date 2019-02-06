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

    public EndpointExecution(Map<String, Object> parameters, HttpServletResponse resp, PrintWriter writer, String[] pathInfo, String firstUriPart, boolean keep, boolean wait, EndpointHttpMethod method, String persistenceContextId, String persistenceContext) {
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
