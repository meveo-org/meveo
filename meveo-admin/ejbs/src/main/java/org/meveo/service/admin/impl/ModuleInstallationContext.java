/**
 * 
 */
package org.meveo.service.admin.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PreDestroy;
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
	private List<Runnable> postInstallActions = new ArrayList<>();
	
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
		
		postInstallActions.forEach(Runnable::run);
		postInstallActions.clear();
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
	
	/**
	 * If the context is active, register the action to execute it later, otherwise execute immediatly
	 * 
	 * @param runnable action to register / execute
	 */
	public void registerOrExecutePostInstallAction(Runnable runnable) {
		if (!active) {
			runnable.run();
		} else {
			postInstallActions.add(runnable);
		}
	}
	
}
