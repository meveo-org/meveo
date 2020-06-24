package org.meveo.api.notification;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.notification.WebNotificationDto;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.notification.WebNotification;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.notification.WebNotificationService;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class WebNotificationApi extends BaseCrudApi<WebNotification, WebNotificationDto> {


    @Inject
    WebNotificationService webNotificationService;

    public WebNotificationApi(){ super(WebNotification.class,WebNotificationDto.class);}

    @Override
    public WebNotificationDto toDto(WebNotification entity) {
        return new WebNotificationDto(entity);
    }

    @Override
    public WebNotification fromDto(WebNotificationDto dto) throws EntityDoesNotExistsException {
        WebNotification result = new WebNotification();
        result.setDataEL(dto.getDataEL());
        //TODO: finish this class

        return result;
    }

    @Override
    public IPersistenceService<WebNotification> getPersistenceService() {
        return null;
    }

    @Override
    public boolean exists(WebNotificationDto dto) {
        return false;
    }

    @Override
    public WebNotificationDto find(String code) throws org.meveo.api.exception.EntityDoesNotExistsException, MissingParameterException, InvalidParameterException, MeveoApiException, EntityDoesNotExistsException {
        return null;
    }

    @Override
    public WebNotification createOrUpdate(WebNotificationDto dtoData) throws MeveoApiException, BusinessException {
        return null;
    }
}
