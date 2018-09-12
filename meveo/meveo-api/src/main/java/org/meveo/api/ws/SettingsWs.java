package org.meveo.api.ws;

import org.meveo.api.dto.*;
import org.meveo.api.dto.account.ProviderContactDto;
import org.meveo.api.dto.communication.EmailTemplateDto;
import org.meveo.api.dto.communication.MeveoInstanceDto;
import org.meveo.api.dto.hierarchy.UserHierarchyLevelDto;
import org.meveo.api.dto.hierarchy.UserHierarchyLevelsDto;
import org.meveo.api.dto.response.*;
import org.meveo.api.dto.response.PagingAndFiltering.SortOrder;
import org.meveo.api.dto.response.account.ProviderContactResponseDto;
import org.meveo.api.dto.response.account.ProviderContactsResponseDto;
import org.meveo.api.dto.response.communication.EmailTemplateResponseDto;
import org.meveo.api.dto.response.communication.EmailTemplatesResponseDto;
import org.meveo.api.dto.response.communication.MeveoInstanceResponseDto;
import org.meveo.api.dto.response.communication.MeveoInstancesResponseDto;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.Date;
import java.util.List;

/**
 * @author Edward P. Legaspi
 * @lastModifiedVersion 5.0
 */
@SuppressWarnings("deprecation")
@WebService
public interface SettingsWs extends IBaseWs {

    // provider

    @Deprecated
    @WebMethod
    ActionStatus createProvider(@WebParam(name = "provider") ProviderDto postData);

    @Deprecated
    @WebMethod
    GetProviderResponse findProvider(@WebParam(name = "providerCode") String providerCode);

    @Deprecated
    @WebMethod
    ActionStatus updateProvider(@WebParam(name = "provider") ProviderDto postData);

    @Deprecated
    @WebMethod
    ActionStatus createOrUpdateProvider(@WebParam(name = "provider") ProviderDto postData);

    @WebMethod
    ActionStatus updateProviderCF(@WebParam(name = "provider") ProviderDto postData);

    @WebMethod
    GetProviderResponse findProviderCF(@WebParam(name = "providerCode") String providerCode);

    // configuration

    // user

    @WebMethod
    ActionStatus createUser(@WebParam(name = "user") UserDto postData);

    @WebMethod
    ActionStatus updateUser(@WebParam(name = "user") UserDto postData);

    @WebMethod
    ActionStatus removeUser(@WebParam(name = "username") String username);

    @WebMethod
    GetUserResponse findUser(@WebParam(name = "username") String username);

    @WebMethod
    ActionStatus createOrUpdateUser(@WebParam(name = "user") UserDto postData);
    
    /**
     * Creates a user in keycloak and core.
     * @param postData The user dto
     * @return ActionStatus
     */
    @WebMethod
    ActionStatus createExternalUser(@WebParam(name = "user") UserDto postData);
    
    /**
     * Updates a user in keycloak and core given a username.
     * @param postData The user dto
     * @return ActionStatus
     */
    @WebMethod
    ActionStatus updateExternalUser(@WebParam(name = "user") UserDto postData);
    
    /**
     * Deletes a user in keycloak and core given a username.
     * @param username the username of the user to be deleted.
     * @return ActionStatus
     */
    @WebMethod
    ActionStatus deleteExternalUser(@WebParam(name = "username") String username);

    /**
     * List users matching a given criteria
     * 
     * @param pagingAndFiltering Pagination and filtering criteria. Specify "securedEntities" in fields to include the secured entities.
     * @return A list of users
     */
    @WebMethod
    public UsersDto listUsers(@WebParam(name = "pagingAndFiltering") PagingAndFiltering pagingAndFiltering);

    // seller


    // tradingLanguage

    @Deprecated
    @WebMethod
    ActionStatus createLanguage(@WebParam(name = "language") LanguageDto postData);

    @Deprecated
    @WebMethod
    GetLanguageResponse findLanguage(@WebParam(name = "languageCode") String languageCode);

