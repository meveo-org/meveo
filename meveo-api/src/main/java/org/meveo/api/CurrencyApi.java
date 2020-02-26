package org.meveo.api;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.CurrencyDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.admin.Currency;
import org.meveo.service.admin.impl.CurrencyService;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Edward P. Legaspi
 *
 **/
@Stateless
public class CurrencyApi extends BaseApi {

    @Inject
    private CurrencyService currencyService;

    public void create(CurrencyDto postData) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }

        handleMissingParameters();

        

        Currency currency = currencyService.findByCode(postData.getCode());

        if (currency == null) {
            // create
            currency = new Currency();
            currency.setCurrencyCode(postData.getCode());
            currency.setDescriptionEn(postData.getDescription());
            currencyService.create(currency);
        }

    }

    public CurrencyDto find(String code) throws MissingParameterException, EntityDoesNotExistsException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
        }
        handleMissingParameters();

        CurrencyDto result = new CurrencyDto();

        Currency currency = currencyService.findByCode(code);
        if (currency == null) {
            throw new EntityDoesNotExistsException(Currency.class, code);
        }

        result = new CurrencyDto(currency);

        return result;
    }

    public void remove(String code) throws BusinessException, MissingParameterException, EntityDoesNotExistsException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
        }
        handleMissingParameters();

        Currency currency = currencyService.findByCode(code);
        if (currency == null) {
            throw new EntityDoesNotExistsException(Currency.class, code);
        }

        currencyService.remove(currency);
    }

    public void update(CurrencyDto postData) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        handleMissingParameters();


        Currency currency = currencyService.findByCode(postData.getCode());

        if (currency != null) {

            currency.setDescriptionEn(postData.getDescription());
            currency = currencyService.update(currency);
            

        } else {
            throw new EntityDoesNotExistsException(Currency.class, postData.getCode());
        }
    }

    public void createOrUpdate(CurrencyDto postData) throws MeveoApiException, BusinessException {
        Currency currency = currencyService.findByCode(postData.getCode());
        if (currency == null) {
            create(postData);
        } else {
            update(postData);
        }
    }

    public void findOrCreate(String currencyCode) throws EntityDoesNotExistsException {
        if (StringUtils.isBlank(currencyCode)){
            return;
        }
			Currency currency = currencyService.findByCode(currencyCode);
			if (currency==null) {
				throw new EntityDoesNotExistsException(Currency.class, currencyCode);
			}
    }
}