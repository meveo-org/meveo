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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.bayad.util.BayadUtils;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.BankOperation;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.service.payments.remote.CustomerAccountServiceRemote;

public class ImportBankFile {
	private static final Logger log = Logger.getLogger(ImportBankFile.class);

	public Provider execute(File file) throws Exception {
		EntityManager em = MeveoPersistence.getEntityManager();
		ExecutorService e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		log.info(file.getName() + " in progress ...");
		em.getTransaction().begin();
		try {
			ExcelBankFileParser excelBankFileParser = new ExcelBankFileParser();
			List<BankOperation> bankOps = excelBankFileParser.parseFile(file);
			int cptLines = bankOps.size();
			log.info("nb operations to process:" + cptLines);
			CountDownLatch doneSignal = new CountDownLatch(cptLines);
			CustomerAccountServiceRemote customerAccountServiceRemote = (CustomerAccountServiceRemote) EjbUtils.getRemoteInterface(
					BayadConfig.getMeveoCustomerAccountServiceJndiName(), BayadConfig.getMeveoProviderUrl());
			for (BankOperation bankOp : bankOps) {
				try {
					ProcessLineTask processLineTask = new ProcessLineTask();
					processLineTask.setEm(em);
					processLineTask.setCustomerAccountServiceRemote(customerAccountServiceRemote);
					processLineTask.setFileName(file.getName());
					processLineTask.setBankOp(bankOp);
					Future<Object> resp = e.submit(processLineTask);
					resp.get();
				} catch (Exception exception) {
					exception.printStackTrace();
					bankOp.setErrorMessage(StringUtils.truncate(exception.getMessage(), 255, true).replaceAll("java.lang.Exception:", ""));
					bankOp.setAuditable(BayadUtils.getAuditable(BayadUtils.getUserBayadSystem()));
					em.persist(bankOp);
				}
				doneSignal.countDown();
			}
			doneSignal.await();
			e.shutdown();
			em.getTransaction().commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				em.getTransaction().rollback();
			} catch (Exception et) {
				et.printStackTrace();
			}
			throw ex;
		}
		return null;

	}





}
