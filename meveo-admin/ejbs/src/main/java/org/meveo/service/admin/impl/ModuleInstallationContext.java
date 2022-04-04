/**
 * 
 */
package org.meveo.service.admin.impl;

import java.util.List;

import javax.enterprise.context.RequestScoped;

import org.meveo.model.module.MeveoModule;
import org.meveo.model.storage.Repository;

/**
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
@RequestScoped
public class ModuleInstallationContext {

	private String ModuleCodeInstallation = "";
	private List<Repository> repositories;
	private MeveoModule module;
	
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
	 * @return the {@link #module}
	 */
	public MeveoModule getModule() {
		return module;
	}
	
	/**
	 * Declares the installation of a module
	 * @param module in the process of being installed
	 */
	public void begin(MeveoModule module) {
		active = true;
		this.ModuleCodeInstallation = module.getCode();
		this.repositories = module.getRepositories();
		this.module = module;
	}
	
	/**
	 * Declares the end of a module installation
	 */
	public void end() {
		active = false;
		this.ModuleCodeInstallation = null;
		this.repositories = null;
	}

	/**
	 * @return the {@link #repositories}
	 */
	public List<Repository> getRepositories() {
		return repositories;
	}

	/**
	 * @param repositories the repositories to set
	 */
	public void setRepositories(List<Repository> repositories) {
		this.repositories = repositories;
	}
	
}
