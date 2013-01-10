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

import java.util.HashMap;

import org.grieg.constants.GriegConstants;
import org.grieg.ticket.GriegTicket;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.step.StepExecution;
import org.meveo.grieg.invoiceConverter.ticket.InvoiceData;
import org.meveo.model.billing.Invoice;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Ignas Lelys
 * @created May 11, 2011
 * 
 */
public class InvoiceLoadingStepTest {

    /**
     * Not validated invoice wont have invoiceNumber set, so same temp invoice
     * number will be used.
     */
    @Test(groups = { "db" })
    public void testInvoiceNotValidated() {
    	InvoiceData.Builder builder = new InvoiceData.Builder().addInvoiceNumber("2");
    	builder.addInvoiceId(2);
        GriegTicket ticket = builder.build();
        InvoiceLoadingStep step = new InvoiceLoadingStep(null, null);
        StepExecution<GriegTicket> stepExecution = new StepExecution<GriegTicket>(ticket, "test",
                new HashMap<String, Object>(), new TaskExecution<GriegTicket>(null, null, null, null, null));
        step.execute(stepExecution);
        Assert.assertEquals(((Invoice) stepExecution.getParameter(GriegConstants.INVOICE)).getId(), (Long) 2L);
        Assert.assertEquals((String) stepExecution.getParameter(GriegConstants.INVOICE_NUMBER_KEY), "2");
    }

    /**
     * Validated invoice has invoiceNumber and it will be set in context.
     */
    @Test(groups = { "db" })
    public void testInvoiceValidated() {
    	InvoiceData.Builder builder = new InvoiceData.Builder().addInvoiceNumber("3");
    	builder.addInvoiceId(1);
        GriegTicket ticket = builder.build();
        InvoiceLoadingStep step = new InvoiceLoadingStep(null, null);
        StepExecution<GriegTicket> stepExecution = new StepExecution<GriegTicket>(ticket, "test",
                new HashMap<String, Object>(), new TaskExecution<GriegTicket>(null, null, null, null, null));
        step.execute(stepExecution);
        Assert.assertEquals(((Invoice) stepExecution.getParameter(GriegConstants.INVOICE)).getId(), (Long) 1L);
        Assert.assertEquals((String) stepExecution.getParameter(GriegConstants.INVOICE_NUMBER_KEY), "3");
    }

}
