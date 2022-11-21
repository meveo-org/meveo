package org.meveo.services.job;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

@Named
@ConversationScoped
public class TimerEntityListBean extends TimerEntityBean {

    /**
     * 
     */
    private static final long serialVersionUID = 291083155570451308L;

    @PostConstruct
	@Override
    public void init() {
		super.init();
        this.filters.put("moduleBelonging", this.getUserCurrentModule());
    }

}
