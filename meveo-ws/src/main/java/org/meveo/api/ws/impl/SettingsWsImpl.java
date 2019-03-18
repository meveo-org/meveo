/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.api.ws.impl;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.*;
import org.meveo.api.account.ProviderContactApi;
import org.meveo.api.communication.EmailTemplateApi;
import org.meveo.api.communication.MeveoInstanceApi;
import org.meveo.api.dto.*;
import org.meveo.api.dto.account.ProviderContactDto;
import org.meveo.api.dto.communication.EmailTemplateDto;
import org.meveo.api.dto.communication.MeveoInstanceDto;
import org.meveo.api.dto.hierarchy.UserHierarchyLevelDto;
import org.meveo.api.dto.hierarchy.UserHierarchyLevelsDto;
import org.meveo.api.dto.response.*;
import org.meveo.api.dto.response.account.ProviderContactResponseDto;
import org.meveo.api.dto.response.account.ProviderContactsResponseDto;
import org.meveo.api.dto.response.communication.EmailTemplateResponseDto;
import org.meveo.api.dto.response.communication.EmailTemplatesResponseDto;
import org.meveo.api.dto.response.communication.MeveoInstanceResponseDto;
import org.meveo.api.dto.response.communication.MeveoInstancesResponseDto;
import org.meveo.api.hierarchy.UserHierarchyLevelApi;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.ws.SettingsWs;

import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Edward P. Legaspi
 * @lastModifiedVersion 5.0
 */
@SuppressWarnings("deprecation")
@WebService(serviceName = "SettingsWs", endpointInterface = "org.meveo.api.ws.SettingsWs")
@Interceptors({ WsRestApiInterceptor.class })
public class SettingsWsImpl extends BaseWs implements SettingsWs {
    
    @Inject
    private CustomFieldTemplateApi customFieldTemplateApi;

    @Inject
    private CountryApi countryApi;

    @Inject
    private CurrencyApi currencyApi;

    @Inject
    private LanguageApi languageApi;

    @Inject
    private ProviderApi providerApi;

    @Inject
    private UserApi userApi;

    @Inject
    private CalendarApi calendarApi;

    @Inject
    private PermissionApi permissionApi;

    @Inject
    private RoleApi roleApi;

    @Inject
    private MultiLanguageFieldApi multilanguageFieldApi;

    @Inject
    private ProviderContactApi providerContactApi;

    @Inject
    private EmailTemplateApi emailTemplateApi;

    @Inject
    private MeveoInstanceApi meveoInstanceApi;

    @Inject
    private UserHierarchyLevelApi userHierarchyLevelApi;

    @Inject
    private ConfigurationApi configurationApi;

