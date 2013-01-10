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
package org.meveo.grieg.invoiceConverter.output;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.log4j.Logger;
import org.grieg.GriegConfig;
import org.grieg.constants.GriegConstants;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.core.output.Output;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.grieg.output.FileNameGenerator;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.Invoice;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.PaymentMethodEnum;

import com.google.inject.Inject;

/**
 * Produces multiple pdf files.
 * 
 * @author Ignas Lelys
 * @created Dec 23, 2010
 * 
 */
public class PDFFilesOutputProducer implements OutputProducer {

    private static final String PDF_DIR_NAME = "pdf";

    private static final String INVOICE_TEMPLATE_FILENAME = "invoice.jasper";

    private static final Logger logger = Logger.getLogger(PDFFilesOutputProducer.class);

    @Inject
    private GriegConfig griegConfig;

    /**
     * @see org.meveo.core.outputproducer.OutputProducer#produceOutput(java.util.List)
     */
     public Object produceOutput(List<Output> outputTickets) {
        List<FileInfoHolder> files = new ArrayList<FileInfoHolder>();

        String resDir = griegConfig.getResourcesFilesDirectory();
        String tempDirectory = griegConfig.getTempFilesDirectory();
        
        try {
            logger.info(String.format("Producing pdf files (number of output tickets = %s):", outputTickets.size()));
            for (Output output : outputTickets) {
                String billingTemplate = (String)output.getParameters().get(GriegConstants.BILLING_TEMPLATE);
                BillingAccount billingAccount = (BillingAccount)output.getParameters().get(GriegConstants.BILLING_ACCOUNT);
                CustomerAccount customerAccount = (CustomerAccount)output.getParameters().get(GriegConstants.CUSTOMER_ACCOUNT);
                File jasperFile = getJasperTemplateFile(resDir, billingTemplate, billingAccount.getPaymentMethod());
                logger.info(String.format("Jasper template used: %s", jasperFile.getCanonicalPath()));
                InputStream reportTemplate = new FileInputStream(jasperFile);
                JRXmlDataSource dataSource = new JRXmlDataSource(new ByteArrayInputStream(output.getTicketOutput().getBytes()),
                        "/invoice/detail/userAccounts/userAccount/categories/category/subCategories/subCategory/line");
                JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportTemplate);
                JasperPrint jasperPrint = JasperFillManager
                        .fillReport(jasperReport, output.getParameters(), dataSource);
                Invoice invoice = (Invoice)output.getParameters().get(GriegConstants.INVOICE);
//                Date invoiceDate = invoice.getInvoiceDate();
                String pdfFileName = FileNameGenerator.getNameWoutSequence(tempDirectory, invoice.getInvoiceDate(), invoice.getInvoiceNumber())+".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFileName);
                logger.info(String.format("PDF file '%s' produced", pdfFileName));
                files.add(new FileInfoHolder(pdfFileName, invoice, billingAccount, customerAccount, billingTemplate));
            }
        } catch (Exception e) {
            logger.error("Error producing PDF output", e);
            throw new ConfigurationException();
        }
        return files;
    }
    
    private File getJasperTemplateFile(String resDir, String billingTemplate, PaymentMethodEnum paymentMethod) {
        String pdfDirName = new StringBuilder(resDir).append(File.separator).append(billingTemplate).append(File.separator).append(PDF_DIR_NAME).toString();
        File pdfDir = new File(pdfDirName);
        String paymentMethodFileName = new StringBuilder("invoice_").append(paymentMethod).append(".jasper").toString();
        File paymentMethodFile = new File(pdfDir, paymentMethodFileName);
        if (paymentMethodFile.exists()) {
            return paymentMethodFile;
        } else {
            File defaultTemplate = new File(pdfDir, INVOICE_TEMPLATE_FILENAME);
            return defaultTemplate;
        }
        
    }

}