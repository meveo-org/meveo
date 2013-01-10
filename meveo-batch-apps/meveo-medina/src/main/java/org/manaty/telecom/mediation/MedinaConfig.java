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
package org.manaty.telecom.mediation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.manaty.utils.DBConfigBean;
import org.manaty.utils.ParamBean;

/**
 * Configuration wrapper for Medina.
 * 
 * @author seb
 * @created Aug 8, 2012
 */
public class MedinaConfig {

    private static final String INSTANCE_NAME = "medina.instanceName";
	private static final String PROVIDER_CODES="medina.providerCodes";
    private static final String SOURCE_FILES_DIR = "medina.sourceFilesDir";
    private static final String REJECTED_FILES_DIR = "medina.rejectedFilesDir";
    private static final String ACCEPTED_FILES_DIR = "medina.acceptedFilesDir";
    private static final String REJECTED_TICKETS_FILES_DIR = "medina.rejectedTicketsFilesDir";
    private static final String IGNORED_TICKETS_FILES_DIR = "medina.ignoredTicketsFilesDir";
    private static final String OUTPUT_FILES_DIR = "medina.outputFilesDir";
    private static final String MAGIC_NUMBERS_RECOVERY_FILE_DIR = "medina.magicNumbersRecoveryDir";
    private static final String TEMP_FILES_DIR = "medina.tempFilesDir";
    private static final String ZONE_IMPORT_DIR = "medina.zoneImportDir";
    private static final String NUMBERING_IMPORT_DIR = "medina.numberingImportDir";
    private static final String FAILED_PA_MATCHINGS_DIR = "medina.failedPaMatchingsDir";
    private static final String SCANNING_INTERVAL = "medina.scanningInterval";
    private static final String IMPORT_SCANNING_INTERVAL = "medina.importScanningInterval";
    private static final String FILE_EXT_TO_PROCESS = "medina.fileExt";
    private static final String PROCESSING_EXT = "medina.processingExt";
    private static final String PROCESSING_FAILED_EXT = "medina.processingFailedExt";
    private static final String ERROR_FILE_EXT = "medina.errorFileExt";
    private static final String ZONE_IMPORT_FILE_EXT = "medina.zoneImportFileExt";
    private static final String NUMBERING_IMPORT_FILE_EXT = "medina.numberingImportFileExt";
    private static final String IGNORED_FILE_EXT = "medina.ignoredFileExt";
    private static final String PERSISTENCE_UNIT_NAME = "medina.persistenceUnitName";
    private static final String MAGIC_NUMBERS_COUNT = "medina.magicNumbersCount";
    private static final String MAGIC_NUMBERS_COUNT_IN_FILE = "medina.magicNumbersCountInFile";
    private static final String SQL_BATCH_SIZE = "medina.sqlBatchSize";
    private static final String DEFAULT_ZONE = "medina.defaultZone";
    private static final String DEFAULT_PLMN = "medina.defaultPLMN";
    private static final String WORKING_THREADS = "medina.workingThreadCount";
    private static final String DB_USERNAME = "hibernate.connection.username";
    private static final String DB_PASSWORD = "hibernate.connection.password";
    private static final String DB_URL = "hibernate.connection.url";
    private static final String DB_HBM2DDL = "hibernate.hbm2ddl.auto";
    private static final String DB_DRIVER_CLASS = "hibernate.connection.driver_class";
    private static final String DB_SHOW_SQL = "hibernate.show_sql";
    private static final String C3P0_POOL_MIN_SIZE = "hibernate.c3p0.min_size";
    private static final String C3P0_POOL_MAX_SIZE = "hibernate.c3p0.max_size";
    private static final String C3P0_POOL_TIMEOUT = "hibernate.c3p0.timeout";
    private static final String C3P0_POOL_IDLE_TEST_PERIOD = "hibernate.c3p0.idle_test_period";
    private static final String CONNECTION_FACTORY_JNDI = "medina.connectionFactoryJNDI";
    private static final String PLMN_CHANGE_QUEUE_JNDI = "medina.plmnChangeQueueJNDI";
    private static final String FULL_PARSE_FIRST = "medina.fullParseFirst";
    private static final String MAGIC_NUMBER_CALCULATION_ALGORITHM = "medina.magicNumberCalculationAlgorithm";
    private static final String PA_MATCHING_ID = "medina.paMatchingId";
    private static final String CDR_TICKET_ID = "medina.cdrTicketId";
    private static final String REJECTED_TICKET_ID = "medina.rejectedTicketId";
    private static final String PA_MATCHING_ACCESS_USER_ID = "medina.paMatchingAccessUserId";
    private static final String PA_MATCHING_ACCESS_SERVICE_ID = "medina.paMatchingIdAccessServiceId";
    private static final String HASH_ID = "medina.hashId";
    private static final String AGGREGATION_BATCH_JOB_CRON = "medina.aggregationBatchJobCron";
    private static final String AGGREGATION_DELAY = "medina.aggregationDelay";
    private static final String REJECTED_TICKETS_BATCH_JOB_CRON = "medina.rejectedTicketsBatchJobCron";
    private static final String REFRESH_PROPERTIES_BATCH_JOB_CRON = "medina.refreshPropertiesBatchJobCron";
    private static final String REJECTION_REASONS_FOR_AUTOMATIC_RETRY = "medina.rejectionReasonsForAutomaticRetry";
    private static final String NUMBER_OF_RETRY_TICKETS_PER_SELECT = "medina.numberOfRetryTicketsPerSelect";
    private static final String REJECTED_TICKETS_DELAY = "medina.rejectedTicketsDelay";
    private static final String RETRIED_FILE_TICKETS_NMBR = "medina.retriedFileTicketsNbr";
    
