/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.grieg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.meveo.commons.utils.ParamBean;
import org.meveo.config.MeveoFileConfig;

/**
 * Configuration wrapper for Grieg.
 * 
 * @author Ignas Lelys
 * @created Dec 17, 2010
 */
public class GriegConfig implements MeveoFileConfig {

    private static final String GRIEG_PROPERTIES_FILENAME = "grieg.properties";
    private static final String VALIDATED_FILES_DIR = "grieg.validatedFilesDir";
    private static final String SOURCE_FILES_DIR = "grieg.sourceFilesDir";
    private static final String REJECTED_FILES_DIR = "grieg.rejectedFilesDir";
    private static final String ACCEPTED_FILES_DIR = "grieg.acceptedFilesDir";
    private static final String OUTPUT_FILES_DIR = "grieg.outputFilesDir";
    private static final String REJECTED_TICKETS_FILES_DIR = "grieg.rejectedTicketsFilesDir";
    private static final String TEMP_FILES_DIR = "grieg.tempFilesDir";
    private static final String RESOURCES_FILES_DIR = "grieg.resourcesFilesDir";
    private static final String ATTACHED_FILES_DIR = "grieg.attachedFilesDir";
    private static final String SCANNING_INTERVAL = "grieg.scanningInterval";
    private static final String MESSAGE_SCANNING_INTERVAL = "grieg.messageScanningInterval";
    private static final String FILE_EXT_TO_PROCESS = "grieg.fileExt";
    private static final String DUNNING_FILE_EXT_TO_PROCESS = "grieg.dunningFileExt";
    private static final String PROCESSING_EXT = "grieg.processingExt";
    private static final String PROCESSING_FAILED_EXT = "grieg.processingFailedExt";
    private static final String ERROR_FILE_EXT = "grieg.errorFileExt";
    private static final String IGNORED_FILE_EXT = "grieg.ignoredFileExt";
    private static final String PERSISTENCE_UNIT_NAME = "grieg.persistenceUnitName";
    private static final String SQL_BATCH_SIZE = "grieg.sqlBatchSize";
    private static final String WORKING_THREADS = "grieg.workingThreadCount";
    private static final String DB_USERNAME = "hibernate.connection.username";
    private static final String DB_PASSWORD = "hibernate.connection.password";
    private static final String DB_URL = "hibernate.connection.url";
    private static final String DB_HBM2DDL = "hibernate.hbm2ddl.auto";
    private static final String DB_DRIVER_CLASS = "hibernate.connection.driver_class";
    private static final String DB_SHOW_SQL = "hibernate.show_sql";
    private static final String DB_DIALECT = "hibernate.dialect";
    private static final String GRIEG_GUICE_MODULE = "grieg.guiceConfigurationModule";
    private static final String ZIP_EMAIL_BATCH_JOB_CRON = "grieg.zipEmailCron";
    private static final String SERVICE_PROVIDER_URL = "grieg.serviceProviderUrl";
    private static final String CUSTOMER_ACCOUNT_SERVICE_NAME = "grieg.customerAccountServiceName";
    private static final String DUNNING_VALIDATED_FILES_DIR = "grieg.dunningValidatedFilesDir";
    private static final String DEFAULT_PROVIDER_ID = "grieg.defaultProvider.id";
    private static final String DEFAULT_DEFAULT_PROVIDER_ID = "1";

