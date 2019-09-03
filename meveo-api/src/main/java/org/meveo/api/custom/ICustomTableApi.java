package org.meveo.api.custom;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.api.dto.custom.CustomTableDataDto;
import org.meveo.api.dto.custom.CustomTableDataResponseDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;

public interface ICustomTableApi<T extends CustomTableDataDto> {

	/**
	 * Create new records in a custom table with an option of deleting existing data first
	 * 
	 * @param dto Values to add
	 * @throws MeveoApiException API exception
	 * @throws BusinessException General exception
	 */
	void create(T dto) throws MeveoApiException, BusinessException;

	/**
	 * Update existing records in a custom table. Values must contain an 'id' field value, to identify an existing record.
	 * 
	 * @param dto Values to update
	 * @throws MeveoApiException API exception
	 * @throws BusinessException General exception
	 */
	void update(T dto) throws MeveoApiException, BusinessException;

	/**
	 * Create new records or update existing ones in a custom table, depending if 'id' value is present
	 * 
	 * @param dto Values to add or update
	 * @throws MeveoApiException API exception
	 * @throws BusinessException General exception
	 */
	void createOrUpdate(T dto) throws MeveoApiException, BusinessException;

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
	CustomTableDataResponseDto list(String customTableCode, PagingAndFiltering pagingAndFiltering) throws MissingParameterException, EntityDoesNotExistsException, InvalidParameterException, ValidationException;

	/**
	 * Remove records, identified by 'id' value, from a custom table. If no 'id' values are passed, will delete all the records in a table.
	 * 
	 * @param dto Values to remove. Should contain only 'id' field values
	 * @throws MeveoApiException API exception
	 * @throws BusinessException General exception
	 */
	void remove(T dto) throws MeveoApiException, BusinessException;

}