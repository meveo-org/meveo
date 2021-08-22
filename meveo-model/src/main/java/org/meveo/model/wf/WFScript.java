/**
 * 
 */
package org.meveo.model.wf;

import java.util.Map;

import org.meveo.model.customEntities.CustomEntityInstance;

public interface WFScript {
	
	default CustomEntityInstance getCustomEntityInstance(Map<String, Object> parameters) {
		Object entity = parameters.get("entity");
		if(entity instanceof CustomEntityInstance) {
			return (CustomEntityInstance) entity;
		} else {
			return null;
		}
	}
	
	Object getResult();
}
