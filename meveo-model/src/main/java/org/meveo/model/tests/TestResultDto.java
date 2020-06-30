/**
 * 
 */
package org.meveo.model.tests;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.meveo.model.IEntity;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class TestResultDto implements IEntity<Long> {

	private JobInstance job;

	private JobExecutionResultImpl latestjobExecResult;

	public TestResultDto(JobExecutionResultImpl latestjobExecResult) {
		super();
		this.latestjobExecResult = latestjobExecResult;
		this.job = latestjobExecResult.getJobInstance();
	}

	public String getFunctionCode() {
		return job.getParametres();
	}
	
	public JobExecutionResultImpl getLatestjobExecResult() {
		return latestjobExecResult;
	}
	
	public void setLatestjobExecResult(JobExecutionResultImpl latestjobExecResult) {
		this.latestjobExecResult = latestjobExecResult;
	}

	public Date getLastExecutionDate() {
		return latestjobExecResult.getEndDate();
	}

	public long getNbKo() {
		return latestjobExecResult.getNbItemsProcessedWithError();
	}

	public long getNbOk() {
		return latestjobExecResult.getNbItemsCorrectlyProcessed();
	}

	public long getNbWarnings() {
		return latestjobExecResult.getNbItemsProcessedWithWarning();
	}

	public boolean isStable() {
		return getNbKo() == 0;
	}

	@Override
	public Long getId() {
		return job.getId();
	}

	@Override
	public void setId(Long id) {

	}

	@Override
	public boolean isTransient() {
		return false;
	}

}