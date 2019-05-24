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

//TODO: Add opencell license
package org.meveo.api.custom;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.custom.CustomTableDataDto;
import org.meveo.api.dto.custom.CustomTableDataResponseDto;
import org.meveo.api.dto.custom.CustomTableRecordDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.service.base.NativePersistenceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomTableService;
import org.primefaces.model.SortOrder;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.*;

/**
 * @author Andrius Karpavicius
 * @author Clément Bareth
 * @lastModifiedVersion 6.0.15
 **/
@Stateless
@Dependent
@Default
public class CustomTableApi extends BaseApi implements ICustomTableApi<CustomTableDataDto> {

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;
    
    @Inject
    private CustomTableService customTableService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;
    
    /**
     * Create new records in a custom table with an option of deleting existing data first
     * 
     * @param dto Values to add
     * @throws MeveoApiException API exception
     * @throws BusinessException General exception
     */
    @Override
    public void create(CustomTableDataDto dto) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(dto.getCustomTableCode())) {
            missingParameters.add("customTableCode");
        }
        if (dto.getValues() == null || dto.getValues().isEmpty()) {
            missingParameters.add("values");
        }

        handleMissingParameters();

        if (dto.getOverwrite() == null) {
            dto.setOverwrite(false);
        }

        CustomEntityTemplate cet = customEntityTemplateService.findByCode(dto.getCustomTableCode());
        if (cet == null) {
            throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getCustomTableCode());
        }

        List<Map<String, Object>> values = new ArrayList<>();

        for (CustomTableRecordDto record : dto.getValues()) {
            values.add(record.getValues());
        }
        
        String dbTablename = SQLStorageConfiguration.getDbTablename(cet);

        customTableService.importData(dbTablename, cet, values, !dto.getOverwrite());

    }

    /**
     * Update existing records in a custom table. Values must contain an 'id' field value, to identify an existing record.
     * 
     * @param dto Values to update
     * @throws MeveoApiException API exception
     * @throws BusinessException General exception
     */
    @Override
    public void update(CustomTableDataDto dto) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(dto.getCustomTableCode())) {
            missingParameters.add("customTableCode");
        }
        if (dto.getValues() == null || dto.getValues().isEmpty()) {
            missingParameters.add("values");
        }

        handleMissingParameters();

        CustomEntityTemplate cet = customEntityTemplateService.findByCode(dto.getCustomTableCode());
        if (cet == null) {
            throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getCustomTableCode());
        }
        Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
        if (cfts == null || cfts.isEmpty()) {
            throw new ValidationException("No fields are defined for custom table", "customTable.noFields");
        }

        int importedLines = 0;
        List<Map<String, Object>> values = new ArrayList<>();

        for (CustomTableRecordDto record : dto.getValues()) {

            // Update every 500 records
            if (importedLines >= 500) {

                values = customTableService.convertValues(values, cfts, false);
                customTableService.update(cet, values);

                values.clear();
                importedLines = 0;
            }

            values.add(record.getValues());
            importedLines++;
        }

        // Update remaining records
        values = customTableService.convertValues(values, cfts, false);
        customTableService.update(cet, values);
    }

    /**
     * Create new records or update existing ones in a custom table, depending if 'id' value is present
     * 
     * @param dto Values to add or update
     * @throws MeveoApiException API exception
     * @throws BusinessException General exception
     */
    @Override
	public void createOrUpdate(CustomTableDataDto dto) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(dto.getCustomTableCode())) {
            missingParameters.add("customTableCode");
        }
        if (dto.getValues() == null || dto.getValues().isEmpty()) {
            missingParameters.add("values");
        }

        handleMissingParameters();

        CustomEntityTemplate cet = customEntityTemplateService.findByCodeOrDbTablename(dto.getCustomTableCode());
        if (cet == null) {
            throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getCustomTableCode());
        }
        Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
        if (cfts == null || cfts.isEmpty()) {
            throw new ValidationException("No fields are defined for custom table", "customTable.noFields");
        }

        for (CustomTableRecordDto record : dto.getValues()) {

            if (record.getValues().containsKey(NativePersistenceService.FIELD_ID)) {
                customTableService.update(cet, customTableService.convertValue(record.getValues(), cfts, false, null));
            } else {
                customTableService.create(cet, customTableService.convertValue(record.getValues(), cfts, false, null));
            }
        }
    }

    /**
     * Retrieve custom table data based on a search criteria
     * 
     * @param customTableCode Custom table/custom entity template code
     * @param pagingAndFiltering Search and pagination criteria
     * @return Values and pagination information
     * @throws MissingParameterException Missing parameters
     * @throws EntityDoesNotExistsException Custom table was not matched
     * @throws InvalidParameterException Invalid parameters passed
     */
    @Override
	public CustomTableDataResponseDto list(String customTableCode, PagingAndFiltering pagingAndFiltering)
            throws MissingParameterException, EntityDoesNotExistsException, InvalidParameterException, ValidationException {

        if (StringUtils.isBlank(customTableCode)) {
            missingParameters.add("customTableCode");
        }
        handleMissingParameters();

        if (pagingAndFiltering == null) {
            pagingAndFiltering = new PagingAndFiltering();
        }

        CustomEntityTemplate cet = customEntityTemplateService.findByCodeOrDbTablename(customTableCode);
        if (cet == null) {
            throw new EntityDoesNotExistsException(CustomEntityTemplate.class, customTableCode);
        }

        Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
        if (cfts == null || cfts.isEmpty()) {
            throw new ValidationException("No fields are defined for custom table", "customTable.noFields");
        }

        pagingAndFiltering.setFilters(customTableService.convertValue(pagingAndFiltering.getFilters(), cfts, true, null));

        PaginationConfiguration paginationConfig = toPaginationConfiguration("id", SortOrder.ASCENDING, null, pagingAndFiltering, null);

        long totalCount = customTableService.count(SQLStorageConfiguration.getDbTablename(cet), paginationConfig);

        CustomTableDataResponseDto result = new CustomTableDataResponseDto();

        result.setPaging(pagingAndFiltering);
        result.getPaging().setTotalNumberOfRecords((int) totalCount);
        result.getCustomTableData().setCustomTableCode(customTableCode);

        result.getCustomTableData().setValuesFromListofMap(customTableService.list(SQLStorageConfiguration.getDbTablename(cet), paginationConfig));

        return result;
    }

    /**
     * Remove records, identified by 'id' value, from a custom table. If no 'id' values are passed, will delete all the records in a table.
     * 
     * @param dto Values to remove. Should contain only 'id' field values
     * @throws MeveoApiException API exception
     * @throws BusinessException General exception
     */
    @Override
	public void remove(CustomTableDataDto dto) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(dto.getCustomTableCode())) {
            missingParameters.add("customTableCode");
        }

        handleMissingParameters();

        CustomEntityTemplate cet = customEntityTemplateService.findByCodeOrDbTablename(dto.getCustomTableCode());
        if (cet == null) {
            throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getCustomTableCode());
        }

        if (dto.getValues() == null || dto.getValues().isEmpty()) {
            customTableService.remove(SQLStorageConfiguration.getDbTablename(cet));
        } else {
            Set<String> ids = new HashSet<>();

            for (CustomTableRecordDto record : dto.getValues()) {

                Object id = record.getValues().get(NativePersistenceService.FIELD_ID);
                if (id != null) {
                    ids.add((String) id);
                } else {
                    throw new InvalidParameterException("Not all values have an 'id' field specified");
                }
            }
            customTableService.remove(SQLStorageConfiguration.getDbTablename(cet), ids);
        }
    }

    /**
     * Enable or disable records, identified by 'id' value, in a custom table. Applies only to tables that contain field 'disabled'.
     * 
     * @param dto Values to enable or disable. Should contain only 'id' field values
     * @param enable True to enable records, False to disable records.
     * @throws MeveoApiException API exception
     * @throws BusinessException General exception
     */
	public void enableDisable(CustomTableDataDto dto, boolean enable) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(dto.getCustomTableCode())) {
            missingParameters.add("customTableCode");
        }
        if (dto.getValues() == null || dto.getValues().isEmpty()) {
            missingParameters.add("values");
        }

        handleMissingParameters();

        CustomEntityTemplate cet = customEntityTemplateService.findByCodeOrDbTablename(dto.getCustomTableCode());
        if (cet == null) {
            throw new EntityDoesNotExistsException(CustomEntityTemplate.class, dto.getCustomTableCode());
        }

        Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
        if (cfts == null || cfts.isEmpty() || cfts.containsKey(NativePersistenceService.FIELD_DISABLED)) {
            throw new ValidationException("Custom table does not contain a field 'disabled'", "customTable.noDisabledField");
        }

        Set<String> ids = new HashSet<>();

        for (CustomTableRecordDto record : dto.getValues()) {

            Object id = record.getValues().get(NativePersistenceService.FIELD_ID);
            if (id != null) {
                ids.add((String) id);

            } else {
                throw new InvalidParameterException("Not all values have an 'id' field specified");
            }
        }
        if (enable) {
            customTableService.enable(SQLStorageConfiguration.getDbTablename(cet), ids);
        } else {
            customTableService.disable(SQLStorageConfiguration.getDbTablename(cet), ids);
        }
    }
    
}