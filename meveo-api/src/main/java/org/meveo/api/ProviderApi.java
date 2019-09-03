package org.meveo.api;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.dto.ProviderDto;
import org.meveo.api.dto.ProvidersDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.*;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.admin.Currency;
import org.meveo.model.billing.Country;
import org.meveo.model.billing.Language;
import org.meveo.model.crm.Provider;
import org.meveo.service.admin.impl.CountryService;
import org.meveo.service.admin.impl.CurrencyService;
import org.meveo.service.admin.impl.LanguageService;
import org.meveo.service.catalog.impl.CalendarService;
import org.meveo.service.crm.impl.ProviderService;
import org.primefaces.model.SortOrder;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author Edward P. Legaspi
 **/
@SuppressWarnings("deprecation")
@Stateless
public class ProviderApi extends BaseApi {

    @Inject
    private ProviderService providerService;

    @Inject
    private CountryService countryService;

    @Inject
    private CurrencyService currencyService;

    @Inject
    private LanguageService languageService;

    @Inject
    private CalendarService calendarService;

    public ProviderDto find() throws MeveoApiException {

        Provider provider = providerService.findById(appProvider.getId(), Arrays.asList("currency", "country", "language"));
        if (currentUser.hasRole("apiAccess") || currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationVisualization"))) {
            return new ProviderDto(provider, entityToDtoConverter.getCustomFieldsDTO(provider, true));
        } else {
            throw new ActionForbiddenException("User has no permission to access provider");
        }
    }

