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
package org.meveo.grieg.invoiceConverter.task;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.grieg.ticket.GriegTicket;
import org.hibernate.Session;
import org.meveo.config.task.MeveoTask;
import org.meveo.core.inputhandler.InputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.InputLoader;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.model.admin.GriegValidatedInvoiceInputHistory;
import org.meveo.model.admin.InputHistory;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Converter task. Takes invoice xmls and processed them.
 * 
 * @author Ignas Lelys
 * @created Dec 21, 2010
 *
 */
public class ValidatedInvoiceProcessorTask extends MeveoTask<GriegTicket> {
    
    private static final Logger logger = Logger.getLogger(ValidatedInvoiceProcessorTask.class);

    @Inject
    public ValidatedInvoiceProcessorTask(Provider<InputLoader> inputLoaderProvider, Provider<InputHandler<GriegTicket>> inputHandlerProvider, Provider<OutputHandler> outputHandlerProvider) {
        super(inputLoaderProvider, inputHandlerProvider, outputHandlerProvider);
    }
    
    /**
     * Save GriegValidatedInvoiceInputHistory with results of input processing.
     * 
     * @param taskExecution
     *            Processing context object that holds all information how did.
     */
    @Override
    protected void persistInputHistory(TaskExecution<GriegTicket> taskExecution) {
        
        try {
            String inputName = taskExecution.getInputObject().getName();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Updating GriegValidatedInvoiceInputHistory entity for source %s", inputName));
            }
            InputHistory inputHistory = taskExecution.getInputHistory();
            Long inputHistoryId = inputHistory.getId();
    
            EntityManager em = MeveoPersistence.getEntityManager();
            org.hibernate.Session session = (Session) em.getDelegate();
            session.evict(inputHistory);
            session.createQuery("update InputHistory set INPUT_TYPE = :newType where id = :id").setString("newType",
                    "GRIEG_VALIDATED").setLong("id", inputHistoryId).executeUpdate();
            GriegValidatedInvoiceInputHistory validatedInvoiceInputHistory = em.find(GriegValidatedInvoiceInputHistory.class, inputHistoryId);
            validatedInvoiceInputHistory.setName(inputName);
            validatedInvoiceInputHistory.setAnalysisStartDate(taskExecution.getStartTime());
            validatedInvoiceInputHistory.setAnalysisEndDate(taskExecution.getEndTime());
            validatedInvoiceInputHistory.setParsedTickets(taskExecution.getParsedTicketsCount());
            validatedInvoiceInputHistory.setRejectedTickets(taskExecution.getRejectedTicketsCount());
            validatedInvoiceInputHistory.setSucceededTickets(taskExecution.getProcessedTicketsCount());
            validatedInvoiceInputHistory.setProvider(taskExecution.getProvider());
            em.merge(validatedInvoiceInputHistory);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Inserting GriegValidatedInvoiceInputHistory entity for source %s completed successfuly", inputName));
            }
        } catch (Exception e) {
            logger.error("Could not save batch process ", e);
        }
    }
    
}
