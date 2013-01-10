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
package org.meveo.core.outputhandler;

import java.io.File;

import org.apache.log4j.Logger;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.config.MeveoFileConfig;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.step.Constants;

import com.google.inject.Inject;

/**
 * Default handler implementation for copying output file form temp to output
 * directory. Output directory is retrieved from configuration, so in
 * implementation project it must be configured, otherwise
 * {@link ConfigurationException} is thrown.
 * 
 * @author Ignas Lelys
 * @created Dec 22, 2010
 * 
 */
public class CopyFileOutputHandler implements OutputHandler {

    private static final Logger logger = Logger.getLogger(CopyFileOutputHandler.class);
    
    @Inject
    private MeveoFileConfig config;
    
    /**
     * @see org.meveo.core.outputhandler.OutputHandler#handleOutput(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleOutput(TaskExecution taskExecution) {
        Object outputObject = taskExecution.getOutputObject(); 
        if (outputObject != null) {
            if (!(outputObject instanceof String)) {
                logger.error(String.format("In TaskExecution context wrong type of argument was put %s parameter. If you want to use CopyFileOutputHandler it must be String with output file name in temp directory", Constants.OUTPUT_OBJECT));
                throw new IllegalStateException("Wrong outputObject type");
            }
            String name = (String)outputObject;
            String outputFilesDirectory = config.getOutputFilesDirectory();
            logger.info(String.format("Copy output file %s to %s", name, outputFilesDirectory));
            FileUtils.moveFile(outputFilesDirectory, new File(name), null);
        }
    }

}
