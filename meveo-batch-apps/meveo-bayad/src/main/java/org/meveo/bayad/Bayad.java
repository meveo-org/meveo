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
package org.meveo.bayad;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.meveo.bayad.ddrequest.DDRequestTask;
import org.meveo.bayad.dunning.DunningTask;
import org.meveo.bayad.invoices.InvoicesTask;
import org.meveo.bayad.tip.TIPTask;
import org.meveo.bayad.bankfile.BankFileTask;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.shutdown.ShutdownTask;



/**
 * Main Bayad class.
 * 
 * Note. Provide a system property -Dbayad.properties=%URL%/bayad.properties
 * when running.
 * 
 * @author anasseh
 * @created 02.12.2010
 */
public class Bayad {

	private static final Logger logger = Logger.getLogger(Bayad.class);

	public static void main(String[] args) {
		logger.info("Starting Bayad ...");
		try{
			MeveoPersistence.init(BayadConfig.getPersistenceUnitName(), BayadConfig.getPersistenceProperties());
	
			DunningTask dunningTask = new DunningTask();
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(BayadConfig.getThreadCount());
			executor.scheduleWithFixedDelay(dunningTask, 10000, BayadConfig.getDunningScanningInterval(), TimeUnit.MILLISECONDS);
	
			ScheduledExecutorService executor2 = Executors.newScheduledThreadPool(BayadConfig.getThreadCount());
			InvoicesTask invoicesTask = new InvoicesTask();
			executor2.scheduleWithFixedDelay(invoicesTask, 1000, BayadConfig.getInvoicesScanningInterval(), TimeUnit.MILLISECONDS);
	
			ScheduledExecutorService executor3 = Executors.newScheduledThreadPool(BayadConfig.getThreadCount());
			DDRequestTask ddrequestTask = new DDRequestTask();
			executor3.scheduleWithFixedDelay(ddrequestTask, 15000, BayadConfig.getDDRequestScanningInterval(), TimeUnit.MILLISECONDS);
	
			ScheduledExecutorService executor4 = Executors.newScheduledThreadPool(BayadConfig.getThreadCount());
			TIPTask tipTask = new TIPTask();
			executor4.scheduleWithFixedDelay(tipTask, 20000, BayadConfig.getTIPScanningInterval(), TimeUnit.MILLISECONDS);
	
			ScheduledExecutorService executor5 = Executors.newScheduledThreadPool(BayadConfig.getThreadCount());
			BankFileTask bankFileTask = new BankFileTask();
			logger.info("Starting BankFileTask ....");
			executor5.scheduleWithFixedDelay(new RunCallable(bankFileTask), 20000, BayadConfig.getBankFileScanningInterval(), TimeUnit.MILLISECONDS);
			
			Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownTask(executor4)));
		}catch (Exception e) {
           e.printStackTrace();
		}
	}

}
