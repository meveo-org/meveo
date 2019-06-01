package org.meveo.admin.job;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldMapKeyEnum;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.jobs.JobCategoryEnum;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.service.job.Job;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Stateless
public class FileProcessingJob extends Job {

    private static final String FILE_PROCESSING_JOB_ARCHIVE_DIR = "FileProcessingJob_archiveDir";

    private static final String FILE_PROCESSING_JOB_FILE_NAME_FILTER = "FileProcessingJob_fileNameFilter";

    private static final String FILE_PROCESSING_JOB_REJECT_DIR = "FileProcessingJobrejectDir";

    private static final String JOB_FILE_PROCESSING_JOB = "JOB_FileProcessingJob";

    private static final String FILE_PROCESSING_JOB_OUTPUT_DIR = "FileProcessingJob_outputDir";

    private static final String FILE_PROCESSING_JOB_VARIABLES = "FileProcessingJob_variables";

    private static final String FILE_PROCESSING_JOB_ERROR_ACTION = "FileProcessingJob_errorAction";

    private static final String FILE_PROCESSING_JOB_SCRIPTS_FLOW = "FileProcessingJob_scriptsFlow";

    private static final String FILE_PROCESSING_JOB_FILE_NAME_EXTENSION = "FileProcessingJob_fileNameExtension";

    private static final String FILE_PROCESSING_JOB_INPUT_DIR = "FileProcessingJob_inputDir";

    private static final String FILE_PROCESSING_JOB_ORIGIN_FILENAME = "FileProcessingJob_originFilename";

    private static final String EMPTY_STRING = "";

    private static final String TWO_POINTS_PARENT_DIR = "\\..";

    /** The file processing job bean. */
    @Inject
    private FileProcessingJobBean fileProcessingJobBean;

    @Inject
    private ParamBeanFactory paramBeanFactory;