    private static final String SMS_OFFERS = "medina.smsOffers";
    private static final String VOICE_OFFERS = "medina.voiceOffers";
    private static final String DATA_CSD_OFFERS = "medina.dataCSDOffers";

    // Defaults
	private static final String DEFAULT_PROVIDER_CODES="MEVEO";
    private static final String DEFAULT_TEMP_FILES_DIR = System.getProperty("java.io.tmpdir");
    private static final String DEFAULT_SCANNING_INTERVAL = "500"; // 0.5s
    private static final String DEFAULT_IMPORT_SCANNING_INTERVAL = "600000"; // 10min
    private static final String DEFAULT_FILE_EXT_TO_PROCESS = ".txt,.asn";
    private static final String DEFAULT_PROCESSING_EXT = ".processing";
    private static final String DEFAULT_PROCESSING_FAILED_EXT = ".failed";
    private static final String DEFAULT_ZONE_IMPORT_FILE_EXT = ".csv";
    private static final String DEFAULT_NUMBERING_IMPORT_FILE_EXT = ".csv";
    private static final String DEFAULT_ERROR_FILE_EXT = "'_'yyyyMMddHHmmss'.err'";
    private static final String DEFAULT_IGNORED_FILE_EXT = "'_'yyyyMMddHHmmss'.ignored'";
    private static final String DEFAULT_PERSISTENCE_UNIT_NAME = "MedinaPU";
    private static final String DEFAULT_SQL_BATCH_SIZE = "100";
    private static final String DEFAULT_MAGIC_NUMBERS_COUNT = "10000000";
    private static final String DEFAULT_MAGIC_NUMBERS_COUNT_IN_FILE = "20000";
    private static final String DEFAULT_DEFAULT_ZONE = "OUT_ZONE";
    private static final String DEFAULT_DEFAULT_PLMN = "20820";
    private static final String DEFAULT_WORKING_THREADS = "2";
    private static final String DEFAULT_CONNECTION_FACTORY_JNDI = "ConnectionFactory";
    private static final String DEFAULT_FULL_PARSE_FIRST = "false";
    private static final String DEFAULT_MAGIC_NUMBER_CALCULATION_ALGORITHM = "md5";
    private static final String DEFAULT_PA_MATCHING_ID = "PA_MATCHING_T_SEQ.NEXTVAL";
    private static final String DEFAULT_CDR_TICKET_ID = "MEDINA_CDR_TICKET_SEQ.NEXTVAL";
    private static final String DEFAULT_REJECTED_TICKET_ID = "MEDINA_REJECTED_CDR_SEQ.NEXTVAL";
    private static final String DEFAULT_PA_MATCHING_ACCESS_USER_ID = "MEDINA_SEQUENCE.NEXTVAL";
    private static final String DEFAULT_PA_MATCHING_ACCESS_SERVICE_ID = "MEDINA_SEQUENCE.NEXTVAL";
    private static final String DEFAULT_HASH_ID = "MEDINA_CDR_SEQ.NEXTVAL";
    private static final String DEFAULT_SMS_OFFERS = "";
    private static final String DEFAULT_VOICE_OFFERS = "";
    private static final String DEFAULT_DATA_CSD_OFFERS = "";
    private static final String DEFAULT_AGGREGATION_BATCH_JOB_CRON = "0 0 * * * ?";
    private static final String DEFAULT_AGGREGATION_DELAY = "72";
    private static final String DEFAULD_REJECTED_TICKETS_BATCH_JOB_CRON = "0 0 * * * ?";
    private static final String DEFAULT_REFRESH_PROPERTIES_BATCH_JOB_CRON = "0 0 * * * ?";
    private static final String DEFAULT_REJECTED_TICKETS_DELAY = "72";
    private static final String DEFAULT_RETRIED_FILE_TICKETS_NMBR = "10000";
    private static final String DEFAULT_REJECTION_REASONS_FOR_AUTOMATIC_RETRY = "";
    private static final String DEFAULT_NUMBER_OF_RETRY_TICKETS_PER_SELECT = "250000";
    
