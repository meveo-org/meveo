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
package org.meveo.grieg.invoiceConverter.process;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.grieg.constants.GriegConstants;
import org.grieg.ticket.GriegTicket;
import org.meveo.config.MeveoConfig;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.StepExecution;
import org.meveo.grieg.invoiceConverter.ticket.InvoiceData;
import org.meveo.model.billing.BillingAccount;
import org.meveo.persistence.MeveoPersistence;

/**
 * Loads billing account and sets it to step execution context for other steps.
 * 
 * @author Ignas Lelys
 * @created Jan 27, 2011
 *
 */
public class BillingAccountLoadingStep extends AbstractProcessStep<GriegTicket> {

    public BillingAccountLoadingStep(AbstractProcessStep<GriegTicket> nextStep, MeveoConfig config) {
        super(nextStep, config);
    }

    /**
     * @see org.meveo.core.process.step.AbstractProcessStep#execute(org.meveo.core.process.step.StepExecution)
     */
    @Override
    protected boolean execute(StepExecution<GriegTicket> stepExecution) {
        TaskExecution<GriegTicket> taskExecution = stepExecution.getTaskExecution();
        InvoiceData ticket = (InvoiceData) stepExecution.getTicket();
        try {
            EntityManager em = MeveoPersistence.getEntityManager();
            Query query = em.createQuery("select ba from BillingAccount ba where ba.code = :code");
            query.setParameter("code", ticket.getBillingAccountCode());
            BillingAccount billingAccount = (BillingAccount) query.getSingleResult();
            logger.info("is electronic invoice ("+billingAccount+":"+billingAccount.getDescription()+") : "+billingAccount.getElectronicBilling());
            if (taskExecution.getProvider() == null) {
                taskExecution.setProvider(billingAccount.getProvider());
            }
            stepExecution.addParameter(GriegConstants.BILLING_ACCOUNT, billingAccount);
            return true;
        } catch (Exception e) {
            setNotAccepted(stepExecution, "BILLING_ACCOUNT_NOT_LOADED");
            return false;
        }
        
    }

}
