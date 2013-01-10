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
package org.meveo.vertina;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.meveo.config.task.MeveoTask;
import org.meveo.core.inputhandler.InputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.InputLoader;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.model.admin.InputHistory;
import org.meveo.model.admin.VertinaInputHistory;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.vertina.ticket.VertinaTicket;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Rater task. Extends {@link MeveoTask} and implements {@link InputHistory} saving logic for vertina.
 * 
 * @author Ignas Lelys
 * @created 2009.06.15
 */
public class RaterTask extends MeveoTask<VertinaTicket> {

    private static final Logger logger = Logger.getLogger(RaterTask.class);
    
    @Inject
    public RaterTask(Provider<InputLoader> inputLoaderProvider, Provider<InputHandler<VertinaTicket>> inputHandlerProvider, Provider<OutputHandler> outputHandlerProvider) {
        super(inputLoaderProvider, inputHandlerProvider, outputHandlerProvider);
    }
    
    /**
     * Save VertinaInputHistory with results of input processing.
     * 
     * @param taskExecution
     *            Processing context object that holds all information how did.
     */
    @Override
    protected void persistInputHistory(TaskExecution<VertinaTicket> taskExecution) {
        try {
            String inputName = taskExecution.getInputObject().getName();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Updating VertinaInputHistory entity for source %s", inputName));
            }
            InputHistory inputHistory = taskExecution.getInputHistory();
            Long inputHistoryId = inputHistory.getId();
    
            EntityManager em = MeveoPersistence.getEntityManager();
            org.hibernate.Session session = (Session) em.getDelegate();
            session.evict(inputHistory);
            session.createQuery("update InputHistory set INPUT_TYPE = :newType where id = :id").setString("newType",
                    "VERTINA").setLong("id", inputHistoryId).executeUpdate();
            VertinaInputHistory vertinaInputHistory = em.find(VertinaInputHistory.class, inputHistoryId);
            vertinaInputHistory.setName(inputName);
            vertinaInputHistory.setAnalysisStartDate(taskExecution.getStartTime());
            vertinaInputHistory.setAnalysisEndDate(taskExecution.getEndTime());
            vertinaInputHistory.setParsedTickets(taskExecution.getParsedTicketsCount());
            vertinaInputHistory.setRejectedTickets(taskExecution.getRejectedTicketsCount());
            vertinaInputHistory.setSucceededTickets(taskExecution.getProcessedTicketsCount());
            vertinaInputHistory.setProvider(taskExecution.getProvider());
            em.merge(vertinaInputHistory);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Inserting VertinaInputHistory entity for source %s completed successfuly",
                        inputName));
            }
        } catch (Exception e) {
            logger.error("Could not save batch process ", e);
        }
    }
    
}
