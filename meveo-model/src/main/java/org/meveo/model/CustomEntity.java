/**
 * 
 */
package org.meveo.model;

/**
 * Interface that the classes generated for each CET implements
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
public interface CustomEntity {
	
	/** @return the code of the corresponding CET */
	String getCetCode();
	
	/** @return the uuid of the entity */
	String getUuid();
	
	default boolean isEqual(CustomEntity other) {
		return this.equals(other);
	}
}
