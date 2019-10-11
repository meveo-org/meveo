/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.service.git;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.commons.utils.ParamBean;
import org.meveo.exceptions.EntityAlreadyExistsException;
import org.meveo.model.BusinessEntity;
import org.meveo.model.git.GitRepository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.base.BusinessService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Persistence class for GitRepository
 *
 * @author Clement Bareth
 * @lastModifiedVersion 6.4.0
 */
@Stateless
public class GitRepositoryService extends BusinessService<GitRepository> {

    protected final static GitRepository MEVEO_DIR;

    static {
        final ParamBean paramBean = ParamBean.getInstance();
        final String remoteUrl = paramBean.getProperty("meveo.git.directory.remote.url", null);
        final String remoteUsername = paramBean.getProperty("meveo.git.directory.remote.username", null);
        final String remotePassword = paramBean.getProperty("meveo.git.directory.remote.password", null);

        MEVEO_DIR = new GitRepository();
        MEVEO_DIR.setCode("Meveo");
        MEVEO_DIR.setRemoteOrigin(remoteUrl);
        MEVEO_DIR.setDefaultRemoteUsername(remoteUsername);
        MEVEO_DIR.setDefaultRemotePassword(remotePassword);
    }

    @Inject
    private GitClient gitClient;

    @Inject
    private Logger log;

    @Inject
    @CurrentUser
    private MeveoUser currentUser;

    /**
     * Initialize the Meveo repository if not initialized
     *
     * @return the Meveo repository instance
     */
    @Produces
    @MeveoRepository
    @ApplicationScoped
    @Named("meveoRepository")
    public GitRepository getMeveoRepository() {

        try {
            gitClient.create(
                    MEVEO_DIR,
                    false,
                    MEVEO_DIR.getDefaultRemoteUsername(),
                    MEVEO_DIR.getDefaultRemotePassword()
            );

        } catch (EntityAlreadyExistsException e) {
            //NOOP
        } catch (BusinessException e) {
            log.error("Cannot retrieve Meveo directory", e);
        }

        return MEVEO_DIR;
    }

    @Override
    public GitRepository findByCode(String code) {
        final GitRepository repository = super.findByCode(code);

        if (repository != null && !GitHelper.hasReadRole(currentUser, repository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }
        setBranchInformation(repository);

        return repository;
    }

    @Override
	public GitRepository findById(Long id) {
		GitRepository repo = super.findById(id);
		setBranchInformation(repo);
		return repo;
	}
    
    

	@Override
	public GitRepository findByCode(String code, List<String> fetchFields) {
		GitRepository repo = super.findByCode(code, fetchFields);
		setBranchInformation(repo);
		return repo;
	}

	@Override
	protected GitRepository findByCode(String code, List<String> fetchFields, String additionalSql, Object... additionalParameters) {
		GitRepository repo = super.findByCode(code, fetchFields, additionalSql, additionalParameters);
		setBranchInformation(repo);
		return repo;
	}

	@Override
	public BusinessEntity findByEntityClassAndCode(Class<?> clazz, String code) {
		GitRepository repo = (GitRepository) super.findByEntityClassAndCode(clazz, code);
		setBranchInformation(repo);
		return repo;
	}

	@Override
	public GitRepository findById(Long id, boolean refresh) {
		GitRepository repo = super.findById(id, refresh);
		setBranchInformation(repo);
		return repo;
	}

	@Override
	public GitRepository findById(Long id, List<String> fetchFields) {
		GitRepository repo = super.findById(id, fetchFields);
		setBranchInformation(repo);
		return repo;
	}

	@Override
	public GitRepository findById(Long id, List<String> fetchFields, boolean refresh) {
		GitRepository repo =  super.findById(id, fetchFields, refresh);
		setBranchInformation(repo);
		return repo;
	}

	@Override
	public List<GitRepository> findByCodeLike(String wildcode) {
		List<GitRepository> repositories = super.findByCodeLike(wildcode);
		return repositories.stream()
	        .filter(r -> GitHelper.hasReadRole(currentUser, r))
	        .map(this::setBranchInformation)
	        .collect(Collectors.toList());
	}

	@Override
    public List<GitRepository> list() {
        final List<GitRepository> repositories = super.list();
        return repositories.stream()
                .filter(r -> GitHelper.hasReadRole(currentUser, r))
                .map(this::setBranchInformation)
                .collect(Collectors.toList());
    }

    /**
     * Remove the GitRepository entity along with the corresponding repository in file system
     *
     * @param entity Repository to remove
     */
    @Override
    public void remove(GitRepository entity) throws BusinessException {
        gitClient.remove(entity);
        super.remove(entity);
    }

    /**
     * Create the GitRepository entity along with the corresponding repository in file system
     *
     * @param entity Repository to create
     */
    @Override
    public void create(GitRepository entity) throws BusinessException {
        gitClient.create(entity, true, null, null);
        super.create(entity);
    }

    public void create(GitRepository entity, boolean failIfExist, String username, String password) throws BusinessException {
        gitClient.create(entity, failIfExist, username, password);
        super.create(entity);
    }

    private GitRepository setBranchInformation(GitRepository repository) {
        // Set branches information if not null
        if (repository != null) {
            try {
                final String currentBranch = gitClient.currentBranch(repository);
                repository.setCurrentBranch(currentBranch);

                final List<String> branches = gitClient.listBranch(repository);
                repository.setBranches(branches);
            } catch (BusinessException e) {
                log.error("Cannot retrieve branch information for {}", repository, e);
            }
        }

        return repository;
    }

}