    @Deprecated
    @WebMethod
    ActionStatus removeLanguage(@WebParam(name = "languageCode") String languageCode);

    @Deprecated
    @WebMethod
    ActionStatus updateLanguage(@WebParam(name = "language") LanguageDto postData);

    @Deprecated
    @WebMethod
    ActionStatus createOrUpdateLanguage(@WebParam(name = "language") LanguageDto postData);

    // tradingCountry
    @Deprecated
    @WebMethod
    ActionStatus createCountry(@WebParam(name = "country") CountryDto countryDto);

    @Deprecated
    @WebMethod
    GetCountryResponse findCountry(@WebParam(name = "countryCode") String countryCode);

    @Deprecated
    @WebMethod
    ActionStatus removeCountry(@WebParam(name = "countryCode") String countryCode, @WebParam(name = "currencyCode") String currencyCode);

    @Deprecated
    @WebMethod
    ActionStatus updateCountry(@WebParam(name = "country") CountryDto countryDto);

    @Deprecated
    @WebMethod
    ActionStatus createOrUpdateCountry(@WebParam(name = "country") CountryDto countryDto);

    // traingCurrency
    @Deprecated
    @WebMethod
    ActionStatus createCurrency(@WebParam(name = "currency") CurrencyDto postData);

    @Deprecated
    @WebMethod
    GetCurrencyResponse findCurrency(@WebParam(name = "currencyCode") String currencyCode);

    @Deprecated
    @WebMethod
    ActionStatus removeCurrency(@WebParam(name = "currencyCode") String currencyCode);

    @Deprecated
    @WebMethod
    ActionStatus updateCurrency(@WebParam(name = "currency") CurrencyDto postData);

    @Deprecated
    @WebMethod
    ActionStatus createOrUpdateCurrency(@WebParam(name = "currency") CurrencyDto postData);

    // tax


    // invoice category


    // calendar

    @WebMethod
    ActionStatus createCalendar(@WebParam(name = "calendar") CalendarDto postData);

    @WebMethod
    ActionStatus updateCalendar(@WebParam(name = "calendar") CalendarDto postData);

    @WebMethod
    GetCalendarResponse findCalendar(@WebParam(name = "calendarCode") String calendarCode);

    @WebMethod
    ActionStatus removeCalendar(@WebParam(name = "calendarCode") String calendarCode);

    @WebMethod
    ActionStatus createOrUpdateCalendar(@WebParam(name = "calendar") CalendarDto postData);

    @WebMethod
    ListCalendarResponse listCalendars();


    // custom field

    @WebMethod
    ActionStatus createCustomFieldTemplate(@WebParam(name = "customField") CustomFieldTemplateDto postData);

    @WebMethod
    ActionStatus updateCustomFieldTemplate(@WebParam(name = "customField") CustomFieldTemplateDto postData);

    @WebMethod
    ActionStatus removeCustomFieldTemplate(@WebParam(name = "customFieldTemplateCode") String customFieldTemplateCode, @WebParam(name = "appliesTo") String appliesTo);

    @WebMethod
    GetCustomFieldTemplateReponseDto findCustomFieldTemplate(@WebParam(name = "customFieldTemplateCode") String customFieldTemplateCode,
            @WebParam(name = "appliesTo") String appliesTo);

    @WebMethod
    ActionStatus createOrUpdateCustomFieldTemplate(@WebParam(name = "customField") CustomFieldTemplateDto postData);

    // permission

    @WebMethod
    PermissionResponseDto listPermissions();

    // role

    @WebMethod
    ActionStatus createRole(@WebParam(name = "role") RoleDto postData);

    @WebMethod
    ActionStatus updateRole(@WebParam(name = "role") RoleDto postData);

    @WebMethod
    ActionStatus removeRole(@WebParam(name = "role") String name);

    @WebMethod
    GetRoleResponse findRole(@WebParam(name = "roleName") String name);

    @WebMethod
    ActionStatus createOrUpdateRole(@WebParam(name = "role") RoleDto postData);

