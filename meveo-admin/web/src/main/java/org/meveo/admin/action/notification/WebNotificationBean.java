package org.meveo.admin.action.notification;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.model.notification.WebNotification;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.notification.WebNotificationService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
@Named
@ViewScoped
public class WebNotificationBean extends BaseNotificationBean<WebNotification> {

	private static final long serialVersionUID = -7254100274979366778L;

	@Inject
	private WebNotificationService webNotificationService;

	public WebNotificationBean() {
		super(WebNotification.class);
	}

	@Override
	protected IPersistenceService<WebNotification> getPersistenceService() {
		return webNotificationService;
	}

}
