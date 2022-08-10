package org.meveo.services.job;

import java.util.*;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.CustomFieldBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.cache.JobCacheContainerProvider;
import org.meveo.cache.JobRunningStatusEnum;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.elresolver.ELException;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.jobs.JobCategoryEnum;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.jobs.TimerEntity;
import org.meveo.model.util.KeyValuePair;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.job.Job;
import org.meveo.service.job.JobExecutionService;
import org.meveo.service.job.JobInstanceService;
import org.meveo.util.view.ServiceBasedLazyDataModel;
import org.primefaces.model.LazyDataModel;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Named
@ViewScoped
public class JobInstanceBean extends CustomFieldBean<JobInstance> {

    private static final long serialVersionUID = 1L;

    @Inject
    private JobInstanceService jobInstanceService;

    @Inject
    private JobExecutionService jobExecutionService;

    @Inject
    private JobCacheContainerProvider jobCacheContainerProvider;

    @Inject
    protected CustomFieldInstanceService customFieldInstanceService;
    
    private List<JobInstance> followingJosbs;
    
    private TimerEntity prevTimerEntity;

	private Set<KeyValuePair> overrideParams;

    public JobInstanceBean() {
        super(JobInstance.class);
    }

    @Override
    public JobInstance initEntity() {
        entity = super.initEntity();
        
		if (entity.getTimerEntity() != null) {
			prevTimerEntity = entity.getTimerEntity();
		}

        try {
            refreshCustomFieldsAndActions();
        } catch (BusinessException e) {
        }

        if (entity.getId() != null && overrideParams == null) {
            Map<String, Object> context = (Map<String, Object>) customFieldInstanceService.getCFValue(entity, "ScriptingJob_variables");
            if (context != null && !context.isEmpty()) {
                overrideParams  = new HashSet<>();
                hasParams = true;
                for (Map.Entry<String, Object> entry : context.entrySet()) {
                    overrideParams.add(new KeyValuePair(entry.getKey(), entry.getValue()));
                }
            }
        }
        
        followingJosbs = jobInstanceService.list();
        followingJosbs.remove(entity);
        followingJosbs.sort((jobA, jobB) -> jobA.getCode().compareTo(jobB.getCode()));
        
        return entity;
    }

    @Override
    protected IPersistenceService<JobInstance> getPersistenceService() {
        return jobInstanceService;
    }

    public List<JobCategoryEnum> getJobCategoryEnumValues() {
        return Arrays.asList(JobCategoryEnum.values());
    }

    /**
     * Get a list of jobs suitable as a next job to execute (all jobs, minus a current one)
     * 
     * @return A list of jobs minus a current one
     */
    public List<JobInstance> getFollowingJobList() {
        return followingJosbs;
    }

    public List<String> getJobNames() {
        if (entity.getJobCategoryEnum() == null) {
            return null;
        }
        return jobInstanceService.getJobNames(entity.getJobCategoryEnum());
    }

    public String execute() {
        try {
            jobExecutionService.manualExecute(entity, null);
            messages.info(new BundleKey("messages", "info.entity.executed"), entity.getJobTemplate());
        } catch (Exception e) {
            messages.error(new BundleKey("messages", "error.execution"));
            return null;
        }

        return getEditViewName();
    }

    public String executeWithParameters() {
        try {
            Map<String, Object> params = new HashMap<>();
            if (CollectionUtils.isNotEmpty(overrideParams)) {
                params = new HashMap<>();
                for (KeyValuePair param : overrideParams) {
                    params.put(param.getKey(), param.getValue());
                }
            }
            jobExecutionService.manualExecute(entity, params);
            messages.info(new BundleKey("messages", "info.entity.executed"), entity.getJobTemplate());
        } catch (Exception e) {
            messages.error(new BundleKey("messages", "error.execution"));
        }
        return null;
    }
    
    public String stop() {
        try {
            jobExecutionService.stopJob(entity);
            messages.info(new BundleKey("messages", "info.entity.stopped"), entity.getJobTemplate());
        } catch (Exception e) {
            messages.error(new BundleKey("messages", "error.execution"));
            return null;
        }

        return getEditViewName();
    }

	@Override
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

        if (entity.getFollowingJob() != null) {
            JobInstance jobInstance = jobInstanceService.findById(entity.getFollowingJob().getId(), getFormFieldsToFetch());
            entity.setFollowingJob(jobInstance);
        }

		// need to cancel existing attached timer
		if (prevTimerEntity != entity.getTimerEntity()) {
			jobInstanceService.scheduleUnscheduleJob(entity.getId());
		}

		super.saveOrUpdate(killConversation);

