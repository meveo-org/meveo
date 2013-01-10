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

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sf.jasperreports.engine.JRParameter;

import org.grieg.GriegConfig;
import org.grieg.constants.GriegConstants;
import org.grieg.ticket.GriegTicket;
import org.meveo.config.MeveoConfig;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.StepExecution;
import org.meveo.grieg.invoiceConverter.ticket.InvoiceData;
import org.meveo.model.billing.BankCoordinates;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.TIP;
import org.meveo.model.crm.Provider;

/**
 * Step that constructs hashmap with all paramters that will be used when
 * creating and handling pdf file with {@link OutputProducer} and
 * {@link OutputHandler}
 * 
 * @author Ignas Lelys
 * @created Dec 17, 2010
 * 
 */
public class PDFParametersConstructionStep extends AbstractProcessStep<GriegTicket> {

    private static final String TIP_PAYMENT_METHOD = "TIP";
    private static final String PDF_DIR_NAME = "pdf";
    private static NumberFormat currencyFormat =  NumberFormat.getInstance(new Locale("FR"));
    static {
	    currencyFormat.setMinimumFractionDigits(2);
    }

    private ClassLoader cl = new URLClassLoader(new URL[] { PDFProductionStep.class.getClassLoader().getResource(
            "reports/fonts.jar") });

    public PDFParametersConstructionStep(AbstractProcessStep<GriegTicket> nextStep, MeveoConfig config) {
        super(nextStep, config);
    }

    @Override
    protected boolean execute(StepExecution<GriegTicket> stepExecution) {
        try {
            GriegConfig griegConfig = (GriegConfig) super.config;
            InvoiceData ticket = (InvoiceData) stepExecution.getTicket();
            Provider provider = (Provider)stepExecution.getTaskExecution().getProvider();
        
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(JRParameter.REPORT_CLASS_LOADER, cl);
            String billingTemplateName =ticket.getBillingTemplateName();
            String resourcesFilesDirectory = griegConfig.getResourcesFilesDirectory();
            String messagePathKey = new StringBuilder(resourcesFilesDirectory).append(File.separator).append(
                    billingTemplateName).append(File.separator).append(PDF_DIR_NAME).append(File.separator).toString();
            parameters.put(GriegConstants.MESSAGE_PATH_KEY, messagePathKey);
            parameters.put(GriegConstants.LOGO_PATH_KEY, messagePathKey);
            parameters.put(GriegConstants.CUSTOMER_ADDRESS_KEY, stepExecution.getParameter(GriegConstants.CUSTOMER_ADDRESS_KEY));
            String resDir = griegConfig.getResourcesFilesDirectory();
            String pdfDirName = new StringBuilder(resDir).append(File.separator).append(billingTemplateName).append(File.separator).append(PDF_DIR_NAME).toString();
            parameters.put(GriegConstants.SUBREPORT_DIR, pdfDirName);
            if (TIP_PAYMENT_METHOD.equals(ticket.getPaymentMethod())) {
            	BigDecimal netToPay = (BigDecimal) stepExecution.getParameter(GriegConstants.NET_TO_PAY_KEY);
            	if(netToPay.signum() != 1){
            		parameters.put(GriegConstants.HIGH_OPTICAL_LINE_KEY," ");
                	parameters.put(GriegConstants.LOW_OPTICAL_LINE_KEY, " ");
            	}
            	else {
            	BankCoordinates bankCoordinates = ((BillingAccount) stepExecution.getParameter(GriegConstants.BILLING_ACCOUNT)).getBankCoordinates();
            	if(bankCoordinates==null || bankCoordinates.getBankCode()==null) {
            		BankCoordinates bankCoordinatesEmpty = new BankCoordinates();
            		bankCoordinatesEmpty.setAccountNumber("           ");
            		bankCoordinatesEmpty.setBankCode("     ");
            		bankCoordinatesEmpty.setBranchCode("     ");
            		bankCoordinatesEmpty.setKey("  ");
                    TIP tip = new TIP(provider.getInterBankTitle().getCodeCreancier(), provider.getInterBankTitle().getCodeEtablissementCreancier(), 
                            provider.getInterBankTitle().getCodeCentre(), bankCoordinatesEmpty, ticket.getCustomerAccountCode(), ticket.getInvoiceId(),
                            ticket.getInvoiceDate(), ticket.getDueDate(), netToPay);
                    parameters.put(GriegConstants.HIGH_OPTICAL_LINE_KEY, tip.getLigneOptiqueHaute());
                    parameters.put(GriegConstants.LOW_OPTICAL_LINE_KEY, tip.getLigneOptiqueBasse());
            	} else {
            		TIP tip = new TIP(provider.getInterBankTitle().getCodeCreancier(), provider.getInterBankTitle().getCodeEtablissementCreancier(), 
                        provider.getInterBankTitle().getCodeCentre(), bankCoordinates, ticket.getCustomerAccountCode(), ticket.getInvoiceId(),
                        ticket.getInvoiceDate(), ticket.getDueDate(), netToPay);
            		parameters.put(GriegConstants.HIGH_OPTICAL_LINE_KEY, tip.getLigneOptiqueHaute());
            		parameters.put(GriegConstants.LOW_OPTICAL_LINE_KEY, tip.getLigneOptiqueBasse());
            	}
            }
            }
            
            parameters.put(GriegConstants.INVOICE_NUMBER_KEY, stepExecution.getParameter(GriegConstants.INVOICE_NUMBER_KEY));
            parameters.put(GriegConstants.BILLING_TEMPLATE, billingTemplateName);
            parameters.put(GriegConstants.BILLING_ACCOUNT, stepExecution.getParameter(GriegConstants.BILLING_ACCOUNT));
            parameters.put(GriegConstants.CUSTOMER_ACCOUNT, stepExecution.getParameter(GriegConstants.CUSTOMER_ACCOUNT));
            parameters.put(GriegConstants.INVOICE, stepExecution.getParameter(GriegConstants.INVOICE));
            
        
            stepExecution.addParameter(GriegConstants.PDF_PARAMETERS, parameters);
            
            return true;
        
        } catch (Exception e) {
        	e.printStackTrace();
            setNotAccepted(stepExecution, "WRONG_TIP_PARAMETER");
            return false;
        }
    }

}