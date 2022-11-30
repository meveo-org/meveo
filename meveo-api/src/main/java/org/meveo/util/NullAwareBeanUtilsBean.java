package org.meveo.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.expression.Resolver;
import org.meveo.api.ApiUtils;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.CustomFieldsDto;
import org.meveo.model.BusinessEntity;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.service.base.BusinessService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author Clément Bareth
 * @version 7.2.0
 * @since 6.10.0
 */
public class NullAwareBeanUtilsBean extends BeanUtilsBean {

	@SuppressWarnings("rawtypes")
	@Override
	public void copyProperty(Object dest, String name, Object value) throws IllegalAccessException, InvocationTargetException {
		if (value == null)
			return;
		
		if (value instanceof CustomFieldsDto || value instanceof CustomFieldValues)
			return;
		
		if (value instanceof String) {
	        try {
	        	Object target = dest;
	            final Resolver resolver = getPropertyUtils().getResolver();
	            while (resolver.hasNested(name)) {
	                try {
	                    target = getPropertyUtils().getProperty(target, resolver.next(name));
	                    name = resolver.remove(name);
	                } catch (final NoSuchMethodException e) {
	                    return; // Skip this property setter
	                }
	            }
	            
	        	PropertyDescriptor descriptor = getPropertyUtils().getPropertyDescriptor(target, name);
	            if (descriptor != null) {
	            	var type = descriptor.getPropertyType();
	            	if (BusinessEntity.class.isAssignableFrom(type)) {
	            		var api = ApiUtils.getApiService(type, true);
	            		if (api instanceof BaseCrudApi) {
	            			var ser = ((BaseCrudApi) api).getPersistenceService();
	            			if (ser instanceof BusinessService<?>) {
	            				var entityService = (BusinessService) ser;
	            				value = entityService.findByCode((String) value);
	            			}
	            		}
	            	}
	            }
	        } catch (final NoSuchMethodException e) {}
		}
		
		try {
			 super.copyProperty(dest, name, value);
		} catch (Exception e) {
			// NOOP
		}
	}
}
