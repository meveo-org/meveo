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


import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.technicalservice.InputPropertyDto;
import org.meveo.api.dto.technicalservice.endpoint.EndpointDto;
import org.meveo.api.dto.technicalservice.endpoint.TSParameterMappingDto;
import org.meveo.api.technicalservice.DescriptionApi;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.technicalservice.InputMeveoProperty;
import org.meveo.model.technicalservice.TechnicalService;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointParameter;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.FunctionService;
import org.meveo.service.script.ScriptInterface;
import org.meveo.service.technicalservice.endpoint.EndpointService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API for managing technical services endpoints
 *
 * @author clement.bareth
 * @since 01.02.2019
 */
@Stateless
public class EndpointApi {

    @Inject
    private DescriptionApi descriptionApi;

    @Inject
    private EndpointService endpointService;

    @Inject
    private ConcreteFunctionService concreteFunctionService;

    /**
     * Create an endpoint
     *
     * @param endpointDto Configuration of the endpoint
     * @return the created Endpoint
     */
    @JpaAmpNewTx @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Endpoint create(EndpointDto endpointDto) throws BusinessException {
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
            return null;
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

        // Update synchronous
        endpoint.setSynchronous(endpointDto.isSynchronous());

        // Update method
        endpoint.setMethod(endpointDto.getMethod());

        // Update path parameters
        endpoint.setPathParameters(getEndpointPathParameters(endpointDto,endpoint));

        // Update parameters mappings
        endpoint.setParametersMapping(getParameterMappings(endpointDto, endpoint));

        endpointService.update(endpoint);
    }

    private EndpointDto toDto(Endpoint endpoint) {
        EndpointDto endpointDto = new EndpointDto();
        endpointDto.setCode(endpoint.getCode());
        endpointDto.setMethod(endpoint.getMethod());
        endpointDto.setTechnicalServiceCode(endpoint.getService().getCode());
        endpointDto.setSynchronous(endpoint.isSynchronous());
        List<InputPropertyDto> pathParameterDtos = new ArrayList<>();
        endpointDto.setPathParameters(pathParameterDtos);
        //TODO: Check order
        for (EndpointPathParameter pathParameter : endpoint.getPathParameters()) {
            InputPropertyDto inputPropertyDto = descriptionApi.toInputPropertyDto(pathParameter.getEndpointParameter().getParameter());
            pathParameterDtos.add(inputPropertyDto);
        }
        List<TSParameterMappingDto> mappingDtos = new ArrayList<>();
        endpointDto.setParameterMappings(mappingDtos);
        for (TSParameterMapping tsParameterMapping : endpoint.getParametersMapping()) {
            TSParameterMappingDto mappingDto = new TSParameterMappingDto();
            mappingDto.setDefaultValue(tsParameterMapping.getDefaultValue());
            mappingDto.setParameterName(mappingDto.getParameterName());
            InputPropertyDto inputPropertyDto = descriptionApi.toInputPropertyDto(tsParameterMapping.getEndpointParameter().getParameter());
            mappingDto.setServiceParameter(inputPropertyDto);
        }
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

        // Technical Service
        final FunctionService<?, ScriptInterface> functionService = concreteFunctionService.getFunctionService(endpointDto.getTechnicalServiceCode());
        TechnicalService service = (TechnicalService) functionService.findByCode(endpointDto.getTechnicalServiceCode());
        endpoint.setService(service);

        // Parameters mappings
        List<TSParameterMapping> tsParameterMappings = getParameterMappings(endpointDto, endpoint);
        endpoint.setParametersMapping(tsParameterMappings);

        // Path parameters
        List<EndpointPathParameter> endpointPathParameters = getEndpointPathParameters(endpointDto, endpoint);
        endpoint.setPathParameters(endpointPathParameters);
        return endpoint;
    }

    private List<EndpointPathParameter> getEndpointPathParameters(EndpointDto endpointDto, Endpoint endpoint) {
        List<EndpointPathParameter> endpointPathParameters = new ArrayList<>();
        for (InputPropertyDto pathParameter : endpointDto.getPathParameters()) {
            EndpointPathParameter endpointPathParameter = new EndpointPathParameter();
            EndpointParameter endpointParameter = buildEndpointParameter(endpoint, endpointDto.getTechnicalServiceCode(), pathParameter);
            endpointPathParameter.setEndpointParameter(endpointParameter);
        }
        return endpointPathParameters;
    }

    private List<TSParameterMapping> getParameterMappings(EndpointDto endpointDto, Endpoint endpoint) {
        List<TSParameterMapping> tsParameterMappings = new ArrayList<>();
        for (TSParameterMappingDto parameterMappingDto : endpointDto.getParameterMappings()) {
            TSParameterMapping tsParameterMapping = new TSParameterMapping();
            tsParameterMapping.setDefaultValue(parameterMappingDto.getDefaultValue());
            tsParameterMapping.setParameterName(parameterMappingDto.getParameterName());
            EndpointParameter endpointParameter = buildEndpointParameter(endpoint, endpointDto.getTechnicalServiceCode(), parameterMappingDto.getServiceParameter());
            tsParameterMapping.setEndpointParameter(endpointParameter);
        }
        return tsParameterMappings;
    }

    private EndpointParameter buildEndpointParameter(Endpoint endpoint, String code, InputPropertyDto inputPropertyDto){
        EndpointParameter endpointParameter = new EndpointParameter();
        endpointParameter.setEndpoint(endpoint);
        InputMeveoProperty inputMeveoProperty = descriptionApi.fromInputPropertyDto(code, inputPropertyDto);
        endpointParameter.setParameter(inputMeveoProperty);
        return endpointParameter;
    }

}