    /**
     * Get the instance name of medina
     * medina processes should have a different instance name
     * to an instance is for instance associated the list of providers to handle
     * 
     * @return
     */
    public static String getInstanceName() {
        return ParamBean.getInstance().getProperty(INSTANCE_NAME);
    }
    /**
     * Get the list of providers to be handles by this instance of Medina
     */
    public static String getProviderCodeList() {
        return DBConfigBean.getInstance().getProperty(PROVIDER_CODES,DEFAULT_PROVIDER_CODES);
    }
    
    /**
     * Get source directory to search for files.
     */
    public static String getSourceFilesDirectory() {
        return ParamBean.getInstance().getProperty(SOURCE_FILES_DIR);
    }

    /**
     * Get rejected files directory.
     */
    public static String getRejectedFilesDirectory() {
        return ParamBean.getInstance().getProperty(REJECTED_FILES_DIR);
    }

    /**
     * Get rejected files directory.
     */
    public static String getIgnoredTicketFilesDirectory() {
        return ParamBean.getInstance().getProperty(IGNORED_TICKETS_FILES_DIR);
    }

    /**
     * Get temporary files directory.
     */
    public static String getTempFilesDirectory() {
        return ParamBean.getInstance().getProperty(TEMP_FILES_DIR, DEFAULT_TEMP_FILES_DIR);
    }

    /**
     * Get accepted files directory.
     */
    public static String getAcceptedFilesDirectory() {
        return ParamBean.getInstance().getProperty(ACCEPTED_FILES_DIR);
    }

    /**
     * Get output files directory for mvno edr tickets.
     */
    public static String getOutputFilesDirectory() {
        return ParamBean.getInstance().getProperty(OUTPUT_FILES_DIR);
    }

    /**
     * Get rejected tickets files directory.
     */
    public static String getRejectedTicketsFilesDirectory() {
        return ParamBean.getInstance().getProperty(REJECTED_TICKETS_FILES_DIR);
    }

    /**
     * Get failed PA_MATCHINGS files directory.
     */
    public static String getFailedPaMatchingsDirectory() {
    	return ParamBean.getInstance().getProperty(FAILED_PA_MATCHINGS_DIR);
    }

    /**
     * Get source directory scanning interval.
     */
    public static long getScanningInterval() {
        String value = ParamBean.getInstance().getProperty(SCANNING_INTERVAL, DEFAULT_SCANNING_INTERVAL);
        return Long.parseLong(value);
    }

