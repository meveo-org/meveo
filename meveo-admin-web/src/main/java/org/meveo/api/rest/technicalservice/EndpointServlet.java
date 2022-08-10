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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.listener.StartupListener;
import org.meveo.api.rest.technicalservice.impl.EndpointResponse;
import org.meveo.api.technicalservice.endpoint.EndpointApi;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;
import org.meveo.service.technicalservice.endpoint.EndpointCacheContainer;
import org.meveo.service.technicalservice.endpoint.EndpointResult;
import org.meveo.service.technicalservice.endpoint.PendingResult;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Servlet that allows to execute technical services through configured endpoints.<br>
 * The first part of Uri after "/rest/" corresponds either to the first part of the path of the endpoint, or an id of a previous asynchronous execution.<br>
 * The last part or the Uri corresponds to the path parameters of the endpoint.<br>
 * If the endpoint is configured as GET, it should be called via GET resquests and parameters should be in query.<br>
 * If the endpoint is configured as POST/PUT, it should be called via POST/PUT requests and parameters should be in body as a JSON map.<br>
 * Header "Keep-data" indicates we don't want to remove the execution result from cache.<br>
 * Header "Wait-For-Finish" indicates that we want to wait until one exuction finishes and get results after. (Otherwise returns status 102).<br>
 * Header "Persistence-Context-Id" indiciates the id of the persistence context we want to save the result
 * @author clement.bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@WebServlet("/rest/*")
@MultipartConfig
public class EndpointServlet extends HttpServlet {

    private static final long serialVersionUID = -8425320629325242067L;

    @Inject
    public Logger log;
    
    @EJB
    private EndpointApi endpointApi;
    
    @Inject
    private StartupListener startupListener;

    @Inject
    private EndpointCacheContainer endpointCacheContainer;

    @Inject
    private EndpointExecutionFactory endpointExecutionFactory;

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> parameters = new HashMap<>();

        final EndpointExecution endpointExecution = endpointExecutionFactory.getExecutionBuilder(req, resp)
                .setParameters(parameters)
                .setMethod(EndpointHttpMethod.DELETE)
                .createEndpointExecution();

