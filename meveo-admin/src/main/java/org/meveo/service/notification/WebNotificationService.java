package org.meveo.service.notification;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.notification.WebNotification;
import org.meveo.service.communication.impl.SseManager;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class WebNotificationService extends NotificationInstanceService<WebNotification> {

    @Inject
    private SseManager sseManager;

    @Override
    public void remove(WebNotification entity) throws BusinessException {
        try {
            sseManager.removeNotification(entity.getCode());
        } catch(Exception e){
            log.warn("error while removing the SSE clients of web notification "+entity.getCode()+" message:"+e.getMessage());
        }
        super.remove(entity);
    }
}