    /**
     * List roles matching a given criteria
     * 
     * @param pagingAndFiltering Pagination and filtering criteria. Specify "permissions" in fields to include the permissions. Specify "roles" to include child roles.
     * @return A list of roles
     */
    @WebMethod
    public RolesDto listRoles(@WebParam(name = "pagingAndFiltering") PagingAndFiltering pagingAndFiltering);
    
    /**
     * List external source such as from keycloak.
     * @return list of external roles
     */
    @WebMethod
    public RolesDto listExternalRoles();

    // Multi Language field value translations

    /**
     * 
     * Provide translation of multi language field values. Deprecated in v.4.7. Use updateTranslations instead
     * 
     * @param postData Translated field values
     * @return action status.
     */
    @WebMethod
    @Deprecated
    public ActionStatus createDescriptions(@WebParam(name = "descriptions") CatMessagesDto postData);

    /**
     * Provide translation of multi language field value. Deprecated in v.4.7. Use updateTranslations instead
     * 
     * @param postData Translated field values
     * @return action status.
     */
    @WebMethod
    @Deprecated
    public ActionStatus updateDescriptions(@WebParam(name = "descriptions") CatMessagesDto postData);

    /**
     * Provide translation of multi language field values.
     * 
     * @param translations list of category messages.
     * @return action status.
     */
    @WebMethod
    public ActionStatus updateTranslations(@WebParam(name = "translations") List<CatMessagesDto> translations);

    /**
     * Find entity field translations for a particular entity, field (optional) and a language (optional). Deprecated in v.4.7. Use findTranslations instead
     * 
     * @param entityClass Entity class name
     * @param code Entity code
     * @param languageCode 3 letter language code
     * @return A list of field value translations
     */
    @WebMethod
    @Deprecated
    GetDescriptionsResponse findDescriptions(@WebParam(name = "entityClass") String entityClass, @WebParam(name = "code") String code,
            @WebParam(name = "languageCode") String languageCode);

    /**
     * Find entity field translations for a particular entity, field (optional) and a language (optional)
     * 
     * @param entityClass Entity class name
     * @param code Entity code
     * @param validFrom Validity dates - from
     * @param validTo Validity dates - to
     * @param fieldname Field name
     * @param languageCode 3 letter language code
     * @return A list of field value translations
     */
    @WebMethod
    public DescriptionsResponseDto findTranslations(@WebParam(name = "entityClass") String entityClass, @WebParam(name = "code") String code,
            @WebParam(name = "validFrom") Date validFrom, @WebParam(name = "validTo") Date validTo, @WebParam(name = "fieldName") String fieldname,
            @WebParam(name = "languageCode") String languageCode);

    /**
     * Remove field value translation for a given entity and language (optional). Deprecated in v.4.7. Use removeTranslations instead.
     * 
     * @param entityClass Entity class name
     * @param code Entity code
     * @param languageCode 3 letter language code. Optional
     * @return action status.
     */
    @WebMethod
    @Deprecated
    public ActionStatus removeDescriptions(@WebParam(name = "entityClass") String entityClass, @WebParam(name = "code") String code,
            @WebParam(name = "languageCode") String languageCode);

    /**
     * Remove field value translation for a given entity, field (optional) and language (optional)
     * 
     * @param entityClass Entity class name
     * @param code Entity code
     * @param validFrom Validity dates - from
     * @param validTo Validity dates - to
     * @param fieldname Field name. Optional
     * @param languageCode 3 letter language code. Optional
     * @return action status.
     */
    @WebMethod
    public ActionStatus removeTranslations(@WebParam(name = "entityClass") String entityClass, @WebParam(name = "code") String code, @WebParam(name = "validFrom") Date validFrom,
            @WebParam(name = "validTo") Date validTo, @WebParam(name = "fieldName") String fieldname, @WebParam(name = "languageCode") String languageCode);

    /**
     * Provide translation of multi language field values. Deprecated in v.4.7. Use updateTranslations instead
     * 
     * @param postData Translated field values
     * @return action status.
     */
    @WebMethod
    @Deprecated
    public ActionStatus createOrUpdateDescriptions(@WebParam(name = "descriptions") CatMessagesDto postData);

