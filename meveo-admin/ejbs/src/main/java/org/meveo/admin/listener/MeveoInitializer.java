/**
 * 
 */
package org.meveo.admin.listener;

/**
 * Interface indicating that a bean should be called after application startup
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
public interface MeveoInitializer {
	
	/**
	 * Operation to do after startup
	 * 
	 * @throws Exception if error occurs
	 */
	void init() throws Exception;

}
