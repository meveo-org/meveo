package org.meveo.service.custom;

import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;

import java.util.Arrays;

public class CustomEntityTemplateUtils {
	
	static final String PRIMITIVE_CFT_VALUE = "value";

	public static void turnIntoPrimitive(CustomEntityTemplate cet, CustomFieldTemplate customFieldTemplate) {
	    customFieldTemplate.setActive(true);                        // Always active
	    customFieldTemplate.setAllowEdit(false);                    // CFT can't be updated
	    customFieldTemplate.setAppliesTo(cet.getAppliesTo());
	    if (cet.getNeo4JStorageConfiguration().getPrimitiveType() == null) {
	        throw new IllegalArgumentException("Primitive type class must be provided");
	    }
	    customFieldTemplate.setFieldType(cet.getNeo4JStorageConfiguration().getPrimitiveType().getCftType());
	    customFieldTemplate.setUnique(true);                        // Must be unique
	    customFieldTemplate.setCode(CustomEntityTemplateUtils.PRIMITIVE_CFT_VALUE);            // Code is 'value'
	    customFieldTemplate.setDescription(CustomEntityTemplateUtils.PRIMITIVE_CFT_VALUE);    // Label is 'value'
	    customFieldTemplate.setFilter(true);                        // Can be used as filter
	    customFieldTemplate.setValueRequired(true);                    // Always required
	    customFieldTemplate.setStorageType(CustomFieldStorageTypeEnum.SINGLE);
	    customFieldTemplate.setStorages(Arrays.asList(DBStorageType.NEO4J));
		customFieldTemplate.setMaxValue(cet.getNeo4JStorageConfiguration().getMaxValue());
	}

}
