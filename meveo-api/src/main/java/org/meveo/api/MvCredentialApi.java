/**
 * 
 */
package org.meveo.api;

import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.api.dto.MvCredentialDto;
import org.meveo.model.admin.MvCredential;
import org.meveo.service.admin.impl.credentials.MvCredentialService;
import org.meveo.service.base.local.IPersistenceService;

@Named("MvCredentialApi")
public class MvCredentialApi extends BaseCrudApi<MvCredential, MvCredentialDto>{

	@Inject
	private MvCredentialService persistenceService;
	
	public MvCredentialApi() {
		super(MvCredential.class, MvCredentialDto.class);
	}

	@Override
	public IPersistenceService<MvCredential> getPersistenceService() {
		return persistenceService;
	}

}
