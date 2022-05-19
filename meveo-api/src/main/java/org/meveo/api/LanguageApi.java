package org.meveo.api;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.LanguageDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.billing.Language;
import org.meveo.service.admin.impl.LanguageService;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Edward P. Legaspi
 *
 **/
@Stateless
public class LanguageApi extends BaseApi {

    @Inject
    private LanguageService languageService;

    public void create(LanguageDto postData) throws MissingParameterException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
            handleMissingParameters();
        }



        Language language = languageService.findByCode(postData.getCode());


        if (language == null) {
            // create
            language = new Language();
            language.setCode(postData.getCode());
            language.setDescription(postData.getDescription());
            languageService.create(language);
        }

    }

    public void remove(String code) throws MissingParameterException, EntityDoesNotExistsException, BusinessException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
            handleMissingParameters();
        }

        Language language = languageService.findByCode(code);
        if (language == null) {
            throw new EntityDoesNotExistsException(Language.class, code);
        }

        languageService.remove(language);

    }

    public void update(LanguageDto postData) throws MissingParameterException, EntityDoesNotExistsException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
            handleMissingParameters();
        }



        Language language = languageService.findByCode(postData.getCode());

        if (language == null) {
            throw new EntityDoesNotExistsException(Language.class, postData.getCode());
        }

        language.setDescription(postData.getDescription());
        languageService.update(language);
    }

    public LanguageDto find(String code) throws MeveoApiException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
            handleMissingParameters();
        }
        LanguageDto result = new LanguageDto();

        Language language = languageService.findByCode(code);
        if (language == null) {
            throw new EntityDoesNotExistsException(Language.class, code);
        }

        result = new LanguageDto(language);

        return result;
    }

    /**
     * Create or update Language based on the trading language code.
     *
     * @param postData posted data

     * @throws MeveoApiException meveo api exception
     * @throws BusinessException business exception.
     */
    public void createOrUpdate(LanguageDto postData) throws MeveoApiException, BusinessException {
        Language language = languageService.findByCode(postData.getCode());
        if (language == null) {
            create(postData);
        } else {
            update(postData);
        }
    }

    public void findOrCreate(String languageCode) {
        if (StringUtils.isBlank(languageCode)){
            return;
        }
    }
}