    /**
     * Get import source directories scanning interval.
     */
    public static long getImportScanningInterval() {
        String value = ParamBean.getInstance().getProperty(IMPORT_SCANNING_INTERVAL,
                DEFAULT_IMPORT_SCANNING_INTERVAL);
        return Long.parseLong(value);
    }

    /**
     * Get extension for the file being processed.
     */
    public static String getFileProcessingExtension() {
        return ParamBean.getInstance().getProperty(PROCESSING_EXT, DEFAULT_PROCESSING_EXT);
    }

    /**
     * Get extension for the file failed to be processed.
     */
    public static String getFileProcessingFailedExtension() {
        return ParamBean.getInstance().getProperty(PROCESSING_FAILED_EXT, DEFAULT_PROCESSING_FAILED_EXT);
    }

    /**
     * Get extension for the error file.
     */
    public static String getErrorFileExtension() {
        return ParamBean.getInstance().getProperty(ERROR_FILE_EXT, DEFAULT_ERROR_FILE_EXT);
    }

    /**
     * Get extension for the ignored tickets file.
     */
    public static String getIgnoredFileExtension() {
        return ParamBean.getInstance().getProperty(IGNORED_FILE_EXT, DEFAULT_IGNORED_FILE_EXT);
    }

    /**
     * Get extensions of files to process.
     * 
     */
    public static List<String> getFileExtensions() {
        String extensions = ParamBean.getInstance().getProperty(FILE_EXT_TO_PROCESS, DEFAULT_FILE_EXT_TO_PROCESS);
        String[] exts = extensions.split(",");
        return Arrays.asList(exts);
    }

    /**
     * Get persistence unit name.
     * 
     */
    public static String getPersistenceUnitName() {
        return ParamBean.getInstance().getProperty(PERSISTENCE_UNIT_NAME, DEFAULT_PERSISTENCE_UNIT_NAME);
    }

    /**
     * Get SQL Batch size.
     * 
     */
    public static long getSQLBatchSize() {
        String value = ParamBean.getInstance().getProperty(SQL_BATCH_SIZE, DEFAULT_SQL_BATCH_SIZE);
        return Long.parseLong(value);
    }

    /**
     * Get Magic numbers count.
     * 
     */
    public static int getMagicNumbersCount() {
        String value = ParamBean.getInstance().getProperty(MAGIC_NUMBERS_COUNT, DEFAULT_MAGIC_NUMBERS_COUNT);
        return Integer.parseInt(value);
    }
    
    /**
     * Get Magic numbers count for temp cache. It should not be smaller than biggest possible number of CDRs per file.
     * 
     */
    public static int getMagicNumbersCountInFile() {
        String value = ParamBean.getInstance().getProperty(MAGIC_NUMBERS_COUNT_IN_FILE, DEFAULT_MAGIC_NUMBERS_COUNT_IN_FILE);
        return Integer.parseInt(value);
    }

    /**
     * Get default zone code.
     * 
     */
    public static String getDefaultZone() {
        return ParamBean.getInstance().getProperty(DEFAULT_ZONE, DEFAULT_DEFAULT_ZONE);
    }

    /**
     * Get default zone code.
     * 
     */
    public static String getDefaultPLMN() {
        return ParamBean.getInstance().getProperty(DEFAULT_PLMN, DEFAULT_DEFAULT_PLMN);
    }

    /**
     * Get maximum number of working threads.
     */
    public static int getThreadCount() {
        String value = ParamBean.getInstance().getProperty(WORKING_THREADS, DEFAULT_WORKING_THREADS);
        return Integer.parseInt(value);
    }

    /**
     * Get separator for hashable fields.
     */
    public static char getHashableFieldsSeparator() {
        return '#';
    }

    /**
     * Get JMS ConnectionFactory JNDI name.
     */
    public static String getJMSConnectionFactoryJNDIName() {
        return ParamBean.getInstance().getProperty(CONNECTION_FACTORY_JNDI, DEFAULT_CONNECTION_FACTORY_JNDI);
    }

    /**
     * Get JMS Queue for Last PLMN change notification.
     */
    public static String getPLMNChangeJMSQueueJNDIName() {
        return ParamBean.getInstance().getProperty(PLMN_CHANGE_QUEUE_JNDI);
    }

