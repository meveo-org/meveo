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
package org.meveo.oudaya.inputhandler;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.core.inputhandler.AbstractInputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.Input;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.Constants;
import org.meveo.model.billing.BillingRun;
import org.meveo.oudaya.InvoicingTicket;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;

public class OudayaInvoicingInputHandler extends AbstractInputHandler<InvoicingTicket> {

    /** Logger. */
    private static final Logger logger = Logger.getLogger(OudayaInvoicingInputHandler.class);

    /**
     * @param processor
     */
    @Inject
    public OudayaInvoicingInputHandler(Processor<InvoicingTicket> processor, OutputProducer outputProducer) {
        super(processor, outputProducer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TaskExecution<InvoicingTicket> executeInputHandling(Input input, TaskExecution<InvoicingTicket> taskExecution)
            throws Exception {
        int loadedBillingRuns = 0;
        int acceptedBillingRuns = 0;
        int rejectedBillingRuns = 0;

        List<BillingRun> billingRuns = (List<BillingRun>) input.getInputObject();
        long processStart = System.currentTimeMillis();
        logger.info(String.format("Found %s billingRuns", (billingRuns == null ? "null" : billingRuns.size())));
        for (BillingRun billingRun : billingRuns) {

            loadedBillingRuns++;
            InvoicingTicket invoicingTicket = new InvoicingTicket(billingRun);
            Map<String, Object> ticketContextParameters = processor.process(invoicingTicket, taskExecution);
            if ((Boolean) ticketContextParameters.get(Constants.ACCEPTED)) {
                acceptedBillingRuns++;
            } else {
                rejectedBillingRuns++;
            }
        }
        logger.info("Processor process acceptedBillingRuns: " + acceptedBillingRuns);
        logger.info("Processor process rejectedBillingRuns: " + rejectedBillingRuns);
        logger.info("Processor process took: " + (System.currentTimeMillis() - processStart));
        long commitStart = System.currentTimeMillis();
        // processor.commit(taskExecution);
        logger.info("Processor commit took: " + (System.currentTimeMillis() - commitStart));

        taskExecution.setParsedTicketsCount(loadedBillingRuns);
        taskExecution.setProcessedTicketsCount(acceptedBillingRuns);
        taskExecution.setRejectedTicketsCount(rejectedBillingRuns);
        return taskExecution;
    }

    /**
     * @see org.meveo.core.inputhandler.AbstractInputHandler#rejectTicket(org.meveo.core.inputloader.Input,
     *      org.meveo.core.ticket.Ticket, java.lang.String)
     */
    protected void rejectTicket(Input input, InvoicingTicket ticket, String status) {
        EntityManager em = MeveoPersistence.getEntityManager();
        BillingRun billingRun = ticket.getBillingRun();
        billingRun.setRejectionReason(status);
        em.merge(billingRun);

    }

}
