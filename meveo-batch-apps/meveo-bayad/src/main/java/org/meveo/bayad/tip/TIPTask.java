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
package org.meveo.bayad.tip;

import java.io.File;
import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.commons.utils.CsvBuilder;
import org.meveo.commons.utils.DateUtils;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ImportFileFiltre;
import org.meveo.model.admin.BayadInvoicingInputHistory;
import org.meveo.model.admin.TIPImportHisto;
import org.meveo.model.crm.Provider;
import org.meveo.persistence.MeveoPersistence;

/**
 * Meveo task for TIP files
 * 
 * @author anasseh
 * @created 04.12.2010
 */
public class TIPTask implements Runnable {

	private static final Logger logger = Logger.getLogger(TIPTask.class);

	@Override
	public void run() {
		logger.info("Starting TIPTask tasks...");
		File dir = new File(BayadConfig.getTIPSourceFilesDirectory());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		logger.info("Import tip  SourceFilesDirectory:" + BayadConfig.getTIPSourceFilesDirectory());
		TIPImportHisto tipImportHisto = new TIPImportHisto();
		tipImportHisto.setExecutionDate(new Date());

		ImportFileFiltre filtre = new ImportFileFiltre("*", BayadConfig.getTIPFileExtensions());
		File[] listFile = dir.listFiles(filtre);
		int nbTIPFiles = (listFile == null ? 0 : listFile.length);
		int nbTIPImported = 0, nbTIPError = 0;

		logger.info("Import TIP job " + nbTIPFiles + " file to import");

		Provider provider = null;
		CsvBuilder errorOut = new CsvBuilder(":", false);
		for (int i = 0; i < listFile.length; i++) {
			File currentFile = null;
			try {
				ImportTIP importTIP = new ImportTIP();
				logger.info("Import TIP  job " + listFile[i].getName() + " in progres");
				currentFile = FileUtils.addExtension(listFile[i], BayadConfig.getInvoicesFileProcessingExtension());
				provider = importTIP.execute(currentFile);

				FileUtils.moveFile(BayadConfig.getTIPAcceptedFilesDirectory(), currentFile, listFile[i].getName());
				logger.info("Import TIP job " + listFile[i].getName() + " done");
				nbTIPImported++;
			} catch (Exception iie) {
				iie.printStackTrace();
				logger.info("Import TIP job " + listFile[i].getName() + " failed");
				FileUtils.moveFile(BayadConfig.getTIPRejectedFilesDirectory(), currentFile, listFile[i].getName());
				nbTIPError++;
				errorOut.appendValue(listFile[i].getName()).appendValue(iie.getMessage()).startNewLine();

			}
		}
		if (!errorOut.isEmpty()) {
			errorOut.toFile(BayadConfig.getTIPErrorDirectory() + File.separator + BayadConfig.getTIPErrorFilePrefix()
					+ DateUtils.formatDateWithPattern(new Date(), BayadConfig.getTIPErrorFileExtension()));
		}
		tipImportHisto.setLinesRead(nbTIPFiles);
		tipImportHisto.setLinesInserted(nbTIPImported);
		tipImportHisto.setLinesRejected(nbTIPError);
		EntityManager em = MeveoPersistence.getEntityManager();
		em.getTransaction().begin();
		em.persist(tipImportHisto);
		em.persist(createNewInputHistory(nbTIPFiles, nbTIPImported, nbTIPError, 0, tipImportHisto.getExecutionDate(), provider));
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
