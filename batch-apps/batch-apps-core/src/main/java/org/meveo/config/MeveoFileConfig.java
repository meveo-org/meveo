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

import java.util.List;

/**
 * Configuration interface, that implementation configuration class must
 * implement if application is working with files.
 * 
 * @author Ignas Lelys
 * @created Dec 22, 2010
 * 
 */
public interface MeveoFileConfig extends MeveoConfig {

    /**
     * Get source directory to search for files.
     */
    public String getSourceFilesDirectory();

    /**
     * Get rejected files directory.
     */
    public String getRejectedFilesDirectory();

    /**
     * Get temporary files directory.
     */
    public String getTempFilesDirectory();

    /**
     * Get accepted files directory.
     */
    public String getAcceptedFilesDirectory();

    /**
     * Get accepted files directory.
     */
    public String getOutputFilesDirectory();

    /**
     * Get rejected tickets files directory.
     */
    public String getRejectedTicketsFilesDirectory();

    /**
     * Get extension for the file that has started being processed.
     */
    public String getFileProcessingExtension();

    /**
     * Get extension for the file failed to be processed.
     */
    public String getFileProcessingFailedExtension();

    /**
     * Get extension for the error file.
     */
    public String getErrorFileExtension();

    /**
     * Get extension for the ignored tickets file.
     */
    public String getIgnoredFileExtension();

    /**
     * Get extensions of files to process.
     */
    public List<String> getFileExtensions();
    
    /**
     * Get string value, that separates tickets in input. Empty string means no separator.
     */
    public String getTicketSeparator();

}
