/**
 * 
 */
package org.meveo.service.admin.impl;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.InitialContext;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.meveo.admin.exception.BusinessException;
import org.meveo.model.module.InstallableModule;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleProvider;
import org.meveo.service.base.BusinessService;
import org.meveo.service.git.GitClient;

@Stateless
@DependsOn("CachesInitializer")
public class MeveoModuleProviderService extends BusinessService<MeveoModuleProvider> {

	@Resource(name = "java:jboss/infinispan/container/meveo/marketplace")
	private Cache<String, List<InstallableModule>> marketplaceCache;
	
	@Inject
	private MeveoModuleService moduleService;
	
	@Inject
	private GitClient gitClient;
	
	@PostConstruct
	private void init() {
    	try {
			InitialContext initialContext = new InitialContext();
			var cacheContainer = (EmbeddedCacheManager) initialContext.lookup("java:jboss/infinispan/container/meveo");
			marketplaceCache = cacheContainer.getCache("marketplace");
    	} catch (Exception e) {
			throw new RuntimeException("Cannot instantiate cache container", e);
		}
	}

	public List<InstallableModule> listInstallableModules(MeveoModuleProvider provider, boolean refresh) throws BusinessException {
		var cachedModules = marketplaceCache.get(provider.getCode());
		if (!refresh && cachedModules != null) {
			return marketplaceCache.get(provider.getCode());
		}
		
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
		
		marketplaceCache.put(provider.getCode(), modules);
		
		return modules;
	}
}
