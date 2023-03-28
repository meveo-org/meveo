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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.cache.CacheKeyLong;
import org.meveo.commons.utils.ParamBean;
import org.meveo.event.qualifier.Created;
import org.meveo.model.BusinessEntity;
import org.meveo.model.git.GitRepository;
import org.meveo.model.jobs.TimerEntity;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.security.PasswordUtils;
import org.meveo.service.base.BusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistence class for GitRepository
 *
 * @author Clement Bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @since 6.4.0
 * @version 6.9.0
 */
@Stateless
public class GitRepositoryService extends BusinessService<GitRepository> {

    public static GitRepository MEVEO_DIR;

    static {
        final ParamBean paramBean = ParamBean.getInstance();
        final String remoteUrl = paramBean.getProperty("meveo.git.directory.remote.url", null);
        final String remoteUsername = paramBean.getProperty("meveo.git.directory.remote.username", null);
        final String remotePassword = paramBean.getProperty("meveo.git.directory.remote.password", null);

        MEVEO_DIR = new GitRepository();
        MEVEO_DIR.setCode("Meveo");
        MEVEO_DIR.setRemoteOrigin(remoteUrl);
        MEVEO_DIR.setDefaultRemoteUsername(remoteUsername);
        MEVEO_DIR.setClearDefaultRemotePassword(remotePassword);
    }
    
    @Resource
    protected TimerService timerService;

    @Inject
    private GitClient gitClient;

    private static Logger log = LoggerFactory.getLogger(GitRepositoryService.class);

    @Inject
    @CurrentUser
    private MeveoUser currentUser;
    
    private static Map<CacheKeyLong, Timer> autoPullTimers = new HashMap<>();

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

