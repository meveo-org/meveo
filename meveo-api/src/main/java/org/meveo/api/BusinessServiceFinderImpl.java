/**
 * 
 */
package org.meveo.api;

import javax.inject.Inject;

import org.meveo.model.BusinessEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.BusinessServiceFinder;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.EntityCustomActionService;

/**
 * Implementation of {@link BusinessServiceFinder}
 * 
 * @author arthur.grenier
 * @since 6.14.0
 * @version 6.14.0
 */
public class BusinessServiceFinderImpl implements BusinessServiceFinder{
	
	@Inject
	private EntityCustomActionService customEntityActionService;
	
	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	@SuppressWarnings("rawtypes")
	@Override
	public BusinessService find(BusinessEntity businessEntity) {
		BusinessService entityService = null;
		
		if(businessEntity instanceof EntityCustomAction) {
			return customEntityActionService;
		}
		
		if (businessEntity instanceof CustomFieldTemplate) {
			return customFieldTemplateService;
		}
		
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