    /**
     * Get JMS Queue for Last PLMN change notification.
     */
    public static String getMagicNumberCalculationAlgorithm() {
        return ParamBean.getInstance().getProperty(MAGIC_NUMBER_CALCULATION_ALGORITHM,
                DEFAULT_MAGIC_NUMBER_CALCULATION_ALGORITHM);
    }

    public static String getPAMatchingId() {
        return ParamBean.getInstance().getProperty(PA_MATCHING_ID, DEFAULT_PA_MATCHING_ID);
    }
    
    public static String getCDRTicketId() {
        return ParamBean.getInstance().getProperty(CDR_TICKET_ID, DEFAULT_CDR_TICKET_ID);
    }

    public static String getRejectedTicketId() {
        return ParamBean.getInstance().getProperty(REJECTED_TICKET_ID, DEFAULT_REJECTED_TICKET_ID);
    }

    public static String getPAMatchingAccessUserId() {
        return ParamBean.getInstance().getProperty(PA_MATCHING_ACCESS_USER_ID, DEFAULT_PA_MATCHING_ACCESS_USER_ID);
    }

    public static String getPAMatchingAccessServiceId() {
        return ParamBean.getInstance()
                .getProperty(PA_MATCHING_ACCESS_SERVICE_ID, DEFAULT_PA_MATCHING_ACCESS_SERVICE_ID);
    }

    public static String getHashId() {
    	return ParamBean.getInstance().getProperty(HASH_ID, DEFAULT_HASH_ID);
    }

    /**
     * Get overridden persistence configuration.
     */
    public static Map<String, String> getPersistenceProperties() {
        String username = ParamBean.getInstance().getProperty(DB_USERNAME);
        String password = ParamBean.getInstance().getProperty(DB_PASSWORD);
        String url = ParamBean.getInstance().getProperty(DB_URL);
        String hbm2ddl = ParamBean.getInstance().getProperty(DB_HBM2DDL);
        String driverClass = ParamBean.getInstance().getProperty(DB_DRIVER_CLASS);
        String showSql = ParamBean.getInstance().getProperty(DB_SHOW_SQL);
        String poolMinSize = ParamBean.getInstance().getProperty(C3P0_POOL_MIN_SIZE);
        String poolMaxSize = ParamBean.getInstance().getProperty(C3P0_POOL_MAX_SIZE);
        String poolTimeout = ParamBean.getInstance().getProperty(C3P0_POOL_TIMEOUT);
        String poolIdleTestPeriod = ParamBean.getInstance().getProperty(C3P0_POOL_IDLE_TEST_PERIOD);

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
        if (poolMinSize != null) {
            params.put(C3P0_POOL_MIN_SIZE, poolMinSize);
        }
        if (poolMaxSize != null) {
            params.put(C3P0_POOL_MAX_SIZE, poolMaxSize);
        }
        if (poolTimeout != null) {
            params.put(C3P0_POOL_TIMEOUT, poolTimeout);
        }
        if (poolIdleTestPeriod != null) {
            params.put(C3P0_POOL_IDLE_TEST_PERIOD, poolIdleTestPeriod);
        }
        return params;
    }

    /**
     * Get if ticket files should be fully parsed before processing.
     */
    public static Boolean getFullParseFirst() {
        return Boolean.valueOf(ParamBean.getInstance().getProperty(FULL_PARSE_FIRST, DEFAULT_FULL_PARSE_FIRST));
    }

    /**
     * Get directory to scan for zone imports.
     */
    public static String getZoneImportFilesDirectory() {
        return ParamBean.getInstance().getProperty(ZONE_IMPORT_DIR);
    }
    
    /**
     * Get directory to scan for numbering imports.
     */
    public static String getNumberingImportFilesDirectory() {
        return ParamBean.getInstance().getProperty(NUMBERING_IMPORT_DIR);
    }

    /**
     * Get extension for the zone import file.
     */
    public static String getZoneImportFileExtension() {
        return ParamBean.getInstance().getProperty(ZONE_IMPORT_FILE_EXT, DEFAULT_ZONE_IMPORT_FILE_EXT);
    }
    