		return getEditViewName();
	}

    @Override
    protected String getListViewName() {
        return "jobInstances";
    }

    /**
     * Get JobInstance name from a jobId
     * 
     * @param jobId
     * @return timename
     */
    public String translateToTimerName(Long jobId) {
        if (jobId != null) {
            JobInstance jobInstance = jobInstanceService.findById(jobId);
            if (jobInstance != null) {
                return jobInstance.getCode();
            }
        }
        return null;
    }

    /**
     * Synchronize definition of custom field templates specified in Job class to those found in DB. Register in DB if was missing.
     */
    private void createMissingCustomFieldTemplates() {
        if (entity.getJobTemplate() == null) {
            return;
        }

        // Get job definition and custom field templates defined in a job
        Job job = jobInstanceService.getJobByName(entity.getJobTemplate());
        if (job == null) {
            return;
        }
        Map<String, CustomFieldTemplate> jobCustomFields = job.getCustomFields();

        // Create missing custom field templates if needed
        Collection<CustomFieldTemplate> jobTemplatesFromJob = null;
        if (jobCustomFields == null) {
            jobTemplatesFromJob = new ArrayList<CustomFieldTemplate>();
        } else {
            jobTemplatesFromJob = jobCustomFields.values();
        }
        try {
            customFieldTemplateService.createMissingTemplates((ICustomFieldEntity) entity, jobTemplatesFromJob);
        } catch (BusinessException e) {
            log.error("Failed to create missing custom field templates", e);
        }
    }

    /**
     * Check if a job is running and where
     * 
     * @param jobInstance JobInstance entity
     * @return Running status
     */
    public JobRunningStatusEnum isTimerRunning(JobInstance jobInstance) {
        return jobCacheContainerProvider.isJobRunning(jobInstance.getId());
    }

    /**
     * Check if job can be run on a current server or cluster node if deployed in cluster environment
     * 
     * @param jobInstance JobInstance entity
     * @return True if it can be executed locally
     */
    public boolean isAllowedToExecute(JobInstance jobInstance) {
		if (jobInstance == null || jobInstance.getId() == null) {
			return false;
		}
    	
        JobRunningStatusEnum isRunning = jobCacheContainerProvider.isJobRunning(jobInstance.getId());
        if (isRunning == JobRunningStatusEnum.NOT_RUNNING) {
            return true;
        } else if (isRunning == JobRunningStatusEnum.RUNNING_THIS) {
            return false;
        } else {

            String nodeToCheck = EjbUtils.getCurrentClusterNode();
            return jobInstance.isRunnableOnNode(nodeToCheck);
        }
    }

    /**
     * Explicitly refresh custom fields and action definitions. Should be used when job template change, as on it depends what fields and actions apply
     * 
     * @throws BusinessException
     */
    public void refreshCustomFieldsAndActions() throws BusinessException {

        createMissingCustomFieldTemplates();
        customFieldDataEntryBean.refreshFieldsAndActions(entity);
    }

    public Set<KeyValuePair> getOverrideParams() {
        return overrideParams;
    }

    @Override
    protected List<String> getFormFieldsToFetch() {
        return Arrays.asList("timerEntity", "followingJob");
    }
    
	@Override
	protected List<String> getListFieldsToFetch() {
		return Arrays.asList("timerEntity");
	}
    
    @Override
    public void enable() {    	
    	super.enable();
    	initEntity();
    }
    
    @Override
    public void disable() {
    	super.disable();
    	initEntity();
    }

    @Override
    public boolean isHasParams() {
        return hasParams;
    }
    
    public LazyDataModel<JobExecutionResultImpl> getExecutionResults() {
    	JobExecutionService jobExecServiceForJob = entity == null || entity.getCode() == null ? jobExecutionService : new JobExecutionService() {
    		
    		@Override
    		public JobExecutionResultImpl findById(Long id) {
    			return jobExecutionService.findById(id);
    		}
    		
    		@Override
    		public long count() {
				return jobExecutionService.count(entity.getCode(), new PaginationConfiguration());
    		}
    		
    		@Override
    		public long count(PaginationConfiguration config) {
				return jobExecutionService.count(entity.getCode(), config);
    		}

			@Override
			public List<JobExecutionResultImpl> list(PaginationConfiguration config) {
				return jobExecutionService.find(entity.getCode(), config);
			}
    		
    	};
    	
    	return new ServiceBasedLazyDataModel<JobExecutionResultImpl>() {

			private static final long serialVersionUID = -6476614953254588085L;

			@Override
			protected Map<String, Object> getSearchCriteria() {
				return new HashMap<>();
			}

			@Override
			protected IPersistenceService<JobExecutionResultImpl> getPersistenceServiceImpl() {
				return jobExecServiceForJob;
			}
		};
    }
}