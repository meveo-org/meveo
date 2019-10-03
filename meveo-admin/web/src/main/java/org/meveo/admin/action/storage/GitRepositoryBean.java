package org.meveo.admin.action.storage;

import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseCrudBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.git.GitRepositoryDto;
import org.meveo.api.git.GitRepositoryApi;
import org.meveo.elresolver.ELException;
import org.meveo.model.git.GitRepository;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitRepositoryService;
import org.slf4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class GitRepositoryBean extends BaseCrudBean<GitRepository, GitRepositoryDto> {

    private static final long serialVersionUID = 8661265102557481231L;

    @Inject
    private GitRepositoryService gitRepositoryService;

    @Inject
    private GitRepositoryApi gitRepositoryApi;

    @Inject
    private Logger log;

    @Inject
    private GitClient gitClient;

    private String username;

    private String password;

    public GitRepositoryBean() {
        super(GitRepository.class);
    }

    @ActionMethod
    public String saveOrUpdateGit() throws BusinessException, ELException {
        String result = saveOrUpdate(false);
        if (result == null) {
            FacesContext.getCurrentInstance().validationFailed();
        }
        return result;
    }

    public void pushRemote() {
        try {
            gitClient.push(entity, this.getUsername(), this.getPassword());
            initEntity(entity.getId());
            messages.info(new BundleKey("messages", "gitRepositories.push.successful"));
        } catch (Exception e) {
        	log.error("Failed to push", e);
            messages.error(new BundleKey("messages", "gitRepositories.push.error"), e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }

    public void pullRemote() {
        try {
            gitClient.pull(entity, this.getUsername(), this.getPassword());
            initEntity(entity.getId());
            messages.info(new BundleKey("messages", "gitRepositories.pull.successful"));
        } catch (Exception e) {
        	log.error("Failed to pull", e);
            messages.error(new BundleKey("messages", "gitRepositories.pull.error"), e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }

    @Override
    protected String getListViewName() {
        return "gitRepositories";
    }

    @Override
    public BaseCrudApi<GitRepository, GitRepositoryDto> getBaseCrudApi() {
        return gitRepositoryApi;
    }

    @Override
    protected IPersistenceService<GitRepository> getPersistenceService() {
        return gitRepositoryService;
    }

    public String getUsername() {
        if (username == null && entity.getDefaultRemoteUsername() != null) {
            username = entity.getDefaultRemoteUsername();
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        if (password == null && entity.getDefaultRemotePassword() != null) {
            password = entity.getDefaultRemotePassword();
        }
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
