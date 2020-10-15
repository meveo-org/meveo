package org.meveo.model.crm.custom;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.meveo.model.crm.EntityReferenceWrapper;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.12.0
 */
public enum CustomFieldTypeEnum {
	
	/**
	 * Secret value, stored encrypted
	 */
	SECRET(false, true, String.class),
	
    /**
     * String value
     */
    STRING(false, true, String.class),

    /**
     * Date value
     */
    DATE(false, true, Instant.class),

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
	
	/**
	 * Regex type value
	 */
    EXPRESSION(false, true, String.class),
    
    /**
     * Boolean value
     */
    BOOLEAN(false, true, Boolean.class),
	
	 /**
     * EMBEDDED_ENTITY value
     */
    
	EMBEDDED_ENTITY(true, true, EntityReferenceWrapper.class),
	
	/**
	 * Binary type value - only the path is stored
	 */
	BINARY(true, true, String.class),

    /**
     * LONG_TEXT value
     */
    LONG_TEXT(false, true, String.class);

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

    /**
	 * Gets the label.
	 *
	 * @return the label
	 */
    public String getLabel() {
        return this.getClass().getSimpleName() + "." + this.name();
    }

    /**
	 * Checks if is is value stored in a serialized form in DB.
	 *
	 * @return the is value stored in a serialized form in DB
	 */
    public boolean isStoredSerialized() {
        return storedSerialized;
    }
    
    

    /**
	 * Checks if is is value stored in a serialized form in DB when using collections.
	 *
	 * @return the is value stored in a serialized form in DB when using collections
	 */
    public boolean isStoredSerializedList() {
		return storedSerializedList;
	}

	/**
	 * Gets the corresponding class to field type for conversion to json.
	 *
	 * @return the corresponding class to field type for conversion to json
	 */
	@SuppressWarnings("rawtypes")
    public Class getDataClass() {
        return dataClass;
    }
	
	/**
	 * Guess enum.
	 *
	 * @param enumType the enum type
	 * @return the custom field type enum
	 */
	public static CustomFieldTypeEnum guessEnum(String enumType) {
		Optional<CustomFieldTypeEnum> opt = Arrays.asList(CustomFieldTypeEnum.values())
				.stream()
				.filter(e -> e.name().toUpperCase().equals(enumType.toUpperCase()))
				.findFirst();
		
		if (opt.isPresent()) {
			return opt.get();
		}

		return CustomFieldTypeEnum.LONG;
	}
}