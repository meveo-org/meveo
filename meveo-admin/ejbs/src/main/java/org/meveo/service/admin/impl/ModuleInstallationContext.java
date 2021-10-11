/**
 * 
 */
package org.meveo.service.admin.impl;

import javax.enterprise.context.RequestScoped;

import org.meveo.model.module.MeveoModule;

/**
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
@RequestScoped
public class ModuleInstallationContext {

	private String ModuleCodeInstallation = "";
	
	private boolean active;
	
	/**
	 * @return whether a module is being installed
	 */
	public boolean isActive() {
		return active;
	}
	
	public String getModuleCodeInstallation() {
		return this.ModuleCodeInstallation;
	}
	
	/**
	 * Declares the installation of a module
	 * @param module in the process of being installed
	 */
	public void begin(MeveoModule module) {
		active = true;
		this.ModuleCodeInstallation = module.getCode();
	}
	
	/**
	 * Declares the end of a module installation
	 */
	public void end() {
		active = false;
		this.ModuleCodeInstallation = "";
	}
}