    /**
     * List entity field value translations. Deprecated in v.4.7. Use listTranslations instead.
     * 
     * @return A list of entity field value translations
     */
    @WebMethod
    @Deprecated
    public DescriptionsResponseDto listDescriptions();

    /**
     * List entity field value translations for a given entity type (optional), field (optional) and language (optional). Note: will provide ONLY those entities that have at least
     * one of multilanguage fields translated.
     * 
     * @param entityClass Entity class name
     * @param fieldname Field name. Optional
     * @param languageCode 3 letter language code. Optional
     * @return A list of entity field value translations
     */
    @WebMethod
    public DescriptionsResponseDto listTranslations(@WebParam(name = "entityClass") String entityClass, @WebParam(name = "fieldName") String fieldname,
            @WebParam(name = "languageCode") String languageCode);


    /**
     * create a providerContact by dto
     * 
     * @param providerContactDto providerContactDto
     * @return action status.
     */
    @WebMethod
    ActionStatus createProviderContact(@WebParam(name = "providerContact") ProviderContactDto providerContactDto);

    /**
     * update a providerContact by dto
     * 
     * @param providerContactDto providerContactDto
     * @return action status.
     */
    @WebMethod
    ActionStatus updateProviderContact(@WebParam(name = "providerContact") ProviderContactDto providerContactDto);

    /**
     * find a providerContact by code
     * 
     * @param providerContactCode providerContactCode
     * @return action status.
     */
    @WebMethod
    ProviderContactResponseDto findProviderContact(@WebParam(name = "providerContactCode") String providerContactCode);

    /**
     * remove a providerContact by code
     * 
     * @param providerContactCode providerContactCode
     * @return action status.
     */
    @WebMethod
    ActionStatus removeProviderContact(@WebParam(name = "providerContactCode") String providerContactCode);

    /**
     * list all providerContacts
     * 
     * @return action status.
     */
    @WebMethod
    ProviderContactsResponseDto listProviderContacts();

    /**
     * createOrUpdate a providerContact by dto
     * 
     * @param providerContactDto providerContactDto
     * @return action status.
     */
    @WebMethod
    ActionStatus createOrUpdateProviderContact(@WebParam(name = "providerContact") ProviderContactDto providerContactDto);

    /**
     * create an emailTemplate by dto
     * 
     * @param emailTemplateDto emailTemplateDto
     * @return action status.
     */
    @WebMethod
    ActionStatus createEmailTemplate(@WebParam(name = "emailTemplate") EmailTemplateDto emailTemplateDto);

    /**
     * update an emailTemplate by dto
     * 
     * @param emailTemplateDto emailTemplateDto
     * @return action status.
     */
    @WebMethod
    ActionStatus updateEmailTemplate(@WebParam(name = "emailTemplate") EmailTemplateDto emailTemplateDto);

    /**
     * find an emailTemplate by code
     * 
     * @param emailTemplateCode emailTemplateCode
     * @return action status.
     */
    @WebMethod
    EmailTemplateResponseDto findEmailTemplate(@WebParam(name = "emailTemplateCode") String emailTemplateCode);

    /**
     * remove an emailTemplate by code
     * 
     * @param emailTemplateCode emailTemplateCode
     * @return action status.
     */
    @WebMethod
    ActionStatus removeEmailTemplate(@WebParam(name = "emailTemplateCode") String emailTemplateCode);

    /**
     * list emailTemplates
     * 
     * @return action status.
     */
    @WebMethod
    EmailTemplatesResponseDto listEmailTemplates();

    /**
     * createOrUpdate an emailTemplate by dto
     * 
     * @param emailTemplateDto emailTemplateDto
     * @return action status.
     */
    @WebMethod
    ActionStatus createOrUpdateEmailTemplate(@WebParam(name = "emailTemplate") EmailTemplateDto emailTemplateDto);

