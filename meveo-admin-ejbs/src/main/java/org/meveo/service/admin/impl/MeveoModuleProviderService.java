/**
 * 
 */
package org.meveo.service.admin.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.module.InstallableModule;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleProvider;
import org.meveo.service.base.BusinessService;
import org.meveo.service.git.GitClient;

@Stateless
public class MeveoModuleProviderService extends BusinessService<MeveoModuleProvider> {
	
	@Inject
	private MeveoModuleService moduleService;
	
	@Inject
	private GitClient gitClient;

	public List<InstallableModule> listInstallableModules(MeveoModuleProvider provider) throws BusinessException {
		List<InstallableModule> modules = null;
		
		try {
			modules = provider.getProviderType().list(provider);
		} catch (Exception e) {
			throw new BusinessException("Failed to list repositories", e);
		}
		
		for (InstallableModule module : modules) {
			MeveoModule existingModule = moduleService.findByCode(module.getCode());
			if (existingModule != null) {
				String sha = gitClient.getHeadCommit(existingModule.getGitRepository()).getId().getName();
				module.setLocalRepositorySha(sha);
			}
		}
		
		return modules;
	}
}
