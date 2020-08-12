/**
 * 
 */
package org.meveo.api;

import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.scripts.FunctionCategory;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.FunctionCategoryService;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class FunctionCategoryApi extends BaseCrudApi<FunctionCategory, BusinessEntityDto> {
	
	@Inject
	private FunctionCategoryService fcService;

	/**
	 * Instantiates a new FunctionCategoryApi
	 *
	 */
	public FunctionCategoryApi() {
		super(FunctionCategory.class, BusinessEntityDto.class);
	}

	@Override
	public BusinessEntityDto find(String code) throws EntityDoesNotExistsException, MissingParameterException, InvalidParameterException, MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {
		FunctionCategory c = fcService.findByCode(code);
		return toDto(c);
	}

	@Override
	public FunctionCategory createOrUpdate(BusinessEntityDto dtoData) throws MeveoApiException, BusinessException {
		FunctionCategory c = fcService.findByCode(dtoData.getCode());
		if(c == null) {
			fcService.create(fromDto(dtoData));
		} else {
			c.setDescription(dtoData.getDescription());
		}
		return c;
	}

	@Override
	public BusinessEntityDto toDto(FunctionCategory entity) {
		return new BusinessEntityDto(entity);
	}

	@Override
	public FunctionCategory fromDto(BusinessEntityDto dto) throws MeveoApiException {
		FunctionCategory fc = new FunctionCategory();
		fc.setCode(dto.getCode());
		fc.setDescription(dto.getDescription());
		return fc;
	}

	@Override
	public IPersistenceService<FunctionCategory> getPersistenceService() {
		return fcService;
	}

	@Override
	public boolean exists(BusinessEntityDto dto) {
		return fcService.findByCodeLazy(dto.getCode()) != null;
	}

	@Override
	public void remove(BusinessEntityDto dto) throws MeveoApiException, BusinessException {
		var cat = fcService.findByCode(dto.getCode());
		if(cat != null) {
			fcService.remove(cat);
		}
	}

}
