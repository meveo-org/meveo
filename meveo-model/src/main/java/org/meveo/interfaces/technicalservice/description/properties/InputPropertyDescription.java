package org.meveo.interfaces.technicalservice.description.properties;


public interface InputPropertyDescription extends PropertyDescription{

    /**
     * If property is not required, default value to give to the property.
     *
     * @return The default value.
     */
    String getDefaultValue();
}
