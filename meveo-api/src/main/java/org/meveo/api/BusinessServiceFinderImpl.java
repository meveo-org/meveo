/**
 * 
 */
package org.meveo.api;

import org.meveo.model.BusinessEntity;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.BusinessServiceFinder;

/**
 * Implementation of {@link BusinessServiceFinder}
 * 
 * @author arthur.grenier
 * @since 6.14.0
 * @version 6.14.0
 */
public class BusinessServiceFinderImpl implements BusinessServiceFinder{

	@SuppressWarnings("rawtypes")
	@Override
	public BusinessService find(BusinessEntity businessEntity) {
		BusinessService entityService = null;
		
		var api = ApiUtils.getApiService(businessEntity.getClass(), true);
		if (api instanceof BaseCrudApi) {
			var ser = ((BaseCrudApi) api).getPersistenceService();
			if (ser instanceof BusinessService<?>) {
				entityService = (BusinessService) ser;
			}
		}
		return entityService;
	}
}
