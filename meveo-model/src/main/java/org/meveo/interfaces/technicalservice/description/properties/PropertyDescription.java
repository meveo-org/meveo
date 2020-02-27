package org.meveo.interfaces.technicalservice.description.properties;

/**
 * PropertyDescription interface
 * 
 * @author clement.bareth
 * @since 6.0.0
 * @version 6.8.0
 */
public interface PropertyDescription {

    /**
     * @return the target property of the description
     */
    String getProperty();
    
    /**
	 * Checks if is inherited.
	 *
	 * @return true, if is inherited
	 */
    boolean isInherited();
}
