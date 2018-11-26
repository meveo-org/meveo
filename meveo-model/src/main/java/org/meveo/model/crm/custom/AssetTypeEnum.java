package org.meveo.model.crm.custom;

import java.util.Date;
import java.util.Map;

import org.meveo.model.crm.EntityReferenceWrapper;

public enum AssetTypeEnum {

	 /**
     * String value
     */
    STRING(String.class),

    /**
     * Date value
     */
    DATE(Date.class),

    /**
     * Long value
     */
    LONG(Long.class),

    /**
     * Double value
     */
    DOUBLE(Double.class);


    /**
     * Corresponding class to field type for conversion to json
     */
    @SuppressWarnings("rawtypes")
    private Class dataClass;

    AssetTypeEnum(@SuppressWarnings("rawtypes") Class dataClass) {
        this.dataClass = dataClass;
    }

    public String getLabel() {
        return this.getClass().getSimpleName() + "." + this.name();
    }

    @SuppressWarnings("rawtypes")
    public Class getDataClass() {
        return dataClass;
    }
}
