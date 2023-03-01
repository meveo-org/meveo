/**
 *
 */
package org.meveo.admin.action;

import static org.meveo.model.admin.MvCredential.AuthenticationType.*;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.MvCredentialApi;
import org.meveo.api.dto.MvCredentialDto;
import org.meveo.elresolver.ELException;
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

    public boolean getIsApi() {
        return API_KEY.equals(entity.getAuthenticationType());
    }

    public boolean getIsHeader() {
        return HEADER.equals(entity.getAuthenticationType());
    }

    public boolean getIsHttpBasic() {
        return HTTP_BASIC.equals(entity.getAuthenticationType());
    }

    public boolean getIsOauth2() {
        return OAUTH2.equals(entity.getAuthenticationType());
    }

    public boolean getIsSSH() {
        return SSH.equals(entity.getAuthenticationType());
    }

    @ActionMethod
    public void delete(MvCredential entity) throws BusinessException {
        service.remove(entity);
    }

    @ActionMethod
    public String deleteAndNavigate() throws BusinessException {
        service.remove(entity);
        return "credentials";
    }

    @Override
    public MvCredential initEntity() {
        MvCredential credential = super.initEntity();
        extractMapTypeFieldFromEntity(credential.getExtraParameters(), "extraParameters");
        return credential;
    }

    @Override
    @ActionMethod
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
        updateMapTypeFieldInEntity(entity.getExtraParameters(), "extraParameters");
        return super.saveOrUpdate(killConversation);
    }

}
