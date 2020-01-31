/**
 * 
 */
package org.meveo.model.persistence;

import java.util.Map;

import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.typereferences.GenericTypeReferences;

/**
 * Utilitary class for manipulating {@link CustomEntityInstance}
 * 
 * @author clement.bareth
 * @since 6.8.0
 * @version 6.8.0
 */
public class CEIUtils {
	
	/**
	 * Converts a POJO to a CEI
	 * 
	 * @param pojo to convert
	 * @return converted CEI
	 */
	public static CustomEntityInstance pojoToCei(Object pojo) {
		Map<String, Object> pojoAsMap = JacksonUtil.convert(pojo, GenericTypeReferences.MAP_STRING_OBJECT);
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setUuid((String) pojoAsMap.get("uuid"));
		cei.setCetCode(pojo.getClass().getSimpleName());
		CustomFieldValues customFieldValues = new CustomFieldValues();
		pojoAsMap.forEach(customFieldValues::setValue);
		cei.setCfValues(customFieldValues);
		return cei;
	}
	
	/**
	 * Converts a CEI to a POJO
	 * 
	 * @param <T> type of the pojo
	 * @param cei the cei to convert
	 * @param pojoClass the class of the pojo
	 * @return the instance of the POJO
	 */
	public static <T> T ceiToPojo(CustomEntityInstance cei, Class<T> pojoClass) {
		Map<String, Object> pojoValues = cei.getCfValuesAsValues();
		pojoValues.put("uuid", cei.getUuid());
		return JacksonUtil.convert(pojoValues, pojoClass);
	}

}