    @SuppressWarnings("unchecked")
    @Override
    @TransactionAttribute(TransactionAttributeType.NEVER)
    protected void execute(JobExecutionResultImpl result, JobInstance jobInstance) throws BusinessException {
        try {
            String inputDir = null;
            String scriptInstanceFlowCode = null;
            String fileNameExtension = null;
            String fileNameFilter = null;
            String archiveDir = null;
            String rejectDir = null;
            Map<String, Object> initContext = new HashMap<String, Object>();
            try {
                inputDir = paramBeanFactory.getChrootDir() + ((String) this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_INPUT_DIR)).replaceAll("\\..", "");
                fileNameExtension = (String) this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_FILE_NAME_EXTENSION);
                scriptInstanceFlowCode = (String) this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_SCRIPTS_FLOW);
                if(this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_REJECT_DIR) != null) {
                    rejectDir = paramBeanFactory.getChrootDir() + File.separator + ((String) this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_REJECT_DIR)).replaceAll(TWO_POINTS_PARENT_DIR, EMPTY_STRING);
                }
                if(this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_ARCHIVE_DIR) != null) {
                    archiveDir = paramBeanFactory.getChrootDir() + File.separator + ((String) this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_ARCHIVE_DIR)).replaceAll(TWO_POINTS_PARENT_DIR, EMPTY_STRING);
                }
                if (this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_VARIABLES) != null) {
                    initContext = (Map<String, Object>) this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_VARIABLES);
                }
                if (this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_FILE_NAME_FILTER) != null) {
                    fileNameFilter = ((String) this.getParamOrCFValue(jobInstance, FILE_PROCESSING_JOB_FILE_NAME_FILTER));
                    fileNameFilter = fileNameFilter.replaceAll(Pattern.quote("*"), "");
                }
            } catch (Exception e) {
                log.warn("Cant get customFields for " + jobInstance.getJobTemplate(), e.getMessage());
            }

            ArrayList<String> fileExtensions = new ArrayList<String>();
            fileExtensions.add(fileNameExtension);

            File f = new File(inputDir);
            if (!f.exists()) {
                log.debug("inputDir {} not exist", inputDir);
                f.mkdirs();
                log.debug("inputDir {} creation ok", inputDir);
            }
            File[] files = FileUtils.listFilesByNameFilter(inputDir, fileExtensions, fileNameFilter);
            if (files == null || files.length == 0) {
                log.debug("there no file in {} with extension {}", inputDir, fileExtensions);
                return;
            }
            for (File file : files) {
                fileProcessingJobBean.execute(result, inputDir, archiveDir, rejectDir, file, scriptInstanceFlowCode, initContext);
            }

        } catch (Exception e) {
            log.error("Failed to run mediation", e);
            result.registerError(e.getMessage());
        }
    }

    @Override
    public JobCategoryEnum getJobCategory() {
        return JobCategoryEnum.MEDIATION;
    }

    @Override
    public Map<String, CustomFieldTemplate> getCustomFields() {
        Map<String, CustomFieldTemplate> result = new HashMap<String, CustomFieldTemplate>();

        CustomFieldTemplate inputDirectoryCF = new CustomFieldTemplate();
        inputDirectoryCF.setCode(FILE_PROCESSING_JOB_INPUT_DIR);
        inputDirectoryCF.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        inputDirectoryCF.setActive(true);
        inputDirectoryCF.setDescription(resourceMessages.getString("flatFile.inputDir"));
        inputDirectoryCF.setFieldType(CustomFieldTypeEnum.STRING);
        inputDirectoryCF.setDefaultValue(null);
        inputDirectoryCF.setValueRequired(true);
        inputDirectoryCF.setMaxValue(256L);
        result.put(FILE_PROCESSING_JOB_INPUT_DIR, inputDirectoryCF);

        CustomFieldTemplate archiveDirectoryCF = new CustomFieldTemplate();
        archiveDirectoryCF.setCode(FILE_PROCESSING_JOB_ARCHIVE_DIR);
        archiveDirectoryCF.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        archiveDirectoryCF.setActive(true);
        archiveDirectoryCF.setDescription(resourceMessages.getString("flatFile.archiveDir"));
        archiveDirectoryCF.setFieldType(CustomFieldTypeEnum.STRING);
        archiveDirectoryCF.setDefaultValue(null);
        archiveDirectoryCF.setValueRequired(false);
        archiveDirectoryCF.setMaxValue(256L);
        result.put(FILE_PROCESSING_JOB_ARCHIVE_DIR, archiveDirectoryCF);

        CustomFieldTemplate rejectDirectoryCF = new CustomFieldTemplate();
        rejectDirectoryCF.setCode(FILE_PROCESSING_JOB_REJECT_DIR);
        rejectDirectoryCF.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        rejectDirectoryCF.setActive(true);
        rejectDirectoryCF.setDescription(resourceMessages.getString("flatFile.rejectDir"));
        rejectDirectoryCF.setFieldType(CustomFieldTypeEnum.STRING);
        rejectDirectoryCF.setDefaultValue(null);
        rejectDirectoryCF.setValueRequired(false);
        rejectDirectoryCF.setMaxValue(256L);
        result.put(FILE_PROCESSING_JOB_REJECT_DIR, rejectDirectoryCF);

        CustomFieldTemplate outputDirectoryCF = new CustomFieldTemplate();
        outputDirectoryCF.setCode(FILE_PROCESSING_JOB_OUTPUT_DIR);
        outputDirectoryCF.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        outputDirectoryCF.setActive(true);
        outputDirectoryCF.setDescription(resourceMessages.getString("flatFile.outputDir"));
        outputDirectoryCF.setFieldType(CustomFieldTypeEnum.STRING);
        outputDirectoryCF.setDefaultValue(null);
        outputDirectoryCF.setValueRequired(false);
        outputDirectoryCF.setMaxValue(256L);
        result.put(FILE_PROCESSING_JOB_OUTPUT_DIR, outputDirectoryCF);

        CustomFieldTemplate fileNameKeyCF = new CustomFieldTemplate();
        fileNameKeyCF.setCode(FILE_PROCESSING_JOB_FILE_NAME_FILTER);
        fileNameKeyCF.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        fileNameKeyCF.setActive(true);
        fileNameKeyCF.setDescription(resourceMessages.getString("flatFile.fileNameFilter"));
        fileNameKeyCF.setFieldType(CustomFieldTypeEnum.STRING);
        fileNameKeyCF.setDefaultValue(null);
        fileNameKeyCF.setValueRequired(false);
        fileNameKeyCF.setMaxValue(256L);
        result.put(FILE_PROCESSING_JOB_FILE_NAME_FILTER, fileNameKeyCF);

        CustomFieldTemplate fileNameExtensionCF = new CustomFieldTemplate();
        fileNameExtensionCF.setCode(FILE_PROCESSING_JOB_FILE_NAME_EXTENSION);
        fileNameExtensionCF.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        fileNameExtensionCF.setActive(true);
        fileNameExtensionCF.setDescription(resourceMessages.getString("flatFile.fileNameExtension"));
        fileNameExtensionCF.setFieldType(CustomFieldTypeEnum.STRING);
        fileNameExtensionCF.setDefaultValue("pdf");
        fileNameExtensionCF.setValueRequired(true);
        fileNameExtensionCF.setMaxValue(256L);
        result.put(FILE_PROCESSING_JOB_FILE_NAME_EXTENSION, fileNameExtensionCF);

        CustomFieldTemplate scriptFlowCF = new CustomFieldTemplate();
        scriptFlowCF.setCode(FILE_PROCESSING_JOB_SCRIPTS_FLOW);
        scriptFlowCF.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        scriptFlowCF.setActive(true);
        scriptFlowCF.setDescription(resourceMessages.getString("flatFile.scriptsFlow"));
        scriptFlowCF.setFieldType(CustomFieldTypeEnum.STRING);
        scriptFlowCF.setDefaultValue(null);
        scriptFlowCF.setValueRequired(true);
        scriptFlowCF.setMaxValue(256L);
        result.put(FILE_PROCESSING_JOB_SCRIPTS_FLOW, scriptFlowCF);

        CustomFieldTemplate variablesCF = new CustomFieldTemplate();
        variablesCF.setCode(FILE_PROCESSING_JOB_VARIABLES);
        variablesCF.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        variablesCF.setActive(true);
        variablesCF.setDescription("Init and finalize variables");
        variablesCF.setFieldType(CustomFieldTypeEnum.STRING);
        variablesCF.setStorageType(CustomFieldStorageTypeEnum.MAP);
        variablesCF.setValueRequired(false);
        variablesCF.setMaxValue(256L);
        variablesCF.setMapKeyType(CustomFieldMapKeyEnum.STRING);
        result.put(FILE_PROCESSING_JOB_VARIABLES, variablesCF);

        CustomFieldTemplate originFilename = new CustomFieldTemplate();
        originFilename.setCode(FILE_PROCESSING_JOB_ORIGIN_FILENAME);
        originFilename.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        originFilename.setActive(true);
        originFilename.setDefaultValue("origin_filename");
        originFilename.setDescription("Filename variable name");
        originFilename.setFieldType(CustomFieldTypeEnum.STRING);
        originFilename.setValueRequired(false);
        originFilename.setMaxValue(256L);
        result.put(FILE_PROCESSING_JOB_ORIGIN_FILENAME, originFilename);

        CustomFieldTemplate errorAction = new CustomFieldTemplate();
        errorAction.setCode(FILE_PROCESSING_JOB_ERROR_ACTION);
        errorAction.setAppliesTo(JOB_FILE_PROCESSING_JOB);
        errorAction.setActive(true);
        errorAction.setDefaultValue(FlatFileProcessingJob.CONTINUE);
        errorAction.setDescription("Error action");
        errorAction.setFieldType(CustomFieldTypeEnum.LIST);
        errorAction.setValueRequired(false);
        Map<String, String> listValuesErrorAction = new HashMap<String, String>();
        listValuesErrorAction.put(FlatFileProcessingJob.CONTINUE, "Continue");
        listValuesErrorAction.put(FlatFileProcessingJob.STOP, "Stop");
        listValuesErrorAction.put(FlatFileProcessingJob.ROLLBBACK, "Rollback");
        errorAction.setListValues(listValuesErrorAction);
        result.put(FILE_PROCESSING_JOB_ERROR_ACTION, errorAction);

        return result;
    }
}
