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
package org.myevo.rating.inputhandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.core.inputhandler.AbstractFileInputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.Input;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.parser.Parser;
import org.meveo.core.process.Processor;
import org.myevo.rating.ticket.EDRTicket;

import com.google.inject.Inject;

public class EDRInputHandler extends AbstractFileInputHandler<EDRTicket> {

    private static final Logger logger = Logger.getLogger(EDRInputHandler.class);

    protected File rejectedTicketsFile;

    protected PrintWriter rejectedTicketsWriter;

    /**
     * Constructor with parameters for guice injection.
     * 
     * @param processor Injected {@link Processor} implementation.
     * @param outputProducer Injected {@link Processor} implementation.
     * @param parser Injected {@link Parser} implementation.
     */
    @Inject
    public EDRInputHandler(Processor<EDRTicket> processor, OutputProducer outputProducer, Parser<EDRTicket> parser) {
        super(processor, outputProducer, parser);
    }

    /**
     * @see org.meveo.core.inputhandler.AbstractFileInputHandler#afterProcessing(org.meveo.core.inputloader.Input, org.meveo.core.inputhandler.TaskExecution)
     */
    @Override
    protected void afterProcessing(Input input, TaskExecution<EDRTicket> taskExecution) {

        super.afterProcessing(input, taskExecution);

        logger.debug(String.format("%s processed with the following results: parsed tickets: %s, processed tickets: %s, rejected tickets: %s", input.getName(), taskExecution
            .getParsedTicketsCount(), taskExecution.getProcessedTicketsCount(), taskExecution.getRejectedTicketsCount()));

        // move rejected tickets file
        if (taskExecution.getRejectedTicketsCount() > 0) {
            if (rejectedTicketsWriter != null) {
                rejectedTicketsWriter.close();
            }
            boolean moved = FileUtils.moveFile(config.getRejectedTicketsFilesDirectory(), rejectedTicketsFile, getRejectedTicketsFilename(input.getName()));
            if (!moved) {
                logger.error("Could not move rejected ticket file from " + rejectedTicketsFile.getAbsolutePath() + " to " + config.getRejectedTicketsFilesDirectory());
            }
        }
    }

    /**
     * @see org.meveo.core.inputhandler.AbstractFileInputHandler#rejectTicket(org.meveo.core.inputloader.Input, java.lang.Object, java.lang.String)
     */
    @Override
    protected void rejectTicket(Input input, EDRTicket ticket, String status) {
        try {
            if (rejectedTicketsWriter == null) {
                if (rejectedTicketsFile == null) {

                    File tempDir = null;
                    if (config.getTempFilesDirectory() != null) {
                        tempDir = new File(config.getTempFilesDirectory());
                    }

                    rejectedTicketsFile = File.createTempFile(input.getName(), String.valueOf(System.currentTimeMillis()), tempDir);
                    logger.debug("Creating rejected ticket file " + rejectedTicketsFile.getAbsolutePath());
                }
                rejectedTicketsWriter = new PrintWriter(rejectedTicketsFile);
            }
            rejectedTicketsWriter.print(String.valueOf(ticket.getSource()));
            rejectedTicketsWriter.print(config.getTicketSeparator());
        } catch (FileNotFoundException e) {
            logger.error("Could not open rejected tickets output file", e);
            throw new ConfigurationException("ould not write to rejected tickets file", e);
        } catch (IOException e) {
            logger.error("Could not create temporary file for rejected tickets", e);
            throw new ConfigurationException("Could not write to rejected tickets file", e);
        }
    }

    /**
     * Takes original name and add.
     * 
     * @param originalName Original file name
     * @return File name for rejected tickets file.
     */
    private String getRejectedTicketsFilename(String originalName) {
        Format format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return FileUtils.replaceFilenameExtension(originalName, format.format(new Date()));
    }

}
