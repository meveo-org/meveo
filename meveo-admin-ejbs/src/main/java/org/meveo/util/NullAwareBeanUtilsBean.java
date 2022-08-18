package org.meveo.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.meveo.api.dto.CustomFieldsDto;
import org.meveo.model.crm.custom.CustomFieldValues;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10.0
 */
public class NullAwareBeanUtilsBean extends BeanUtilsBean {

	@Override
	public void copyProperty(Object dest, String name, Object value) throws IllegalAccessException, InvocationTargetException {
		if (value == null)
			return;
		
		if (value instanceof CustomFieldsDto || value instanceof CustomFieldValues)
			return;
		
		super.copyProperty(dest, name, value);
	}
}
