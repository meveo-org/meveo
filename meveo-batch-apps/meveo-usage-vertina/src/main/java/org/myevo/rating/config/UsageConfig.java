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
package org.myevo.rating.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.meveo.commons.utils.ParamBean;
import org.meveo.config.MeveoFileConfig;

public class UsageConfig implements MeveoFileConfig {

    private static final String VERTINA_PROPERTIES_FILENAME = "vertina.properties";
    private static final String SCANNING_INTERVAL = "vertina.scanningInterval";
    private static final String PERSISTENCE_UNIT_NAME = "vertina.persistenceUnitName";
    private static final String SQL_BATCH_SIZE = "vertina.sqlBatchSize";
    private static final String WORKING_THREADS = "vertina.workingThreadCount";
    private static final String DB_USERNAME = "hibernate.connection.username";
    private static final String DB_PASSWORD = "hibernate.connection.password";
    private static final String DB_URL = "hibernate.connection.url";
    private static final String DB_HBM2DDL = "hibernate.hbm2ddl.auto";
    private static final String DB_DRIVER_CLASS = "hibernate.connection.driver_class";
    private static final String DB_SHOW_SQL = "hibernate.show_sql";
    private static final String FULL_PARSE_FIRST = "vertina.fullParseFirst";
    private static final String VERTINA_GUICE_MODULE = "vertina.guiceConfigurationModule";
    private static final String VERTINA_PROVIDER_CODES = "vertina.provider.codes";
    private static final String VERTINA_EDR_LOCATION = "vertina.edr.files.location";
    private static final String VERTINA_EDR_PROCESSED_LOCATION = "vertina.edr.files.processed.location";
    private static final String VERTINA_EDR_REJECTED_FILE_LOCATION = "vertina.edr.files.rejected.location";
    private static final String VERTINA_EDR_REJECTED_TICKET_LOCATION = "vertina.edr.tickets.rejected.location";
    private static final String VERTINA_DEFAULT_PROVIDER_ID = "vertina.default.provider.id";

    // Defaults
    private static final String DEFAULT_SCANNING_INTERVAL = "500"; // 0.5s
    private static final String DEFAULT_PERSISTENCE_UNIT_NAME = "VertinaPU";
    private static final String DEFAULT_SQL_BATCH_SIZE = "100";
    private static final String DEFAULT_WORKING_THREADS = "2";
    private static final String DEFAULT_FULL_PARSE_FIRST = "false";

    public String getEdrDirectory() {
        return ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(VERTINA_EDR_LOCATION);
    }

    /**
     * Get source directory scanning interval.
     */
    public long getScanningInterval() {
        String value = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(SCANNING_INTERVAL, DEFAULT_SCANNING_INTERVAL);
        return Long.parseLong(value);
    }

    /**
     * Get SQL Batch size.
     * 
     */
    public long getSQLBatchSize() {
        String value = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(SQL_BATCH_SIZE, DEFAULT_SQL_BATCH_SIZE);
        return Long.parseLong(value);
    }

    /**
     * Get maximum number of working threads.
     */
    public int getThreadCount() {
        String value = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(WORKING_THREADS, DEFAULT_WORKING_THREADS);
        return Integer.parseInt(value);
    }

    /**
     * Get if ticket files should be fully parsed before processing.
     */
    public Boolean getFullParseFirst() {
        return Boolean.valueOf(ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(FULL_PARSE_FIRST, DEFAULT_FULL_PARSE_FIRST));
    }

    public String[] getProviderCodes() {
        return ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(VERTINA_PROVIDER_CODES, ",").replaceAll(";", ",").split(",");
    }

    public String getApplicationName() {
        return "VERTINA";
    }

    /**
     * @see org.meveo.config.MeveoConfig#getBatchJobCron(java.lang.String)
     */
    public String getBatchJobCron(String batchJobName) {
        return null;
    }

    /**
     * Get configuration module for Vertina application.
     */
    public static String getVertinaConfigurationModule() {
        return ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(VERTINA_GUICE_MODULE);
    }

    /**
     * Get persistence unit name.
     * 
     */
    public static String getPersistenceUnitName() {
        return ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(PERSISTENCE_UNIT_NAME, DEFAULT_PERSISTENCE_UNIT_NAME);
    }

    /**
     * Get overridden persistence configuration.
     */
    public static Map<String, String> getPersistenceProperties() {
        String username = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(DB_USERNAME);
        String password = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(DB_PASSWORD);
        String url = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(DB_URL);
        String hbm2ddl = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(DB_HBM2DDL);
        String driverClass = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(DB_DRIVER_CLASS);
        String showSql = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(DB_SHOW_SQL);

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
        return params;
    }

    public String getAcceptedFilesDirectory() {
        return ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(VERTINA_EDR_PROCESSED_LOCATION);
    }

    public String getErrorFileExtension() {
        return "ftick";
    }

    public List<String> getFileExtensions() {
        List<String> strings = new ArrayList<String>();
        strings.add("csv");

        return strings;
    }

    public String getFileProcessingExtension() {
        return "edr";
    }

    public String getFileProcessingFailedExtension() {
        return "fedr";
    }

    public String getIgnoredFileExtension() {
        return null;
    }

    public String getOutputFilesDirectory() {
        return ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(VERTINA_EDR_PROCESSED_LOCATION);
    }

    public String getRejectedFilesDirectory() {
        return ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(VERTINA_EDR_REJECTED_FILE_LOCATION);
    }

    public String getRejectedTicketsFilesDirectory() {
        return ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(VERTINA_EDR_REJECTED_TICKET_LOCATION);
    }

    public String getSourceFilesDirectory() {
        return getEdrDirectory();
    }

    public String getTempFilesDirectory() {
        return null;
    }

    public String getTicketSeparator() {
        return "\n";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.meveo.config.MeveoConfig#getDefaultProviderId()
     */
    public Long getDefaultProviderId() {
        String value = ParamBean.getInstance(VERTINA_PROPERTIES_FILENAME).getProperty(VERTINA_DEFAULT_PROVIDER_ID);
        if (value != null) {
            return Long.parseLong(value);
        } else {
            return 1L;
        }
    }
}