package org.meveo.api;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.LanguageDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
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
 * @deprecated will be renammed to TradingLanguageApi
 **/
@Stateless
public class LanguageApi extends BaseApi {

    @Inject
    private LanguageService languageService;

    public void create(LanguageDto postData) throws MissingParameterException, EntityAlreadyExistsException, EntityDoesNotExistsException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
            handleMissingParameters();
        }

        

        Language language = languageService.findByCode(postData.getCode());


        if (language == null) {
            // create
            language = new Language();
            language.setLanguageCode(postData.getCode());
            language.setDescriptionEn(postData.getDescription());
            languageService.create(language);
        }

    }

    public void remove(String code) throws MissingParameterException, EntityDoesNotExistsException, BusinessException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
            handleMissingParameters();
        }

    }

    public void update(LanguageDto postData) throws MissingParameterException, EntityDoesNotExistsException, EntityAlreadyExistsException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
            handleMissingParameters();
        }

        

        Language language = languageService.findByCode(postData.getCode());

        if (language == null) {
            throw new EntityDoesNotExistsException(Language.class, postData.getCode());
        }

        language.setDescriptionEn(postData.getDescription());


    }

    public LanguageDto find(String code) throws MeveoApiException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
            handleMissingParameters();
        }
        return null;
    }

    /**
     * Create or update Language based on the trading language code.
     * 
     * @param postData posted data

     * @throws MeveoApiException meveo api exception
     * @throws BusinessException business exception.
     */
    public void createOrUpdate(LanguageDto postData) throws MeveoApiException, BusinessException {
        
    }
    public void findOrCreate(String languageCode) throws EntityDoesNotExistsException, BusinessException {
        if (StringUtils.isBlank(languageCode)){
            return;
        }
	}
}
