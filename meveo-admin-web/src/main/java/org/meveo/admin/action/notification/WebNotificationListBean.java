package org.meveo.admin.action.notification;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
@Named
@ConversationScoped
public class WebNotificationListBean extends WebNotificationBean {

	private static final long serialVersionUID = -7160105377348715515L;

	@PostConstruct
	@Override
    public void init() {
		super.init();
        this.filters.put("moduleBelonging", this.getUserCurrentModule());
    }

}
