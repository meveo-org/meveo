package org.meveo.api.dto.notification;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.notification.JobTrigger;
import org.meveo.model.notification.Notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Part of the notification package that handles job trigger.
 *
 * @author Tyshan Shi
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "JobTrigger")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("JobTriggerDto")
public class JobTriggerDto extends NotificationDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The job params. */
	@ApiModelProperty("Map of job params")
	private Map<String, String> jobParams = new HashMap<String, String>();

	/** The job instance. */
	@XmlElement(required = true)
	@ApiModelProperty(required = true, value = "The job instance")
	private String jobInstance;

	/**
	 * Gets the job params.
	 *
	 * @return the job params
	 */
	public Map<String, String> getJobParams() {
		return jobParams;
	}

	/**
	 * Sets the job params.
	 *
	 * @param jobParams the job params
	 */
	public void setJobParams(Map<String, String> jobParams) {
		this.jobParams = jobParams;
	}

	/**
	 * Gets the job instance.
	 *
	 * @return the job instance
	 */
	public String getJobInstance() {
		return jobInstance;
	}

	/**
	 * Sets the job instance.
	 *
	 * @param jobInstance the new job instance
	 */
	public void setJobInstance(String jobInstance) {
		this.jobInstance = jobInstance;
	}

	/**
	 * Instantiates a new job trigger dto.
	 */
	public JobTriggerDto() {
		super();
	}

	/**
	 * Instantiates a new job trigger dto.
	 *
	 * @param jobTrigger the job trigger
	 */
	public JobTriggerDto(JobTrigger jobTrigger) {
		super((Notification) jobTrigger);
		if (jobTrigger.getJobParams() != null) {
			jobParams.putAll(jobTrigger.getJobParams());
		}
		if (jobTrigger.getJobInstance() != null) {
			jobInstance = jobTrigger.getJobInstance().getCode();
		}
	}

	@Override
	public String toString() {
		return "JobTriggerDto [jobParams=" + jobParams + ", jobInstance=" + jobInstance + "]";
	}

}