    // Defaults
    private static final String DEFAULT_TEMP_FILES_DIR = System.getProperty("java.io.tmpdir");
    private static final String DEFAULT_SCANNING_INTERVAL = "100"; // 0.1s
    private static final String DEFAULT_MESSAGE_SCANNING_INTERVAL = "60000"; // 1min
    private static final String DEFAULT_FILE_EXT_TO_PROCESS = ".xml";
    private static final String DEFAULT_DUNNING_FILE_EXT_TO_PROCESS = ".csv";
    private static final String DEFAULT_PROCESSING_EXT = ".processing";
    private static final String DEFAULT_PROCESSING_FAILED_EXT = ".failed";
    private static final String DEFAULT_ERROR_FILE_EXT = "'_'yyyyMMddHHmmss'.err'";
    private static final String DEFAULT_IGNORED_FILE_EXT = "'_'yyyyMMddHHmmss'.ignored'";
    private static final String DEFAULT_PERSISTENCE_UNIT_NAME = "GriegPU";
    private static final String DEFAULT_SQL_BATCH_SIZE = "100";
    private static final String DEFAULT_WORKING_THREADS = "2";
    private static final String DEFAULT_ZIP_EMAIL_BATCH_JOB_CRON = "0 0 0 * * ?";
    private static final String DEFAULT_SERVICE_PROVIDER_URL = "jnp://127.0.0.1:1099";
    private static final String DEFAULT_CUSTOMER_ACCOUNT_SERVICE_NAME = "meveo-admin/CustomerAccountService/remote";
    private static final String DEFAULT_ATTACHED_FILES_DIR =".Ò";
    /**
     * @see org.meveo.config.MeveoFileConfig#getSourceFilesDirectory()
     */
    public String getSourceFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(SOURCE_FILES_DIR);
    }

    /**
     * @see org.meveo.config.MeveoFileConfig#getRejectedFilesDirectory()
     */
    public String getRejectedFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(REJECTED_FILES_DIR);
    }

    /**
     * @see org.meveo.config.MeveoFileConfig#getTempFilesDirectory()
     */
    public String getTempFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(TEMP_FILES_DIR, DEFAULT_TEMP_FILES_DIR);
    }

    /**
     * @see org.meveo.config.MeveoFileConfig#getAcceptedFilesDirectory()
     */
    public String getAcceptedFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(ACCEPTED_FILES_DIR);
    }

    /**
     * @see org.meveo.config.MeveoFileConfig#getOutputFilesDirectory()
     */
    public String getOutputFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(OUTPUT_FILES_DIR);
    }

    /**
     * @see org.meveo.config.MeveoFileConfig#getRejectedTicketsFilesDirectory()
     */
    public String getRejectedTicketsFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(REJECTED_TICKETS_FILES_DIR);
    }
    

    /**
     * @see org.meveo.config.MeveoFileConfig#getAttachedTicketsFilesDirectory()
     */
   
    public String getAttachedFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(ATTACHED_FILES_DIR,DEFAULT_ATTACHED_FILES_DIR);
    }
    
    

    /**
     * @see org.meveo.config.MeveoConfig#getScanningInterval()
     */
    public long getScanningInterval() {
        String value = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(SCANNING_INTERVAL,
                DEFAULT_SCANNING_INTERVAL);
        return Long.parseLong(value);
    }

    /**
     * Interval for scanning message input.
     */
    public long getMessageScanningInterval() {
        String value = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(MESSAGE_SCANNING_INTERVAL,
                DEFAULT_MESSAGE_SCANNING_INTERVAL);
        return Long.parseLong(value);
    }
    
    /**
     * @see org.meveo.config.MeveoFileConfig#getFileProcessingExtension()
     */
    public String getFileProcessingExtension() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(PROCESSING_EXT, DEFAULT_PROCESSING_EXT);
    }

    /**
     * @see org.meveo.config.MeveoFileConfig#getFileProcessingFailedExtension()
     */
    public String getFileProcessingFailedExtension() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(PROCESSING_FAILED_EXT,
                DEFAULT_PROCESSING_FAILED_EXT);
    }

    /**
     * @see org.meveo.config.MeveoFileConfig#getErrorFileExtension()
     */
    public String getErrorFileExtension() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(ERROR_FILE_EXT, DEFAULT_ERROR_FILE_EXT);
    }

    /**
     * @see org.meveo.config.MeveoFileConfig#getIgnoredFileExtension()
     */
    public String getIgnoredFileExtension() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(IGNORED_FILE_EXT, DEFAULT_IGNORED_FILE_EXT);
    }

    /**
     * @see org.meveo.config.MeveoFileConfig#getFileExtensions()
     */
    public List<String> getFileExtensions() {
        String extensions = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(FILE_EXT_TO_PROCESS,
                DEFAULT_FILE_EXT_TO_PROCESS);
        String[] exts = extensions.split(",");
        return Arrays.asList(exts);
    }

    /**
     * @see org.meveo.config.MeveoConfig#getSQLBatchSize()
     */
    public long getSQLBatchSize() {
        String value = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(SQL_BATCH_SIZE,
                DEFAULT_SQL_BATCH_SIZE);
        return Long.parseLong(value);
    }

    /**
     * @see org.meveo.config.MeveoConfig#getThreadCount()
     */
    public int getThreadCount() {
        String value = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(WORKING_THREADS,
                DEFAULT_WORKING_THREADS);
        return Integer.parseInt(value);
    }

    /**
     * @see org.meveo.config.MeveoConfig#getTicketSeparator()
     */
    public String getTicketSeparator() {
        return "";
    }

    /**
     * Returns batch job cron for archive email folder.
     */
    // TODO does not belong in core grieg project, batch jobs is implementation
    // specific.
    public String getBatchJobCron(String batchJobName) {
        if (batchJobName.equals("ZipEmailBatchJobTask")) {
            return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(ZIP_EMAIL_BATCH_JOB_CRON,
                    DEFAULT_ZIP_EMAIL_BATCH_JOB_CRON);
        } else {
            return DEFAULT_ZIP_EMAIL_BATCH_JOB_CRON;
        }
    }

    /**
     * File dir with resources for each billing cycle for each brand.
     */
    public String getResourcesFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(RESOURCES_FILES_DIR);
    }

    /**
     * Validated files dir.
     */
    public String getValidatedFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(VALIDATED_FILES_DIR);
    }

    /**
     * Service provider url.
     */
    public String getServiceProviderUrl() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(SERVICE_PROVIDER_URL,
                DEFAULT_SERVICE_PROVIDER_URL);
    }

    /**
     * Get configured persistence unit name.
     */
    public static String getPersistenceUnitName() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(PERSISTENCE_UNIT_NAME,
                DEFAULT_PERSISTENCE_UNIT_NAME);
    }

    /**
     * Customer account service name.
     */
    public String getCustomerAccountServiceName() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(CUSTOMER_ACCOUNT_SERVICE_NAME,
                DEFAULT_CUSTOMER_ACCOUNT_SERVICE_NAME);
    }

    /**
     * Get overridden persistence configuration.
     */
    public static Map<String, String> getPersistenceProperties() {
        String username = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DB_USERNAME);
        String password = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DB_PASSWORD);
        String url = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DB_URL);
        String hbm2ddl = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DB_HBM2DDL);
        String driverClass = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DB_DRIVER_CLASS);
        String showSql = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DB_SHOW_SQL);
        String dialect = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DB_DIALECT);

        Map<String, String> params = new HashMap<String, String>();
        if (username != null) {
            params.put(DB_USERNAME, username);
        }
        if (password != null) {
            params.put(DB_PASSWORD, password);
        }
        if (url != null) {
            params.put(DB_URL, url);
        }
        if (hbm2ddl != null) {
            params.put(DB_HBM2DDL, hbm2ddl);
        }
        if (driverClass != null) {
            params.put(DB_DRIVER_CLASS, driverClass);
        }
        if (showSql != null) {
            params.put(DB_SHOW_SQL, showSql);
        }
        if (dialect != null) {
            params.put(DB_DIALECT, dialect);
        }
        return params;
    }

    /**
     * Get Guice configuration module for Meveo application.
     */
    public static String getGriegConfigurationModule() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(GRIEG_GUICE_MODULE);
    }

    public String getValidatedDunningFilesDirectory() {
        return ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DUNNING_VALIDATED_FILES_DIR);
    }

    public List<String> getDunningFileExtensions() {
        String extensions = ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DUNNING_FILE_EXT_TO_PROCESS,
                DEFAULT_DUNNING_FILE_EXT_TO_PROCESS);
        String[] exts = extensions.split(",");
        return Arrays.asList(exts);
    }

    public String getApplicationName() {
        return "GRIEG";
    }

  public Long getDefaultProviderId() {
		return Long.valueOf(ParamBean.getInstance(GRIEG_PROPERTIES_FILENAME).getProperty(DEFAULT_PROVIDER_ID,
				DEFAULT_DEFAULT_PROVIDER_ID));
	}

}
