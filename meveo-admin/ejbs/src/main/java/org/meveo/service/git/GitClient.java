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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.WindowCache;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.SubmoduleConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.git.CommitEvent;
import org.meveo.event.qualifier.git.CommitReceived;
import org.meveo.event.qualifier.git.Commited;
import org.meveo.exceptions.EntityAlreadyExistsException;
import org.meveo.model.git.GitBranch;
import org.meveo.model.git.GitRepository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.synchronization.KeyLock;
import org.slf4j.Logger;

/**
 * Git client class to manipulate repositories
 *
 * @author Clement Bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @since 6.4.0
 * @version 6.9.0
 */
@ApplicationScoped
public class GitClient {

    @Inject
    @CurrentUser
    private Provider<MeveoUser> currentUser;

    @Inject
    @CommitReceived
    private Event<CommitEvent> gitRepositoryCommitedEvent;

    @Inject
    @Commited
    private Event<CommitEvent> commitedEvent;

    @Inject
    @Created
    private Event<GitBranch> branchCreated;

    @Inject
    @Removed
    private Event<GitBranch> branchRemoved;

    @Inject
    @MeveoRepository
    private GitRepository meveoRepository;

    @Inject
    private KeyLock keyLock;
    
    @Inject
    private Logger log;



    /**
     * Remove the corresponding git repository from file system
     *
     * @param gitRepository Repository to delete
     * @throws BusinessException          if the directory cannot be deleted
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     */
    protected void remove(GitRepository gitRepository) throws BusinessException {
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repoDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());
        WindowCache.reconfigure(new WindowCacheConfig());
        
