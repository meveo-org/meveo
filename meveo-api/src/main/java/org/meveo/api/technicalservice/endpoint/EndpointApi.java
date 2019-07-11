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
package org.meveo.api.technicalservice.endpoint;


import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.technicalservice.endpoint.EndpointDto;
import org.meveo.api.dto.technicalservice.endpoint.TSParameterMappingDto;
import org.meveo.api.rest.technicalservice.EndpointExecution;
import org.meveo.keycloak.client.KeycloakAdminClientConfig;
import org.meveo.keycloak.client.KeycloakAdminClientService;
import org.meveo.keycloak.client.KeycloakUtils;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.endpoint.*;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.FunctionService;
import org.meveo.service.script.ScriptInterface;
import org.meveo.service.technicalservice.endpoint.EndpointService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * API for managing technical services endpoints
 *
 * @author clement.bareth
 * @since 01.02.2019
 */
@Stateless
public class EndpointApi {

    @EJB
    private EndpointService endpointService;

    @Inject
    private ConcreteFunctionService concreteFunctionService;

    @EJB
    private KeycloakAdminClientService keycloakAdminClientService;

    public EndpointApi(){

    }

    public EndpointApi(EndpointService endpointService, ConcreteFunctionService concreteFunctionService) {
        this.endpointService = endpointService;
        this.concreteFunctionService = concreteFunctionService;
    }

