/**
 * 
 */
package org.meveo.admin.async;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.job.FlatFileProcessingJob;
import org.meveo.admin.job.UnitFlatFileProcessingJobBean;
import org.meveo.commons.parsers.IFileParser;
import org.meveo.commons.parsers.RecordContext;
import org.meveo.model.crm.Provider;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.crm.impl.ImportWarningException;
import org.meveo.service.job.JobExecutionService;
import org.meveo.service.script.Script;
import org.meveo.service.script.ScriptInterface;
import org.meveo.util.ApplicationProvider;
import org.slf4j.Logger;

/**
 * Asynchronous FlatFile processing.
 * 
 * @author anasseh
 * @lastModifiedVersion willBeSetLater
 *
 */

@Stateless
public class FlatFileProcessingAsync {

	private static final String THREAD_POOL_SIZE = FlatFileProcessingJob.FLAT_FILE_PROCESSING_JOB_THREAD_POOL_SIZE;

	/** The log. */
	@Inject
	private Logger log;

	/** The unit flat file processing job bean. */
	@Inject
	private UnitFlatFileProcessingJobBean unitFlatFileProcessingJobBean;

	/** The job execution service. */
	@Inject
	private JobExecutionService jobExecutionService;

	@Inject
	@CurrentUser
	protected MeveoUser currentUser;

	@Inject
	@ApplicationProvider
	protected Provider appProvider;

	/**
	 * Read/parse file and execute script for each line.
	 * 
	 * @param fileParser         FlatFile parser
	 * @param result             job execution result
	 * @param script             script to execute
	 * @param recordVariableName record var name
	 * @param fileName           file name
	 * @param originFilename     originFilename var name
	 * @param errorAction        action to do on error : continue, stop or rollback
	 *                           after an error
	 * @return Future of FlatFileAsyncListResponse
	 * @throws Exception Exception
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Future<FlatFileAsyncListResponse> launchAndForget(IFileParser fileParser, JobExecutionResultImpl result,
			ScriptInterface script, String recordVariableName, String fileName, String originFilename, String errorAction)
			throws Exception {
		long cpLines = 0;
		FlatFileAsyncListResponse flatFileAsyncListResponse = new FlatFileAsyncListResponse();
		JobInstance jobInstance = result.getJobInstance();
		CustomFieldValues values = jobInstance.getCfValuesNullSafe();
		Long threadPoolSize = jobInstance != null ? (Long) values.getValue(THREAD_POOL_SIZE) : null;
		int parallelism = threadPoolSize != null ? threadPoolSize.intValue() : 1;
		ForkJoinPool pool = new ForkJoinPool(parallelism);
		final AtomicBoolean doStop = new AtomicBoolean(false);
		final AtomicReference<BusinessException> rollBackException = new AtomicReference<BusinessException>();

		while (fileParser.hasNext() && jobExecutionService.isJobRunningOnThis(jobInstance) && !doStop.get()) {
			RecordContext recordContext = null;
			cpLines++;
			FlatFileAsyncUnitResponse flatFileAsyncResponse = new FlatFileAsyncUnitResponse();
			flatFileAsyncResponse.setLineNumber(cpLines);
			flatFileAsyncListResponse.getResponses().add(flatFileAsyncResponse);
			try {
				recordContext = fileParser.getNextRecord();
				final RecordContext recordContextFinal = recordContext;
				flatFileAsyncResponse.setLineRecord(recordContext.getLineContent());
				log.trace("record line content:{}", recordContext.getLineContent());
				if (recordContext.getRecord() == null) {
					throw new Exception(recordContext.getReason());
				}
				Map<String, Object> executeParams = new HashMap<String, Object>();
				executeParams.put(recordVariableName, recordContextFinal.getRecord());
				executeParams.put(originFilename, fileName);
				if (FlatFileProcessingJob.ROLLBBACK.equals(errorAction)) {
					executeParams.put(Script.CONTEXT_CURRENT_USER, currentUser);
					executeParams.put(Script.CONTEXT_APP_PROVIDER, appProvider);
					script.execute(executeParams);
					flatFileAsyncResponse.setSuccess(true);
				} else if (FlatFileProcessingJob.STOP.equals(errorAction)) {
					unitFlatFileProcessingJobBean.execute(script, executeParams);
					flatFileAsyncResponse.setSuccess(true);
				} else {
					while (pool.hasQueuedSubmissions()) {
						Thread.sleep(10);
					}

					pool.submit(new Runnable() {
						@Override
						public void run() {
							try {
								unitFlatFileProcessingJobBean.execute(script, executeParams);
								flatFileAsyncResponse.setSuccess(true);
							} catch (ImportWarningException e) {
								String erreur = (recordContextFinal == null || recordContextFinal.getReason() == null) ? e.getMessage()
										: recordContextFinal.getReason();
								log.warn("record on warning :" + erreur);
								flatFileAsyncResponse.setSuccess(true);
								flatFileAsyncResponse.setWarning(true);
								flatFileAsyncResponse.setReason(erreur);
							} catch (Throwable e) {
								if (FlatFileProcessingJob.ROLLBBACK.equals(errorAction)) {
									rollBackException.set(new BusinessException(e.getMessage(), e));
									doStop.set(true);
								}
								String erreur = (recordContextFinal == null || recordContextFinal.getReason() == null) ? e.getMessage()
										: recordContextFinal.getReason();
								log.warn("record on error :" + erreur);
								flatFileAsyncResponse.setSuccess(false);
								flatFileAsyncResponse.setReason(erreur);
								if (FlatFileProcessingJob.STOP.equals(errorAction)) {
									doStop.set(true);
								}
							}
						}
					});
				}

			} catch (ImportWarningException e) {
				String erreur = (recordContext == null || recordContext.getReason() == null) ? e.getMessage()
						: recordContext.getReason();
				log.warn("record on warning :" + erreur);
				flatFileAsyncResponse.setSuccess(true);
				flatFileAsyncResponse.setWarning(true);
				flatFileAsyncResponse.setReason(erreur);
			} catch (Throwable e) {
				if (FlatFileProcessingJob.ROLLBBACK.equals(errorAction)) {
					throw new BusinessException(e.getMessage(), e);
				}
				String erreur = (recordContext == null || recordContext.getReason() == null) ? e.getMessage()
						: recordContext.getReason();
				log.warn("record on error :" + erreur);
				flatFileAsyncResponse.setSuccess(false);
				flatFileAsyncResponse.setReason(erreur);
				if (FlatFileProcessingJob.STOP.equals(errorAction)) {
					doStop.set(true);
				}
			}
		}
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.HOURS);

		if (rollBackException.get() != null)
			throw rollBackException.get();
		return new AsyncResult<FlatFileAsyncListResponse>(flatFileAsyncListResponse);
	}
}
