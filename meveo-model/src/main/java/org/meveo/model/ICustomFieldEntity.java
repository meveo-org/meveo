package org.meveo.model;

import org.meveo.model.crm.custom.CustomFieldValues;

import java.util.Map;

/**
 * An entity that contains custom fields
 * 
 * @author Andrius Karpavicius
 * 
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
     * Get an array of parent custom field entity in case custom field values should be inherited from a parent entity.
     * 
     * @return An entity
     */
    ICustomFieldEntity[] getParentCFEntities();
    
    CustomFieldValues getCfValues();
    
    CustomFieldValues getCfValuesNullSafe();
    
    void clearCfValues();

    /**
     * Get custom field values (not CF value entity). In case of versioned values (more than one entry in CF value list) a CF value corresponding to today will be returned
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
}