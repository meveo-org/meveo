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
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.git.CommitEvent;
import org.meveo.event.qualifier.git.Commited;
import org.meveo.exceptions.EntityAlreadyExistsException;
import org.meveo.model.git.GitBranch;
import org.meveo.model.git.GitRepository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Inject
    @Commited
    private Event<CommitEvent> commitedEvent;

    @Inject
    @Created
    private Event<GitBranch> branchCreated;

    @Inject
    @Removed
    private Event<GitBranch> branchRemoved;

    /**
     * Remove the corresponding git repository from file system
     *
     * @param gitRepository Repository to delete
     * @throws BusinessException          if the directory cannot be deleted
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     */
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

    /**
     * Create and initiate the git repository in the file system
     *
     * @param gitRepository Repository to create
     * @throws BusinessException          if repository cannot be cloned or initiated
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     */
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

    /**
     * Stage the files correspondings to the given pattern and create commit
     *
     * @param gitRepository Repository to commit
     * @param patterns      Pattern of the files to stage
     * @param message       Commit message
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     * @throws BusinessException          if repository cannot be open or commited
     * @throws IllegalArgumentException   if pattern list is empty
     */
    public void commit(GitRepository gitRepository, List<String> patterns, String message) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {

            final AddCommand add = git.add();

            if (CollectionUtils.isNotEmpty(patterns)) {
                patterns.forEach(add::addFilepattern);


                final Status status = git.status().call();

                final RmCommand rm = git.rm();
                boolean doRm = false;

                for (String missing : status.getMissing()) {
                    if (patterns.contains(missing)) {
                        rm.addFilepattern(missing);
                        doRm = true;
                    }
                }

                if (doRm) {
                    rm.call();
                }

                if (status.hasUncommittedChanges()) {
                    git.commit().setMessage(message)
                            .setAuthor(currentUser.getUserName(), currentUser.getMail())
                            .setCommitter(currentUser.getUserName(), currentUser.getMail())
                            .call();

                    Set<String> modifiedFiles = new HashSet<>();
                    modifiedFiles.addAll(status.getAdded());
                    modifiedFiles.addAll(status.getChanged());
                    modifiedFiles.addAll(status.getModified());
                    modifiedFiles.addAll(status.getRemoved());

                    commitedEvent.fire(new CommitEvent(gitRepository, modifiedFiles));

                }

            } else {
                throw new IllegalArgumentException("Cannot commit repository " + gitRepository.getCode() + " : no staged files");
            }

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot create commit on repository " + gitRepository.getCode(), e);
        }

    }

    /**
     * Stage the files in parameter and create commit
     *
     * @param gitRepository Repository to commit
     * @param files         Files to stage
     * @param message       Commit message
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     * @throws BusinessException          if repository cannot be open, commited, or if a file is not child of the repository
     */
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

    /**
     * Push the repositories' commits to upstream
     *
     * @param gitRepository Repository to push
     * @param username      Optional - Username to use when pushing
     * @param password      Optional - Password to use when pushing
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     * @throws IllegalArgumentException   if repository has no remote
     * @throws BusinessException          if repository cannot be opened or if a problem happen during the push
     */
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

    /**
     * Pull with rebase the upstream's content
     *
     * @param gitRepository Repository to update
     * @param username      Optional - Username to use when pulling
     * @param password      Optional - Password to use when pulling
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     * @throws IllegalArgumentException   if repository has no remote
     * @throws BusinessException          if repository cannot be opened or if a problem happen during the pull
     */
    public void pull(GitRepository gitRepository, String username, String password) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
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

    /**
     * Create a branch base on the current branch
     *
     * @param gitRepository Repository to update
     * @param branch        Name of the branch to create
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     * @throws BusinessException          if repository cannot be opened or if a problem happen during branch creation
     */
    public void createBranch(GitRepository gitRepository, String branch) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {

            branchCreated.fire(new GitBranch(branch, gitRepository));
            git.branchCreate().setName(branch).call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot create new branch " + branch + " for repository " + gitRepository.getCode(), e);
        }

    }

    /**
     * Remove a branch
     *
     * @param gitRepository Repository to update
     * @param branch        Name of the branch to delete
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     * @throws BusinessException          if repository cannot be opened or if a problem happen during branch deletion
     */
    public void deleteBranch(GitRepository gitRepository, String branch) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {
            branchRemoved.fire(new GitBranch(branch, gitRepository));

            git.branchDelete().setBranchNames(branch).setForce(true).call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot delete branch " + branch + " for repository " + gitRepository.getCode(), e);
        }

    }

    /**
     * Retrieve the name of the current branch of the repository
     *
     * @param gitRepository Repository to get branch name from
     * @return current branch name
     * @throws BusinessException          if repository cannot be opened
     * @throws UserNotAuthorizedException if user does not have read access to the repository
     */
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

    /**
     * Checkout an other branch of the repository
     *
     * @param gitRepository GitRepository where to execute the checkout
     * @param branch        Name of the branch to checkout
     * @param createBranch  Whether to create the branch if it does not exists
     * @throws BusinessException          if the GitRepository cannot be opened or the checkout cannot be done
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     */
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

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot checkout branch " + branch + " for repository " + gitRepository.getCode(), e);
        }
    }

    /**
     * Merge a branch into an other
     *
     * @param gitRepository GitRepository where to merge branch
     * @param from          Branch to get changes
     * @param to            Branch to be updated
     * @return <code>true</code> if the merge has no conflict.
     * @throws BusinessException          if problem happens during merge
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     */
    public boolean merge(GitRepository gitRepository, String from, String to) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {
            String previousBranch = git.getRepository().getBranch();

            git.checkout().setCreateBranch(false).setName(to).call();

            try {

                boolean successful = git.rebase().setUpstream(from)
                        .call()
                        .getStatus()
                        .isSuccessful();

                if (!successful) {
                    git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
                }

                return successful;

            } catch (GitAPIException e) {
                throw new BusinessException("Cannot merge " + from + " into " + to, e);
            } finally {
                git.checkout().setCreateBranch(false).setName(previousBranch).call();
            }

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Checkout problem for repository " + gitRepository.getCode(), e);
        }
    }

    /**
     * Retrieve the names of the branches of the repository
     *
     * @param gitRepository Repository to get branches names from
     * @return available branches name - local and remote
     * @throws BusinessException          if repository cannot be opened or branches cannot be read
     * @throws UserNotAuthorizedException if user does not have read access to the repository
     */
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

    /**
     * Return the head commit of a repository
     *
     * @param gitRepository {@link GitRepository} to retrieve head commit from
     * @return the head commit
     * @throws BusinessException if we cannot read the repositories branches
     */
    public RevCommit getHeadCommit(GitRepository gitRepository) throws BusinessException {
        if (!GitHelper.hasReadRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);

        try (Git git = Git.open(repositoryDir)) {
            try (RevWalk rw = new RevWalk(git.getRepository())) {
                ObjectId head = git.getRepository().resolve(Constants.HEAD);
                return rw.parseCommit(head);
            }
        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);
        }
    }

    /**
     * Build the list of files modified in a given commit
     *
     * @param gitRepository Repository holding the commit
     * @param commit        The commit to analyze
     * @return the list of files modified
     */
    public Set<String> getModifiedFiles(GitRepository gitRepository, RevCommit commit) throws BusinessException {
        if (!GitHelper.hasReadRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        Set<String> modifiedFiles = new HashSet<>();

        try (Git git = Git.open(repositoryDir)) {
            Repository repository = git.getRepository();
            RevWalk rw = new RevWalk(repository);
            RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
            List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
            for (DiffEntry diff : diffs) {
                modifiedFiles.add(diff.getNewPath());
            }

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        }

        return modifiedFiles;
    }

    /**
     * Reset a repository to a given commit
     *
     * @param gitRepository {@link GitRepository} to reset
     * @param commit        Commit to reset onto
     */
    public void reset(GitRepository gitRepository, RevCommit commit) throws BusinessException {
        if (!GitHelper.hasWriteRole(currentUser, gitRepository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        try (Git git = Git.open(repositoryDir)) {
            git.reset().setMode(ResetCommand.ResetType.HARD)
                    .setRef(commit.getId().getName())
                    .call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot reset repository to commit " + commit.getId().getName(), e);
        }
    }

}