    public void update(ProviderDto postData) throws MeveoApiException, BusinessException {

        // search for provider
        Provider provider = providerService.findById(appProvider.getId(), Arrays.asList("currency", "country", "language"));

        if (!(currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationManagement")))) {
            throw new ActionForbiddenException("User has no permission to manage provider " + provider.getCode());
        }

        provider = fromDto(postData, provider);

        // populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), provider, false);
        } catch (MissingParameterException | InvalidParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        provider = providerService.update(provider);
    }



    public void updateProviderCF(ProviderDto postData) throws MeveoApiException, BusinessException {

        if (!(currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationManagement")))) {
            throw new ActionForbiddenException("User has no permission to manage provider ");
        }

        Provider provider = providerService.findById(appProvider.getId());
        // populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), provider, false);
        } catch (MissingParameterException | InvalidParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        provider = providerService.update(provider);
    }

    public ProviderDto findProviderCF() throws MeveoApiException {

        Provider provider = providerService.findById(appProvider.getId());
        if (currentUser.hasRole("apiAccess") || currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationVisualization"))) {
            return new ProviderDto(provider, entityToDtoConverter.getCustomFieldsDTO(provider, true), false);
        } else {
            throw new ActionForbiddenException("User has no permission to access provider");
        }
    }

    public Provider fromDto(ProviderDto postData, Provider entity) throws MeveoApiException {

        Provider provider = null;
        if (entity == null) {
            provider = new Provider();
        } else {
            provider = entity;
        }
        if (!StringUtils.isBlank(postData.getCode())) {
            provider.setCode(postData.getCode().toUpperCase());
        }

        if (!StringUtils.isBlank(postData.getDescription())) {
            provider.setDescription(postData.getDescription());
        }
        // search for currency
        if (!StringUtils.isBlank(postData.getCurrency())) {
            Currency currency = currencyService.findByCode(postData.getCurrency());
            if (currency == null) {
                throw new EntityDoesNotExistsException(Currency.class.getName(), postData.getCurrency());
            }
            provider.setCurrency(currency);
        }
        // search for country
        if (!StringUtils.isBlank(postData.getCountry())) {
            Country country = countryService.findByCode(postData.getCountry());
            if (country == null) {
                throw new EntityDoesNotExistsException(Country.class.getName(), postData.getCountry());
            }
            provider.setCountry(country);
        }
        // search for language
        if (!StringUtils.isBlank(postData.getLanguage())) {
            Language language = languageService.findByCode(postData.getLanguage());
            if (language == null) {
                throw new EntityDoesNotExistsException(Language.class.getName(), postData.getLanguage());
            }
            provider.setLanguage(language);
        }
        if (!StringUtils.isBlank(postData.getEmail())) {
            provider.setEmail(postData.getEmail());
        }
        return provider;
    }

    /**
     * New tenant/provider creation
     * 
     * @param postData postData Provider Dto
     * @throws MeveoApiException MeveoApiException
     * @throws BusinessException BusinessException
     */
    public void createTenant(ProviderDto postData) throws MeveoApiException, BusinessException {

        // Tenant/provider management is available for superadmin user only of main provider
        if (!ParamBean.isMultitenancyEnabled()) {
            throw new ActionForbiddenException("Multitenancy is not enabled");

        } else if (currentUser.getProviderCode() != null) {
            throw new ActionForbiddenException("Tenants should be managed by a main tenant's super administrator");

        } else if (!currentUser.hasRole("superAdminManagement")) {
            throw new ActionForbiddenException("User has no permission to manage tenants ");
        }

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }

        handleMissingParameters();

        // check if provider already exists
        if (providerService.findByCode(postData.getCode()) != null) {
            throw new EntityAlreadyExistsException(Provider.class, postData.getCode());
        }

        Provider provider = new Provider();
        provider.setCode(postData.getCode());
        provider.setDescription(postData.getDescription());

        providerService.create(provider);
    }

    /**
     * List tenants/providers
     * 
     * @return A list of tenants/providers
     * @throws ActionForbiddenException action forbidden exception
     * @throws InvalidParameterException invalid parameter exception.
     */
    public ProvidersDto listTenants() throws ActionForbiddenException, InvalidParameterException {

        // Tenant/provider management is available for superadmin user only of main provider
        if (!ParamBean.isMultitenancyEnabled()) {
            throw new ActionForbiddenException("Multitenancy is not enabled");

        } else if (currentUser.getProviderCode() != null) {
            throw new ActionForbiddenException("Tenants should be managed by a main tenant's super administrator");

        } else if (!currentUser.hasRole("superAdminManagement")) {
            throw new ActionForbiddenException("User has no permission to manage tenants ");
        }

        PaginationConfiguration paginationConfig = toPaginationConfiguration("id", SortOrder.ASCENDING, null, null, Provider.class);

        Long totalCount = providerService.count(paginationConfig);

        ProvidersDto result = new ProvidersDto();
        result.setPaging(new PagingAndFiltering());
        result.getPaging().setTotalNumberOfRecords(totalCount.intValue());

        if (totalCount > 0) {
            List<Provider> providers = providerService.list(paginationConfig);
            for (Provider provider : providers) {
                result.getProviders().add(new ProviderDto(provider, appProvider.getId().equals(provider.getId()) ? entityToDtoConverter.getCustomFieldsDTO(provider, true) : null));
            }
        }

        return result;
    }

    /**
     * Remove tenant/provider
     * 
     * @param providerCode providerCode
     * @throws MeveoApiException MeveoApiException
     */
    public void removeTenant(String providerCode) throws MeveoApiException {

        // Tenant/provider management is available for superadmin user only of main provider
        if (!ParamBean.isMultitenancyEnabled()) {
            throw new ActionForbiddenException("Multitenancy is not enabled");

        } else if (currentUser.getProviderCode() != null) {
            throw new ActionForbiddenException("Tenants should be managed by a main tenant's super administrator");

        } else if (!currentUser.hasRole("superAdminManagement")) {
            throw new ActionForbiddenException("User has no permission to manage tenants ");
        }

        if (StringUtils.isBlank(providerCode)) {
            missingParameters.add("providerCode");
            handleMissingParameters();
        }

        if (appProvider.getCode().equalsIgnoreCase(providerCode)) {
            throw new BusinessApiException("Can not remove a main provider");
        }

        Provider provider = providerService.findByCode(providerCode);
        if (provider == null) {
            throw new EntityDoesNotExistsException(Provider.class, providerCode);
        }

        try {
            providerService.remove(provider);
            providerService.flush();
        } catch (Exception e) {
            if (e.getMessage().indexOf("ConstraintViolationException") > -1) {
                throw new DeleteReferencedEntityException(Provider.class, providerCode);
            }
            throw new MeveoApiException(MeveoApiErrorCodeEnum.BUSINESS_API_EXCEPTION, "Cannot delete entity");
        }
    }
}