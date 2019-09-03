package org.meveo.api.rest;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.CustomEntityCategoryDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.customEntities.CustomEntityCategory;
import org.meveo.service.custom.CustomEntityCategoryService;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class CustomEntityCategoryApi extends BaseApi {

    @Inject
    CustomEntityCategoryService customEntityCategoryService;

    public void create(CustomEntityCategoryDto dto) throws MeveoApiException, BusinessException {

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
    }

    public void update(CustomEntityCategoryDto dto) throws MeveoApiException, BusinessException {

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

    public void createOrUpdateEntityCategory(CustomEntityCategoryDto dto) throws MeveoApiException, BusinessException {
        CustomEntityCategory cec = customEntityCategoryService.findByCode(dto.getCode());
        if (cec == null) {
            create(dto);
        } else {
            update(dto);
        }
    }
}
