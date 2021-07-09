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

import org.meveo.api.rest.technicalservice.impl.EndpointRequest;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.service.technicalservice.endpoint.EndpointCacheContainer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

public class EndpointExecutionFactory {
	
    @Inject
    private EndpointCacheContainer endpointCacheContainer;

    public EndpointExecutionBuilder getExecutionBuilder(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo.length() == 0) {
            throw new ServletException("Incomplete URL");
        }
        
        // Retrieve endpoint
        final Endpoint endpoint = endpointCacheContainer.getEndpointForPath(req.getPathInfo(),req.getMethod());

        return new EndpointExecutionBuilder()
                .setRequest(new EndpointRequest(req, endpoint))
                .setResponse(resp)
                .setEndpoint(endpoint)
                .setPathInfo(pathInfo)
                .setKeep(Headers.KEEP_DATA.getValue(req, Boolean.class, false))
                .setWait(Headers.WAIT_FOR_FINISH.getValue(req, Boolean.class, false))
                .setBudgetUnit(Headers.BUDGET_UNIT.getValue(req, String.class))
                .setBugetMax(Headers.BUDGET_MAX_VALUE.getValue(req, Double.class))
                .setDelayUnit(Headers.DELAY_UNIT.getValue(req, TimeUnit.class, TimeUnit.SECONDS))
                .setDelayValue(Headers.DELAY_MAX_VALUE.getValue(req, Long.class))
                .setPersistenceContextId(Headers.PERSISTENCE_CONTEXT_ID.getValue(req));
    }
}
