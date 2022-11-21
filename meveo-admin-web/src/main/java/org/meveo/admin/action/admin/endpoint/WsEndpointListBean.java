package org.meveo.admin.action.admin.endpoint;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since
 */
@Named
@ConversationScoped
public class WsEndpointListBean extends WsEndpointBean {

    @PostConstruct
	@Override
    public void init() {
		super.init();
        this.filters.put("moduleBelonging", this.getUserCurrentModule());
    }
}
