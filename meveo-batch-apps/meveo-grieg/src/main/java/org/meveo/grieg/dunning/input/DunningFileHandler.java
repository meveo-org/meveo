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
package org.meveo.grieg.dunning.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.grieg.GriegConfig;
import org.grieg.ticket.GriegTicket;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.core.inputhandler.AbstractFileInputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.Input;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.parser.Parser;
import org.meveo.core.process.Processor;

import com.google.inject.Inject;

/**
 * @author R.AITYAAZZA
 * @created 18 mars 11
 */
public class DunningFileHandler extends AbstractFileInputHandler<GriegTicket> {

    private static final Logger logger = Logger.getLogger(DunningFileHandler.class);

    protected File rejectedTicketsFile;

    protected PrintWriter rejectedTicketsWriter;

    /**
     * @param processor
     */
    @Inject
    public DunningFileHandler(Processor<GriegTicket> processor, OutputProducer outputProducer,
            Parser<GriegTicket> parser) {
        super(processor, outputProducer, parser);
    }

    /**
     * @see org.meveo.core.inputhandler.AbstractFileInputHandler#afterProcessing(org.meveo.core.inputloader.Input,
     *      org.meveo.core.inputhandler.TaskExecution)
     */
    @Override
    protected void afterProcessing(Input input, TaskExecution<GriegTicket> taskExecution) {
        // move original file to accepted files dir
        super.afterProcessing(input, taskExecution);

        // move rejected tickets file
        if (taskExecution.getRejectedTicketsCount() > 0) {
            boolean moved = false;
            try {
                moved = FileUtils.moveFile(((GriegConfig)config).getRejectedTicketsFilesDirectory(), rejectedTicketsFile,
                        getRejectedTicketsFilename(input.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            logger.info("moved=" + moved);

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("No rejected tickets in %s", input.getName()));
            }
        }
    }

    /**
     * @see org.meveo.core.inputhandler.AbstractFileInputHandler#rejectTicket(org.meveo.core.inputloader.Input,
     *      java.lang.Object, java.lang.String)
     */
    @Override
    protected void rejectTicket(Input input, GriegTicket ticket, String status) {
        try {
            if (rejectedTicketsFile == null) {
                try {
                    rejectedTicketsFile = File.createTempFile(input.getName(), String.valueOf(System
                            .currentTimeMillis()), new File(((GriegConfig)config).getTempFilesDirectory()));
                } catch (IOException e) {
                    throw new ConfigurationException("Could not set up parsing environment.", e);
                }
            }
            if (rejectedTicketsWriter == null) {
                rejectedTicketsWriter = new PrintWriter(rejectedTicketsFile);
            }
            rejectedTicketsWriter.println(new StringBuilder("<rejectionReason>").append(status).append(
                    "</rejectionReason>"));
            rejectedTicketsWriter.print(ticket.getSource());
            rejectedTicketsWriter.println(((GriegConfig)config).getTicketSeparator());
            rejectedTicketsWriter.flush();
            // TODO close writer
        } catch (FileNotFoundException e) {
            logger.error("Could not open ignored tickets output file", e);
            throw new ConfigurationException("Could not write to ignored output", e);
        }
    }

    /**
     * Takes original name and add.
     * 
     * @param originalName
     *            Original file name
     * @return File name for rejected tickets file.
     */
    private String getRejectedTicketsFilename(String originalName) {
        Format rejectedTicketsFileExtensionFormat = new SimpleDateFormat(((GriegConfig)config).getErrorFileExtension());
        return FileUtils.replaceFilenameExtension(originalName, rejectedTicketsFileExtensionFormat.format(new Date()));
    }

}
