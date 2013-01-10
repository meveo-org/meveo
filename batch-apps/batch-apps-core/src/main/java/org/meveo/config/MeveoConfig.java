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
package org.meveo.config;

/**
 * Interface for meveo configuration object. Implementation config class must
 * provide this minimum set of configuration options for meveo application to
 * work.
 * 
 * @author Ignas Lelys
 * @created Dec 22, 2010
 * 
 */
public interface MeveoConfig {

    /**
     * Get input scanning interval (in milliseconds).
     */
    public long getScanningInterval();

    /**
     * Get SQL Batch size.
     */
    public long getSQLBatchSize();

    /**
     * Get maximum number of working threads.
     */
    public int getThreadCount();

    /**
     * Gets application name in upper case. It is same name that is used in
     * InputInfo as discriminator column.
     */
    public String getApplicationName();

    /**
     * Get batch job cron by batch job name. If application uss batch job then
     * for {@link TaskAndBatchJobsLauncher} this method must be implemented.
     * Otherwise if application has no batch jobs then this method just can
     * return null since it wont be used.
     */
    public String getBatchJobCron(String batchJobName);

    
    /**
     * Provide a default provider identifier when creating records in meveo that are tied to a provider
     * 
     * @return Default provider identifier
     */
    public Long getDefaultProviderId();

}