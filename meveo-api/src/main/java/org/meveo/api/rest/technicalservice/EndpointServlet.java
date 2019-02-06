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
import org.meveo.elresolver.ELException;
import org.meveo.interfaces.EntityOrRelation;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.service.neo4j.scheduler.AtomicPersistencePlan;
import org.meveo.service.neo4j.scheduler.CyclicDependencyException;
import org.meveo.service.neo4j.scheduler.ScheduledPersistenceService;
import org.meveo.service.neo4j.scheduler.SchedulingService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Servlet that allows to execute technical services through configured endpoints.<br>
 * The first part of Uri after "/rest/" corresponds either to the code of the endpoint, or an id of a previous asynchronous execution.<br>
 * The last part or the Uri corresponds to the path parameters of the endpoint.<br>
 * If the endpoint is configured as GET, it should be called via GET resquests and parameters should be in query.<br>
 * If the endpoint is configured as POST, it should be called via POST requests and parameters should be in body as a JSON map.<br>
 * Header "Keep-data" indicates we don't want to remove the execution result from cache.<br>
 * Header "Wait-For-Finish" indicates that we want to wait until one exuction finishes and get results after. (Otherwise returns status 102).<br>
 * Header "Persistence-Context-Id" indiciates the id of the persistence context we want to save the result

 * @author clement.bareth
 */
@WebServlet("/rest/*")
public class EndpointServlet extends HttpServlet {

    private static final Cache<String, Future<Map<String, Object>>> pendingExecutions = CacheBuilder.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    @Inject
    private Logger log;

    @Inject
    private EndpointApi endpointApi;

    @Inject
    private EndpointService endpointService;

    @Inject
    private SchedulingService schedulingService;

    @Inject
    private ScheduledPersistenceService scheduledPersistenceService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final PrintWriter writer = resp.getWriter();
        resp.setCharacterEncoding("UTF-8");

        String[] pathInfo = req.getPathInfo().split("/");
        if (pathInfo.length == 0) {
            throw new ServletException("Incomplete URL");
        }

        String requestBody = StringUtils.readBuffer(req.getReader());
        final Map<String, Object> parameters = JacksonUtil.fromString(requestBody, new TypeReference<Map<String, Object>>() {});

        final EndpointExecution endpointExecution = new EndpointExecutionBuilder()
                .setParameters(parameters)
                .setResponse(resp)
                .setWriter(writer)
                .setPathInfo(pathInfo)
                .setFirstUriPart(pathInfo[1])
                .setKeep(Headers.KEEP_DATA.getValue(req, Boolean.class, false))
                .setWait(Headers.WAIT_FOR_FINISH.getValue(req, Boolean.class, false))
                .setMethod(EndpointHttpMethod.POST)
                .setPersistenceContextId(Headers.PERSISTENCE_CONTEXT_ID.getValue(req))
                .createEndpointExecution();

        doRequest(endpointExecution);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter writer = resp.getWriter();
        resp.setCharacterEncoding("UTF-8");

        String[] pathInfo = req.getPathInfo().split("/");
        if (pathInfo.length == 0) {
            throw new ServletException("Incomplete URL");
        }

        final EndpointExecution endpointExecution = new EndpointExecutionBuilder()
                .setParameters(new HashMap<>(req.getParameterMap()))
                .setResponse(resp)
                .setWriter(writer)
                .setPathInfo(pathInfo)
                .setFirstUriPart(pathInfo[1])
                .setKeep(Headers.KEEP_DATA.getValue(req, Boolean.class, false))
                .setWait(Headers.WAIT_FOR_FINISH.getValue(req, Boolean.class, false))
                .setMethod(EndpointHttpMethod.GET)
                .setPersistenceContextId(Headers.PERSISTENCE_CONTEXT_ID.getValue(req))
                .createEndpointExecution();

