package org.meveo.model.crm.custom;

import java.util.Date;
import java.util.Map;

import org.meveo.model.crm.EntityReferenceWrapper;

public enum CustomFieldTypeEnum {
    /**
     * String value
     */
    STRING(false, true, String.class),

    /**
     * Date value
     */
    DATE(false, true, Date.class),

    /**
     * Long value
     */
    LONG(false, true, Long.class),

    /**
     * Double value
     */
    DOUBLE(false, true, Double.class),

    /**
     * String value picked from a list of values
     */
    LIST(false, true, String.class),

    /**
     * A reference to an entity
     */
    ENTITY(false, false, EntityReferenceWrapper.class),

    /**
     * A long string value
     */
    TEXT_AREA(false, true, String.class),

    /**
     * An embedded entity data
     */
    CHILD_ENTITY(true, true, EntityReferenceWrapper.class),

    /**
     * Multi value (map) type value
     */
    MULTI_VALUE(true, true, Map.class),
	
	
    EXPRESSION(false, true, String.class),
    
    /**
     * Boolean value
     */
    BOOLEAN(false, true, Boolean.class),
	
	 /**
     * EMBEDDED_ENTITY value
     */
    
	EMBEDDED_ENTITY(true, true, EntityReferenceWrapper.class),
	
	BINARY(true, true, String.class);

    /**
     * Is value stored in a serialized form in DB
     */
    private boolean storedSerialized;
    
    /**
     * Is value stored in a serialized form in DB when using collections
     */
    private boolean storedSerializedList;

    /**
     * Corresponding class to field type for conversion to json
     */
    @SuppressWarnings("rawtypes")
    private Class dataClass;

    CustomFieldTypeEnum(boolean storedSerialized, boolean storedSerializedList, @SuppressWarnings("rawtypes") Class dataClass) {
        this.storedSerialized = storedSerialized;
        this.dataClass = dataClass;
        this.storedSerializedList = storedSerializedList ;
    }

    public String getLabel() {
        return this.getClass().getSimpleName() + "." + this.name();
    }

    public boolean isStoredSerialized() {
        return storedSerialized;
    }
    
    

    public boolean isStoredSerializedList() {
		return storedSerializedList;
	}

	@SuppressWarnings("rawtypes")
    public Class getDataClass() {
        return dataClass;
    }
}