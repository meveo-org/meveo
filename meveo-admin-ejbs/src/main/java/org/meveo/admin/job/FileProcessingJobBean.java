package org.meveo.admin.job;

import org.meveo.admin.job.logging.JobLoggingInterceptor;
import org.meveo.commons.utils.FileUtils;
import org.meveo.interceptor.PerformanceInterceptor;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.crm.Provider;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.script.Script;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.script.ScriptInterface;
import org.meveo.util.ApplicationProvider;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class FileProcessingJobBean.
 * 
 */
@Stateless
public class FileProcessingJobBean {

    /** The log. */
    @Inject
    private Logger log;

    /** The script instance service. */
    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Inject
    @CurrentUser
    protected MeveoUser currentUser;

    @Inject
    @ApplicationProvider
    protected Provider appProvider;

    /** The file name. */
    String fileName;

    /** The reject dir. */
    String rejectDir;

    /** The archive dir. */
    String archiveDir;

    /** The report. */
    String report;

    /**
     * Execute.
     *
     * @param result the result
     * @param inputDir the input dir
     * @param file the file
     * @param scriptInstanceFlowCode the script instance flow code
     * @param context the context
     */
    @JpaAmpNewTx
    @Interceptors({ JobLoggingInterceptor.class, PerformanceInterceptor.class })
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void execute(JobExecutionResultImpl result, String inputDir, String archDir, String rejDir, File file, String scriptInstanceFlowCode,
                        Map<String, Object> context) {
        log.debug("Running for inputDir={}, scriptInstanceFlowCode={},formatTransfo={}", inputDir, scriptInstanceFlowCode);

        rejectDir = rejDir != null ? rejDir : inputDir + File.separator + "reject";
        archiveDir = archDir != null ? archDir :inputDir + File.separator + "archive";

        File f = new File(rejectDir);
        if (!f.exists()) {
            log.debug("rejectDir {} not exist", rejectDir);
            f.mkdirs();
            log.debug("rejectDir {} creation ok", rejectDir);
        }
        f = new File(archiveDir);
        if (!f.exists()) {
            log.debug("saveDir {} not exist", archiveDir);
            f.mkdirs();
            log.debug("saveDir {} creation ok", archiveDir);
        }
        report = "";

        if (file != null) {
            fileName = file.getName();
            ScriptInterface script = null;
            try {
                log.info("InputFile job {} in progress...", file.getAbsolutePath());
                script = scriptInstanceService.getScriptInstance(scriptInstanceFlowCode);
                script.init(context);
                Map<String, Object> executeParams = new HashMap<String, Object>();
                executeParams.put("file", file);
                executeParams.put(Script.CONTEXT_CURRENT_USER, currentUser);
                executeParams.put(Script.CONTEXT_APP_PROVIDER, appProvider);
                executeParams.put("archiveDir", archiveDir);
                script.execute(executeParams);
                FileUtils.moveFile(archiveDir, file, fileName);
                result.addNbItemsCorrectlyProcessed(1);
            } catch (Exception e) {
                report += "\r\n " + e.getMessage();
                FileUtils.moveFile(rejectDir, file, fileName);
                result.addNbItemsProcessedWithError(1);
            } finally {
                try {
                    if (script != null) {
                        script.finalize(context);
                    }
                } catch (Exception e) {
                    report += "\r\n error in script finailzation : " + e.getMessage();
                }
                try {
                    if (file != null) {
                        // Move current CSV file to save directory, else PDF file was deleted.
                        file.delete();
                    }
                } catch (Exception e) {
                    report += "\r\n cannot move file to save directory " + fileName;
                }
            }
            result.addReport(report);
        } else {
            log.info("no file to process");
        }
    }
}
