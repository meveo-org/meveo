package org.meveo.api.notification;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.notification.EmailNotificationDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.catalog.CounterTemplate;
import org.meveo.model.notification.EmailNotification;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.catalog.impl.CounterTemplateService;
import org.meveo.service.notification.EmailNotificationService;
import org.meveo.service.script.ScriptInstanceService;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 **/
@Stateless
public class EmailNotificationApi extends NotificationApi<EmailNotification, EmailNotificationDto> {

    public EmailNotificationApi() {
		super(EmailNotification.class, EmailNotificationDto.class);
	}

	@Inject
    private EmailNotificationService emailNotificationService;

    @Inject
    private CounterTemplateService counterTemplateService;

    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Override
    public EmailNotification create(EmailNotificationDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getClassNameFilter())) {
            missingParameters.add("classNameFilter");
        }
        if (postData.getEventTypeFilter() == null) {
            missingParameters.add("eventTypeFilter");
        }
        if (StringUtils.isBlank(postData.getEmailFrom())) {
            missingParameters.add("emailFrom");
        }
        if (StringUtils.isBlank(postData.getSubject())) {
            missingParameters.add("subject");
        }

        handleMissingParameters();

        return super.create(postData);
    }

    /* (non-Javadoc)
     * @see org.meveo.api.ApiService#find(java.lang.String)
     */
    @Override
    public EmailNotificationDto find(String notificationCode) throws MeveoApiException {
        EmailNotificationDto result = new EmailNotificationDto();

        if (!StringUtils.isBlank(notificationCode)) {
            EmailNotification notif = emailNotificationService.findByCode(notificationCode);

            if (notif == null) {
                throw new EntityDoesNotExistsException(EmailNotification.class, notificationCode);
            }

            result = new EmailNotificationDto(notif);
        } else {
            missingParameters.add("code");

            handleMissingParameters();
        }

        return result;
    }
    
    @Override
	public EmailNotification fromDto(EmailNotificationDto postData, EmailNotification notif) throws MeveoApiException {
    	if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getClassNameFilter())) {
            missingParameters.add("classNameFilter");
        }
        if (postData.getEventTypeFilter() == null) {
            missingParameters.add("eventTypeFilter");
        }
        if (StringUtils.isBlank(postData.getEmailFrom())) {
            missingParameters.add("emailFrom");
        }
        if (StringUtils.isBlank(postData.getSubject())) {
            missingParameters.add("subject");
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
        notif.setCode(StringUtils.isBlank(postData.getUpdatedCode()) ? postData.getCode() : postData.getUpdatedCode());
        notif.setClassNameFilter(postData.getClassNameFilter());
        notif.setEventTypeFilter(postData.getEventTypeFilter());
        notif.setFunction(scriptInstance);
        notif.setParams(postData.getScriptParams());
        notif.setElFilter(postData.getElFilter());
        notif.setCounterTemplate(counterTemplate);

        notif.setEmailFrom(postData.getEmailFrom());
        notif.setEmailToEl(postData.getEmailToEl());
        notif.setSubject(postData.getSubject());
        notif.setBody(postData.getBody());
        notif.setHtmlBody(postData.getHtmlBody());
        Set<String> emails = new HashSet<String>();
        for (String email : postData.getSendToMail()) {
            emails.add(email);
        }
        notif.setEmails(emails);

        return notif;
	}


    @Override
    public void remove(String notificationCode) throws MeveoApiException, BusinessException {
        if (!StringUtils.isBlank(notificationCode)) {
            EmailNotification notif = emailNotificationService.findByCode(notificationCode);

            if (notif == null) {
                throw new EntityDoesNotExistsException(EmailNotification.class, notificationCode);
            }

            emailNotificationService.remove(notif);
        } else {
            missingParameters.add("code");

            handleMissingParameters();
        }
    }

	@Override
	public EmailNotificationDto toDto(EmailNotification entity) {
		return new EmailNotificationDto(entity);
	}

	@Override
	public EmailNotification fromDto(EmailNotificationDto postData) throws MeveoApiException {
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
        
		EmailNotification notif = new EmailNotification();
        notif.setCode(postData.getCode());
        notif.setClassNameFilter(postData.getClassNameFilter());
        notif.setEventTypeFilter(postData.getEventTypeFilter());
        notif.setFunction(scriptInstance);
        notif.setParams(postData.getScriptParams());
        notif.setElFilter(postData.getElFilter());
        notif.setCounterTemplate(counterTemplate);

        notif.setEmailFrom(postData.getEmailFrom());
        notif.setEmailToEl(postData.getEmailToEl());
        notif.setSubject(postData.getSubject());
        notif.setBody(postData.getBody());
        notif.setHtmlBody(postData.getHtmlBody());

        Set<String> emails = new HashSet<String>();
        for (String email : postData.getSendToMail()) {
            emails.add(email);
        }
        notif.setEmails(emails);
        
        return notif;
	}

	@Override
	public IPersistenceService<EmailNotification> getPersistenceService() {
		return emailNotificationService;
	}

	@Override
	public void remove(EmailNotificationDto dto) throws MeveoApiException, BusinessException {
		var entity = emailNotificationService.findByCode(dto.getCode());
		if(entity != null) {
			emailNotificationService.remove(entity);
		}
	}
}
