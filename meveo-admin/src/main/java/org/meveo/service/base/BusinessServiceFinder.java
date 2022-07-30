/**
 * 
 */
package org.meveo.service.base;

import org.meveo.model.BusinessEntity;

/**
 * 
 * @author arthur.grenier
 * @since 6.14.0
 * @version 6.14.0
 */
public interface BusinessServiceFinder {
	
	@SuppressWarnings("rawtypes")
	BusinessService find(BusinessEntity businessEntity);
}
