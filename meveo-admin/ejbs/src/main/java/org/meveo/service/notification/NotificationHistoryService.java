package org.meveo.service.notification;

import javax.ejb.Stateless;
import javax.persistence.EntityNotFoundException;

import org.meveo.admin.exception.BusinessException;
import org.meveo.event.IEvent;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.IEntity;
import org.meveo.model.notification.Notification;
import org.meveo.model.notification.NotificationHistory;
import org.meveo.model.notification.NotificationHistoryStatusEnum;
import org.meveo.service.base.PersistenceService;

/**
 * @author phung
 *
 */
@Stateless
public class NotificationHistoryService extends PersistenceService<NotificationHistory> {
	
    /**
     * @param notification notification which will put on history
     * @param entityOrEvent entity or event
     * @param result result of notification
     * @param status status of notification history status.
     * @return notification history
     * @throws BusinessException business exception.
     */
    public NotificationHistory create(Notification notification, Object entityOrEvent, String result, NotificationHistoryStatusEnum status) throws BusinessException {
        IEntity<?> entity = null;

        if (entityOrEvent instanceof IEntity) {
            entity = (IEntity<?>) entityOrEvent;
        } else if (entityOrEvent instanceof IEvent) {
            entity = ((IEvent) entityOrEvent).getEntity();
        }
        
        
        // Get reference to the notification in database
        try {
        	notification = getEntityManager().getReference(Notification.class, notification.getId());
        } catch (EntityNotFoundException e) {
        	throw new EntityDoesNotExistsException(notification);
        }

        NotificationHistory history = new NotificationHistory();
        history.setNotification(notification);
        history.setEntityClassName(entityOrEvent.getClass().getName());
        history.setResult(result);
        history.setStatus(status);

        if(entity != null) {
            history.setSerializedEntity(entity.getId() == null ? entity.toString() : entity.getId().toString());
        } else {
            history.setSerializedEntity(entityOrEvent.toString());
        }

        create(history);

        return history;

    }
}