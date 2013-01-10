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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.commons.utils.FileUtils;
import org.meveo.grieg.output.FileNameGenerator;
import org.meveo.model.admin.User;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.Invoice;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.persistence.MeveoPersistence;

/**
 * Output handler for maileva. If electoric billing set in billing account - add email recipient to res/email/maileva.jps (which later will be handled by batch job). If there are
 * no electronic billing flag set - then create zip archive in output dir which contains pdf file from invoice in db and maileva.jps file.
 * 
 * @author Ignas Lelys
 * @created Jan 11, 2011
 * 
 */
public class MailevaOutputHandler extends PDFOutputHandler {

    private static final String MAILEVA_FILENAME = "maileva.jps";

    private static final String MAILEVA_FILENAME_IN_ZIP = "maileva.pjs";

    private static final Logger logger = Logger.getLogger(MailevaOutputHandler.class);

    private static String newline = System.getProperty("line.separator");

    @Override
    protected void handlePDFFile(FileInfoHolder pdfFile, BillingAccount billingAccount, Invoice invoice, String outputFilesDirectory) {

        String resDirectory = config.getResourcesFilesDirectory();
        String tempDirectory = config.getTempFilesDirectory();

        String mailevaFileName = new StringBuilder(resDirectory).append(File.separator).append(pdfFile.getBillingTemplate()).append(File.separator).append("paper")
            .append(File.separator).append(MAILEVA_FILENAME).toString();
        String modifiedMailevaFileName = new StringBuilder(tempDirectory).append(File.separator).append(MAILEVA_FILENAME_IN_ZIP).toString();
        try {
            replaceELInTemplate(mailevaFileName, modifiedMailevaFileName, billingAccount, pdfFile.getCustomerAccount());
        } catch (IOException e) {
            logger.error("error", e); // TODO
        }
        String zipName = getZipName(pdfFile.getInvoice().getInvoiceNumber(), FileNameGenerator.formatInvoiceDate(invoice.getInvoiceDate()));
        String fullZipFileName = new StringBuilder(tempDirectory).append(File.separator).append(zipName).toString();
        logger.info(String.format("Electronic billing flag is not set. Producing zip file '%s'", fullZipFileName));

        File pdfFileFile = new File(pdfFile.getFileName());
        String finalPDFFileName = null;
        if (billingAccount.getInvoicePrefix() != null) {
            finalPDFFileName = new StringBuilder(billingAccount.getInvoicePrefix()).append("_").append("maileva.001").toString();

        } else {
            finalPDFFileName = "maileva.001";
        }
        String finalPDFFileFullName = new StringBuilder(tempDirectory).append(File.separator).append(finalPDFFileName).toString();
        FileUtils.renameFile(pdfFileFile, finalPDFFileName);

        // add to archive original pdf file and modified maileva.jps
        // and save zip file in temp dir
        FileUtils.createZipArchive(fullZipFileName, finalPDFFileFullName, modifiedMailevaFileName);

        logger.info(String.format("Moving zip file from %s dir to %s dir", tempDirectory, outputFilesDirectory));
        // after zip file was created move it to output dir
        FileUtils.moveFile(outputFilesDirectory, new File(fullZipFileName), zipName);
    }

    private void replaceELInTemplate(String mailevaTemplateFileName, String mailevaFileName, BillingAccount billingAccount, CustomerAccount customerAccount) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(mailevaFileName));
        try {
            String content = FileUtils.getFileAsString(mailevaTemplateFileName);
            String id = billingAccount.getId().toString();
            String lastName = billingAccount.getName().getLastName();
            if (lastName == null) {
                lastName = "";
            }
            String firstName = billingAccount.getName().getFirstName();
            if (firstName == null) {
                firstName = "";
            }
            String address1 = customerAccount.getAddress().getAddress1();
            if (address1 == null) {
                address1 = "";
            }
            String address2 = customerAccount.getAddress().getAddress2();
            if (address2 == null) {
                address2 = "";
            }
            String address3 = customerAccount.getAddress().getAddress3();
            if (address3 == null) {
                address3 = "";
            }
            String zipCode = customerAccount.getAddress().getZipCode();
            if (zipCode == null) {
                zipCode = "";
            }
            String city = customerAccount.getAddress().getCity();
            if (city == null) {
                city = "";
            }
            String country = customerAccount.getAddress().getCountry();
            if (country == null) {
                country = "FRANCE";
            }
            content = content.replace("#{billingAccount.id}", id);
            content = content.replace("#{billingAccount.name.name}", lastName);
            content = content.replace("#{billingAccount.name.firstname}", firstName);
            content = content.replace("#{billingAccount.address.address1}", address1);
            content = content.replace("#{billingAccount.address.address2}", address2);
            content = content.replace("#{billingAccount.address.address3}", address3);
            content = content.replace("#{billingAccount.address.postalCode}", zipCode);
            content = content.replace("#{billingAccount.address.city}", city);
            content = content.replace("#{billingAccount.address.country}", country);
            content = content.replace("#{billingAccount.address.country.code}", "FR");
            writer.write(content);
            writer.write(newline);

        } finally {
            writer.flush();
            writer.close();
        }
    }

    private String getZipName(String invoiceNumber, String invoiceDate) {
        return new StringBuilder(invoiceDate).append("_").append(invoiceNumber).append(".zcou").toString();
    }

    public User getUser(Long userId) {
        EntityManager em = MeveoPersistence.getEntityManager();
        User userBayad = null;
        userBayad = (User) em.createQuery("from " + User.class.getSimpleName() + " where id =:id").setParameter("id", userId).getSingleResult();
        return userBayad;
    }

}