        return MEVEO_DIR;
    }

    public void createGitMeveoFolder(GitRepository gitRepository) throws BusinessException {
        File dir = GitHelper.getRepositoryDir(currentUser, gitRepository);
        if(dir.exists() && new File(dir, ".git").exists()) {
            return;
        }

        dir.mkdirs();
        gitClient.createGitMeveoFolder(gitRepository, dir);
    }

    @Override
    public GitRepository findByCode(String code) {
        final GitRepository repository = super.findByCode(code);

        if (repository != null && !GitHelper.hasReadRole(currentUser, repository)) {
            throw new UserNotAuthorizedException(currentUser.getUserName());
        }
        //setBranchInformation(repository);

        return repository;
    }

    @Override
	public GitRepository findById( Long id) {
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
	public GitRepository findById( Long id, boolean refresh) {
		GitRepository repo = super.findById(id, refresh);
		setBranchInformation(repo);
		return repo;
	}

	@Override
	public GitRepository findById( Long id, List<String> fetchFields) {
		GitRepository repo = super.findById(id, fetchFields);
		setBranchInformation(repo);
		return repo;
	}

	@Override
	public GitRepository findById( Long id, List<String> fetchFields, boolean refresh) {
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
	

    @Override
	public List<GitRepository> list(PaginationConfiguration config) {
		return super.list(config);
	}

	/**
     * Remove the GitRepository entity along with the corresponding repository in file system
     *
     * @param entity Repository to remove
     */
    @Override
    public void remove( GitRepository entity) throws BusinessException {
        super.remove(entity);
        gitClient.remove(entity);
        
        if(entity.getAutoPullTimer() != null) {
        	unScheduleAutoPull(entity);
        }
    }

    /**
     * Create the GitRepository entity along with the corresponding repository in file system
     *
     * @param entity Repository to create
     */
    @Override
    public void create( GitRepository entity) throws BusinessException {
        gitClient.create(entity, false, null, null);
        super.create(entity);
        
        if(entity.getAutoPullTimer() != null) {
        	scheduleAutoPull(entity);
        }
    }

    @Transactional
    public GitRepository create( GitRepository entity, boolean failIfExist, String username, String password) throws BusinessException {
        gitClient.create(entity, failIfExist, username, password);
        
        super.create(entity);
        
        if(entity.getAutoPullTimer() != null) {
        	scheduleAutoPull(entity);
        }
        
        return entity;
    }
    
    @Override
	public GitRepository update(GitRepository entity) throws BusinessException {
    	super.update(entity);
    	
    	gitClient.checkout(entity, entity.getDefaultBranch(), true);
    	if (entity.getRemoteOrigin() != null) {
    		gitClient.setRemote(entity);
    	}
    	
    	Timer autoPullTimer = autoPullTimers.get(new CacheKeyLong(currentUser.getProviderCode(), entity.getId()));
    	if(entity.getAutoPullTimer() != null) {
    		if(autoPullTimer == null) {
    			// no timer was set before
    			scheduleAutoPull(entity);
    		} else if(!autoPullTimer.getSchedule().equals(getScheduleExpression(entity.getAutoPullTimer()))) {
				// timer was changed
				unScheduleAutoPull(entity);
				scheduleAutoPull(entity);
    		}
    	} else if(entity.getAutoPullTimer() == null && autoPullTimer != null) {
    		// timer was removed
    		unScheduleAutoPull(entity);
    	}
    	
		return entity;
	}

    public GitRepository setBranchInformation(GitRepository repository) {
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
    
    /**
     * Removes the files on hard drive when creation fails
     * 
     * @param repo the repository
     * @throws BusinessException if the files can't be removed
     */
    public void onCreationFailed(@Observes(during = TransactionPhase.AFTER_FAILURE) @Created GitRepository repo) throws BusinessException {
    	gitClient.remove(repo);
    }
    
    public void scheduleAutoPull(GitRepository repo) {
    	ScheduleExpression scheduleExpression = getScheduleExpression(repo.getAutoPullTimer());
    	TimerConfig timerConfig = new TimerConfig();
    	timerConfig.setInfo("auto_pull_" + repo.getId());
    	timerConfig.setPersistent(false);
    	
    	Timer timer = timerService.createCalendarTimer(scheduleExpression, timerConfig);
    	
    	autoPullTimers.put(new CacheKeyLong(currentUser.getProviderCode(), repo.getId()), timer);
    	
    	log.info("Scheduled auto pull for git repo " + repo.getCode());
    }
     
    public void unScheduleAutoPull(GitRepository repo) {
    	Timer autoPullTimer = autoPullTimers.get(new CacheKeyLong(currentUser.getProviderCode(), repo.getId()));
    	
    	if(autoPullTimer != null) {
    		autoPullTimer.cancel();
    	}
    	autoPullTimers.remove(new CacheKeyLong(currentUser.getProviderCode(), repo.getId()));
    	
    	log.info("Unscheduled auto pull for git repo " + repo.getCode());
    }
    
    public void scheduleAllAutoPulls() {
    	List<GitRepository> activeRepos = this.listActive();
    	for(GitRepository repo : activeRepos) {
    		if(repo.getAutoPullTimer() != null) {
    			scheduleAutoPull(repo);
    		}
    	}
    }
    
    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void trigger(Timer timer) {
    	if(timer.getInfo() instanceof String && ((String) timer.getInfo()).startsWith("auto_pull_")) {
    		GitRepository gitRepo = this.findById(Long.valueOf(((String) timer.getInfo()).replaceAll("auto_pull_", "")));
    		if(gitRepo != null) {    			
    			String username = gitRepo.getDefaultRemoteUsername();
    			String password = PasswordUtils.decrypt(gitRepo.getSalt(), gitRepo.getDefaultRemotePassword());
    			
    			try {
    				gitClient.pull(gitRepo, username, password);
    				log.info("Auto pull on repo " + gitRepo.getCode());
    			} catch (BusinessException e) {
    				log.error("Couldn't auto pull on repo " + gitRepo.getCode(), e);
    			}
    		} else {    			
    			log.error("Couldn't find repo for auto pull with id " + ((String) timer.getInfo()).replaceAll("auto_pull_", ""));
    		}
    	}
    }
    
    private ScheduleExpression getScheduleExpression(TimerEntity timerEntity) {
        ScheduleExpression expression = new ScheduleExpression();
        expression.dayOfMonth(timerEntity.getDayOfMonth());
        expression.dayOfWeek(timerEntity.getDayOfWeek());
        expression.end(timerEntity.getEnd());
        expression.hour(timerEntity.getHour());
        expression.minute(timerEntity.getMinute());
        expression.month(timerEntity.getMonth());
        expression.second(timerEntity.getSecond());
        expression.start(timerEntity.getStart());
        expression.year(timerEntity.getYear());
        expression.timezone(timerEntity.getTimezone());
        return expression;
    }
}
