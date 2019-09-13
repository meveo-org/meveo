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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.exceptions.EntityAlreadyExistsException;
import org.meveo.model.git.GitRepository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Git client class to manipulate repositories
 *
 * @author Clement Bareth
 * @lastModifiedVersion 6.4.0
 */
public class GitClient {

    @Inject
    @CurrentUser
    private MeveoUser currentUser;

    protected void remove(GitRepository gitRepository) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repoDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        if (repoDir.exists()) {
            try {
                FileUtils.deleteDirectory(repoDir);
            } catch (IOException e) {
                throw new BusinessException("Cannot delete git repository " + gitRepository.getCode() + " from file system", e);
            }
        }
    }

    protected void create(GitRepository gitRepository) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repoDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        if (repoDir.exists()) {
            throw new EntityAlreadyExistsException(GitRepository.class, gitRepository.getCode());
        }

        repoDir.mkdirs();

        if (gitRepository.isRemote()) {
            try {
                final CloneCommand cloneCommand = Git.cloneRepository()
                        .setURI(gitRepository.getRemoteOrigin())
                        .setDirectory(repoDir);

                CredentialsProvider usernamePasswordCredentialsProvider = GitHelper.getCredentialsProvider(gitRepository, null, null, currentUser);
                cloneCommand.setCredentialsProvider(usernamePasswordCredentialsProvider).call().close();

            } catch (GitAPIException e) {
                repoDir.delete();
                throw new BusinessException("Error cloning repository " + gitRepository.getRemoteOrigin(), e);
            }

        } else {
            try {
                Git.init().setDirectory(repoDir).call().close();

            } catch (GitAPIException e) {
                repoDir.delete();
                throw new BusinessException("Error initating repository " + gitRepository.getCode(), e);
            }
        }

    }

    public void commit(GitRepository gitRepository, List<String> patterns, String message) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {

            final AddCommand add = git.add();

            if (CollectionUtils.isNotEmpty(patterns)) {
                patterns.forEach(add::addFilepattern);

                add.call();

                git.commit().setMessage(message)
                        .setAuthor(currentUser.getUserName(), currentUser.getMail())
                        .setCommitter(currentUser.getUserName(), currentUser.getMail())
                        .call();

            } else {
                throw new IllegalArgumentException("Cannot commit repository " + gitRepository.getCode() + " : no staged files");
            }

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot create commit on repository " + gitRepository.getCode(), e);
        }

    }

    public void commitFiles(GitRepository gitRepository, List<File> files, String message) throws BusinessException {
        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        List<String> patterns = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(files)) {
            for (File f : files) {
                final String relativePath = GitHelper.computeRelativePath(repositoryDir, f);
                if (relativePath.equals(StringUtils.EMPTY)) {
                    patterns.add(".");
                    break;

                } else if (relativePath.equals(f.getAbsolutePath())) {
                    throw new BusinessException("File " + f.getAbsolutePath() + " is not child of repository " + repositoryDir.getAbsolutePath());

                } else {
                    patterns.add(relativePath);
                }
            }
        }

        commit(gitRepository, patterns, message);
    }

    public void push(GitRepository gitRepository, String username, String password) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        if (!gitRepository.isRemote()) {
            throw new IllegalArgumentException("Repository " + gitRepository.getCode() + " has no remote to push to");
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {
            final PushCommand push = git.push();
            CredentialsProvider usernamePasswordCredentialsProvider = GitHelper.getCredentialsProvider(gitRepository, username, password, currentUser);
            push.setCredentialsProvider(usernamePasswordCredentialsProvider).call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot push repository " + gitRepository.getCode(), e);
        }
    }

    public void pull(GitRepository gitRepository, String username, String password) throws BusinessException {
        if (!GitHelper.hasReadRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        if (!gitRepository.isRemote()) {
            throw new IllegalArgumentException("Repository " + gitRepository.getCode() + " has no remote to pull from");
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {

            CredentialsProvider usernamePasswordCredentialsProvider = GitHelper.getCredentialsProvider(gitRepository, username, password, currentUser);

            git.pull().setCredentialsProvider(usernamePasswordCredentialsProvider)
                    .setRebase(true)
                    .call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot push repository " + gitRepository.getCode(), e);
        }
    }

    public void createBranch(GitRepository gitRepository, String branch) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {
            git.branchCreate().setName(branch).call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot create new branch " + branch +  " for repository " + gitRepository.getCode(), e);
        }

    }

    public void deleteBranch(GitRepository gitRepository, String branch) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {
            git.branchDelete().setBranchNames(branch).setForce(true).call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot delete branch " + branch +  " for repository " + gitRepository.getCode(), e);
        }

    }

    public String currentBranch(GitRepository gitRepository) throws BusinessException {
        if (!GitHelper.hasReadRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {
            return git.getRepository().getBranch();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        }
    }

    public void checkout(GitRepository gitRepository, String branch, boolean createBranch) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {
            git.checkout().setCreateBranch(createBranch).setName(branch).call();
            gitRepository.setCurrentBranch(branch);

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        }  catch (GitAPIException e) {
            throw new BusinessException("Cannot checkout branch " + branch +  " for repository " + gitRepository.getCode(), e);
        }
    }

    public List<String> listBranch(GitRepository gitRepository) throws BusinessException {
        if (!GitHelper.hasReadRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {
            return git.branchList().call()
                    .stream()
                    .map(Ref::getName)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot list branches of repository " + gitRepository.getCode(), e);
        }

    }

}
