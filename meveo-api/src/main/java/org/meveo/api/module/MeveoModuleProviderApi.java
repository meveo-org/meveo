/**
 * 
 */
package org.meveo.api.module;

import javax.inject.Inject;

import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.module.MeveoModuleProviderDto;
import org.meveo.model.module.MeveoModuleProvider;
import org.meveo.service.admin.impl.MeveoModuleProviderService;
import org.meveo.service.base.local.IPersistenceService;

public class MeveoModuleProviderApi extends BaseCrudApi<MeveoModuleProvider, MeveoModuleProviderDto> {

	@Inject
	private MeveoModuleProviderService dao;
	
	public MeveoModuleProviderApi() {
		super(MeveoModuleProvider.class, MeveoModuleProviderDto.class);
	}

	@Override
	public IPersistenceService<MeveoModuleProvider> getPersistenceService() {
		return dao;
	}
}
