package org.meveo.admin.action.storage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseCrudBean;
import org.meveo.admin.action.admin.EntityPermissionBean;
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
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;

/**
 * Controller for managing git repository model.
 * 
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.9.0
 * @see GitRepository
 */
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
    
    @Inject //TODO: test that solution
    private EntityPermissionBean entityPermissionBean;
    
    private String username;

    private String password;

    private String branch;

    public GitRepositoryBean() {
        super(GitRepository.class);
    }

    @ActionMethod
    public String saveOrUpdateGit() throws BusinessException, ELException {
        if (entity.getId() == null && entity.getRemoteOrigin() != null) {
            gitRepositoryService.create(entity, false, this.getUsername(), this.getPassword());
        }
        String result = saveOrUpdate(false);
        if (result == null) {
            FacesContext.getCurrentInstance().validationFailed();
        }
        
        entityPermissionBean.save();
        
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

    public String importZip(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        String filename = file.getFileName();
        try {
            InputStream inputStream = file.getInputstream();
            GitRepositoryDto dto = gitRepositoryApi.toDto(entity);
            gitRepositoryApi.importZip(inputStream, dto, isEdit());
            messages.info(new BundleKey("messages", "importZip.successfull"), filename);
        } catch (Exception e) {
            messages.error(new BundleKey("messages","importZip.error"), filename, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
        return getEditViewName();
    }

    public StreamedContent exportZip() {
        String filename = entity.getCode();
        branch = branch != null ? branch : entity.getCurrentBranch();
        try {
            byte[] exportZip = gitRepositoryApi.exportZip(entity.getCode(), branch);
            InputStream is = new ByteArrayInputStream(exportZip);
            return new DefaultStreamedContent(is, "application/octet-stream", filename + "-" + branch + ".zip");
        } catch (Exception e) {
            messages.error(new BundleKey("messages","exportZip.error"), e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
        return null;
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
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
