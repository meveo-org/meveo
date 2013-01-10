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
package org.meveo.rating.process;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.AbstractProcessor;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.model.billing.ApplicationChgStatusEnum;
import org.meveo.model.billing.ChargeApplication;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.rating.ticket.RatingTicket;
import org.meveo.vertina.constants.VertinaConstants;

import com.google.inject.Inject;

/**
 * Class responsible for processing chargeApplications
 * 
 * @author Ignas Lelys
 * @created 2009.06.17
 * 
 */
public class RatingProcessor extends AbstractProcessor<RatingTicket> {

    private static final Logger logger = Logger.getLogger(RatingProcessor.class);

    /**
     * @param processStepsChain
     */
    @Inject
    public RatingProcessor(AbstractProcessStep<RatingTicket> processStepsChain) {
        super(processStepsChain);
    }

    /**
     * @see org.meveo.core.process.AbstractProcessor#doCommit(org.meveo.core.inputhandler.InputContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doCommit(TaskExecution<RatingTicket> taskExecution) throws SQLException {

        List<RatedTransaction> transactions = (List<RatedTransaction>) taskExecution
                .getExecutionContextParameter(VertinaConstants.LIST_OF_TRANSACTIONS_KEY);
        List<ChargeApplication> listChargeApplication = (List<ChargeApplication>) taskExecution
                .getExecutionContextParameter(VertinaConstants.PROCESSED_CHARGE_APPLICATIONS_KEY);

        EntityManager em = MeveoPersistence.getEntityManager();
        logger.info(String.format("Commiting %s transactions", transactions != null ? transactions.size() : 0));
        if (transactions != null) {
            for (RatedTransaction transaction : transactions) {
            	if(transaction.getId()==null){
            		em.persist(transaction);
            	} else {
            	    transaction.setInputHistoryId(taskExecution.getInputHistory().getId());
            		em.merge(transaction);
            	}
            }
        }
        if (listChargeApplication != null) {
            Date statusDate = new Date();
            for (ChargeApplication chargeApplication : listChargeApplication) {
                chargeApplication.setStatus(ApplicationChgStatusEnum.TREATED);
                chargeApplication.setStatusDate(statusDate);
                chargeApplication.setInputHistoryId(taskExecution.getInputHistory().getId());
                em.merge(chargeApplication);
            }
        }

    }

    /**
     * @see org.meveo.core.process.AbstractProcessor#getNamedQueries()
     */
    @Override
    protected Map<String, String> getNamedQueries() {
        return new HashMap<String, String>();
    }

}
