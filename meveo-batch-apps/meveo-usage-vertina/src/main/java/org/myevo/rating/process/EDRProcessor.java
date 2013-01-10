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
package org.myevo.rating.process;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.AbstractProcessor;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.vertina.constants.VertinaConstants;
import org.myevo.rating.ticket.EDRTicket;

import com.google.inject.Inject;

public class EDRProcessor extends AbstractProcessor<EDRTicket> {

	private static final Logger logger = Logger.getLogger(EDRProcessor.class);

	@Inject
	public EDRProcessor(AbstractProcessStep<EDRTicket> processStepsChain) {
		super(processStepsChain);
	}

	@SuppressWarnings("unchecked")
    @Override
	protected void doCommit(TaskExecution<EDRTicket> taskExecution)
			throws SQLException {
		List<RatedTransaction> transactions = (List<RatedTransaction>) taskExecution
				.getExecutionContextParameter(VertinaConstants.LIST_OF_TRANSACTIONS_KEY);

		EntityManager em = MeveoPersistence.getEntityManager();
        logger.info(String.format("Commiting %s transactions", transactions != null ? transactions.size() : 0));
		if (transactions != null) {
			for (RatedTransaction transaction : transactions) {
				if (transaction.getId() == null) {
					em.persist(transaction);
				} else {
					transaction.setInputHistoryId(taskExecution.getInputHistory().getId());
					em.merge(transaction);
				}
			}
		}
	}

	@Override
	protected Map<String, String> getNamedQueries() {
		return new HashMap<String, String>();
	}

}
