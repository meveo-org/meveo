package org.meveo.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.persistence.DBStorageType;


/**
 * An entity that contains custom fields
 * 
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.4.0
 */
public interface ICustomFieldEntity {

	/**
	 * Get unique identifier.
	 *
	 * @return uuid
	 */
	String getUuid();

	/**
	 * Set a new UUID value.
	 *
	 * @return Old UUID value
	 */
	String clearUuid();

	/**
	 * Get an array of parent custom field entity in case custom field values should
	 * be inherited from a parent entity.
	 *
	 * @return An entity
	 */
	ICustomFieldEntity[] getParentCFEntities();

	CustomFieldValues getCfValues();

	CustomFieldValues getCfValuesNullSafe();

	void clearCfValues();

	/**
	 * Get custom field values (not CF value entity). In case of versioned values
	 * (more than one entry in CF value list) a CF value corresponding to today will
	 * be returned
	 *
	 * @return A map of values with key being custom field code.
	 */
	default Map<String, Object> getCfValuesAsValues() {
		CustomFieldValues cfValues = getCfValues();
		if (cfValues != null && cfValues.getValuesByCode() != null) {
			return cfValues.getValues();
		}
		return null;
	}

	/**
	 * Retrieves the custom field values.
	 *
	 * @param filterType Storage type filtering. Fields must be able to stored on an SQL database.
	 * @param cfts collection of {@link CustomFieldTemplate}
	 * @param removeNullValues remove null values if true
	 * @return map of values with key being custom field code.
	 */
	default Map<String, Object> getCfValuesAsValues(DBStorageType filterType, Collection<CustomFieldTemplate> cfts, boolean removeNullValues) {
		if(cfts == null) {
			throw new IllegalArgumentException("Argument 'cfts' can't be null");
		}
		
		CustomFieldValues cfValues = getCfValues();

		if (cfValues == null || cfValues.getValuesByCode() == null) {
			return null;
		}

		Map<String, Object> mapValues = cfValues.getValues();

		Map<String, Object> returnedMap = mapValues.entrySet().stream().filter(entry -> {

			if (entry.getKey().equals("uuid")) {
				return true;
			}

			if (entry.getValue() == null && removeNullValues) {
				return false;
			}

			Optional<CustomFieldTemplate> customFieldTemplateOpt = getCustomFieldTemplate(cfts, entry);

			if(filterType != null){
				return customFieldTemplateOpt.map(customFieldTemplate -> customFieldTemplate.getStoragesNullSafe().contains(filterType))
						.orElse(false);
			} else {
				return true;
			}

		}).collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);

		returnedMap.putIfAbsent("uuid", this.getUuid());

		return returnedMap;
	}

	default Optional<CustomFieldTemplate> getCustomFieldTemplate(Collection<CustomFieldTemplate> cfts, Entry<String, Object> entry) {
		return cfts.stream()
				.filter(f -> f.getCode().equals(entry.getKey()) || (f.getDbFieldname() != null && f.getDbFieldname().equals(entry.getKey())))
				.findFirst();
	}

    default Map<String, Object> getValuesNullSafe() {
        CustomFieldValues cfValues = getCfValues();
        if (cfValues != null && cfValues.getValuesByCode() != null) {
            return cfValues.getValues();
        }
        return new HashMap<>();
    }

}