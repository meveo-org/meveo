package org.meveo.api.rest;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.CustomEntityCategoryDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.customEntities.CustomEntityCategory;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.custom.CustomEntityCategoryService;

@Stateless
public class CustomEntityCategoryApi extends BaseCrudApi<CustomEntityCategory, CustomEntityCategoryDto>{

    public CustomEntityCategoryApi() {
		super(CustomEntityCategory.class, CustomEntityCategoryDto.class);
	}

	@Inject
    CustomEntityCategoryService customEntityCategoryService;

    public CustomEntityCategory create(CustomEntityCategoryDto dto) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(dto.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(dto.getName())) {
            missingParameters.add("name");
        }
        handleMissingParameters();
        if (customEntityCategoryService.findByCode(dto.getCode()) != null) {
            throw new EntityAlreadyExistsException(CustomEntityCategory.class, dto.getCode());
        }
        CustomEntityCategory cec = CustomEntityCategoryDto.fromDTO(dto, null);
        customEntityCategoryService.create(cec);
        return cec;
    }

    public CustomEntityCategory update(CustomEntityCategoryDto dto) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(dto.getName())) {
            missingParameters.add("code");
        }

        if (StringUtils.isBlank(dto.getName())) {
            missingParameters.add("name");
        }
        handleMissingParameters();

        CustomEntityCategory cec = customEntityCategoryService.findByCode(dto.getCode());
        if (cec == null) {
            throw new EntityDoesNotExistsException(CustomEntityCategory.class, dto.getCode());
        }

        cec = CustomEntityCategoryDto.fromDTO(dto, cec);
        customEntityCategoryService.update(cec);
        return cec;
    }

    public void removeCustomEntityCategory(String code) throws EntityDoesNotExistsException, MissingParameterException, BusinessException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("customEntityTemplateCode");
        }

        handleMissingParameters();

        CustomEntityCategory cec = customEntityCategoryService.findByCode(code);
        if (cec != null) {
            // Related custom entity category will be removed along with CEC
            customEntityCategoryService.remove(cec);
        } else {
            throw new EntityDoesNotExistsException(CustomEntityCategory.class, code);
        }
    }

    @Override
    public CustomEntityCategory createOrUpdate(CustomEntityCategoryDto dto) throws MeveoApiException, BusinessException {
        CustomEntityCategory cec = customEntityCategoryService.findByCode(dto.getCode());
        if (cec == null) {
            return create(dto);
        } else {
            return update(dto);
        }
    }

	@Override
	public CustomEntityCategoryDto find(String code) throws EntityDoesNotExistsException, MissingParameterException, InvalidParameterException, MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {
		return CustomEntityCategoryDto.toDTO(customEntityCategoryService.findByCode(code));
	}

	@Override
	public CustomEntityCategoryDto findIgnoreNotFound(String code) throws MissingParameterException, InvalidParameterException, MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {
		return CustomEntityCategoryDto.toDTO(customEntityCategoryService.findByCode(code));
	}

	@Override
	public CustomEntityCategoryDto toDto(CustomEntityCategory entity) {
		return CustomEntityCategoryDto.toDTO(entity);
	}

	@Override
	public CustomEntityCategory fromDto(CustomEntityCategoryDto dto) throws org.meveo.exceptions.EntityDoesNotExistsException {
		return CustomEntityCategoryDto.fromDTO(dto, new CustomEntityCategory());
	}

	@Override
	public IPersistenceService<CustomEntityCategory> getPersistenceService() {
		return customEntityCategoryService;
	}

	@Override
	public boolean exists(CustomEntityCategoryDto dto) {
		try {
			return find(dto.getCode()) != null;
		} catch (org.meveo.exceptions.EntityDoesNotExistsException | MeveoApiException e) {
			return false;
		}
	}
}
