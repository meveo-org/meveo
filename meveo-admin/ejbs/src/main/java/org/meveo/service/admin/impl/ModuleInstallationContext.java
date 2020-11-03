/**
 * 
 */
package org.meveo.service.admin.impl;

import javax.enterprise.context.RequestScoped;

/**
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
@RequestScoped
public class ModuleInstallationContext {

	private boolean active;
	
	/**
	 * @return whether a module is being installed
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Declares the installation of a module
	 */
	public void begin() {
		active = true;
	}
	
	/**
	 * Declares the end of a module installation
	 */
	public void end() {
		active = false;
	}
}