        doRequest(endpointExecution);
    }

    private void doRequest(EndpointExecution endpointExecution) {
        try {
            final Future<Map<String, Object>> execResult = pendingExecutions.getIfPresent(endpointExecution.getFirstUriPart());
            if (execResult != null && endpointExecution.getMethod() == EndpointHttpMethod.GET) {
                if (execResult.isDone() || endpointExecution.isWait()) {
                    endpointExecution.getResp().setContentType(MediaType.APPLICATION_JSON);
                    endpointExecution.getResp().setStatus(200);
                    endpointExecution.getWriter().print(JacksonUtil.toString(execResult.get()));
                    if (!endpointExecution.isKeep()) {
                        log.info("Removing execution results with id {}", endpointExecution.getFirstUriPart());
                        pendingExecutions.invalidate(endpointExecution.getFirstUriPart());
                    }
                } else {
                    endpointExecution.getResp().setStatus(102);    // In progress
                }
            } else {
                launchEndpoint(endpointExecution);
            }
        } catch (Exception e) {
            log.error("Error while executing request", e);
            endpointExecution.getResp().setStatus(500);
            endpointExecution.getWriter().print(e.toString());
        } finally {
            endpointExecution.getWriter().flush();
            endpointExecution.getWriter().close();
        }
    }

    private void launchEndpoint(EndpointExecution endpointExecution) throws BusinessException, ExecutionException, InterruptedException {
        // Retrieve endpoint
        final Endpoint endpoint = endpointService.findByCode(endpointExecution.getFirstUriPart());
        if (endpoint != null) {
            if (endpoint.getMethod() == endpointExecution.getMethod()) {
                List<String> pathParameters = new ArrayList<>(Arrays.asList(endpointExecution.getPathInfo()).subList(2, endpointExecution.getPathInfo().length));
                // Execute service
                if (endpoint.isSynchronous()) {
                    final Map<String, Object> result = endpointApi.execute(endpoint, pathParameters, endpointExecution.getParameters());
                    endpointExecution.getWriter().print(JacksonUtil.toString(result));
                    endpointExecution.getResp().setContentType(MediaType.APPLICATION_JSON);
                    endpointExecution.getResp().setStatus(200);    // OK
                    if(endpointExecution.getPersistenceContextId() != null){
                        saveResult(endpointExecution, result);
                    }
                } else {
                    final UUID id = UUID.randomUUID();
                    log.info("Added pending execution number {} for endpoint {}", id, endpointExecution.getFirstUriPart());
                    final CompletableFuture<Map<String, Object>> execution = CompletableFuture.supplyAsync(() -> {
                        try {
                            return endpointApi.execute(endpoint, pathParameters, endpointExecution.getParameters());
                        } catch (BusinessException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    pendingExecutions.put(id.toString(), execution);

                    if(endpointExecution.getPersistenceContextId() != null){
                        execution.thenAccept(map -> saveResult(endpointExecution, map));
                    }

                    /*
                        If header wait was true, wait for execution of the service.
                        If header keep was true, return the id of the execution and the result,
                        if it was false, return only the results and remove the execution from the cache
                    */
                    if(endpointExecution.isWait()){
                        endpointExecution.getResp().setStatus(200);    // Accepted
                        final Map<String, Object> execResult = execution.get();
                        if(endpointExecution.isKeep()){
                            Map<String, Object> returnedValue = new HashMap<>();
                            returnedValue.put("id", id.toString());
                            returnedValue.put("data", execResult);
                            endpointExecution.getWriter().println(JacksonUtil.toString(returnedValue));
                        }else{
                            endpointExecution.getWriter().println(JacksonUtil.toString(execResult));
                            pendingExecutions.invalidate(id.toString());
                        }

                    }else{
                        endpointExecution.getWriter().println(id.toString().trim());
                        endpointExecution.getResp().setStatus(202);    // Accepted
                    }
                }
            } else {
                endpointExecution.getResp().setStatus(400);
                endpointExecution.getWriter().print("Endpoint is not available for " + endpointExecution.getMethod() + " requests");
            }
        } else {
            endpointExecution.getResp().setStatus(404);    // Not found
            try {
                UUID uuid = UUID.fromString(endpointExecution.getFirstUriPart());
                endpointExecution.getWriter().print("No results for execution id " + uuid.toString());
            } catch (IllegalArgumentException e) {
                endpointExecution.getWriter().print("No endpoint for " + endpointExecution.getFirstUriPart() + " has been found");
            }
        }
    }

    private void saveResult(EndpointExecution endpointExecution, Map<String, Object> results){
        final List<EntityOrRelation> resultsToSave = JacksonUtil.OBJECT_MAPPER .convertValue(results.get("results"), new TypeReference<List<EntityOrRelation>>() {});
        try {
            AtomicPersistencePlan atomicPersistencePlan = schedulingService.schedule(resultsToSave);
            scheduledPersistenceService.persist(endpointExecution.getPersistenceContextId(), atomicPersistencePlan);
        } catch (CyclicDependencyException | BusinessException | ELException e) {
            throw new RuntimeException(e);
        }
    }

}
