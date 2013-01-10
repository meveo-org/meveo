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
package org.meveo.grieg.invoiceConverter.config;

import org.grieg.ticket.GriegTicket;
import org.meveo.config.MeveoConfig;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.ValidationStep;
import org.meveo.core.validator.Validator;
import org.meveo.grieg.invoiceConverter.process.BillingAccountLoadingStep;
import org.meveo.grieg.invoiceConverter.process.CustomerAccountLoadingStep;
import org.meveo.grieg.invoiceConverter.process.InvoiceLoadingStep;
import org.meveo.grieg.invoiceConverter.process.PDFParametersConstructionStep;
import org.meveo.grieg.invoiceConverter.process.PDFProductionStep;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provides chain of steps used when processing ticket.
 * 
 * @author Ignas Lelys
 * @created Jan 27, 2011
 *
 */
public class StepsProvider implements Provider<AbstractProcessStep<GriegTicket>> {

    private final Validator<GriegTicket> validator;

    private final MeveoConfig meveoConfig;

    @Inject
    public StepsProvider(Validator<GriegTicket> validator, MeveoConfig meveoConfig) {
        this.validator = validator;
        this.meveoConfig = meveoConfig;
    }

    public AbstractProcessStep<GriegTicket> get() {
        PDFProductionStep pdfProductionStep = new PDFProductionStep(null, meveoConfig);
        PDFParametersConstructionStep pdfParametersConstructionStep = new PDFParametersConstructionStep(pdfProductionStep, meveoConfig);
        InvoiceLoadingStep invoiceLoadingStep = new InvoiceLoadingStep(pdfParametersConstructionStep, meveoConfig);
        BillingAccountLoadingStep billingAccountLoadingStep = new BillingAccountLoadingStep(invoiceLoadingStep, meveoConfig);
        CustomerAccountLoadingStep customerAccountLoadingStep = new CustomerAccountLoadingStep(billingAccountLoadingStep, meveoConfig);
        ValidationStep<GriegTicket> validationStep = new ValidationStep<GriegTicket>(customerAccountLoadingStep, meveoConfig, validator);
        return validationStep;
    }
}