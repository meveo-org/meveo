package org.meveo.api.job;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.MeveoApiErrorCodeEnum;
import org.meveo.api.dto.job.JobInstanceDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.jobs.JobCategoryEnum;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.jobs.TimerEntity;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.job.Job;
import org.meveo.service.job.JobExecutionService;
import org.meveo.service.job.JobInstanceService;
import org.meveo.service.job.TimerEntityService;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
@Stateless
public class JobInstanceApi extends BaseCrudApi<JobInstance, JobInstanceDto> {

    public JobInstanceApi() {
		super(JobInstance.class, JobInstanceDto.class);
	}

	@Inject
    private JobInstanceService jobInstanceService;

    @Inject
    private TimerEntityService timerEntityService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;
    
    @Inject
    private JobExecutionService jobExecutionService;

    public JobInstance create(JobInstanceDto postData) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(postData.getJobTemplate()) || StringUtils.isBlank(postData.getCode())) {

            if (StringUtils.isBlank(postData.getJobTemplate())) {
                missingParameters.add("jobTemplate");
            }
            if (StringUtils.isBlank(postData.getCode())) {
                missingParameters.add("code");
            }
            handleMissingParametersAndValidate(postData);
        }

        if (jobInstanceService.findByCode(postData.getCode()) != null) {
            throw new EntityAlreadyExistsException(JobInstance.class, postData.getCode());
        }

        Job job = jobInstanceService.getJobByName(postData.getJobTemplate());

        JobInstance jobInstance = fromDto(postData);

        // Create any missing CFT for a given provider and job
        Map<String, CustomFieldTemplate> jobCustomFields = job.getCustomFields();
        if (jobCustomFields != null) {
            customFieldTemplateService.createMissingTemplates(jobInstance, jobCustomFields.values());
        }

        // Populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), jobInstance, true);
        } catch (MissingParameterException | InvalidParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        try {
            jobInstanceService.create(jobInstance);
        } catch (BusinessException e1) {
            throw new MeveoApiException(e1.getMessage());
        }

        return jobInstance;
    }

    /**
     * Updates JobInstance based on Code.
     * 
     * @param postData posted data to API.
     * @return job instance
     * @throws MeveoApiException meveo api exception
     * @throws BusinessException business exception.
     */
    public JobInstance update(JobInstanceDto postData) throws MeveoApiException, BusinessException {

        String jobInstanceCode = postData.getCode();

        if (StringUtils.isBlank(jobInstanceCode)) {
            missingParameters.add("code");
        }

        handleMissingParametersAndValidate(postData);

        JobInstance jobInstance = jobInstanceService.findByCode(jobInstanceCode, List.of("executionResults"));

        if (jobInstance == null) {
            throw new EntityDoesNotExistsException(JobInstance.class, jobInstanceCode);
        }

		// need to cancel existing attached timer
		if ((jobInstance.getTimerEntity() != null && !jobInstance.getTimerEntity().getCode().equals(postData.getTimerCode()))) {
			jobInstanceService.scheduleUnscheduleJob(jobInstance.getId());
		}
		
        Job job = jobInstanceService.getJobByName(postData.getJobTemplate());
        JobCategoryEnum jobCategory = job.getJobCategory();

        jobInstance.setJobTemplate(postData.getJobTemplate());
        jobInstance.setParametres(postData.getParameter());
        jobInstance.setActive(postData.isActive());
        jobInstance.setJobCategoryEnum(jobCategory);
        jobInstance.setDescription(postData.getDescription());
        jobInstance.setCode(StringUtils.isBlank(postData.getUpdatedCode()) ? postData.getCode() : postData.getUpdatedCode());

        if (postData.getRunOnNodes() != null) {
            jobInstance.setRunOnNodes(postData.getRunOnNodes());
        }
        if (postData.getLimitToSingleNode() != null) {
            jobInstance.setLimitToSingleNode(postData.getLimitToSingleNode());
        }

        if (!StringUtils.isBlank(postData.getTimerCode())) {
            TimerEntity timerEntity = timerEntityService.findByCode(postData.getTimerCode());
            jobInstance.setTimerEntity(timerEntity);
            if (timerEntity == null) {
                throw new MeveoApiException(MeveoApiErrorCodeEnum.BUSINESS_API_EXCEPTION, "Invalid timer entity=" + postData.getTimerCode());
            }
        }

        if (!StringUtils.isBlank(postData.getFollowingJob())) {
            JobInstance nextJob = jobInstanceService.findByCode(postData.getFollowingJob());
            jobInstance.setFollowingJob(nextJob);
            if (nextJob == null) {
                throw new MeveoApiException(MeveoApiErrorCodeEnum.BUSINESS_API_EXCEPTION, "Invalid next job=" + postData.getFollowingJob());
            }
        }

        // Create any missing CFT for a given provider and job
        Map<String, CustomFieldTemplate> jobCustomFields = job.getCustomFields();
        if (jobCustomFields != null) {
            customFieldTemplateService.createMissingTemplates(jobInstance, jobCustomFields.values());
        }

        // Populate customFields
        try {
            populateCustomFields(postData.getCustomFields(), jobInstance, false);
        } catch (MissingParameterException | InvalidParameterException e) {
            log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to associate custom field instance to an entity", e);
            throw e;
        }

        jobInstance = jobInstanceService.update(jobInstance);

        return jobInstance;
    }

    /**
     * Create or update Job Instance based on code.
     * 
     * @param jobInstanceDto job instance dto
     * 
     * @throws MeveoApiException meveo api excepion
     * @throws BusinessException business exception.
     */
    public JobInstance createOrUpdate(JobInstanceDto jobInstanceDto) throws MeveoApiException, BusinessException {
        if (jobInstanceService.findByCode(jobInstanceDto.getCode()) == null) {
            return create(jobInstanceDto);
        } else {
            return update(jobInstanceDto);
        }
    }

    /**
     * Retrieves a Job Instance base on the code if it is existing.
     * 
     * @param code job instance code
     * @return job instance dto
     * @throws MeveoApiException meveo api exception.
     */
    public JobInstanceDto find(String code) throws EntityDoesNotExistsException, MissingParameterException, InvalidParameterException, MeveoApiException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
            handleMissingParameters();
        }

        JobInstance jobInstance = jobInstanceService.findByCode(code, Arrays.asList("timerEntity"));
        if (jobInstance == null) {
            throw new EntityDoesNotExistsException(JobInstance.class, code);
        }

        JobInstanceDto jobInstanceDto = jobInstanceToDto(jobInstance);
        return jobInstanceDto;

    }

    /**
     * 
     * Removes a Job Instance base on a code.
     * 
     * @param code job instance code
     * @throws MeveoApiException meveo api exception
     * @throws BusinessException business exception.
     */
    public void remove(String code) throws MeveoApiException, BusinessException {
        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
            handleMissingParameters();
        }
        JobInstance jobInstance = jobInstanceService.findByCode(code);
        if (jobInstance == null) {
            throw new EntityDoesNotExistsException(JobInstance.class, code);
        }
        jobInstanceService.remove(jobInstance);
    }

    private JobInstanceDto jobInstanceToDto(JobInstance jobInstance) {
        JobInstanceDto dto = new JobInstanceDto();

        dto.setCode(jobInstance.getCode());
        dto.setActive(jobInstance.isActive());

        dto.setDescription(jobInstance.getDescription());

        dto.setJobCategory(jobInstance.getJobCategoryEnum());
        dto.setJobTemplate(jobInstance.getJobTemplate());
        dto.setParameter(jobInstance.getParametres());

        if (jobInstance.getTimerEntity() != null) {
            dto.setTimerCode(jobInstance.getTimerEntity().getCode());
        }

        dto.setRunOnNodes(jobInstance.getRunOnNodes());
        dto.setLimitToSingleNode(jobInstance.isLimitToSingleNode());
        
        dto.setCustomFields(entityToDtoConverter.getCustomFieldsDTO(jobInstance, true));

        if (jobInstance.getFollowingJob() != null) {
            dto.setFollowingJob(jobInstance.getFollowingJob().getCode());
        }

        return dto;
    }

	@Override
	public JobInstanceDto toDto(JobInstance entity) {
		return jobInstanceToDto(entity);
	}

	@Override
	public JobInstance fromDto(JobInstanceDto dto) throws MeveoApiException {
        Job job = jobInstanceService.getJobByName(dto.getJobTemplate());
        JobCategoryEnum jobCategory = job.getJobCategory();
        
		JobInstance jobInstance = new JobInstance();
        jobInstance.setActive(dto.isActive());
        jobInstance.setParametres(dto.getParameter());
        jobInstance.setJobCategoryEnum(jobCategory);
        jobInstance.setJobTemplate(dto.getJobTemplate());
        jobInstance.setCode(dto.getCode());
        jobInstance.setDescription(dto.getDescription());
        jobInstance.setRunOnNodes(dto.getRunOnNodes());
        if (dto.getLimitToSingleNode() != null) {
            jobInstance.setLimitToSingleNode(dto.getLimitToSingleNode());
        }

        if (!StringUtils.isBlank(dto.getTimerCode())) {
            TimerEntity timerEntity = timerEntityService.findByCode(dto.getTimerCode());
            jobInstance.setTimerEntity(timerEntity);
            if (timerEntity == null) {
                throw new MeveoApiException(MeveoApiErrorCodeEnum.BUSINESS_API_EXCEPTION, "Invalid timer entity=" + dto.getTimerCode());
            }
        }

        if (!StringUtils.isBlank(dto.getFollowingJob())) {
            JobInstance nextJob = jobInstanceService.findByCode(dto.getFollowingJob());
            jobInstance.setFollowingJob(nextJob);
            if (nextJob == null) {
                throw new MeveoApiException(MeveoApiErrorCodeEnum.BUSINESS_API_EXCEPTION, "Invalid next job=" + dto.getFollowingJob());
            }
        }
        
		return jobInstance;
	}

	@Override
	public IPersistenceService<JobInstance> getPersistenceService() {
		return jobInstanceService;
	}

	@Override
	public boolean exists(JobInstanceDto dto) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove(JobInstanceDto dto) throws MeveoApiException, BusinessException {
		// TODO Auto-generated method stub
		
	}
}