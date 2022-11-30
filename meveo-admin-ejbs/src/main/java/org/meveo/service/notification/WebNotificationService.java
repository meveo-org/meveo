package org.meveo.service.notification;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.notification.WebNotification;
import org.meveo.service.communication.impl.SseManager;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class WebNotificationService extends NotificationInstanceService<WebNotification> {

    private static Logger log = LoggerFactory.getLogger(WebNotificationService.class);

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

	@Override
	public Logger getLogger() {
		return log;
	}
}
