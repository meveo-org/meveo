package org.meveo.service.crm.impl;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.meveo.model.CustomFieldEntity;
import org.meveo.model.ICustomFieldEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomFieldTemplateUtils {

	/**
	 * Calculate custom field template AppliesTo value for a given entity. AppliesTo consist of a prefix and optionally one or more entity fields. e.g. JOB_jobTemplate
	 * 
	 * @param entity Entity
	 * @return A appliesTo value
	 * @throws CustomFieldException An exception when AppliesTo value can not be calculated. Occurs when value that is part of CFT.AppliesTo calculation is not set yet on entity
	 */
	public static String calculateAppliesToValue(ICustomFieldEntity entity) throws CustomFieldException {
		if(entity == null) {
			return null;
		}
		
	    CustomFieldEntity cfeAnnotation = entity.getClass().getAnnotation(CustomFieldEntity.class);
	
	    String appliesTo = cfeAnnotation.cftCodePrefix();
	    if (cfeAnnotation.cftCodeFields().length > 0) {
	        for (String fieldName : cfeAnnotation.cftCodeFields()) {
	            try {
	                Object fieldValue = FieldUtils.getField(entity.getClass(), fieldName, true).get(entity);
	                if (fieldValue == null) {
	                    throw new CustomFieldException("Can not calculate AppliesTo value");
	                }
	                appliesTo = appliesTo + "_" + fieldValue;
	            } catch (IllegalArgumentException | IllegalAccessException e) {
	                Logger log = LoggerFactory.getLogger(CustomFieldTemplateService.class);
	                log.error("Unable to access field {}.{}", entity.getClass().getSimpleName(), fieldName);
	                throw new RuntimeException("Unable to access field " + entity.getClass().getSimpleName() + "." + fieldName);
	            }
	        }
	    }
	    return appliesTo;
	}

}