        if (repoDir.exists()) {
            keyLock.lock(gitRepository.getCode());
            try {
                FileUtils.deleteDirectory(repoDir);
            } catch (IOException e) {
                throw new BusinessException("Cannot delete git repository " + gitRepository.getCode() + " from file system", e);
            } finally {
                keyLock.unlock(gitRepository.getCode());
            }
        }

    }

    /**
     * Create and initiate the git repository in the file system
     *
     * @param gitRepository Repository to create
     * @param failIfExist   Whether to fail if directory already exists
     * @param username      Optional - Username to connect remote repository if any
     * @param password      Optional - Password to connect remote repository if any
     * @throws BusinessException          if repository cannot be cloned or initiated
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     */
    public void create(GitRepository gitRepository, boolean failIfExist, String username, String password) throws BusinessException {
        MeveoUser user = currentUser.get();
        final File repoDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());
        if (repoDir.exists() && failIfExist) {
            throw new EntityAlreadyExistsException(GitRepository.class, gitRepository.getCode());
        } else if(repoDir.exists() && new File(repoDir, ".git").exists()) {
            return;
        }

        repoDir.mkdirs();

        if (gitRepository.isRemote()) {
            keyLock.lock(gitRepository.getCode());
            try {
                final CloneCommand cloneCommand = Git.cloneRepository()
                        .setURI(gitRepository.getRemoteOrigin())
                        .setDirectory(repoDir);
                
                if (gitRepository.getDefaultBranch() != null) {
                	cloneCommand.setBranchesToClone(List.of(gitRepository.getDefaultBranch()));
                	cloneCommand.setBranch(gitRepository.getDefaultBranch());
                }
                
                Git repository;
                cloneCommand.setCloneSubmodules(true);
                if(gitRepository.getRemoteOrigin().startsWith("http")) {
                    CredentialsProvider usernamePasswordCredentialsProvider = GitHelper.getCredentialsProvider(gitRepository, username, password, user);
                    repository = cloneCommand.setCredentialsProvider(usernamePasswordCredentialsProvider).call();
                } else {
                    SshTransportConfigCallback sshTransportConfigCallback = new SshTransportConfigCallback(user.getSshPrivateKey(), user.getSshPublicKey(), password);
                    repository = cloneCommand.setTransportConfigCallback(sshTransportConfigCallback).call();
                }
                
                try (repository) {
	                if (gitRepository.getDefaultBranch() != null) {
	                	repository.checkout()
	                		.setCreateBranch(false)
	                		.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
	                		.setName(gitRepository.getDefaultBranch())
	                		.call();
	                }
                }

            } catch (GitAPIException e) {
                try {
					FileUtils.deleteDirectory(repoDir);
				} catch (IOException e1) {
					log.error("Failed to delete repository", repoDir);
				}
                throw new BusinessException("Error cloning repository {}", e, gitRepository.getRemoteOrigin());

            } finally {
                keyLock.unlock(gitRepository.getCode());
            }

        } else {
            keyLock.lock(gitRepository.getCode());
            try (Git git = Git.init().setDirectory(repoDir).call()){
                // Init repo with a dummy commit
                new File(repoDir, "README.md").createNewFile();
                git.add().addFilepattern(".").call();
                git.commit().setMessage("First commit")
                        .setAuthor(user.getUserName(), user.getMail())
                        .setCommitter(user.getUserName(), user.getMail())
                        .call();
            } catch (GitAPIException | IOException e) {
                repoDir.delete();
                throw new BusinessException("Error initating repository " + gitRepository.getCode(), e);

            } finally {
                keyLock.unlock(gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {

            final AddCommand add = git.add();

            if (CollectionUtils.isNotEmpty(patterns)) {
                patterns.forEach(add::addFilepattern);
                add.call();

                final Status status = git.status().call();

                final RmCommand rm = git.rm();
                boolean doRm = false;

                for (String missing : status.getMissing()) {
                    if (patterns.contains(missing) || patterns.contains(".")) {
                        rm.addFilepattern(missing);
                        doRm = true;
                    }
                }

                if (doRm) {
                    rm.call();
                }

                if (status.hasUncommittedChanges()) {
                	try {
	                    RevCommit commit = git.commit().setMessage(message)
	                            .setAuthor(user.getUserName(), user.getMail())
	                            .setAllowEmpty(false)
	                            .setCommitter(user.getUserName(), user.getMail())
	                            .call();
	
	                    Set<String> modifiedFiles = new HashSet<>();
	                    modifiedFiles.addAll(status.getAdded());
	                    modifiedFiles.addAll(status.getChanged());
	                    modifiedFiles.addAll(status.getModified());
	                    modifiedFiles.addAll(status.getRemoved());
	                    
	                    List<DiffEntry> diffs = getDiffs(gitRepository, commit);
	
	                    commitedEvent.fire(new CommitEvent(gitRepository, modifiedFiles, diffs));
                	} catch (EmptyCommitException e) {
                		// NOOP
                	}

                }

            } else {
                throw new IllegalArgumentException("Cannot commit repository " + gitRepository.getCode() + " : no staged files");
            }

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot create commit on repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        if (!gitRepository.isRemote()) {
            throw new IllegalArgumentException("Repository " + gitRepository.getCode() + " has no remote to push to");
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
            final PushCommand push = git.push();
            if(gitRepository.getRemoteOrigin().startsWith("http")) {
                CredentialsProvider usernamePasswordCredentialsProvider = GitHelper.getCredentialsProvider(gitRepository, username, password, user);
                push.setCredentialsProvider(usernamePasswordCredentialsProvider).call();
            } else {
                SshTransportConfigCallback sshTransportConfigCallback = new SshTransportConfigCallback(user.getSshPrivateKey(), user.getSshPublicKey(), password);
                push.setTransportConfigCallback(sshTransportConfigCallback).call();
            }

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot push repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
        }
    }
    
    /**
     * Fetch remote
     *
     * @param gitRepository Repository to update
     * @param username      Optional - Username to use when pulling
     * @param password      Optional - Password to use when pulling
     * @throws UserNotAuthorizedException if user does not have write access to the repository
     * @throws IllegalArgumentException   if repository has no remote
     * @throws BusinessException          if repository cannot be opened or if a problem happen during the pull
     */
    public void fetch(GitRepository gitRepository, String username, String password) throws BusinessException {
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        if (!gitRepository.isRemote()) {
            throw new IllegalArgumentException("Repository " + gitRepository.getCode() + " has no remote to pull from");
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
            FetchCommand fetch = git.fetch()
            		.setRecurseSubmodules(SubmoduleConfig.FetchRecurseSubmodulesMode.YES);

			if (gitRepository.getRemoteOrigin().startsWith("http")) {
				CredentialsProvider usernamePasswordCredentialsProvider = GitHelper.getCredentialsProvider(gitRepository, username, password, user);
				fetch = fetch.setCredentialsProvider(usernamePasswordCredentialsProvider);
			
			} else {
				SshTransportConfigCallback sshTransportConfigCallback = new SshTransportConfigCallback(user.getSshPrivateKey(), user.getSshPublicKey(), password);
				fetch = fetch.setTransportConfigCallback(sshTransportConfigCallback);
			}
			
            fetch.call();
            
        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot pull repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        if (!gitRepository.isRemote()) {
            throw new IllegalArgumentException("Repository " + gitRepository.getCode() + " has no remote to pull from");
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
        	String branch = git.getRepository().getBranch();
        	
            RevCommit headCommitBeforePull = getHeadCommit(gitRepository);

            PullCommand pull = git.pull();
            pull = pull.setRebase(true);
            pull = pull.setRecurseSubmodules(SubmoduleConfig.FetchRecurseSubmodulesMode.YES);

			if (gitRepository.getRemoteOrigin().startsWith("http")) {
				CredentialsProvider usernamePasswordCredentialsProvider = GitHelper.getCredentialsProvider(gitRepository, username, password, user);
				pull = pull.setCredentialsProvider(usernamePasswordCredentialsProvider);
			
			} else {
				SshTransportConfigCallback sshTransportConfigCallback = new SshTransportConfigCallback(user.getSshPrivateKey(), user.getSshPublicKey(), password);
				pull = pull.setTransportConfigCallback(sshTransportConfigCallback);
			}
			
            pull.call();
            
            if(git.getRepository().getRepositoryState().isRebasing()) {
            	git.rebase().setOperation(Operation.ABORT).call();
                git.reset().setMode(ResetType.HARD).setRef("origin/" + branch).call();
            }

            triggerCommitEvent(gitRepository, git, headCommitBeforePull);
            
        	git.submoduleUpdate().call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot pull repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
        }

    }

	protected void triggerCommitEvent(GitRepository gitRepository, Git git, RevCommit headCommitBeforePull) throws AmbiguousObjectException, IncorrectObjectTypeException, IOException, MissingObjectException, GitAPIException, CheckoutConflictException, BusinessException {
		try (RevWalk rw = new RevWalk(git.getRepository())) {
		    ObjectId head = git.getRepository().resolve(Constants.HEAD);
		    RevCommit headCommitAfterPull = rw.parseCommit(head);

		    // Fire commit received event if commits are different and Meveo repository is concerned
		    if(!headCommitBeforePull.getId().equals(headCommitAfterPull.getId())) {
		        var diffs = getDiffs(git.getRepository(), headCommitBeforePull, headCommitAfterPull);
		    	Set<String> modifiedFiles = getModifiedFiles(diffs);
		        if(modifiedFiles != null && !modifiedFiles.isEmpty()) {
		        	try {
		        		gitRepositoryCommitedEvent.fire(new CommitEvent(gitRepository, modifiedFiles, diffs));
		        	} catch (Exception e) {
		        		// Roll-back repository state
		        		git.reset()
		        			.setRef(headCommitBeforePull.getName())
		        			.setMode(ResetType.HARD)
		        			.call();
		        		throw new BusinessException(e);
		        	}
		        }
		    }
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {

            branchCreated.fire(new GitBranch(branch, gitRepository));
            git.branchCreate().setName(branch).call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot create new branch " + branch + " for repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
            branchRemoved.fire(new GitBranch(branch, gitRepository));

            git.branchDelete().setBranchNames(branch).setForce(true).call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot delete branch " + branch + " for repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasReadRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
            return git.getRepository().getBranch();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        if (!gitRepository.getDefaultBranch().equals(branch) && gitRepository.isLocked()) {
            throw new BusinessException("Cannot checkout branch " + branch + " because it is blocked.");
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
        	RevCommit headCommitBeforePull = getHeadCommit(gitRepository);
        	
        	if(createBranch) {
        		// Don't create branch if already exist
        		createBranch = git.branchList().call()
	                .stream()
	                .map(Ref::getName)
	                .map(Repository::shortenRefName)
	                .noneMatch(branch::equals);
        	}
        	
            if(!git.getRepository().getBranch().equals(branch)) {
                var checkout = git.checkout()
                	.setUpstreamMode(SetupUpstreamMode.TRACK)
                	.setCreateBranch(createBranch)
                	.setName(branch);
                
                if (gitRepository.isRemote()) {
                	List<Ref> remoteBranches = git.branchList()
                		.setListMode(ListMode.REMOTE)
                		.call();
                	
                	remoteBranches.stream()
    	                .filter(ref -> ref.getName().endsWith(branch))
    	                .findFirst()
    	                .ifPresent(ref -> {
    	                	checkout.setStartPoint(ref.getName());
    	                });
                }
                
            	checkout.call();
                gitRepository.setCurrentBranch(branch);
                triggerCommitEvent(gitRepository, git, headCommitBeforePull);
            }

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot checkout branch " + branch + " for repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

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

            }  catch (JGitInternalException e) {
            	log.warn("Cannot merge {} into {}", from, to, e);
            	git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
            	return false;
            	
            }  catch (GitAPIException e) {
                throw new BusinessException("Cannot merge " + from + " into " + to, e);
            } finally {
                git.checkout().setCreateBranch(false).setName(previousBranch).call();
            }

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Checkout problem for repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
        }
    }
    
    public List<String> listRefs(GitRepository gitRepository) throws BusinessException {
    	MeveoUser user = currentUser.get();
        if (!GitHelper.hasReadRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }
        
        List<String> results = new ArrayList<>();

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
        	var repo = git.getRepository();
        	
            git.branchList()
        		.call()
                .stream()
                .map(Ref::getName)
                .map(Repository::shortenRefName)
                .forEach(results::add);
            
            git.branchList()
        		.setListMode(ListMode.REMOTE)
        		.call()
                .stream()
                .map(Ref::getName)
                .map(repo::shortenRemoteBranchName)
                .forEach(results::add);
            
            git.tagList()
            	.call()
	            .stream()
	            .map(Ref::getName)
	            .map(Repository::shortenRefName)
            	.forEach(results::add);
            
            return results.stream().distinct().collect(Collectors.toList());

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot list branches of repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasReadRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
            return git.branchList().call()
                    .stream()
                    .map(Ref::getName)
                    .map(Repository::shortenRefName)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot list branches of repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
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
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasReadRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
            try (RevWalk rw = new RevWalk(git.getRepository())) {
                ObjectId head = git.getRepository().resolve(Constants.HEAD);
                return rw.parseCommit(head);
            }
        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
        }
    }

    /**
     * Build the list of files modified in a given commit
     *
     * @param gitRepository Repository holding the commit
     * @param commit        The commit to analyze
     * @return the list of files modified
     */
    public List<DiffEntry> getDiffs(GitRepository gitRepository, RevCommit commit) throws BusinessException {
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasReadRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
            Repository repository = git.getRepository();
            try(RevWalk rw = new RevWalk(repository)) {
	            RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
	            var diffs = getDiffs(repository, parent, commit);
                return diffs;
            }
        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);


        } finally {
            keyLock.unlock(gitRepository.getCode());
        }
    }

    /**
     * Reset a repository to a given commit
     *
     * @param gitRepository {@link GitRepository} to reset
     * @param commit        Commit to reset onto
     */
    public void reset(GitRepository gitRepository, RevCommit commit) throws BusinessException {
        MeveoUser user = currentUser.get();
        if (!GitHelper.hasWriteRole(user, gitRepository)) {
            throw new UserNotAuthorizedException(user.getUserName());
        }

        final File repositoryDir = GitHelper.getRepositoryDir(user, gitRepository.getCode());

        keyLock.lock(gitRepository.getCode());

        try (Git git = Git.open(repositoryDir)) {
            git.reset().setMode(ResetCommand.ResetType.HARD)
                    .setRef(commit.getId().getName())
                    .call();

        } catch (IOException e) {
            throw new BusinessException("Cannot open repository " + gitRepository.getCode(), e);

        } catch (GitAPIException e) {
            throw new BusinessException("Cannot reset repository to commit " + commit.getId().getName(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
        }
    }
    
    protected List<DiffEntry> getDiffs(Repository repository, RevCommit leftCommit, RevCommit rightCommit) throws IOException {
        try(DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
            return df.scan(leftCommit.getTree(), rightCommit.getTree());
        }
    }

    /**
     * Compute difference between two commits for a given repository
     *
     * @param repository  {@link Repository} to scan
     * @param leftCommit  Left commit to compare
     * @param rightCommit Right commit to compare
     * @return the modified files between the two commits
     */
    public Set<String> getModifiedFiles(List<DiffEntry> diffs) throws IOException {
        Set<String> modifiedFiles = new HashSet<>();

        for (DiffEntry diff : diffs) {
            modifiedFiles.add(diff.getNewPath());
            modifiedFiles.add(diff.getOldPath());
        }

        return modifiedFiles;
    }

    protected void createGitMeveoFolder(GitRepository gitRepository, File repoDir) throws BusinessException {
        MeveoUser user = currentUser.get();
        keyLock.lock(gitRepository.getCode());
        try (Git git = Git.init().setDirectory(repoDir).call()){
            // Init repo with a dummy commit
            new File(repoDir, "README.md").createNewFile();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("First commit")
                    .setAuthor(user.getUserName(), user.getMail())
                    .setCommitter(user.getUserName(), user.getMail())
                    .call();
        } catch (GitAPIException | IOException e) {
            repoDir.delete();
            throw new BusinessException("Error initating repository " + gitRepository.getCode(), e);

        } finally {
            keyLock.unlock(gitRepository.getCode());
        }
    }

}
