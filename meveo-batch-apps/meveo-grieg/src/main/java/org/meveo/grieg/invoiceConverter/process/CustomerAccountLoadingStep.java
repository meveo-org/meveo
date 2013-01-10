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
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.StepExecution;
import org.meveo.grieg.invoiceConverter.ticket.InvoiceData;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.persistence.MeveoPersistence;

/**
 * Loads billing account and sets it to step execution context for other steps.
 * 
 * @author Ignas Lelys
 * @created Jan 27, 2011
 * 
 */
public class CustomerAccountLoadingStep extends AbstractProcessStep<GriegTicket> {

    public CustomerAccountLoadingStep(AbstractProcessStep<GriegTicket> nextStep, MeveoConfig config) {
        super(nextStep, config);
    }

    /**
     * @see org.meveo.core.process.step.AbstractProcessStep#execute(org.meveo.core.process.step.StepExecution)
     */
    @Override
    protected boolean execute(StepExecution<GriegTicket> stepExecution) {

        InvoiceData ticket = (InvoiceData) stepExecution.getTicket();
        try {
            EntityManager em = MeveoPersistence.getEntityManager();

            Query query = em.createQuery("select ca from CustomerAccount ca where ca.code = :code");
            query.setParameter("code", ticket.getCustomerAccountCode());
            CustomerAccount customerAccount = (CustomerAccount) query.getSingleResult();
            String name = "";
            if (customerAccount.getName() != null) {
                name = customerAccount.getName().getTitle() == null ? "" : (customerAccount.getName().getTitle()
                        .getCode() + " ");
                name += customerAccount.getName().getFirstName() == null ? "" : (customerAccount.getName()
                        .getFirstName() + " ");
                name += customerAccount.getName().getLastName() == null ? "" : customerAccount.getName().getLastName();
            }
            String address = "";
            if (customerAccount.getAddress() != null) {
                address = customerAccount.getAddress().getAddress1() == null ? "" : (customerAccount.getAddress()
                        .getAddress1() + "\n");
                address += customerAccount.getAddress().getAddress2() == null ? "" : (customerAccount.getAddress()
                        .getAddress2() + "\n");
                address += customerAccount.getAddress().getAddress3() == null ? "" : (customerAccount.getAddress()
                        .getAddress3() + "\n");
                address += customerAccount.getAddress().getZipCode() == null ? "" : (customerAccount.getAddress()
                        .getZipCode() + " ");
                address += customerAccount.getAddress().getCity() == null ? "" : (customerAccount.getAddress()
                        .getCity());
            }
            stepExecution.addParameter(GriegConstants.CUSTOMER_ADDRESS_KEY, name + "\n" + address);
            stepExecution.addParameter(GriegConstants.CUSTOMER_ACCOUNT, customerAccount);
            return true;
        } catch (Exception e) {
            logger.error("Could not load CustomerAccount", e);
            setNotAccepted(stepExecution, "CUSTOMER_ACCOUNT_NOT_LOADED");
            return false;
        }

    }

}