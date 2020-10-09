package org.meveo.service.notification;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.cache.NotificationCacheContainerProvider;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.notification.Notification;
import org.meveo.model.notification.ScriptNotification;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @author akadid abdelmounaim
 * @version 6.10.0
 */
@Stateless
public class ScriptNotificationService extends NotificationInstanceService<ScriptNotification> {

	@SuppressWarnings("unchecked")
	public List<Notification> listAll() {
		QueryBuilder qb = new QueryBuilder(Notification.class, "d");
		qb.addBooleanCriterion("disabled", false);
		return qb.getQuery(getEntityManager()).getResultList();
	}

	/**
	 * Get a list of notifications to populate a cache
	 * 
	 * @return A list of active notifications
	 */
	public List<Notification> getNotificationsForCache() {
		return getEntityManager().createNamedQuery("Notification.getNotificationsForCache", Notification.class)
				.getResultList();
	}

	@Inject
	private NotificationCacheContainerProvider notificationCacheContainerProvider;

	/**
	 * Update scriptNotification v5.0: adding notification to cache only when
	 * notification is active
	 * 
	 * @param scriptNotification scriptNotification
	 * @return scriptNotification scriptNotification
	 * @author akadid abdelmounaim
	 * @lastModifiedVersion 5.0
	 */
	@Override
	public ScriptNotification update(ScriptNotification scriptNotification) throws BusinessException {
		scriptNotification = super.update(scriptNotification);
		notificationCacheContainerProvider.removeNotificationFromCache(scriptNotification);
		if (scriptNotification.isActive()) {
			notificationCacheContainerProvider.addNotificationToCache(scriptNotification);
		}
		return scriptNotification;
	}
}
