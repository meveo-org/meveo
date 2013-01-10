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
package org.meveo.oudaya;

import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.meveo.config.task.MeveoTask;
import org.meveo.core.inputhandler.InputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.InputLoader;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.model.admin.InputHistory;
import org.meveo.model.admin.OudayaInputHistory;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author R.AITYAAZZA
 * @created 11 janv. 11
 */
public class InvoicerTask extends MeveoTask<InvoicingTicket> {

    private static final Logger logger = Logger.getLogger(InvoicerTask.class);

    @Inject
    public InvoicerTask(Provider<InputLoader> inputLoaderProvider,
            Provider<InputHandler<InvoicingTicket>> inputHandlerProvider, Provider<OutputHandler> outputHandlerProvider) {
        super(inputLoaderProvider, inputHandlerProvider, outputHandlerProvider);
    }

    /**
     * Persist OudayaInput with results of input processing.
     * 
     * @param taskExecution
     *            Processing context object that holds all information how did.
     *            process go.
     * @param result
     *            Processing result.
     */
    protected void persistInputHistory(TaskExecution<InvoicingTicket> taskExecution) {

        try {
            String inputName = taskExecution.getInputObject().getName();

            InputHistory inputHistory = taskExecution.getInputHistory();
            Long inputHistoryId = inputHistory.getId();

            logger.info(String.format("Updating OudayaInputHistory inputHistoryId %s", inputHistoryId));

            EntityManager em = MeveoPersistence.getEntityManager();
            org.hibernate.Session session = (Session) em.getDelegate();
            session.evict(inputHistory);
            OudayaInputHistory oudayaInputHistory = em.find(OudayaInputHistory.class, inputHistoryId);
            logger.info(String.format("Updating OudayaInputHistory name= %s",
                    oudayaInputHistory != null ? oudayaInputHistory.getName() : null));
            if (oudayaInputHistory == null) {
                Date startTime = new Date();
                oudayaInputHistory = new OudayaInputHistory();
                oudayaInputHistory.setName(inputName);
                oudayaInputHistory.setAnalysisStartDate(startTime);
                oudayaInputHistory.setVersion(1);
            }
            oudayaInputHistory.setName(inputName);
            oudayaInputHistory.setAnalysisStartDate(taskExecution.getStartTime());
            oudayaInputHistory.setAnalysisEndDate(taskExecution.getEndTime());
            oudayaInputHistory.setParsedTickets(taskExecution.getParsedTicketsCount());
            oudayaInputHistory.setRejectedTickets(taskExecution.getRejectedTicketsCount());
            oudayaInputHistory.setSucceededTickets(taskExecution.getProcessedTicketsCount());
            oudayaInputHistory.setProvider(taskExecution.getProvider());
            if (!MeveoPersistence.getEntityManager().getTransaction().isActive()) {
                MeveoPersistence.getEntityManager().getTransaction().begin();
            }

            em.merge(oudayaInputHistory);
            session.createQuery("update InputHistory set INPUT_TYPE = :newType where id = :id").setString("newType",
                    "OUDAYA").setLong("id", inputHistoryId).executeUpdate();
            logger.info(String.format("Inserting OudayaInputHistory entity for source %s completed successfuly",
                    inputName));

        } catch (Exception e) {
            logger.error("Could not save batch process ", e);
        }
    }

}
