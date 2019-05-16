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

package org.meveo.api.custom;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.api.dto.custom.CustomTableDataDto;
import org.meveo.api.dto.custom.CustomTableDataRelationDto;
import org.meveo.api.dto.custom.CustomTableDataResponseDto;
import org.meveo.api.dto.custom.CustomTableRecordDto;
import org.meveo.api.dto.custom.CustomTableRelationRecordDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.custom.CustomTableRelationService;

/**
 * @author Clément Bareth
 * @lastModifiedVersion 6.0.15
 **/
@Stateless
public class CustomTableRelationApi implements ICustomTableApi<CustomTableDataRelationDto> {

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;
    
    @Inject @Any
    private ICustomTableApi<CustomTableDataDto> customTableApi;
    
    @Inject
    private CustomTableRelationService customTableRelationService;
	
    /**
     * Create new records in a custom table for a given relationship
     * 
     * @param dto Values to add
     * @throws MeveoApiException API exception
     * @throws BusinessException General exception
     */
    @Override
    public void create(CustomTableDataRelationDto dto) throws MeveoApiException, BusinessException {
        if (dto.getOverwrite() == null) {
            dto.setOverwrite(false);
        }
        
		CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(dto.getCustomTableCode());

        for(CustomTableRelationRecordDto record : dto.getRecords()) {
        	customTableRelationService.createRelation(crt, record.getStartUuid(), record.getEndUuid(), record.getValues());
        }
    }
    
    /**
     * Update records in a custom table for a given relationship
     * 
     * @param dto Values to update
     * @throws MeveoApiException API exception
     * @throws BusinessException General exception
     */
	@Override
	public void update(CustomTableDataRelationDto dto) throws MeveoApiException, BusinessException {
		CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(dto.getCustomTableCode());
		
        for(CustomTableRelationRecordDto record : dto.getRecords()) {
        	customTableRelationService.updateRelation(crt, record.getStartUuid(), record.getEndUuid(), record.getValues());
        }
	}

	@Override
	public void createOrUpdate(CustomTableDataRelationDto dto) throws MeveoApiException, BusinessException {
		CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(dto.getCustomTableCode());
    	CustomTableDataDto newDto = turnIntoRecordDto(crt, dto);
    	customTableApi.createOrUpdate(newDto);
	}

	@Override
	public CustomTableDataResponseDto list(String customTableCode, PagingAndFiltering pagingAndFiltering) throws MissingParameterException, EntityDoesNotExistsException, InvalidParameterException, ValidationException {
		return customTableApi.list(customTableCode, pagingAndFiltering);
	}

	@Override
	public void remove(CustomTableDataRelationDto dto) throws MeveoApiException, BusinessException {
		CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(dto.getCustomTableCode());
    	CustomTableDataDto newDto = turnIntoRecordDto(crt, dto);
    	customTableApi.remove(newDto);
	}

	@Override
	public void enableDisable(CustomTableDataRelationDto dto, boolean enable) throws MeveoApiException, BusinessException {
		CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(dto.getCustomTableCode());
    	CustomTableDataDto newDto = turnIntoRecordDto(crt, dto);
    	customTableApi.enableDisable(newDto, enable);
	}
	
	private CustomTableDataDto turnIntoRecordDto(CustomRelationshipTemplate crt, CustomTableDataRelationDto dto) {
		
    	CustomEntityTemplate startCet = crt.getStartNode();
    	CustomEntityTemplate endCet = crt.getEndNode();
    	
    	// Include the start end end uuids into the records' values
    	List<CustomTableRecordDto> values = dto.getRecords()
    			.stream()
    			.map(r -> {
    				r.getValues().put(SQLStorageConfiguration.getDbTablename(startCet), r.getStartUuid());
    				r.getValues().put(SQLStorageConfiguration.getDbTablename(endCet), r.getEndUuid());
    				return r;
    			}).collect(Collectors.toList());
    	
    	dto.setValues(values);
    	
    	CustomTableDataDto newDto = (CustomTableDataDto) dto;
		return newDto;
	}
}