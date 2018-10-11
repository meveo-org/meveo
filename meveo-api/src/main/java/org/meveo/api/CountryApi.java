package org.meveo.api;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.CountryDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.admin.Currency;
import org.meveo.model.billing.Country;
import org.meveo.model.billing.Language;
import org.meveo.service.admin.impl.CountryService;
import org.meveo.service.admin.impl.CurrencyService;
import org.meveo.service.admin.impl.LanguageService;

import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Edward P. Legaspi
 * @since Oct 4, 2013
 * 
 * @deprecated will be renammed to TradingCountryApi
 **/
@Stateless
public class CountryApi extends BaseApi {

    @Inject
    private CountryService countryService;

    @Inject
    private CurrencyService currencyService;

    @Inject
    private LanguageService languageService;

    public void remove(String countryCode, String currencyCode) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(countryCode)) {
            missingParameters.add("countryCode");
        }

        handleMissingParameters();

    }

    public void create(CountryDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCountryCode())) {
            missingParameters.add("countryCode");
        }

        handleMissingParameters();
        


        Country country = countryService.findByCode(postData.getCountryCode());

        // If country code doesn't exist in the reference table, create the country in this table ("adm_country") with the currency code for the default provider.
        if (country == null) {
            country = new Country();
            country.setDescription(postData.getName());
            country.setCountryCode(postData.getCountryCode());
        }
        if (!StringUtils.isBlank(postData.getLanguageCode())) {
            Language language = languageService.findByCode(postData.getLanguageCode());
            if (language == null) {
                throw new EntityDoesNotExistsException(Language.class, postData.getLanguageCode());
            }

            country.setLanguage(language);
        }

        Currency currency = null;
        if (postData.getCurrencyCode() != null) { //
            currency = currencyService.findByCode(postData.getCurrencyCode());
            // If currencyCode don't exist in reference table ("adm_currency"), return error.
            if (currency == null) {
                throw new EntityDoesNotExistsException(Currency.class, postData.getCurrencyCode());
            }
            country.setCurrency(currency);

        } else {
            if (appProvider.getCurrency() != null) {
                currency = appProvider.getCurrency();
                country.setCurrency(currency);
            }
        }
        if (country.isTransient()) {
            countryService.create(country);
        } else {
            countryService.update(country);
        }

    }

    public CountryDto find(String countryCode) throws MeveoApiException {
        if (StringUtils.isBlank(countryCode)) {
            missingParameters.add("countryCode");
        }
        
        handleMissingParameters();

        throw new EntityDoesNotExistsException(Country.class, countryCode);
    }


    public void update(CountryDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCountryCode())) {
            missingParameters.add("countryCode");
        }
        if (StringUtils.isBlank(postData.getCurrencyCode())) {
            missingParameters.add("currencyCode");
        }

        handleMissingParameters();
        

        
        Currency currency = currencyService.findByCode(postData.getCurrencyCode());
        if (currency == null) {
            throw new EntityDoesNotExistsException(Currency.class, postData.getCurrencyCode());
        }
        Country country = countryService.findByCode(postData.getCountryCode());
        if (country == null) {
            throw new EntityDoesNotExistsException(Country.class, postData.getCountryCode());
        }

        Language language = null;
        if (!StringUtils.isBlank(postData.getLanguageCode())) {
            language = languageService.findByCode(postData.getLanguageCode());
            if (language == null) {
                throw new EntityDoesNotExistsException(Language.class, postData.getLanguageCode());
            }
        }

    }

    public void createOrUpdate(CountryDto postData) throws MeveoApiException, BusinessException {

    }

    public void findOrCreate(String countryCode) {

	}
}