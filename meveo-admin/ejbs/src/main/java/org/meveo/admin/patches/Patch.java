/**
 * 
 */
package org.meveo.admin.patches;

/**
 * Represent a patch that should be ran at meveo startup
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
public interface Patch {
	
	/**
	 * Execute the patch.
	 * 
	 * @throws Exception if the patch execution failed
	 */
	void execute() throws Exception;
	
	/** @return Execution order */
	int order();
	
	/** @return Unique patch name */
	String name();
	
}
