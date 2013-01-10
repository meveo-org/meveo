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
import java.util.Map;

import org.apache.log4j.Logger;
import org.meveo.commons.utils.FileUtils;
import org.meveo.config.MeveoFileConfig;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.core.process.step.Constants;

import com.google.inject.Inject;

/**
 * If output is multiple files from one input, this handler copies them all to
 * output directory. Output object must be passed as Map, where values are file
 * names. Keys should be identifier of input.
 * 
 * @author Ignas Lelys
 * @created Dec 23, 2010
 * 
 */
public class MultipleFilesOutputHandler implements OutputHandler {

    private static final Logger logger = Logger.getLogger(MultipleFilesOutputHandler.class);

    @Inject
    private MeveoFileConfig config;

    /**
     * @see org.meveo.core.outputhandler.OutputHandler#handleOutput(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void handleOutput(TaskExecution taskExecution) {
        Object outputObject = taskExecution.getOutputObject();
        logger.info("Moving output files to output dir:");
        if (outputObject != null) {
            if (!(outputObject instanceof Map)) {
                logger.error(String.format("In TaskExecution context wrong type of argument was put %s parameter. " +
                		"If you want to use MultipleFileOutputHandler it must be List<String> with list of output file names in temp directory", Constants.OUTPUT_OBJECT));
                throw new IllegalStateException("Wrong outputObject type");
            }
            Map<String, String> fileNames = (Map<String, String>) outputObject;
            String outputFilesDirectory = config.getOutputFilesDirectory();
            for (String name : fileNames.values()) {
                logger.info(String.format("Copy output file %s to %s", name, outputFilesDirectory));
                FileUtils.moveFile(outputFilesDirectory, new File(name), null);
            }
        }
    }

}