    /**
     * Get extension for the numbering import file.
     */
    public static String getNumberingImportFileExtension() {
        return ParamBean.getInstance().getProperty(NUMBERING_IMPORT_FILE_EXT, DEFAULT_NUMBERING_IMPORT_FILE_EXT);
    }
    
    /**
     * Get magic numbers recovery directory to search for files.
     */
    public static String getMagicNumbersRecoveryDir() {
        return ParamBean.getInstance().getProperty(MAGIC_NUMBERS_RECOVERY_FILE_DIR);
    }
    
    /**
     * Get offers that can be used for SMS usage.
     */
    public static String getSMSOffers() {
        return DBConfigBean.getInstance().getProperty(SMS_OFFERS, DEFAULT_SMS_OFFERS);
    }
    
    /**
     * Get offers that can be used for VOICE usage.
     */
    public static String getVOICEOffers() {
        return DBConfigBean.getInstance().getProperty(VOICE_OFFERS, DEFAULT_VOICE_OFFERS);
    }
    
    /**
     * Get offers that can be used for VOICE usage.
     */
    public static String getDataCSDOffers() {
        return DBConfigBean.getInstance().getProperty(DATA_CSD_OFFERS, DEFAULT_DATA_CSD_OFFERS);
    }
    
    
    /**
     * Get get aggregation batch job cron
     */
    public static String getAggregationBatchJobCron() {
        return ParamBean.getInstance().getProperty(AGGREGATION_BATCH_JOB_CRON, DEFAULT_AGGREGATION_BATCH_JOB_CRON);
    }

    /**
     * Get aggregation delay in hours.
     */
    public static String getAggregationDelay() {
        return ParamBean.getInstance().getProperty(AGGREGATION_DELAY, DEFAULT_AGGREGATION_DELAY);
    }

    /**
     * Get get rejected tickets batch job cron.
     */
    public static String getRejectedTicketsBatchJobCron() {
        return ParamBean.getInstance().getProperty(REJECTED_TICKETS_BATCH_JOB_CRON, DEFAULD_REJECTED_TICKETS_BATCH_JOB_CRON);
    }

    /**
     * Get rejected tickets delay in hours (the time span after ticket is not retried anymore and is rejected to file finally). 
     */
    public static String getRejectedTicketsDelay() {
        return ParamBean.getInstance().getProperty(REJECTED_TICKETS_DELAY, DEFAULT_REJECTED_TICKETS_DELAY);
    }
    
    /**
     * Gets number of ticket per file for retried rejected tickets.
     */
    public static String getRetryFileTicketNbr() {
    	return ParamBean.getInstance().getProperty(RETRIED_FILE_TICKETS_NMBR, DEFAULT_RETRIED_FILE_TICKETS_NMBR);
    }
    
    /**
     * Rejection reasons for which tickets are retried automatically.
     */
    public static String getRejectionReasonsForAutoRetry() {
    	return ParamBean.getInstance().getProperty(REJECTION_REASONS_FOR_AUTOMATIC_RETRY, DEFAULT_REJECTION_REASONS_FOR_AUTOMATIC_RETRY);
    }
    
    /**
     * Maximum number of retry tickets to select. Should be configured about number of tickets that can be put in memory, otherwise OutOfMemoryError might occur.
     */
    public static String getNumberOfRetryTicketsToSelect() {
    	return ParamBean.getInstance().getProperty(NUMBER_OF_RETRY_TICKETS_PER_SELECT, DEFAULT_NUMBER_OF_RETRY_TICKETS_PER_SELECT);
    }
    
    /**
     * Cron when medina properties should be refreshed.
     */
    public static String getRefreshPropertiesBatchJobCron() {
    	return ParamBean.getInstance().getProperty(REFRESH_PROPERTIES_BATCH_JOB_CRON, DEFAULT_REFRESH_PROPERTIES_BATCH_JOB_CRON);
    }
    
    /**
     * Billing service id.
     */
    public static String getBillingAccessServiceId(String accessServiceId) {
    	return ParamBean.getInstance().getProperty(accessServiceId, accessServiceId);
    }

}
