/**
 * 
 */
package org.meveo.model.module;

import java.util.List;

import org.meveo.model.module.providers.GithubProvider;
import org.meveo.model.module.providers.MeveoModuleProviderInterface;

public enum MeveoModuleProviderType {
	
	GITHUB(GithubProvider.class);
	
	private Class<? extends MeveoModuleProviderInterface> providerImplClass;
	
	private MeveoModuleProviderType (Class<? extends MeveoModuleProviderInterface> clazz) {
		this.providerImplClass = clazz;
	}
	
	public List<InstallableModule> list(MeveoModuleProvider provider) throws Exception {
		MeveoModuleProviderInterface providerImpl = providerImplClass.getDeclaredConstructor().newInstance();
		providerImpl.init(provider);
		return providerImpl.list();
	}
}
