/**
 * 
 */
package org.meveo.service.admin.impl;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.meveo.model.module.MeveoModule;

public class ModuleUninstall {
	
	@QueryParam("removeFiles")
	private boolean removeFiles = false;
	
	@QueryParam("removeData")
	private boolean removeData = false;
	
	@QueryParam("removeItems") @DefaultValue ("true")
	private boolean removeItems = true;
	
	@QueryParam("withDependencies")
	private boolean withDependencies = false;
	
	private boolean childModule;
	private MeveoModule module;
	
	@PathParam("code")
	private String moduleCode;
	
	@Deprecated
	public ModuleUninstall() {
		
	}
	
	/** @return Whether to remove the files associated to the module */
	public boolean removeFiles() {
		return removeFiles;
	}

	/** @return Whether to remove the data schema of the custom templates of the module */
	public boolean removeData() {
		return removeData;
	}
	
	/** @return Whether to remove the module items from the database. If false, only disable them */
	public boolean removeItems() {
		return removeItems;
	}
	
	/**
	 * @return Whether to uninstall dependencies as well
	 */
	public boolean withDependencies() {
		return withDependencies;
	}
	
	/** @return Whether the module is a submodule */
	public boolean childModule() {
		return childModule;
	}
	
	public String moduleCode() {
		return this.moduleCode;
	}
	
	/** @return The module to uninstall */
	public MeveoModule module() {
		return module;
	}
	
	public ModuleUninstall withModule(MeveoModule module) {
		return builder(this)
				.module(module)
				.build();
	}
	
	public static ModuleUninstall of(MeveoModule module) {
		ModuleUninstall opts = new ModuleUninstall();
		opts.module = module;
		return opts;
	}
	
	public static ModuleUninstall of(String module) {
		ModuleUninstall opts = new ModuleUninstall();
		opts.moduleCode = module;
		return opts;
	}
	
	
	

	public static ModuleUninstallBuilder builder() {
		return new ModuleUninstallBuilder();
	}
	
	public static ModuleUninstallBuilder builder(ModuleUninstall options) {
		ModuleUninstall opts = new ModuleUninstall();
		opts.childModule = options.childModule;
		opts.module = options.module;
		opts.removeData = options.removeData;
		opts.removeFiles = options.removeFiles;
		opts.removeItems = options.removeItems;
		opts.moduleCode = options.moduleCode == null && options.module != null ? options.module.getCode() : null;
		opts.withDependencies = options.withDependencies;
		return new ModuleUninstallBuilder(opts);
	}
	
	public static class ModuleUninstallBuilder {
		ModuleUninstall options;
		
		private ModuleUninstallBuilder(ModuleUninstall options) {
			this.options = options;
		}
		
		private ModuleUninstallBuilder() {
			options = new ModuleUninstall();
		}
		
		public ModuleUninstallBuilder withDependencies(boolean withDependencies) {
			this.options.withDependencies = withDependencies;
			return this;
		}
		
		public void setWithDependencies(boolean withDependencies) {
			withDependencies(withDependencies);
		}
		
		/**
		 * @param removeFiles Whether to remove the files associated to the module 
		 * @return the builder
		 */
		public ModuleUninstallBuilder removeFiles(boolean removeFiles) {
			options.removeFiles = removeFiles;
			return this;
		}
		
		/**
		 * @param removeData Whether to remove the data schema of the custom templates of the module
		 * @return the builder
		 */
		public ModuleUninstallBuilder removeData(boolean removeData) {
			options.removeData = removeData;
			return this;
		}
		
		/** 
		 *  @param module The module to uninstall 
		 *  @return the builder
		 */
		public ModuleUninstallBuilder module(MeveoModule module) {
			options.module = module;
			options.moduleCode = module.getCode();
			return this;
		}
		
		/**
		 * @param removeItems Whether to remove the module items from the database. If false, only disable them
		 * @return the builder
		 */
		public ModuleUninstallBuilder removeItems(boolean removeItems) {
			options.removeItems = removeItems;
			return this;
		}
		
		/**
		 * 
		 * @param childModule Whether the module is a submodule
		 * @return the builder
		 */
		public ModuleUninstallBuilder childModule(boolean childModule) {
			options.childModule = childModule;
			return this;
		}
		
		public ModuleUninstallBuilder moduleCode(String moduleCode) {
			options.moduleCode = moduleCode;
			return this;
		}
		
		public ModuleUninstall build() {
			if (options.module == null && options.moduleCode == null) {
				throw new IllegalArgumentException("module / module code should not be null");
			}
			
			return options;
		}
		

		/**
		 * @return the {@link #removeFiles}
		 */
		public boolean isRemoveFiles() {
			return options.removeFiles;
		}

		/**
		 * @param removeFiles the removeFiles to set
		 */
		public void setRemoveFiles(boolean removeFiles) {
			this.options.removeFiles = removeFiles;
		}

		/**
		 * @return the {@link #removeData}
		 */
		public boolean isRemoveData() {
			return options.removeData;
		}

		/**
		 * @param removeData the removeData to set
		 */
		public void setRemoveData(boolean removeData) {
			this.options.removeData = removeData;
		}

		/**
		 * @return the {@link #removeItems}
		 */
		public boolean isRemoveItems() {
			return options.removeItems;
		}

		/**
		 * @param removeItems the removeItems to set
		 */
		public void setRemoveItems(boolean removeItems) {
			this.options.removeItems = removeItems;
		}

		/**
		 * @return the {@link #childModule}
		 */
		public boolean isChildModule() {
			return options.childModule;
		}

		/**
		 * @param childModule the childModule to set
		 */
		public void setChildModule(boolean childModule) {
			this.options.childModule = childModule;
		}

		/**
		 * @return the {@link #module}
		 */
		public MeveoModule getModule() {
			return options.module;
		}

		/**
		 * @param module the module to set
		 */
		public void setModule(MeveoModule module) {
			this.options.module = module;
		}

		/**
		 * @return the {@link #moduleCode}
		 */
		public String getModuleCode() {
			return options.moduleCode;
		}

		/**
		 * @param moduleCode the moduleCode to set
		 */
		public void setModuleCode(String moduleCode) {
			this.options.moduleCode = moduleCode;
		}
		
		public boolean isWithDependencies() {
			return this.options.withDependencies;
		}
		
		
	}
}
