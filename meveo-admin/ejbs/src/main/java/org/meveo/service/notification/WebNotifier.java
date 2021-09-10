package org.meveo.service.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.elresolver.ELException;
import org.meveo.model.notification.NotificationHistoryStatusEnum;
import org.meveo.model.notification.WebNotification;
import org.meveo.model.notification.WebNotificationIdStrategyEnum;
import org.meveo.security.MeveoUser;
import org.meveo.security.keycloak.CurrentUserProvider;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.communication.impl.SseManager;
import org.meveo.service.communication.impl.WebsocketNotifManager;
import org.slf4j.Logger;

@Stateless
class WebNotifier {

	@Inject
	private Logger log;

	@Inject
	private SseManager sseManager;

	@Inject
	private WebsocketNotifManager websocketManager;
	
	@Inject
	private NotificationHistoryService notificationHistoryService;

	@Inject
	private CurrentUserProvider currentUserProvider;

	private String evaluate(String expression, Object entityOrEvent, Map<String, Object> context) throws ELException {
		HashMap<Object, Object> userMap = new HashMap<>();
		userMap.put("event", entityOrEvent);
		userMap.put("context", context);
		return (String) MeveoValueExpressionWrapper.evaluateExpression(expression, userMap, String.class);
	}

	void sendMessage(WebNotification webNotif, Object entityOrEvent, Map<String, Object> context,
			MeveoUser lastCurrentUser) {

		currentUserProvider.reestablishAuthentication(lastCurrentUser);

		Map<Object, Object> oContext = new HashMap<>(context);
		oContext.put("event", entityOrEvent);

		log.debug("WebNotification sendMessage");

		String id = UUID.randomUUID().toString();
		if (webNotif.getIdStrategy() == WebNotificationIdStrategyEnum.TIMESTAMP) {
			id = "" + System.currentTimeMillis();
		}
		String data=entityOrEvent.toString();
		//send SSE notif
		try {
			if (webNotif.getDataEL() != null && !webNotif.getDataEL().isEmpty()) {
				data = evaluate(webNotif.getDataEL(), entityOrEvent, context);
				log.debug("Evaluated dataEL_evaluated={}", data);
				sseManager.sendMessage(id, webNotif.getCode(), webNotif.getDescription(), data, oContext);
			} else {
				// FIXME: serialize correctly entityOrEvent
				sseManager.sendMessage(id, webNotif.getCode(), webNotif.getDescription(), data,
						oContext);
			}
		} catch (Exception e) {
			try {
				log.debug("WebNotification business error : ", e);
				notificationHistoryService.create(webNotif, entityOrEvent, e.getMessage(),
						NotificationHistoryStatusEnum.FAILED);

			} catch (BusinessException e2) {
				log.error("Failed to create notification history", e2);
			}
		}
		// then websocket notif
		try {
				websocketManager.sendMessage(id, webNotif.getCode(), data,
						oContext);

		} catch (Exception e) {
			try {
				log.debug("WebNotification business error : ", e);
				notificationHistoryService.create(webNotif, entityOrEvent, e.getMessage(),
						NotificationHistoryStatusEnum.FAILED);

			} catch (BusinessException e2) {
				log.error("Failed to create notification history", e2);
			}
		}
	}
}
