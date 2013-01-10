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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.grieg.GriegConfig;
import org.grieg.constants.GriegConstants;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.core.output.Output;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.grieg.output.FileNameGenerator;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.Invoice;
import org.meveo.model.payments.CustomerAccount;

import com.google.inject.Inject;

/**
 * Loads pdf from invoice in database and creates pdf file in temp dir, which
 * will be later used in outputHandler.
 * 
 * @author Ignas Lelys
 * @created Jan 27, 2011
 * 
 */
// NOT USED anymore.
@Deprecated
public class PDFFromDBOutputProducer implements OutputProducer {

    @Inject
    private GriegConfig griegConfig;

    private static final Logger logger = Logger.getLogger(PDFFromDBOutputProducer.class);

    public Object produceOutput(List<Output> outputTickets) {
        List<FileInfoHolder> files = new ArrayList<FileInfoHolder>();
        for (Output output : outputTickets) {
            Invoice invoice = (Invoice) output.getParameters().get(GriegConstants.INVOICE);
            BillingAccount billingAccount = (BillingAccount) output.getParameters().get(GriegConstants.BILLING_ACCOUNT);
            CustomerAccount customerAccount = (CustomerAccount)output.getParameters().get(GriegConstants.CUSTOMER_ACCOUNT);
            String billingTemplate = (String) output.getParameters().get(GriegConstants.BILLING_TEMPLATE);
            byte[] pdfFileData = invoice.getPdf();
            if (pdfFileData == null) {
                logger
                        .error(String
                                .format(
                                        "No pdf found on invoice %s. Do you process invoice, that was not preprocessed and validated? To preprocess invoice put it in %s directory",
                                        invoice.getInvoiceNumber(), griegConfig.getSourceFilesDirectory()));
                throw new ConfigurationException("No pdf found on invoice. Preprocess invoice before running it on validated invoices process.");
            }
            String tempFilesDir = griegConfig.getTempFilesDirectory();
            String fileName = FileNameGenerator.getNameWoutSequence(tempFilesDir, invoice.getInvoiceDate(), invoice
                    .getInvoiceNumber());
            OutputStream pdfFileWriter = null;
            try {
                pdfFileWriter = new BufferedOutputStream(new FileOutputStream(fileName));
                pdfFileWriter.write(pdfFileData);
                pdfFileWriter.flush();
                files.add(new FileInfoHolder(fileName, invoice, billingAccount, customerAccount, billingTemplate));
            } catch (Exception e) {
                logger.error("Could not write pdf file from db to temp directory!", e);
                throw new ConfigurationException();
            } finally {
                FileUtils.closeStream(pdfFileWriter);
            }
        }
        return files;
    }
}