        doRequest(endpointExecution, true);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPutPost(req, resp,EndpointHttpMethod.POST);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPutPost(req, resp,EndpointHttpMethod.PUT);
    }

    protected void doPutPost(HttpServletRequest req, HttpServletResponse resp,EndpointHttpMethod method) throws ServletException, IOException {
        Map<String, Object> parameters = new HashMap<>();
        String contentType = req.getHeader("Content-Type");

        if (contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA)) {
        	Collection<Part> parts = req.getParts();
        	for(var part : parts) {
        		Object partValue;
		        if (part.getContentType() != null && part.getContentType().startsWith(MediaType.APPLICATION_JSON)) {
		        	partValue = JacksonUtil.read(part.getInputStream(), new TypeReference<Map<String, Object>>() {});
		        } else if (part.getContentType() != null && part.getContentType().startsWith(MediaType.APPLICATION_XML)) {
		            XmlMapper xmlMapper = new XmlMapper();
		            partValue = xmlMapper.readValue(part.getInputStream(), new TypeReference<Map<String, Object>>() {});
		        } else if(part.getSubmittedFileName() != null) {
		        	partValue = part.getInputStream();
		        } else {
		        	partValue = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
		        }
		        parameters.put(part.getName(), partValue);
        	}

        } else {
            String requestBody = StringUtils.readBuffer(req.getReader());
            parameters.put("REQUEST_BODY",requestBody);
        	if (!StringUtils.isBlank(requestBody) && contentType != null) {
		        if (contentType.startsWith(MediaType.APPLICATION_JSON)) {
		            parameters = JacksonUtil.fromString(requestBody, new TypeReference<Map<String, Object>>() {});
		        } else if (contentType.startsWith(MediaType.APPLICATION_XML) || contentType.startsWith(MediaType.TEXT_XML)) {
		            XmlMapper xmlMapper = new XmlMapper();
		            parameters = xmlMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});
		        }
        	}
        }

        final EndpointExecution endpointExecution = endpointExecutionFactory.getExecutionBuilder(req, resp)
                .setParameters(parameters)
                .setMethod(EndpointHttpMethod.POST)
                .createEndpointExecution();

        doRequest(endpointExecution, false);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final EndpointExecution endpointExecution = endpointExecutionFactory.getExecutionBuilder(req, resp)
                .setParameters(new HashMap<>(req.getParameterMap()))
                .setMethod(EndpointHttpMethod.GET)
                .createEndpointExecution();

        doRequest(endpointExecution, false);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final EndpointExecution endpointExecution = endpointExecutionFactory.getExecutionBuilder(req, resp)
                .setParameters(new HashMap<>(req.getParameterMap()))
                .setMethod(EndpointHttpMethod.HEAD)
                .createEndpointExecution();

        doRequest(endpointExecution, false);
    }


    private void doRequest(EndpointExecution endpointExecution, boolean cancel) throws IOException {
    	if (!startupListener.isStarted()) {
    		endpointExecution.getResp().setStatus(503);
    		return;
    	}
    	
        // Retrieve endpoint
        final Endpoint endpoint = endpointExecution.getEndpoint();

        if(endpoint==null){
                endpointExecution.getResp().setStatus(404);
                endpointExecution.getResp().getWriter().print("Endpoint not found");
                return;
        }

        // Check if a required parameter is missing at endpoint execution
        if (CollectionUtils.isNotEmpty(endpoint.getParametersMappingNullSafe())) {
            List<TSParameterMapping> requiredParameters = new ArrayList<>();
            for (TSParameterMapping tsParameterMapping : endpoint.getParametersMappingNullSafe()) {
                if (tsParameterMapping.isValueRequired() && tsParameterMapping.getDefaultValue() == null) {
                    requiredParameters.add(tsParameterMapping);
                }
            }
            
            if (CollectionUtils.isNotEmpty(requiredParameters)) {
                for (TSParameterMapping param : requiredParameters) {
                    String parameterName = param.getEndpointParameter().getParameter();
                    // if there's an exposed parameter name :
                    if (param.getParameterName() != null)
                    	parameterName = param.getParameterName();
                	if(!endpointExecution.getParameters().containsKey(parameterName)) {
	                    endpointExecution.getResp().setStatus(400);
						endpointExecution.getResp().getWriter().println("Parameter '" + parameterName + "' is missing");
		                return;
                	}
                }
            }
        }

        // If endpoint security is enabled, check if user has right to access that particular endpoint
        boolean endpointSecurityEnabled = Boolean.parseBoolean(ParamBean.getInstance().getProperty("endpointSecurityEnabled", "true"));
        if (endpointSecurityEnabled && endpoint != null && !endpointApi.isUserAuthorized(endpoint)) {
            endpointExecution.getResp().setStatus(403);
            endpointExecution.getResp().getWriter().print("You are not authorized to access this endpoint");
            return;
        }

        try {
            String uuidStr = endpointExecution.getPathInfo().split("/")[0];
            PendingResult pendingExecution = endpointCacheContainer.getPendingExecution(uuidStr);
            final Future<EndpointResult> execResult = pendingExecution != null ? pendingExecution.getResult() : null;
            if (execResult != null && (endpointExecution.getMethod() == EndpointHttpMethod.GET || endpointExecution.getMethod() == EndpointHttpMethod.DELETE)) {
                if (cancel && pendingExecution != null && pendingExecution.getEngine() != null) {
                    pendingExecution.getEngine().cancel();
                }

                // Wait for max delay if defined
                long start = System.currentTimeMillis();
                if (endpointExecution.getDelayMax() != null) {
                    while (System.currentTimeMillis() - start < endpointExecution.getDelayUnit().toMillis(endpointExecution.getDelayMax())) {
                        if (execResult.isDone()) {
                            break;
                        }
                        Thread.sleep(10L);
                    }
                }

                if (execResult.isDone() || endpointExecution.isWait()) {
                    EndpointResult endpointResult = execResult.get();
                    setReponse(endpointResult.getResult(), endpointExecution);
                    if (!endpointExecution.isKeep()) {
                        log.info("Removing execution results with id {}", uuidStr);
                        endpointCacheContainer.remove(uuidStr);
                    }
                } else {
                    endpointExecution.getResp().getWriter().print("In progress");
                    endpointExecution.getResp().setStatus(202);
                }
            } else {
                launchEndpoint(endpointExecution, endpoint);
            }
        } catch (Exception e) {
            log.error("Error while executing request", e);
            endpointExecution.getResp().setStatus(500);
            endpointExecution.getResp().getWriter().print(e.toString());
        } finally {
            if(endpointExecution.getResponse().getOutput() == null) {
                endpointExecution.getResp().getWriter().flush();
                endpointExecution.getResp().getWriter().close();
            }
        }
    }

    private void launchEndpoint(EndpointExecution endpointExecution, Endpoint endpoint) throws BusinessException, ExecutionException, InterruptedException, IOException {
        // Endpoint does not exists
        if (endpoint == null) {
            endpointExecution.getResp().setStatus(404);    // Not found
            String uuidStr = endpointExecution.getPathInfo().split("/")[0];
            try {
                UUID uuid = UUID.fromString(uuidStr);
                endpointExecution.getResp().getWriter().print("No results for execution id " + uuid.toString());
            } catch (IllegalArgumentException e) {
                endpointExecution.getResp().getWriter().print("No endpoint for " + uuidStr + " has been found");
            }
            return;
        }

        // If endpoint is synchronous, execute the script straight and return the response
        if (endpoint.isSynchronous()) {
            final Map<String, Object> result = endpointApi.execute(endpoint, endpointExecution);
            String transformedResult = endpointApi.transformData(endpoint, result);
            setReponse(transformedResult, endpointExecution);
            return;
        }

        // Execute the endpoint asynchronously
        final UUID id = UUID.randomUUID();
        log.info("Added pending execution number {} for endpoint {}", id, endpoint.getCode());
        PendingResult execution = endpointApi.executeAsync(endpoint, endpointExecution);

        // Store the pending result
        endpointCacheContainer.put(id.toString(), execution);

        // Don't wait execution to finish
        if (!endpointExecution.isWait()) {
            // Return the id of the execution so the user can retrieve it later
            endpointExecution.getResp().getWriter().println(id.toString().trim());
            endpointExecution.getResp().setStatus(202);    // Accepted
            return;
        }

        final EndpointResult execResult = execution.getResult().get();    // Wait until execution is over
        endpointExecution.getResp().setStatus(200); // OK
        if (endpointExecution.isKeep()) {
            // If user wants to keep the result in cache, return the data along with its id
            Map<String, Object> returnedValue = new HashMap<>();
            returnedValue.put("id", id.toString());
            returnedValue.put("data", execResult.getResult());
            endpointExecution.getResp().getWriter().println(JacksonUtil.toString(returnedValue));
        } else {
            // If user doesn't want to keep the result in cache, only return the data
            endpointExecution.getResp().setContentType(execResult.getContentType());
            endpointExecution.getResp().getWriter().println(execResult.getResult());
            endpointCacheContainer.remove(id.toString());
        }

    }

    private void setReponse(String transformedResult, EndpointExecution endpointExecution) throws IOException {
        // HTTP Status

        EndpointResponse response = endpointExecution.getResponse();
        HttpServletResponse servletResponse = endpointExecution.getResp();
        Integer status = response.getStatus();
        if (status != null) {
            servletResponse.setStatus(status);
        } else {
            servletResponse.setStatus(200);    // OK
        }

        // Content type
        String contentType = response.getContentType();
        if (!StringUtils.isBlank(contentType)) {
            servletResponse.setContentType(contentType);
        } else {
        	servletResponse.setContentType(endpointExecution.getEndpoint().getContentType());
        }

        // Buffer size
        Integer bufferSize = response.getBufferSize();
        if (bufferSize != null) {
            servletResponse.setBufferSize(bufferSize);
        }

        // Headers
        Map<String, String> headers = response.getHeaders();
        if (headers != null) {
            headers.forEach(servletResponse::setHeader);
        }

        // Date Headers
        Map<String, Long> dateHeaders = response.getDateHeaders();
        if (dateHeaders != null) {
            dateHeaders.forEach(servletResponse::setDateHeader);
        }

        // Body of the response
        String errorMessage = response.getErrorMessage();
        byte[] output = response.getOutput();
        if (!StringUtils.isBlank(errorMessage)) {    // Priority to error message
            servletResponse.getWriter().print(errorMessage);
        } else if (output != null) {                // Output has been set
            ByteArrayInputStream in = new ByteArrayInputStream(output);
            ServletOutputStream out = servletResponse.getOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            out.close();
        } else {    // Use the endpoint script's result
            servletResponse.getWriter().print(transformedResult);
        }
    }

}
