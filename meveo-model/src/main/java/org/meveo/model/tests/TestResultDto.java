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
	
	private long nbOk;
	private long nbKo;
	private long nbWarning;
	private String functionCode;
	private Date endDate;

	private Long id;
	
	public TestResultDto() {
		
	}

	public TestResultDto(JobExecutionResultImpl latestjobExecResult) {
		super();
		this.latestjobExecResult = latestjobExecResult;
		this.job = latestjobExecResult.getJobInstance();
	}
	
	public TestResultDto(long id, long nbOk, long nbKo, long nbWarning, String functionCode, Date endDate) {
		super();
		this.id = id;
		this.nbOk = nbOk;
		this.nbKo = nbKo;
		this.nbWarning = nbWarning;
		this.functionCode = functionCode;
		this.endDate = endDate;
	}

	public String getFunctionCode() {
		return functionCode;//job.getParametres();
	}
	
	public JobExecutionResultImpl getLatestjobExecResult() {
		return latestjobExecResult;
	}
	
	public void setLatestjobExecResult(JobExecutionResultImpl latestjobExecResult) {
		this.latestjobExecResult = latestjobExecResult;
	}

	public Date getLastExecutionDate() {
		return this.endDate;//return latestjobExecResult.getEndDate();
	}

	public long getNbKo() {
		return nbKo;//return latestjobExecResult.getNbItemsProcessedWithError();
	}

	public long getNbOk() {
		return nbOk;//return latestjobExecResult.getNbItemsCorrectlyProcessed();
	}

	public long getNbWarnings() {
		return nbWarning;//return latestjobExecResult.getNbItemsProcessedWithWarning();
	}
	
	public boolean isStable() {
		return getNbKo() == 0;
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	public JobInstance getJob() {
		return job;
	}

	public void setJob(JobInstance job) {
		this.job = job;
	}

	public long getNbWarning() {
		return nbWarning;
	}

	public void setNbWarning(long nbWarning) {
		this.nbWarning = nbWarning;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setNbOk(long nbOk) {
		this.nbOk = nbOk;
	}

	public void setNbKo(long nbKo) {
		this.nbKo = nbKo;
	}

	public void setFunctionCode(String functionCode) {
		this.functionCode = functionCode;
	}
	
	

}