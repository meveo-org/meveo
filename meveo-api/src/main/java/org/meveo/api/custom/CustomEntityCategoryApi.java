package org.meveo.api.custom;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.CustomEntityCategoryDto;
import org.meveo.api.dto.response.CustomEntityCategoriesResponseDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.customEntities.CustomEntityCategory;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.custom.CustomEntityCategoryService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.primefaces.model.SortOrder;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@Stateless
public class CustomEntityCategoryApi extends BaseCrudApi<CustomEntityCategory, CustomEntityCategoryDto> {

    public CustomEntityCategoryApi() {
        super(CustomEntityCategory.class, CustomEntityCategoryDto.class);
    }

    @Inject
    private CustomEntityCategoryService customEntityCategoryService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

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

    public void removeCustomEntityCategory(String code, boolean deleteRelatedTemplates) throws EntityDoesNotExistsException, MissingParameterException, BusinessException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("customEntityTemplateCode");
        }

        handleMissingParameters();

        CustomEntityCategory cec = customEntityCategoryService.findByCode(code);
        if (cec != null) {
            // Delete the related CETs if deleteRelatedTemplates true
            if (deleteRelatedTemplates) {
                customEntityTemplateService.removeCETsByCategoryId(cec.getId());
            } else {
                // Set the category to null for each related CETs if deleteRelatedTemplates is absent or set to false.
                customEntityTemplateService.resetCategoryCETsByCategoryId(cec.getId());
            }

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
	public CustomEntityCategoryDto find(String code) throws MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {

		CustomEntityCategory cec = customEntityCategoryService.findByCode(code);
		if (cec == null) {
			throw new EntityDoesNotExistsException(CustomEntityCategory.class, code);
		}

		return CustomEntityCategoryDto.toDTO(cec);
	}

    @Override
    public CustomEntityCategoryDto findIgnoreNotFound(String code) {
        CustomEntityCategory category = customEntityCategoryService.findByCode(code);
        
        if(category == null) {
        	return null;
        }
        
		return CustomEntityCategoryDto.toDTO(category);
    }

    @Override
    public CustomEntityCategoryDto toDto(CustomEntityCategory entity) {
        return CustomEntityCategoryDto.toDTO(entity);
    }

    @Override
    public CustomEntityCategory fromDto(CustomEntityCategoryDto dto) throws MeveoApiException {
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

	public CustomEntityCategoriesResponseDto list(PagingAndFiltering pagingAndFiltering) throws InvalidParameterException {

		if (pagingAndFiltering == null) {
			pagingAndFiltering = new PagingAndFiltering();
		}

		PaginationConfiguration paginationConfig = toPaginationConfiguration("id", SortOrder.ASCENDING, null, pagingAndFiltering, CustomEntityCategoryDto.class);

		long totalCount = customEntityCategoryService.count(paginationConfig);

		CustomEntityCategoriesResponseDto result = new CustomEntityCategoriesResponseDto();
		result.setPaging(pagingAndFiltering);
		result.getPaging().setTotalNumberOfRecords((int) totalCount);

		List<CustomEntityCategoryDto> customEntityCategoryDtos = new ArrayList<>();
		if (totalCount > 0) {
			List<CustomEntityCategory> customEntityCategoryCategories = customEntityCategoryService.list(paginationConfig);
			if (customEntityCategoryCategories != null) {
				for (CustomEntityCategory cec : customEntityCategoryCategories) {
					customEntityCategoryDtos.add(new CustomEntityCategoryDto(cec));
				}
			}
		}
		result.setCustomEntityCategories(customEntityCategoryDtos);

		return result;
	}

	@Override
	public void remove(CustomEntityCategoryDto dto) throws MeveoApiException, BusinessException {
		try {
			this.removeCustomEntityCategory(dto.getCode(), false);
		} catch (EntityDoesNotExistsException e) {
			// Do nothing
		}
	}
	
}
