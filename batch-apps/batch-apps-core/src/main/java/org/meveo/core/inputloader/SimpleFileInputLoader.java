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
package org.meveo.core.inputloader;

import java.io.File;

import org.apache.log4j.Logger;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.core.inputhandler.TaskExecution;

/**
 * Input loader that scans source directory(specified in properties) for input
 * file. Time interval is also configurable in properties file, but it has
 * default 0.5s. That means every half a second loader checks if no new file
 * appeared in source directory.
 * 
 * @author Ignas Lelys
 * @created Apr 20, 2010
 * 
 */
public class SimpleFileInputLoader extends AbstractFileInputLoader {

    private static final Logger logger = Logger.getLogger(SimpleFileInputLoader.class);

    /**
     * File input loading logic. Check for a file in source files directory
     * (configured in properties). If file is found lock it from other thread by
     * appending extension. Then return this file as input.
     * 
     * @see org.meveo.core.inputloader.InputLoader#loadInput()
     */
    @Override
    public Input loadInput() {

        File file = getFileForProcessing();
        if (file != null) {
            String originalName = file.getName();
            // Avoid file of being taken by other threads by changing it's
            // extension.
            File lockedFile = FileUtils.addExtension(file, meveoFileConfig.getFileProcessingExtension());
            if (lockedFile == null) {
                logger.info(String.format("File '%s' could not be renamed. "
                        + "Another thread might have taken it first", originalName));
                throw new InputNotLoadedException(String.format("File '%s' could not be renamed. "
                    + "Another thread might have taken it first", originalName)); 
            }

            // check if file was already processed
            if (isDuplicateInput(lockedFile, originalName)) {
                FileUtils.moveFile(meveoFileConfig.getRejectedFilesDirectory(), lockedFile, originalName);
                throw new InputNotLoadedException(String
                    .format("Input with name '%s' was found in InputInfo DB entries. Input will be rejected",
                        originalName));
            }

            // return input with original name but with locked file.
            logger.info(String.format("File '%s' taken for processing", originalName));
            return new Input(originalName, lockedFile);
        } else {
            return null;
        }
    }
    
    /**
     * Move file to accepted tickets directory. Override this method if needed
     * some more handling after processing for example moving files with
     * rejected tickets, etc...
     * 
     * @param input
     *            Processed Input. In this case - file.
     * @param taskExecution
     *            TaskExecution object with context information about file
     *            processing.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleInputAfterProcessing(Input input, TaskExecution taskExecution) {
        String acceptedFilesDirectory = meveoFileConfig.getAcceptedFilesDirectory();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Moving processed file %s to %s directory", input.getName(),
                    acceptedFilesDirectory));
        }
        boolean successful = FileUtils.moveFile(acceptedFilesDirectory, (File) input.getInputObject(), input.getName());
        if (!successful) {
            throw new ConfigurationException(String.format("Input file couldn't be moved to %s", acceptedFilesDirectory));
        }
    }

    /**
     * This default implementation append file name with error suffix (for
     * example '.failed'). If different error handling is needed this method
     * should be overriden.
     * 
     * @param input
     *            Processed Input. In this case - file.
     * @param e
     *            Exception that was thrown when file was handled. It is not
     *            used in default implementation, but it can be used for
     *            different implementations of this method (for example save
     *            stack trace to db).
     * 
     * @see org.meveo.core.inputloader.InputLoader#handleInputFailure()
     */
    @Override
    public void handleInputAfterFailure(Input input, Throwable e) {
        FileUtils.addExtension((File) input.getInputObject(), meveoFileConfig.getFileProcessingFailedExtension());
    }

}
