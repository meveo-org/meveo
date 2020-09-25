package org.meveo.api.notification;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.notification.WebHookDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.catalog.CounterTemplate;
import org.meveo.model.notification.HttpProtocol;
import org.meveo.model.notification.WebHook;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.catalog.impl.CounterTemplateService;
import org.meveo.service.notification.WebHookService;
import org.meveo.service.script.ScriptInstanceService;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 **/
@Stateless
public class WebHookApi extends NotificationApi<WebHook, WebHookDto> {

    @Inject
    private WebHookService webHookService;

    @Inject
    private CounterTemplateService counterTemplateService;

    @Inject
    private ScriptInstanceService scriptInstanceService;
    
    public WebHookApi() {
    	super(WebHook.class, WebHookDto.class);
    }

    @Override
    public WebHook create(WebHookDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getClassNameFilter())) {
            missingParameters.add("classNameFilter");
        }
        if (postData.getEventTypeFilter() == null) {
            missingParameters.add("eventTypeFilter");
        }
        if (StringUtils.isBlank(postData.getHost())) {
            missingParameters.add("host");
        }
        if (StringUtils.isBlank(postData.getPage())) {
            missingParameters.add("page");
        }
        if (postData.getHttpMethod() == null) {
            missingParameters.add("httpMethod");
        }

        handleMissingParameters();

        return super.create(postData);
    }

    /* (non-Javadoc)
     * @see org.meveo.api.ApiService#find(java.lang.String)
     */
    @Override
    public WebHookDto find(String notificationCode) throws MeveoApiException {
        WebHookDto result = new WebHookDto();

        if (!StringUtils.isBlank(notificationCode)) {
            WebHook notif = webHookService.findByCode(notificationCode);

            if (notif == null) {
                throw new EntityDoesNotExistsException(WebHook.class, notificationCode);
            }

            result = new WebHookDto(notif);
        } else {
            missingParameters.add("code");

            handleMissingParameters();
        }

        return result;
    }

    @Override
    public void remove(String notificationCode) throws MeveoApiException, BusinessException {
        if (!StringUtils.isBlank(notificationCode)) {
            WebHook webHook = webHookService.findByCode(notificationCode);

            if (webHook == null) {
                throw new EntityDoesNotExistsException(WebHook.class, notificationCode);
            }

            webHookService.remove(webHook);
        } else {
            missingParameters.add("code");

            handleMissingParameters();
        }
    }

	@Override
	public WebHookDto toDto(WebHook entity) {
		return new WebHookDto(entity);
	}
	
	@Override
	public WebHook fromDto(WebHookDto postData, WebHook webHook) throws MeveoApiException {
		if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getClassNameFilter())) {
            missingParameters.add("classNameFilter");
        }
        if (postData.getEventTypeFilter() == null) {
            missingParameters.add("eventTypeFilter");
        }
        if (StringUtils.isBlank(postData.getHost())) {
            missingParameters.add("host");
        }
        if (StringUtils.isBlank(postData.getPage())) {
            missingParameters.add("page");
        }
        if (postData.getHttpMethod() == null) {
            missingParameters.add("httpMethod");
        }

        handleMissingParameters();

        ScriptInstance scriptInstance = null;
        if (!StringUtils.isBlank(postData.getScriptInstanceCode())) {
            scriptInstance = scriptInstanceService.findByCode(postData.getScriptInstanceCode());
            if (scriptInstance == null) {
                throw new EntityDoesNotExistsException(ScriptInstance.class, postData.getScriptInstanceCode());
            }
        }

        // check class
        try {
            Class.forName(postData.getClassNameFilter());
        } catch (Exception e) {
            throw new InvalidParameterException("classNameFilter", postData.getClassNameFilter());
        }

        CounterTemplate counterTemplate = null;
        if (!StringUtils.isBlank(postData.getCounterTemplate())) {
            counterTemplate = counterTemplateService.findByCode(postData.getCounterTemplate());
            if (counterTemplate == null) {
                throw new EntityDoesNotExistsException(CounterTemplate.class, postData.getCounterTemplate());
            }
        }

        webHook.setCode(StringUtils.isBlank(postData.getUpdatedCode()) ? postData.getCode() : postData.getUpdatedCode());
        webHook.setClassNameFilter(postData.getClassNameFilter());
        webHook.setEventTypeFilter(postData.getEventTypeFilter());
        webHook.setFunction(scriptInstance);
        webHook.setParams(postData.getScriptParams());
        webHook.setElFilter(postData.getElFilter());
        webHook.setCounterTemplate(counterTemplate);

        if (!StringUtils.isBlank(postData.getHttpProtocol())) {
            webHook.setHttpProtocol(postData.getHttpProtocol());
        }
        webHook.setHost(postData.getHost());
        webHook.setPort(postData.getPort());
        webHook.setPage(postData.getPage());
        webHook.setHttpMethod(postData.getHttpMethod());
        webHook.setUsername(postData.getUsername());
        webHook.setPassword(postData.getPassword());
        if (postData.getHeaders() != null) {
            webHook.getHeaders().putAll(postData.getHeaders());
        }
        if (postData.getParams() != null) {
            webHook.getWebhookParams().putAll(postData.getParams());
        }
        
        return webHook;
	}

	@Override
	public WebHook fromDto(WebHookDto dto) throws MeveoApiException {
        ScriptInstance scriptInstance = null;
        if (!StringUtils.isBlank(dto.getScriptInstanceCode())) {
            scriptInstance = scriptInstanceService.findByCode(dto.getScriptInstanceCode());
            if (scriptInstance == null) {
                throw new EntityDoesNotExistsException(ScriptInstance.class, dto.getScriptInstanceCode());
            }
        }
        // check class
        try {
            Class.forName(dto.getClassNameFilter());
        } catch (Exception e) {
            throw new InvalidParameterException("classNameFilter", dto.getClassNameFilter());
        }

        CounterTemplate counterTemplate = null;
        if (!StringUtils.isBlank(dto.getCounterTemplate())) {
            counterTemplate = counterTemplateService.findByCode(dto.getCounterTemplate());
            if (counterTemplate == null) {
                throw new EntityDoesNotExistsException(CounterTemplate.class, dto.getCounterTemplate());
            }
        }

        WebHook webHook = new WebHook();
        webHook.setCode(dto.getCode());
        webHook.setClassNameFilter(dto.getClassNameFilter());
        webHook.setEventTypeFilter(dto.getEventTypeFilter());
        webHook.setFunction(scriptInstance);
        webHook.setParams(dto.getScriptParams());
        webHook.setElFilter(dto.getElFilter());
        webHook.setCounterTemplate(counterTemplate);

        if (!StringUtils.isBlank(dto.getHttpProtocol())) {
            webHook.setHttpProtocol(dto.getHttpProtocol());
        } else {
            webHook.setHttpProtocol(HttpProtocol.HTTP);
        }

        webHook.setHost(dto.getHost());
        webHook.setPort(dto.getPort());
        webHook.setPage(dto.getPage());
        webHook.setHttpMethod(dto.getHttpMethod());
        webHook.setUsername(dto.getUsername());
        webHook.setPassword(dto.getPassword());
        if (dto.getHeaders() != null) {
            webHook.getHeaders().putAll(dto.getHeaders());
        }
        if (dto.getParams() != null) {
            webHook.getWebhookParams().putAll(dto.getParams());
        }

		return webHook;
	}

	@Override
	public IPersistenceService<WebHook> getPersistenceService() {
		return webHookService;
	}
	
	@Override
	public void remove(WebHookDto dto) throws MeveoApiException, BusinessException {
		var entity = webHookService.findByCode(dto.getCode());
		if(entity != null) {
			webHookService.remove(entity);
		}
	}
}
