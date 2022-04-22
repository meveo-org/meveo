/**
 * 
 */
package org.meveo.service.admin.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.ModulePostInstall;
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
	
	@Inject
	@ModulePostInstall
	private Event<MeveoModule> postInstallEvent;

	private String ModuleCodeInstallation = "";
	private List<Repository> repositories;
	private MeveoModule module;
	private List<PostInstallAction> postInstallActions = new ArrayList<>();
	
	private boolean active;
	private boolean failed;
	
	/**
	 * @return whether a module is being installed
	 */
	public boolean isActive() {
		return active;
	}
	
	public void markFailed() {
		this.failed = true;
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
		failed = false;
		this.ModuleCodeInstallation = module.getCode();
		this.repositories = module.getRepositories();
		this.module = module;
	}
	
	/**
	 * Declares the end of a module installation
	 * @throws BusinessException if a post install action fails
	 */
	public void end() throws BusinessException {
		if (!failed) {
			for (var action : postInstallActions) {
				action.run();
			}
			postInstallActions.clear();
			postInstallEvent.fire(module);
		}
		
		
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
	
	/**
	 * If the context is active, register the action to execute it later, otherwise execute immediatly
	 * 
	 * @param runnable action to register / execute
	 * @throws BusinessException if action is executed and fails
	 */
	public void registerOrExecutePostInstallAction(PostInstallAction runnable) throws BusinessException {
		if (!active) {
			runnable.run();
		} else {
			postInstallActions.add(runnable);
		}
	}
	
	@FunctionalInterface
	public static interface PostInstallAction {
		public void run() throws BusinessException;
	}
	
}
