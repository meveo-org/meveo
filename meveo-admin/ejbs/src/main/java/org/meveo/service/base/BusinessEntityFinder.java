/**
 * 
 */
package org.meveo.service.base;

import org.meveo.model.BusinessEntity;

/**
 * 
 * @author arthur.grenier
 * @since
 * @version
 */
public interface BusinessEntityFinder {
	
	@SuppressWarnings("rawtypes")
	BusinessService find(BusinessEntity businessEntity);
}
