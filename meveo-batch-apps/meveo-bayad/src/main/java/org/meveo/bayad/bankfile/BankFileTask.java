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
package org.meveo.bayad.bankfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.commons.utils.CsvBuilder;
import org.meveo.commons.utils.DateUtils;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ImportFileFiltre;
import org.meveo.model.admin.BankFileImportHisto;
import org.meveo.model.crm.Provider;
import org.meveo.persistence.MeveoPersistence;

/**
 * Meveo task for Bank files
 * 
 * @author anasseh
 * @created 04.12.2010
 */
public class BankFileTask implements Callable<BankFileTaskResponse> {

	private static final Logger logger = Logger.getLogger(BankFileTask.class);

	@Override
	public BankFileTaskResponse call() throws Exception {
		logger.info("Starting BankFileTask tasks...");
		File dir = new File(BayadConfig.getBankFileSourceFilesDirectory());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		logger.info("Import bankFile  SourceFilesDirectory:" + BayadConfig.getBankFileSourceFilesDirectory());
		BankFileImportHisto bankFileImportHisto = new BankFileImportHisto();
		bankFileImportHisto.setExecutionDate(new Date());

		ImportFileFiltre filtre = new ImportFileFiltre("*", BayadConfig.getBankFileFileExtensions());
		File[] listFile = dir.listFiles(filtre);
		int nbBankFileFiles = (listFile == null ? 0 : listFile.length);
		int nbBankFileImported = 0, nbBankFileError = 0;

		logger.info("Import BankFile job " + nbBankFileFiles + " file to import");

		Provider provider = null;
		CsvBuilder errorOut = new CsvBuilder(":", false);
		List<String> fileNames = new ArrayList<String>();
		for (int i = 0; i < listFile.length; i++) {
			File currentFile = null;
			try {
				ImportBankFile importBankFile = new ImportBankFile();
				logger.info("Import BankFile  job " + listFile[i].getName() + " in progres");
				fileNames.add(listFile[i].getName());
				currentFile = FileUtils.addExtension(listFile[i], BayadConfig.getInvoicesFileProcessingExtension());
				provider = importBankFile.execute(currentFile);

				FileUtils.moveFile(BayadConfig.getBankFileAcceptedFilesDirectory(), currentFile, listFile[i].getName());
				logger.info("Import BankFile job " + listFile[i].getName() + " done");
				nbBankFileImported++;
			} catch (Exception iie) {
				iie.printStackTrace();
				logger.info("Import BankFile job " + listFile[i].getName() + " failed");
				FileUtils.moveFile(BayadConfig.getBankFileRejectedFilesDirectory(), currentFile, listFile[i].getName());
				nbBankFileError++;
				errorOut.appendValue(listFile[i].getName()).appendValue(iie.getMessage()).startNewLine();

			}
		}
		if (!errorOut.isEmpty()) {
			errorOut.toFile(BayadConfig.getBankFileErrorDirectory() + File.separator + BayadConfig.getBankFileErrorFilePrefix()
					+ DateUtils.formatDateWithPattern(new Date(), BayadConfig.getBankFileErrorFileExtension()));
		}
		bankFileImportHisto.setLinesRead(nbBankFileFiles);
		bankFileImportHisto.setLinesInserted(nbBankFileImported);
		bankFileImportHisto.setLinesRejected(nbBankFileError);
		EntityManager em = MeveoPersistence.getEntityManager();
		em.getTransaction().begin();
		em.persist(bankFileImportHisto);
		em.getTransaction().commit();
		BankFileTaskResponse bankFileTaskResponse = new BankFileTaskResponse();
		bankFileTaskResponse.setFileNames(fileNames);
		bankFileTaskResponse.setNbImported(nbBankFileImported);
		bankFileTaskResponse.setNbRejected(nbBankFileError);
		return bankFileTaskResponse;

	}
}