    /**
     * Execute the technical service associated to the endpoint
     *
     * @param endpoint Endpoint to execute
     * @param execution Parameters of the execution
     * @return The result of the execution
     * @throws BusinessException if error occurs while execution
     */
    public Map<String, Object> execute(Endpoint endpoint, EndpointExecution execution) throws BusinessException, ExecutionException, InterruptedException {

    	System.out.println(execution.getRequest().getRemainingPath());
    	
        List<String> pathParameters = new ArrayList<>(Arrays.asList(execution.getPathInfo()).subList(2, execution.getPathInfo().length));

        Function service = endpoint.getService();
        Map<String, Object> parameterMap = new HashMap<>(execution.getParameters());

        // Set budget variables
        parameterMap.put(EndpointVariables.MAX_BUDGET, execution.getBugetMax());
        parameterMap.put(EndpointVariables.BUDGET_UNIT, execution.getBudgetUnit());

        // Assign path parameters
        for (EndpointPathParameter pathParameter : endpoint.getPathParameters()) {
            parameterMap.put(pathParameter.toString(), pathParameters.get(pathParameter.getPosition()));
        }

        // Assign query or post parameters
        for (TSParameterMapping tsParameterMapping : endpoint.getParametersMapping()) {
            Object parameterValue = execution.getParameters().get(tsParameterMapping.getParameterName());

            // Use default value if parameter not provided
            if (parameterValue == null) {
                parameterValue = tsParameterMapping.getDefaultValue();
            } else {
                // Handle cases where parameter is multivalued
                if (parameterValue instanceof String[]) {
                    String[] arrValue = (String[]) parameterValue;
                    if (tsParameterMapping.isMultivalued()) {
                        parameterValue = new ArrayList<>(Arrays.asList(arrValue));
                    } else if (arrValue.length == 1) {
                        parameterValue = arrValue[0];
                    } else {
                        throw new IllegalArgumentException("Parameter " + tsParameterMapping.getParameterName() + "should not be multivalued");
                    }
                } else if (parameterValue instanceof Collection) {
                    Collection colValue = (Collection) parameterValue;
                    if (!tsParameterMapping.isMultivalued() && colValue.size() == 1) {
                        parameterValue = colValue.iterator().next();
                    } else if (!tsParameterMapping.isMultivalued()) {
                        throw new IllegalArgumentException("Parameter " + tsParameterMapping.getParameterName() + "should not be multivalued");
                    }
                }
            }
            String paramName = tsParameterMapping.getEndpointParameter().toString();
            parameterMap.remove(tsParameterMapping.getParameterName());
            parameterMap.put(paramName, parameterValue);
        }

        final FunctionService<?, ScriptInterface> functionService = concreteFunctionService.getFunctionService(service.getCode());

        if(execution.getDelayValue() != null){
            final ScriptInterface executionEngine = functionService.getExecutionEngine(service.getCode(), parameterMap);
            try {
                final CompletableFuture<Map<String, Object>> resultFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return functionService.execute(executionEngine, parameterMap);
                    } catch (BusinessException e) {
                        throw new RuntimeException(e);
                    }
                });
                return resultFuture.get(execution.getDelayValue(), execution.getDelayUnit());
            } catch (TimeoutException e) {
                return executionEngine.cancel();
            }
        }

        // Implicitly pass the request information to the script
        parameterMap.put("request", execution.getRequest());

        return functionService.execute(service.getCode(), parameterMap);
    }

    /**
     * Create an endpoint
     *
     * @param endpointDto Configuration of the endpoint
     * @return the created Endpoint
     */
    public Endpoint create(EndpointDto endpointDto) throws BusinessException {
        validateCompositeRoles(endpointDto);
        Endpoint endpoint = fromDto(endpointDto);
        endpointService.create(endpoint);
        return endpoint;
    }

    /**
     * Create or update an endpoint
     *
     * @param endpointDto Configuration of the endpoint
     * @return null if endpoint was updated, the endpoint if it was created
     */
    public Endpoint createOrReplace(EndpointDto endpointDto) throws BusinessException {
        Endpoint endpoint = endpointService.findByCode(endpointDto.getCode());
        if(endpoint != null){
            update(endpoint, endpointDto);
            return endpoint;
        }else {
            return create(endpointDto);
        }
    }

    /**
     * Remove the endpoint with the given code
     *
     * @param code Code of the endpoint to remove
     * @throws BusinessException if endpoint does not exists
     */
    public void delete(String code) throws BusinessException {

        // Retrieve existing entity
        Endpoint endpoint = endpointService.findByCode(code);

        endpointService.remove(endpoint);

    }

    /**
     * Retrieve an endpoint by its code
     *
     * @param code Code of the endpoint to retrieve
     * @return DTO of the endpoint corresponding to the given code
     */
    public EndpointDto findByCode(String code){
        return toDto(endpointService.findByCode(code));
    }

    /**
     * Retrieve endpoints by service code
     *
     * @param code Code of the service used to filter endpoints
     * @return DTOs of the endpoints that are associated to the given technical service
     */
    public List<EndpointDto> findByServiceCode(String code){
        return endpointService.findByServiceCode(code)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all endpoints
     *
     * @return DTOs for all stored endpoints
     */
    public List<EndpointDto> list(){
        return endpointService.list()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private void update(Endpoint endpoint, EndpointDto endpointDto) throws BusinessException {
        validateCompositeRoles(endpointDto);
    	Endpoint updatedEndpoint = new Endpoint();
    	updatedEndpoint.setId(endpoint.getId());
    	updatedEndpoint.setCode(endpointDto.getCode());
    	
    	updatedEndpoint.setSynchronous(endpointDto.isSynchronous());
    	updatedEndpoint.setMethod(endpointDto.getMethod());

        final List<EndpointPathParameter> endpointPathParameters = getEndpointPathParameters(endpointDto, endpoint);
        updatedEndpoint.setPathParameters(endpointPathParameters);

        final List<TSParameterMapping> parameterMappings = getParameterMappings(endpointDto, endpoint);
        updatedEndpoint.setParametersMapping(parameterMappings);
        
        updatedEndpoint.setReturnedVariableName(endpointDto.getReturnedVariableName());
        updatedEndpoint.setJsonataTransformer(endpointDto.getJsonataTransformer());
        updatedEndpoint.setSerializeResult(endpointDto.isSerializeResult());
        updatedEndpoint.setContentType(endpointDto.getContentType());

        endpointService.update(updatedEndpoint);
    }

    private EndpointDto toDto(Endpoint endpoint) {
        EndpointDto endpointDto = new EndpointDto();
        endpointDto.setCode(endpoint.getCode());
        endpointDto.setMethod(endpoint.getMethod());
        endpointDto.setServiceCode(endpoint.getService().getCode());
        endpointDto.setSynchronous(endpoint.isSynchronous());
        endpointDto.setReturnedVariableName(endpoint.getReturnedVariableName());
        endpointDto.setSerializeResult(endpoint.isSerializeResult());
        List<String> pathParameterDtos = new ArrayList<>();
        endpointDto.setPathParameters(pathParameterDtos);
        for (EndpointPathParameter pathParameter : endpoint.getPathParameters()) {
            pathParameterDtos.add(pathParameter.getEndpointParameter().getParameter());
        }
        List<TSParameterMappingDto> mappingDtos = new ArrayList<>();
        endpointDto.setParameterMappings(mappingDtos);
        for (TSParameterMapping tsParameterMapping : endpoint.getParametersMapping()) {
            TSParameterMappingDto mappingDto = new TSParameterMappingDto();
            mappingDto.setDefaultValue(tsParameterMapping.getDefaultValue());
            mappingDto.setParameterName(tsParameterMapping.getParameterName());
            mappingDto.setServiceParameter(tsParameterMapping.getEndpointParameter().getParameter());
            mappingDtos.add(mappingDto);
        }
        endpointDto.setJsonataTransformer(endpoint.getJsonataTransformer());
        endpointDto.setContentType(endpoint.getContentType());
        return endpointDto;
    }

    private Endpoint fromDto(EndpointDto endpointDto){

        Endpoint endpoint = new Endpoint();
        
        // Code
        endpoint.setCode(endpointDto.getCode());

        // Method
        endpoint.setMethod(endpointDto.getMethod());

        // Synchronous
        endpoint.setSynchronous(endpointDto.isSynchronous());

        // JSONata query
        endpoint.setJsonataTransformer(endpointDto.getJsonataTransformer());
        
        // Returned variable name
        endpoint.setReturnedVariableName(endpointDto.getReturnedVariableName());

        // Technical Service
        final FunctionService<?, ScriptInterface> functionService = concreteFunctionService.getFunctionService(endpointDto.getServiceCode());
        Function service = functionService.findByCode(endpointDto.getServiceCode());
        endpoint.setService(service);

        // Parameters mappings
        List<TSParameterMapping> tsParameterMappings = getParameterMappings(endpointDto, endpoint);
        endpoint.setParametersMapping(tsParameterMappings);

        // Path parameters
        List<EndpointPathParameter> endpointPathParameters = getEndpointPathParameters(endpointDto, endpoint);
        endpoint.setPathParameters(endpointPathParameters);

        endpoint.setSerializeResult(endpointDto.isSerializeResult());
        
        endpoint.setContentType(endpointDto.getContentType());
        
        return endpoint;
    }

    private List<EndpointPathParameter> getEndpointPathParameters(EndpointDto endpointDto, Endpoint endpoint) {
        List<EndpointPathParameter> endpointPathParameters = new ArrayList<>();
        for (String pathParameter : endpointDto.getPathParameters()) {
            EndpointPathParameter endpointPathParameter = new EndpointPathParameter();
            EndpointParameter endpointParameter = buildEndpointParameter(endpoint, pathParameter);
            endpointPathParameter.setEndpointParameter(endpointParameter);
            endpointPathParameters.add(endpointPathParameter);
        }
        return endpointPathParameters;
    }

    private List<TSParameterMapping> getParameterMappings(EndpointDto endpointDto, Endpoint endpoint) {
        List<TSParameterMapping> tsParameterMappings = new ArrayList<>();
        for (TSParameterMappingDto parameterMappingDto : endpointDto.getParameterMappings()) {
            TSParameterMapping tsParameterMapping = new TSParameterMapping();
            tsParameterMapping.setDefaultValue(parameterMappingDto.getDefaultValue());
            tsParameterMapping.setParameterName(parameterMappingDto.getParameterName());
            EndpointParameter endpointParameter = buildEndpointParameter(endpoint, parameterMappingDto.getServiceParameter());
            tsParameterMapping.setEndpointParameter(endpointParameter);
            tsParameterMappings.add(tsParameterMapping);
        }
        return tsParameterMappings;
    }

    private EndpointParameter buildEndpointParameter(Endpoint endpoint, String param){
        EndpointParameter endpointParameter = new EndpointParameter();
        endpointParameter.setEndpoint(endpoint);
        endpointParameter.setParameter(param);
        return endpointParameter;
    }

    public List<String> validateCompositeRoles(EndpointDto endpointDto) throws IllegalArgumentException {
        KeycloakAdminClientConfig keycloakAdminClientConfig = KeycloakUtils.loadConfig();
        List<String> roles = keycloakAdminClientService.getCompositeRolesByRealmClientId(keycloakAdminClientConfig.getClientId(), keycloakAdminClientConfig.getRealm());
        if (CollectionUtils.isNotEmpty(roles)) {
            for (String compositeRole : roles) {
                for (String selectedRole : endpointDto.getRoles()) {
                    if (!compositeRole.equals(selectedRole)) {
                        throw new IllegalArgumentException("The roles does not exists");
                    }
                }
            }
        }
        return roles;
    }
}
