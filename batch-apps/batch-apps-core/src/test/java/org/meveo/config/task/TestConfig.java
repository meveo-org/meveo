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
package org.meveo.config.task;

import java.util.Arrays;
import java.util.List;

import org.meveo.config.MeveoFileConfig;

/**
 * @author Ignas Lelys
 * @created Dec 23, 2010
 *
 */
public class TestConfig implements MeveoFileConfig {
    
    private static final String TEST_DIR = "target/test-classes/files/";
    private static final String TEST_ACCEPTED_DIR = "target/test-classes/files/accepted/";

    @Override
    public String getAcceptedFilesDirectory() {
        return TEST_ACCEPTED_DIR;
    }
    @Override
    public String getErrorFileExtension() {
        return null;
    }
    @Override
    public List<String> getFileExtensions() {
        return Arrays.asList(".csv");
    }
    @Override
    public String getFileProcessingExtension() {
        return ".processing";
    }
    @Override
    public String getFileProcessingFailedExtension() {
        return ".failed";
    }
    @Override
    public String getIgnoredFileExtension() {
        return null;
    }
    @Override
    public String getOutputFilesDirectory() {
        return null;
    }
    @Override
    public String getRejectedFilesDirectory() {
        return null;
    }
    @Override
    public String getRejectedTicketsFilesDirectory() {
        return null;
    }
    @Override
    public String getSourceFilesDirectory() {
        return TEST_DIR;
    }
    @Override
    public String getTempFilesDirectory() {
        return System.getProperty("java.io.tmpdir");
    }
    @Override
    public long getSQLBatchSize() {
        return 0;
    }
    @Override
    public long getScanningInterval() {
        return 0;
    }
    @Override
    public int getThreadCount() {
        return 0;
    }
    @Override
    public String getTicketSeparator() {
        return "";
    }
    @Override
    public String getApplicationName() {
        return "MEDINA";
    }
    @Override
    public String getBatchJobCron(String batchJobName) {
        return null;
    }
    @Override
    public Long getDefaultProviderId() {
        return 1L;
    }
}