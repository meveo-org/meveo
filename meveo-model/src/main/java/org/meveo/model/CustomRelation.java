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
public interface CustomRelation {
	
	/** @return the code of the corresponding CET */
	String getCrtCode();
	
	/** @return the uuid of the entity */
	String getUuid();
}
