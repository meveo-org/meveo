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
package org.meveo.core.inputhandler;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.meveo.commons.exceptions.UnexpectedMeveoException;
import org.meveo.config.MeveoFileConfig;
import org.meveo.core.inputloader.Input;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.parser.Parser;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.Constants;

import com.google.inject.Inject;

/**
 * Abstract input handler for handling files. getParser() must be implemented to
 * load a parser for received file. rejectEDR() method must be implemented logic
 * how to handle rejected tickets.
 * 
 * @author Ignas Lelys
 * @created 2009.07.16
 */
public abstract class AbstractFileInputHandler<T> extends AbstractInputHandler<T> {

    /** Parser implementation used to parse files. */
    protected Parser<T> parser;

    /**
     * Constructor with parameters for guice injection.
     * 
     * @param processor
     *            Injected {@link Processor} implementation.
     * @param outputProducer
     *            Injected {@link OutputProducer} implementation.
     * @param parser
     *            Injected {@link Parser} implementation.
     */
    @Inject
    public AbstractFileInputHandler(Processor<T> processor, OutputProducer outputProducer, Parser<T> parser) {
        super(processor, outputProducer);
        this.parser = parser;
    }

    /** Logger. */
    private static final Logger logger = Logger.getLogger(AbstractFileInputHandler.class);

    /**
     * Process file input.
     * 
     * @return {@link FileHandlingResult} bean with data which is actually
     *         subclass of {@link InputHandlingResult}.
     * @throws Exception
     */
    public TaskExecution<T> executeInputHandling(Input input, TaskExecution<T> taskExecution) throws Exception {
        int parsedTickets = 0;
        int acceptedTickets = 0;
        int rejectedTickets = 0;
        try {
            File inputFile = (File) input.getInputObject();
            parser.setParsingFile(inputFile.getAbsolutePath());
            T ticket = null;
            while ((ticket = parser.next()) != null) {
                parsedTickets++;
                Map<String, Object> ticketContextParameters = processor.process(ticket, taskExecution);
                if ((Boolean) ticketContextParameters.get(Constants.ACCEPTED) == true) {
                    acceptedTickets++;
                } else {
                    rejectedTickets++;
                    rejectTicket(input, ticket, (String) ticketContextParameters.get(Constants.STATUS));
                }
            }

            taskExecution.setParsedTicketsCount(parsedTickets);
            taskExecution.setProcessedTicketsCount(acceptedTickets);
            taskExecution.setRejectedTicketsCount(rejectedTickets);

            afterProcessing(input, taskExecution);
            return taskExecution;
        } catch (Throwable e) {
            logger.error("Unexpected error", e);
            throw new UnexpectedMeveoException();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    /**
     * This method is invoked after all tickets are processed. It can be
     * implemented if something has to be done at that time - for example move
     * rejected tickets file from temp directory to rejected tickets directory.
     */
    protected void afterProcessing(Input input, TaskExecution<T> taskExecution) {
    }

    /**
     * Ticket rejection logic.
     * 
     * @param input
     *            Input information.
     * @param ticket
     *            Rejected ticket.
     * @param status
     *            Ticket status.
     */
    protected abstract void rejectTicket(Input input, T ticket, String status);

}
