package org.meveo.model.crm.custom;

import java.time.Instant;

public enum PrimitiveTypeEnum {

	 /**
     * String value
     */
    STRING(String.class, CustomFieldTypeEnum.STRING),

    /**
     * Date value
     */
    DATE(Instant.class, CustomFieldTypeEnum.DATE),

    /**
     * Long value
     */
    LONG(Long.class, CustomFieldTypeEnum.LONG),

    /**
     * Double value
     */
    DOUBLE(Double.class, CustomFieldTypeEnum.DOUBLE);


    /**
     * Corresponding class to field type for conversion to json
     */
    @SuppressWarnings("rawtypes")
    private final Class dataClass;
	private final CustomFieldTypeEnum cftType;

    PrimitiveTypeEnum(@SuppressWarnings("rawtypes") Class dataClass, CustomFieldTypeEnum matchingCftType) {
        this.dataClass = dataClass;
        this.cftType = matchingCftType;
    }

    public String getLabel() {
        return this.getClass().getSimpleName() + "." + this.name();
    }

    @SuppressWarnings("rawtypes")
    public Class getDataClass() {
        return dataClass;
    }

	public CustomFieldTypeEnum getCftType() {
		return cftType;
	}
    
}
