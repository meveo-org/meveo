/**
 * 
 */
package org.meveo.model.wf;

import org.meveo.model.BusinessEntity;

public interface WFScript {
	
	void setEntity(BusinessEntity entity);
	
	Object getResult();
}
