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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.technicalservice.endpoint.EndpointApi;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.service.technicalservice.endpoint.EndpointService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Servlet that allows to execute technical services through configured endpoints.<br>
 * The first part of Uri after "/rest/" corresponds either to the code of the endpoint, or an id of a previous asynchronous execution.<br>
 * The last part or the Uri corresponds to the path parameters of the endpoint.<br>
 * If the endpoint is configured as GET, it should be called via GET resquests and parameters should be in query.<br>
 * If the endpoint is configured as POST, it should be called via POST requests and parameters should be in body as a JSON map.<br>
 * Parameter "keep" indicates we don't want to remove the execution result from cache.<br>
 * Parameter "wait" indicates that we want to wait until one exuction finishes and get results after. (Otherwise returns status 102).<br>
 *
 * @author clement.bareth
 */
@WebServlet("/rest/*")
public class EndpointServlet extends HttpServlet {

    private static final String KEEP = "keep";
    private static final String WAIT = "wait";
    private static final String FALSE = "false";

    private static final Cache<String, Future<Map<String, Object>>> pendingExecutions = CacheBuilder.newBuilder()
        .expireAfterWrite(7, TimeUnit.DAYS)
        .build();

    @Inject
    private Logger log;

    @Inject
    private EndpointApi endpointApi;

    @Inject
    private EndpointService endpointService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final PrintWriter writer = resp.getWriter();
        resp.setCharacterEncoding("UTF-8");

        String[] pathInfo = req.getPathInfo().split("/");
        if(pathInfo.length == 0){
            throw new ServletException("Incomplete URL");
        }

        final String firstUriPart = pathInfo[1];

        String requestBody = StringUtils.readBuffer(req.getReader());
        final Map<String, Object> parameters = JacksonUtil.fromString(requestBody, new TypeReference<Map<String, Object>>() {});
        final boolean keep = (Boolean) parameters.getOrDefault(KEEP, false);
        final boolean wait = (Boolean) parameters.getOrDefault(WAIT, false);

        doRequest(parameters, resp, writer, pathInfo, firstUriPart, keep, wait, EndpointHttpMethod.POST);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter writer = resp.getWriter();
        resp.setCharacterEncoding("UTF-8");

        String[] pathInfo = req.getPathInfo().split("/");
        if(pathInfo.length == 0){
            throw new ServletException("Incomplete URL");
        }

        final String firstUriPart = pathInfo[1];
        final String keepParam = Optional.ofNullable(req.getParameter(KEEP)).orElse(FALSE);
        final String waitParam = Optional.ofNullable(req.getParameter(WAIT)).orElse(FALSE);
        final boolean keep = Boolean.parseBoolean(keepParam);
        final boolean wait = Boolean.parseBoolean(waitParam);

        doRequest(new HashMap<>(req.getParameterMap()), resp, writer, pathInfo, firstUriPart, keep, wait, EndpointHttpMethod.GET);
    }

    private void doRequest(Map<String, Object> parameters, HttpServletResponse resp, PrintWriter writer, String[] pathInfo, String firstUriPart, boolean keep, boolean wait, EndpointHttpMethod method) {
        try {
            final Future<Map<String, Object>> execResult = pendingExecutions.getIfPresent(firstUriPart);
            if (execResult != null && method == EndpointHttpMethod.GET) {
                if (execResult.isDone() || wait) {
                    resp.setContentType(MediaType.APPLICATION_JSON);
                    resp.setStatus(200);
                    writer.print(JacksonUtil.toString(execResult.get()));
                    if(!keep){
                        log.info("Removing execution results with id {}", firstUriPart);
                        pendingExecutions.invalidate(firstUriPart);
                    }
                } else {
                    resp.setStatus(102);    // In progress
                }
            } else {
                launchEndpoint(parameters, resp, writer, pathInfo, firstUriPart, method);
            }
        } catch (Exception e) {
            resp.setStatus(500);
            writer.print(e.toString());
        } finally {
            writer.flush();
            writer.close();
        }
    }

    private void launchEndpoint(Map<String, Object> parameters, HttpServletResponse resp, PrintWriter writer, String[] pathInfo, String endpointCode, EndpointHttpMethod method) throws BusinessException {
        // Retrieve endpoint
        final Endpoint endpoint = endpointService.findByCode(endpointCode);
        if(endpoint != null){
            if (endpoint.getMethod() == method) {
                List<String> pathParameters = new ArrayList<>(Arrays.asList(pathInfo).subList(2, pathInfo.length));
                // Execute service
                if (endpoint.isSynchronous()) {
                    final Map<String, Object> result = endpointApi.execute(endpoint, pathParameters, parameters);
                    writer.print(JacksonUtil.toString(result));
                    resp.setContentType(MediaType.APPLICATION_JSON);
                    resp.setStatus(200);    // OK
                } else {
                    final UUID id = UUID.randomUUID();
                    log.info("Added pending execution number {} for endpoint {}", id, endpointCode);
                    final Future<Map<String, Object>> execution = endpointApi.executeAsync(endpoint, pathParameters, parameters);
                    pendingExecutions.put(id.toString(), execution);
                    writer.println(id.toString().trim());
                    resp.setStatus(202);    // Accepted
                }
            } else {
                resp.setStatus(400);
                writer.print("Endpoint is not available for " + method + " requests");
            }
        }else{
            resp.setStatus(404);    // Not found
            try {
                UUID uuid = UUID.fromString(endpointCode);
                writer.print("No results for execution id " + uuid.toString());
            } catch (IllegalArgumentException e) {
                writer.print("No endpoint for " + endpointCode + " has been found");
            }
        }
    }

}
