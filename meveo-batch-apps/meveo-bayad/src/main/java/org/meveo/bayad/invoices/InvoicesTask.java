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
package org.meveo.bayad.invoices;

import java.io.File;
import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.bayad.invoices.exception.ImportInvoiceException;
import org.meveo.bayad.invoices.exception.InvoiceExistException;
import org.meveo.commons.utils.CsvBuilder;
import org.meveo.commons.utils.DateUtils;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ImportFileFiltre;
import org.meveo.model.admin.BayadInvoicingInputHistory;
import org.meveo.model.admin.InvoiceImportHisto;
import org.meveo.model.crm.Provider;
import org.meveo.persistence.MeveoPersistence;

/**
 * Meveo task for invoices
 * 
 * @author anasseh
 * @created 04.12.2010
 */
public class InvoicesTask implements Runnable {

    private static final Logger logger = Logger.getLogger(InvoicesTask.class);

    @Override
    public void run() {
        logger.info("Starting InvoicesTask tasks...");
        File dir = new File(BayadConfig.getInvoicesSourceFilesDirectory());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        logger.info("Import invoices  SourceFilesDirectory:" + BayadConfig.getInvoicesSourceFilesDirectory());
        InvoiceImportHisto invoiceImportHisto = new InvoiceImportHisto();
        invoiceImportHisto.setExecutionDate(new Date());

        ImportFileFiltre filtre = new ImportFileFiltre("*", BayadConfig.getInvoicesFileExtensions());
        File[] listFile = dir.listFiles(filtre);
        int nbInvoices = (listFile == null ? 0 : listFile.length);
        int nbInvoiceCreated = 0, nbInvoiceError = 0, nbInvoiceIgnored = 0;

        logger.info("Import invoices job " + nbInvoices + " file to import");

        Provider provider = null;
        CsvBuilder errorOut = new CsvBuilder(":", false);
        for (int i = 0; i < listFile.length; i++) {
            File currentFile = null;
            try {
                ImportInvoice importInvoice = new ImportInvoice();
                logger.info("Import invoices  job " + listFile[i].getName() + " in progres");
                currentFile = FileUtils.addExtension(listFile[i], BayadConfig.getInvoicesFileProcessingExtension());
                provider = importInvoice.execute(currentFile);

                FileUtils.moveFile(BayadConfig.getInvoicesAcceptedFilesDirectory(), currentFile, listFile[i].getName());
                logger.info("Import invoices job " + listFile[i].getName() + " done");
                nbInvoiceCreated++;
            } catch (ImportInvoiceException iie) {
                logger.info("Import invoices job " + listFile[i].getName() + " failed");
                FileUtils.moveFile(BayadConfig.getInvoicesRejectedFilesDirectory(), currentFile, listFile[i].getName());
                nbInvoiceError++;
                errorOut.appendValue(listFile[i].getName()).appendValue(iie.getMessage()).startNewLine();

            } catch (InvoiceExistException iee) {
                logger.info("Import invoices job " + listFile[i].getName() + " ignored");
                FileUtils.moveFile(BayadConfig.getInvoicesIgnoredFilesDirectory(), currentFile, listFile[i].getName());
                nbInvoiceIgnored++;
            }
        }
        if (!errorOut.isEmpty()) {
            errorOut.toFile(BayadConfig.getInvoicesErrorDirectory() + File.separator + BayadConfig.getInvoicesErrorFilePrefix()
                    + DateUtils.formatDateWithPattern(new Date(), BayadConfig.getInvoicesErrorFileExtension()));
        }
        invoiceImportHisto.setLinesRead(nbInvoices);
        invoiceImportHisto.setLinesInserted(nbInvoiceCreated);
        invoiceImportHisto.setLinesRejected(nbInvoiceError);
        invoiceImportHisto.setNbInvoicesIgnored(nbInvoiceIgnored);
        EntityManager em = MeveoPersistence.getEntityManager();
        em.getTransaction().begin();
        em.persist(invoiceImportHisto);
        em.persist(createNewInputHistory(nbInvoices, nbInvoiceCreated, nbInvoiceError, nbInvoiceIgnored, invoiceImportHisto.getExecutionDate(), provider));
        em.getTransaction().commit();

    }

    /**
     * Creates input history object, to save it to DB.
     */
    private BayadInvoicingInputHistory createNewInputHistory(int nbTicketsParsed, int nbTicketsSucceeded, int nbTicketsRejected, int nbIgnoredTickets,
            Date startDate, Provider provider) {
        BayadInvoicingInputHistory inputHistory = new BayadInvoicingInputHistory();
        inputHistory.setName(startDate.toString());
        inputHistory.setParsedTickets(nbTicketsParsed);
        inputHistory.setRejectedTickets(nbTicketsRejected);
        inputHistory.setSucceededTickets(nbTicketsSucceeded);
        inputHistory.setIgnoredTickets(nbIgnoredTickets);
        inputHistory.setAnalysisStartDate(startDate);
        inputHistory.setAnalysisEndDate(new Date());
        inputHistory.setProvider(provider);
        return inputHistory;
    }
}
