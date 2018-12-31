package org.meveo.api;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.CustomEntityTemplateUniqueConstraintDto;
import org.meveo.api.dto.ScriptInstanceErrorDto;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.crm.CustomEntityTemplateUniqueConstraint;
import org.meveo.service.custom.CustomEntityTemplateUniqueConstraintService;

/**
 * @author Edward P. Legaspi
 **/

@Stateless
public class CustomEntityTemplateUniqueConstraintApi extends BaseApi {

    @Inject
    private CustomEntityTemplateUniqueConstraintService uniqueConstraintService;

    public void create(CustomEntityTemplateUniqueConstraintDto uniqueConstraintDto, String appliesTo)
            throws MissingParameterException, EntityAlreadyExistsException, MeveoApiException, BusinessException {

        checkDtoAndSetAppliesTo(uniqueConstraintDto, appliesTo, false);

        if (uniqueConstraintService.findByCodeAndAppliesTo(uniqueConstraintDto.getCode(), uniqueConstraintDto.getAppliesTo()) != null) {
            throw new EntityAlreadyExistsException(CustomEntityTemplateUniqueConstraint.class, uniqueConstraintDto.getCode() + "/" + uniqueConstraintDto.getAppliesTo());
        }

        CustomEntityTemplateUniqueConstraint action = new CustomEntityTemplateUniqueConstraint();
        uniqueConstraintFromDTO(uniqueConstraintDto, action);

        uniqueConstraintService.create(action);
    }

    public void update(CustomEntityTemplateUniqueConstraintDto uniqueConstraintDto, String appliesTo)
            throws MissingParameterException, EntityDoesNotExistsException, MeveoApiException, BusinessException {

        checkDtoAndSetAppliesTo(uniqueConstraintDto, appliesTo, true);

        CustomEntityTemplateUniqueConstraint uniqueConstraint = uniqueConstraintService.findByCodeAndAppliesTo(uniqueConstraintDto.getCode(), uniqueConstraintDto.getAppliesTo());
        if (uniqueConstraint == null) {
            throw new EntityDoesNotExistsException(CustomEntityTemplateUniqueConstraint.class, uniqueConstraintDto.getCode() + "/" + uniqueConstraintDto.getAppliesTo());
        }

        uniqueConstraintFromDTO(uniqueConstraintDto, uniqueConstraint);

        uniqueConstraintService.update(uniqueConstraint);
    }

    /**
     * Find entity custom action by its code and appliesTo attributes
     * 
     * @param actionCode Entity custom action code
     * @param appliesTo Applies to
     * @return DTO
     * @throws EntityDoesNotExistsException Entity custom action was not found
     * @throws MissingParameterException A parameter, necessary to find an entity custom action, was not provided
     */
    public CustomEntityTemplateUniqueConstraintDto find(String actionCode, String appliesTo) throws EntityDoesNotExistsException, MissingParameterException {

        if (StringUtils.isBlank(actionCode)) {
            missingParameters.add("actionCode");
        }
        if (StringUtils.isBlank(appliesTo)) {
            missingParameters.add("appliesTo");
        }
        handleMissingParameters();

        CustomEntityTemplateUniqueConstraint action = uniqueConstraintService.findByCodeAndAppliesTo(actionCode, appliesTo);
        if (action == null) {
            throw new EntityDoesNotExistsException(CustomEntityTemplateUniqueConstraint.class, actionCode + "/" + appliesTo);
        }
        CustomEntityTemplateUniqueConstraintDto actionDto = new CustomEntityTemplateUniqueConstraintDto(action);

        return actionDto;
    }

    public void remove(String actionCode, String appliesTo) throws EntityDoesNotExistsException, MissingParameterException, BusinessException {

        if (StringUtils.isBlank(actionCode)) {
            missingParameters.add("actionCode");
        }
        if (StringUtils.isBlank(appliesTo)) {
            missingParameters.add("appliesTo");
        }

        handleMissingParameters();

        CustomEntityTemplateUniqueConstraint scriptInstance = uniqueConstraintService.findByCodeAndAppliesTo(actionCode, appliesTo);
        if (scriptInstance == null) {
            throw new EntityDoesNotExistsException(CustomEntityTemplateUniqueConstraint.class, actionCode);
        }
        uniqueConstraintService.remove(scriptInstance);
    }

    public void createOrUpdate(CustomEntityTemplateUniqueConstraintDto postData, String appliesTo)
            throws MissingParameterException, EntityAlreadyExistsException, EntityDoesNotExistsException, MeveoApiException, BusinessException {

        List<ScriptInstanceErrorDto> result = new ArrayList<ScriptInstanceErrorDto>();
        checkDtoAndSetAppliesTo(postData, appliesTo, true);

        CustomEntityTemplateUniqueConstraint uniqueConstraint = uniqueConstraintService.findByCodeAndAppliesTo(postData.getCode(), postData.getAppliesTo());
        if (uniqueConstraint == null) {
            create(postData, appliesTo);
        } else {
            update(postData, appliesTo);
        }
    }

    private void checkDtoAndSetAppliesTo(CustomEntityTemplateUniqueConstraintDto dto, String appliesTo, boolean isUpdate) throws MissingParameterException, BusinessApiException {

        if (StringUtils.isBlank(dto.getCode())) {
            missingParameters.add("code");
        }

        if (appliesTo != null) {
            dto.setAppliesTo(appliesTo);
        }

        if (StringUtils.isBlank(dto.getAppliesTo())) {
            missingParameters.add("appliesTo");
        }
        if (StringUtils.isBlank(dto.getCypherQuery())) {
            missingParameters.add("cypherQuery");
        }

        handleMissingParameters();
    }

    private void uniqueConstraintFromDTO(CustomEntityTemplateUniqueConstraintDto uniqueConstraintDto, CustomEntityTemplateUniqueConstraint uniqueConstraint) {
        if (uniqueConstraint.isTransient()) {
            uniqueConstraint.setCode(uniqueConstraintDto.getCode());
            uniqueConstraint.setAppliesTo(uniqueConstraintDto.getAppliesTo());
        }
        if (uniqueConstraintDto.getDescription() != null) {
            uniqueConstraint.setDescription(uniqueConstraintDto.getDescription());
        }
        if (uniqueConstraintDto.getApplicableOnEl() != null) {
            uniqueConstraint.setApplicableOnEl(uniqueConstraintDto.getApplicableOnEl());
        }
        if (uniqueConstraintDto.getCypherQuery() != null) {
            uniqueConstraint.setCypherQuery(uniqueConstraintDto.getCypherQuery());
        }
        if (uniqueConstraintDto.getTrustScore() != null) {
            uniqueConstraint.setTrustScore(uniqueConstraintDto.getTrustScore());
        } else {
            uniqueConstraint.setTrustScore(100);
        }
        uniqueConstraint.setDisabled(uniqueConstraintDto.isDisabled());
    }

}