    @Deprecated
    @Override
    public ActionStatus createCountry(CountryDto countryDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            countryApi.create(countryDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Deprecated
    @Override
    public GetCountryResponse findCountry(String countryCode) {
        GetCountryResponse result = new GetCountryResponse();
        result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);

        try {
            result.setCountry(countryApi.find(countryCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Deprecated
    @Override
    public ActionStatus removeCountry(String countryCode, String currencyCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            countryApi.remove(countryCode, currencyCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Deprecated
    @Override
    public ActionStatus updateCountry(CountryDto countryDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            countryApi.update(countryDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Deprecated
    @Override
    public ActionStatus createCurrency(CurrencyDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            currencyApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Deprecated
    @Override
    public GetCurrencyResponse findCurrency(String currencyCode) {
        GetCurrencyResponse result = new GetCurrencyResponse();

        try {
            result.setCurrency(currencyApi.find(currencyCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Deprecated
    @Override
    public ActionStatus removeCurrency(String currencyCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            currencyApi.remove(currencyCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Deprecated
    @Override
    public ActionStatus updateCurrency(CurrencyDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            currencyApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Deprecated
    @Override
    public ActionStatus createOrUpdateCurrency(CurrencyDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            currencyApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }


    @Override
    public ActionStatus createLanguage(LanguageDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            languageApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetLanguageResponse findLanguage(String languageCode) {
        GetLanguageResponse result = new GetLanguageResponse();

        try {
            result.setLanguage(languageApi.find(languageCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus removeLanguage(String languageCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            languageApi.remove(languageCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus updateLanguage(LanguageDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            languageApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Deprecated
    @Override
    public ActionStatus createProvider(ProviderDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            throw new BusinessException("There should already be a provider setup");
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Deprecated
    @Override
    public GetProviderResponse findProvider(String providerCode) {
        GetProviderResponse result = new GetProviderResponse();

        try {
            result.setProvider(providerApi.find());

        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Deprecated
    @Override
    public ActionStatus updateProvider(ProviderDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            providerApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }



    @Override
    public ActionStatus createUser(UserDto postData) {
        ActionStatus result = new ActionStatus();

        try {
            userApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus updateUser(UserDto postData) {
        ActionStatus result = new ActionStatus();

        try {
            userApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus removeUser(String username) {
        ActionStatus result = new ActionStatus();

        try {
            userApi.remove(username);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetUserResponse findUser(String username) {
        GetUserResponse result = new GetUserResponse();

        try {
            result.setUser(userApi.find(getHttpServletRequest(), username));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }
    
    @Override
    public ActionStatus createExternalUser(UserDto postData) {
        ActionStatus result = new ActionStatus();

        try {            
            userApi.createExternalUser(getHttpServletRequest(), postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }
    
    @Override
    public ActionStatus updateExternalUser(UserDto postData) {
        ActionStatus result = new ActionStatus();

        try {            
            userApi.updateExternalUser(getHttpServletRequest(), postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }
    
    @Override
    public ActionStatus deleteExternalUser(String username) {
        ActionStatus result = new ActionStatus();

        try {            
            userApi.deleteExternalUser(getHttpServletRequest(), username);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public UsersDto listUsers(PagingAndFiltering pagingAndFiltering) {

        UsersDto result = new UsersDto();

        try {
            result = userApi.list(getHttpServletRequest(), pagingAndFiltering);
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }


    @Override
    public ActionStatus createCalendar(CalendarDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            calendarApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus updateCalendar(CalendarDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            calendarApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetCalendarResponse findCalendar(String calendarCode) {
        GetCalendarResponse result = new GetCalendarResponse();
        result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);

        try {
            result.setCalendar(calendarApi.find(calendarCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ListCalendarResponse listCalendars() {
        ListCalendarResponse result = new ListCalendarResponse();
        CalendarsDto calendarsDto = new CalendarsDto();

        try {
            calendarsDto.setCalendar(calendarApi.list());
            result.setCalendars(calendarsDto);
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus removeCalendar(String calendarCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            calendarApi.remove(calendarCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }


    @Override
    public ActionStatus createCustomFieldTemplate(CustomFieldTemplateDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customFieldTemplateApi.create(postData, null);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus updateCustomFieldTemplate(CustomFieldTemplateDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customFieldTemplateApi.update(postData, null);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus removeCustomFieldTemplate(String customFieldTemplateCode, String appliesTo) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            customFieldTemplateApi.remove(customFieldTemplateCode, appliesTo);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetCustomFieldTemplateReponseDto findCustomFieldTemplate(String customFieldTemplateCode, String appliesTo) {
        GetCustomFieldTemplateReponseDto result = new GetCustomFieldTemplateReponseDto();

        try {
            result.setCustomFieldTemplate(customFieldTemplateApi.find(customFieldTemplateCode, appliesTo));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdateCountry(CountryDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            countryApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdateCalendar(CalendarDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            calendarApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }


    @Override
    public ActionStatus createOrUpdateCustomFieldTemplate(CustomFieldTemplateDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            customFieldTemplateApi.createOrUpdate(postData, null);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }


    @Deprecated
    @Override
    public ActionStatus createOrUpdateProvider(ProviderDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            providerApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdateUser(UserDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }


    @Override
    public PermissionResponseDto listPermissions() {
        PermissionResponseDto result = new PermissionResponseDto();
        try {
            result.setPermissionsDto(permissionApi.list());
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createRole(RoleDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            roleApi.create(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus updateRole(RoleDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            roleApi.update(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus removeRole(String name) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            roleApi.remove(name);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetRoleResponse findRole(String name) {
        GetRoleResponse result = new GetRoleResponse();
        try {
            result.setRoleDto(roleApi.find(name));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdateRole(RoleDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
        try {
            roleApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public RolesDto listRoles(PagingAndFiltering pagingAndFiltering) {

        RolesDto result = new RolesDto();

        try {
            result = roleApi.list(pagingAndFiltering);
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }
    
    @Override
    public RolesDto listExternalRoles() {
        RolesDto result = new RolesDto();

        try {
            result.setRoles(roleApi.listExternalRoles(getHttpServletRequest()));

        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createDescriptions(CatMessagesDto postData) {
        return updateTranslations(Arrays.asList(postData));
    }

    @Override
    public ActionStatus updateDescriptions(CatMessagesDto postData) {
        return updateTranslations(Arrays.asList(postData));
    }

    @Override
    public ActionStatus updateTranslations(List<CatMessagesDto> translations) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            multilanguageFieldApi.update(translations);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetDescriptionsResponse findDescriptions(String entityClass, String code, String languageCode) {

        GetDescriptionsResponse response = new GetDescriptionsResponse();

        DescriptionsResponseDto multiResponse = findTranslations(entityClass, code, null, null, null, languageCode);

        response.setActionStatus(multiResponse.getActionStatus());
        if (multiResponse.getCatMessages() != null && multiResponse.getCatMessages().getCatMessage() != null && multiResponse.getCatMessages().getCatMessage().size() > 0) {
            response.setCatMessagesDto(multiResponse.getCatMessages().getCatMessage().get(0));
        }
        return response;
    }

    @Override
    public DescriptionsResponseDto findTranslations(String entityClass, String code, Date validFrom, Date validTo, String fieldname, String languageCode) {

        DescriptionsResponseDto result = new DescriptionsResponseDto();
        result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);

        try {
            result.setCatMessages(multilanguageFieldApi.find(entityClass, code, validFrom, validTo, fieldname, languageCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus removeDescriptions(String entityClass, String code, String languageCode) {
        return removeTranslations(entityClass, code, null, null, null, languageCode);
    }

    @Override
    public ActionStatus removeTranslations(String entityClass, String code, Date validFrom, Date validTo, String fieldname, String languageCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            multilanguageFieldApi.remove(entityClass, code, validFrom, validTo, fieldname, languageCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdateDescriptions(CatMessagesDto postData) {
        return updateTranslations(Arrays.asList(postData));
    }

    @Override
    public DescriptionsResponseDto listDescriptions() {
        return listTranslations(null, null, null);
    }

    @Override
    public DescriptionsResponseDto listTranslations(String entityClass, String fieldname, String languageCode) {

        DescriptionsResponseDto result = new DescriptionsResponseDto();

        try {
            result.setCatMessages(multilanguageFieldApi.list(entityClass, fieldname, languageCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }


    @Deprecated
    @Override
    public ActionStatus createOrUpdateLanguage(LanguageDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            languageApi.createOrUpdate(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }


    @Override
    public ActionStatus updateProviderCF(ProviderDto postData) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            providerApi.updateProviderCF(postData);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public GetProviderResponse findProviderCF(String providerCode) {
        GetProviderResponse result = new GetProviderResponse();

        try {
            result.setProvider(providerApi.findProviderCF());

        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createProviderContact(ProviderContactDto providerContactDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            providerContactApi.create(providerContactDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus updateProviderContact(ProviderContactDto providerContactDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            providerContactApi.update(providerContactDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ProviderContactResponseDto findProviderContact(String providerContactCode) {
        ProviderContactResponseDto result = new ProviderContactResponseDto();

        try {
            result.setProviderContact(providerContactApi.find(providerContactCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus removeProviderContact(String providerContactCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            providerContactApi.remove(providerContactCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ProviderContactsResponseDto listProviderContacts() {
        ProviderContactsResponseDto result = new ProviderContactsResponseDto();

        try {
            result.setProviderContacts(providerContactApi.list());
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }
        return result;
    }

    @Override
    public ActionStatus createOrUpdateProviderContact(ProviderContactDto providerContactDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            providerContactApi.createOrUpdate(providerContactDto);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus createEmailTemplate(EmailTemplateDto emailTemplateDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            emailTemplateApi.create(emailTemplateDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus updateEmailTemplate(EmailTemplateDto emailTemplateDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            emailTemplateApi.update(emailTemplateDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public EmailTemplateResponseDto findEmailTemplate(String emailTemplateCode) {
        EmailTemplateResponseDto result = new EmailTemplateResponseDto();

        try {
            result.setEmailTemplate(emailTemplateApi.find(emailTemplateCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus removeEmailTemplate(String emailTemplateCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            emailTemplateApi.remove(emailTemplateCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public EmailTemplatesResponseDto listEmailTemplates() {
        EmailTemplatesResponseDto result = new EmailTemplatesResponseDto();

        try {
            result.setEmailTemplates(emailTemplateApi.list());
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdateEmailTemplate(EmailTemplateDto emailTemplateDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            emailTemplateApi.createOrUpdate(emailTemplateDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus createMeveoInstance(MeveoInstanceDto meveoInstanceDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            log.debug("start to create in meveoInstanceApi");
            meveoInstanceApi.create(meveoInstanceDto);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus updateMeveoInstance(MeveoInstanceDto meveoInstanceDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            meveoInstanceApi.update(meveoInstanceDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public MeveoInstanceResponseDto findMeveoInstance(String meveoInstanceCode) {
        MeveoInstanceResponseDto result = new MeveoInstanceResponseDto();
        try {
            result.setMeveoInstance(meveoInstanceApi.find(meveoInstanceCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus removeMeveoInstance(String meveoInstanceCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            meveoInstanceApi.remove(meveoInstanceCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public MeveoInstancesResponseDto listMeveoInstances() {
        MeveoInstancesResponseDto result = new MeveoInstancesResponseDto();

        try {
            result.setMeveoInstances(meveoInstanceApi.list());
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdateMeveoInstance(MeveoInstanceDto meveoInstanceDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            meveoInstanceApi.createOrUpdate(meveoInstanceDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus createUserHierarchyLevel(UserHierarchyLevelDto userHierarchyLevelDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userHierarchyLevelApi.create(userHierarchyLevelDto);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public ActionStatus updateUserHierarchyLevel(UserHierarchyLevelDto userHierarchyLevelDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userHierarchyLevelApi.update(userHierarchyLevelDto);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public ActionStatus removeUserHierarchyLevel(String hierarchyLevelCode) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userHierarchyLevelApi.remove(hierarchyLevelCode);
        } catch (Exception e) {
            processException(e, result);
        }

        return result;
    }

    @Override
    public UserHierarchyLevelResponseDto findUserHierarchyLevel(String hierarchyLevelCode) {
        UserHierarchyLevelResponseDto result = new UserHierarchyLevelResponseDto();
        result.getActionStatus().setStatus(ActionStatusEnum.SUCCESS);

        try {
            result.setUserHierarchyLevel(userHierarchyLevelApi.find(hierarchyLevelCode));
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;
    }

    @Override
    public ActionStatus createOrUpdateUserHierarchyLevel(UserHierarchyLevelDto userHierarchyLevelDto) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            userHierarchyLevelApi.createOrUpdate(userHierarchyLevelDto);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }

    @Override
    public UserHierarchyLevelsDto listUserHierarchyLevels(PagingAndFiltering pagingAndFiltering) {

        UserHierarchyLevelsDto result = new UserHierarchyLevelsDto();

        try {
            result = userHierarchyLevelApi.list(pagingAndFiltering);
        } catch (Exception e) {
            processException(e, result.getActionStatus());
        }

        return result;

    }

    @Override
    public ActionStatus setConfigurationProperty(String property, String value) {
        ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

        try {
            configurationApi.setProperty(property, value);
        } catch (Exception e) {
            processException(e, result);
        }
        return result;
    }
}