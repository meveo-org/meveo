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

import java.util.Map;

import org.grieg.constants.GriegConstants;
import org.grieg.output.GriegOutput;
import org.grieg.ticket.GriegTicket;
import org.meveo.config.MeveoConfig;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.Constants;
import org.meveo.core.process.step.StepExecution;

/**
 * Create output object that will be converted later to pdf file in
 * {@link OutputProducer}.
 * 
 * @author Ignas Lelys
 * @created Dec 28, 2010
 * 
 */
public class PDFProductionStep extends AbstractProcessStep<GriegTicket> {

    public PDFProductionStep(AbstractProcessStep<GriegTicket> nextStep, MeveoConfig config) {
        super(nextStep, config);
    }

    /**
     * @see org.meveo.core.process.step.AbstractProcessStep#execute(org.meveo.core.process.step.StepExecution)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean execute(StepExecution<GriegTicket> stepExecution) {
        TaskExecution<GriegTicket> taskExecution = stepExecution.getTaskExecution();
        GriegTicket ticket = stepExecution.getTicket();
        String xmlData = ticket.getSource();
        GriegOutput output = new GriegOutput(xmlData, (Map<String, Object>) stepExecution
                .getParameter(GriegConstants.PDF_PARAMETERS)); // PDFParametersConstructionStep

        putToTaskExecutionListContextParameter(Constants.OUTPUT_TICKETS, output, taskExecution);
        return true;
    }
}