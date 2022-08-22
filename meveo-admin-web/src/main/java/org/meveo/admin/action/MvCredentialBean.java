/**
 * 
 */
package org.meveo.admin.action;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.api.BaseCrudApi;
import org.meveo.api.MvCredentialApi;
import org.meveo.api.dto.MvCredentialDto;
import org.meveo.model.admin.MvCredential;
import org.meveo.service.admin.impl.credentials.MvCredentialService;
import org.meveo.service.base.local.IPersistenceService;

@Named
@ViewScoped
public class MvCredentialBean extends BaseCrudBean<MvCredential, MvCredentialDto> {

	private static final long serialVersionUID = -3185146361412053533L;

	@Inject
	private transient MvCredentialApi api;
	
	@Inject
	private transient MvCredentialService service;
	
    public MvCredentialBean() {
    	super(MvCredential.class);
    }
	
	@Override
	public BaseCrudApi<MvCredential, MvCredentialDto> getBaseCrudApi() {
		return api;
	}

	@Override
	protected IPersistenceService<MvCredential> getPersistenceService() {
		return service;
	}

	@Override
	public String getEditViewName() {
		return "credentialDetail";
	}

	@Override
	protected String getListViewName() {
		return "credentials";
	}

}