    /**
     * create a meveoInstance by dto
     * 
     * @param meveoInstanceDto meveoInstanceDto
     * @return action status.
     */
    @WebMethod
    ActionStatus createMeveoInstance(@WebParam(name = "meveoInstance") MeveoInstanceDto meveoInstanceDto);

    /**
     * update a mveoInstance by dto
     * 
     * @param meveoInstanceDto meveoInstanceDto
     * @return action status.
     */
    @WebMethod
    ActionStatus updateMeveoInstance(@WebParam(name = "meveoInstance") MeveoInstanceDto meveoInstanceDto);

    /**
     * find a meveoInstance by code
     * 
     * @param meveoInstanceCode meveoInstanceCode
     * @return action status.
     */
    @WebMethod
    MeveoInstanceResponseDto findMeveoInstance(@WebParam(name = "meveoInstanceCode") String meveoInstanceCode);

    /**
     * remove a meveoInstance by code
     * 
     * @param meveoInstanceCode meveoInstanceCode
     * @return action status.
     */
    @WebMethod
    ActionStatus removeMeveoInstance(@WebParam(name = "meveoInstanceCode") String meveoInstanceCode);

    /**
     * list meveoInstances
     * 
     * @return action status.
     */
    @WebMethod
    MeveoInstancesResponseDto listMeveoInstances();

    /**
     * createOrUpdate meveoInstance by dto
     * 
     * @param meveoInstanceDto meveoInstanceDto
     * @return action status.
     */
    @WebMethod
    ActionStatus createOrUpdateMeveoInstance(@WebParam(name = "meveoInstance") MeveoInstanceDto meveoInstanceDto);

    /**
     * create userHierarchyLevel by dto
     * 
     * @param userHierarchyLevelDto userHierarchyLevelDto
     * @return action status.
     */
    @WebMethod
    ActionStatus createUserHierarchyLevel(@WebParam(name = "userHierarchyLevel") UserHierarchyLevelDto userHierarchyLevelDto);

    /**
     * update userHierarchyLevel by dto
     * 
     * @param userHierarchyLevelDto userHierarchyLevelDto
     * @return action status.
     */
    @WebMethod
    ActionStatus updateUserHierarchyLevel(@WebParam(name = "userHierarchyLevel") UserHierarchyLevelDto userHierarchyLevelDto);

    /**
     * remove a userHierarchyCode by code
     * 
     * @param hierarchyLevelCode hierarchyLevelCode
     * @return action status.
     */
    @WebMethod
    ActionStatus removeUserHierarchyLevel(@WebParam(name = "hierarchyLevelCode") String hierarchyLevelCode);

    /**
     * find a userHierarchyCode by code
     * 
     * @param hierarchyLevelCode hierarchyLevelCode
     * @return action status.
     */
    @WebMethod
    UserHierarchyLevelResponseDto findUserHierarchyLevel(@WebParam(name = "hierarchyLevelCode") String hierarchyLevelCode);

    /**
     * createOrUpdate userHierarchyLevel by dto
     * 
     * @param userHierarchyLevelDto The user hierarchy level dto
     * @return action status.
     */
    @WebMethod
    ActionStatus createOrUpdateUserHierarchyLevel(@WebParam(name = "userHierarchyLevel") UserHierarchyLevelDto userHierarchyLevelDto);

    /**
     * List user hierarchy levels matching a given criteria
     * 
     * @param pagingAndFiltering Pagination and filtering criteria. Specify "childLevels" in fields to include the child levels of user hierarchy level.
     * @return A list of user hierarchy levels
     */
    @WebMethod
    public UserHierarchyLevelsDto listUserHierarchyLevels(@WebParam(name = "pagingAndFiltering") PagingAndFiltering pagingAndFiltering);

    /**
     * Set configuration (stored in meveo-admin.properties file) property
     * 
     * @param property Property key
     * @param value Value to set
     * @return action status.
     */
    @WebMethod
    public ActionStatus setConfigurationProperty(@WebParam(name = "property") String property, @WebParam(name = "value") String value);


}