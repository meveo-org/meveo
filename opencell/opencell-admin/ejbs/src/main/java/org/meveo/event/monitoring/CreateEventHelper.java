package org.meveo.event.monitoring;

import org.meveo.commons.utils.ParamBean;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Date;

@Stateless
public class CreateEventHelper {
	
	@Inject
	private Event<BusinessExceptionEvent> event;
	
	public void  register(Exception e){
		BusinessExceptionEvent bee = new BusinessExceptionEvent();
		bee.setException(e);
		bee.setDateTime(new Date());
		bee.setMeveoInstanceCode(ParamBean.getInstance().getProperty("monitoring.instanceCode",""));
		if("true".equals(ParamBean.getInstance().getProperty("monitoring.sendException", "true"))){
			event.fire(bee);
		}
	}
}
