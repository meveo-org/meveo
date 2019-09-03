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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.custom.CustomTableDataDto;
import org.meveo.api.dto.custom.CustomTableDataRelationDto;
import org.meveo.api.dto.custom.CustomTableDataResponseDto;
import org.meveo.api.dto.custom.CustomTableRelationRecordDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.BaseEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.custom.CustomTableRelationService;
import org.meveo.service.custom.CustomTableService;
import org.primefaces.model.SortOrder;

/**
 * @author Clément Bareth
 * @lastModifiedVersion 6.0.15
 **/
@Stateless
public class CustomTableRelationApi extends BaseApi implements ICustomTableApi<CustomTableDataRelationDto> {

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;
    
    @Inject @Any
    private ICustomTableApi<CustomTableDataDto> customTableApi;
    
    @Inject
    private CustomTableRelationService customTableRelationService;
    
    @Inject
    private CustomFieldTemplateService customFieldTemplateService;
    
    @Inject
    private CustomTableService customTableService;
	
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

	/**
	 * Insert or update records in a custom table for a given relationship
	 */
	@Override
	public void createOrUpdate(CustomTableDataRelationDto dto) throws MeveoApiException, BusinessException {
		CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(dto.getCustomTableCode());
		
        for(CustomTableRelationRecordDto record : dto.getRecords()) {
        	if(customTableRelationService.exists(crt, record.getStartUuid(), record.getEndUuid(), record.getValues())) {
        		customTableRelationService.updateRelation(crt, record.getStartUuid(), record.getEndUuid(), record.getValues());
        	}else {
        		customTableRelationService.createRelation(crt, record.getStartUuid(), record.getEndUuid(), record.getValues());
        	}
        }
	}

	@Override
	public CustomTableDataResponseDto list(String customTableCode, PagingAndFiltering pagingAndFiltering) throws MissingParameterException, EntityDoesNotExistsException, InvalidParameterException, ValidationException {
		CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(customTableCode);
		
        String startUuid = (String) pagingAndFiltering.getFilters().remove("startUuid");
        String endUuid = (String) pagingAndFiltering.getFilters().remove("endUuid");
        
        Set<String> filtersKeys = new HashSet<>(pagingAndFiltering.getFilters().keySet());
        for(String filterKey : filtersKeys) {
        	Object value = pagingAndFiltering.getFilters().remove(filterKey);
        	String newKey = BaseEntity.cleanUpAndLowercaseCodeOrId(filterKey);
        	pagingAndFiltering.getFilters().put(newKey, value);
        }
		
        Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(crt.getAppliesTo());

        pagingAndFiltering.setFilters(customTableService.convertValue(pagingAndFiltering.getFilters(), cfts, true, null));
        

        // Replace the "startUuid" reference with source field
        if(startUuid != null) {
			String startColumn = SQLStorageConfiguration.getDbTablename(crt.getStartNode());
        	pagingAndFiltering.getFilters().put(startColumn, startUuid);
        	
        }
        
        // Replace the "endUuid" reference with target field
        if(endUuid != null) {
			String endColumn = SQLStorageConfiguration.getDbTablename(crt.getEndNode());
        	pagingAndFiltering.getFilters().put(endColumn, endUuid);
        	
        }
        
        pagingAndFiltering.getFilters().put("$FILTER", null);

        // No sort by field
        PaginationConfiguration paginationConfig = toPaginationConfiguration(null, SortOrder.ASCENDING, null, pagingAndFiltering, null);
        
        String dbTablename = SQLStorageConfiguration.getDbTablename(crt);
		long totalCount = customTableService.count(dbTablename, paginationConfig);

        CustomTableDataResponseDto result = new CustomTableDataResponseDto();

        result.setPaging(pagingAndFiltering);
        result.getPaging().setTotalNumberOfRecords((int) totalCount);
        result.getCustomTableData().setCustomTableCode(customTableCode);

        result.getCustomTableData().setValuesFromListofMap(customTableService.list(dbTablename, paginationConfig));

        return result;
	}

	/**
	 * Remove specified records from a table associated to a {@link CustomRelationshipTemplate}
	 */
	@Override
	public void remove(CustomTableDataRelationDto dto) throws MeveoApiException, BusinessException {
		CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(dto.getCustomTableCode());
		
        for(CustomTableRelationRecordDto record : dto.getRecords()) {
    		customTableRelationService.removeRelation(crt, record.getStartUuid(), record.getEndUuid(), record.getValues());
        }
	}
}