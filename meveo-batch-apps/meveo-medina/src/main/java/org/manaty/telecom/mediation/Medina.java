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
package org.manaty.telecom.mediation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.manaty.telecom.mediation.cache.NumberingPlanCache;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache.CacheTransaction;
import org.manaty.utils.DBConfigBean;
import org.manaty.utils.MagicNumberConverter;
//import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

/**
 * Main Medina class.
 * 
 * Note. Provide a system property -Dmedina.properties=%URL%/medina.properties
 * when running.
 * 
 * @author Donatas Remeika
 * @created Mar 2, 2009
 */
public class Medina {

    private static final String RECOVERY_EDR_EXTENSION = ".csv";

    private static final Logger logger = Logger.getLogger(Medina.class);

    /**
     * Mediation task executor.
     */
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(MedinaConfig
            .getThreadCount());

    public static void main(String[] args) throws ParseException {
    	
    	// Create entity manager for main thread.
    	MedinaPersistence.getEntityManager();
    	try {
	    	// initialize caches
	    	TransactionalMagicNumberCache.getInstance();
	    	NumberingPlanCache.getInstance();
	    	DBConfigBean.getInstance();
	    	
	        // add shutdown hook for tasks to be done on shutdown
	        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownTask(executor)));
	
	        // check for edr recovery files to recover magic numbers from them
	        //checkForEDRRecovery();
    	} finally {
    		MedinaPersistence.closeEntityManager();
    	}
    	
        // start batch jobs
        //scheduleBatchJobs();

        // start medina tasks
        startMedina();
    }
    
    private static void startMedina() {
        logger.info("Starting mediation tasks...");
        MedinaPersistence.init();
        MediatorTask task = new MediatorTask();
        //executor.scheduleAtFixedRate(new ZoneImportTask(), 0, MedinaConfig.getImportScanningInterval(),
        //        TimeUnit.MILLISECONDS);
        //executor.scheduleAtFixedRate(new NumberingImportTask(), 0, MedinaConfig.getImportScanningInterval(),
        //        TimeUnit.MILLISECONDS);
        while (!executor.isShutdown()) {
            executor.execute(task);
            try {
                Thread.sleep(MedinaConfig.getScanningInterval());
            } catch (InterruptedException e) {
            	if (logger.isDebugEnabled()) {
            		logger.debug("Mediation tasks : Thread.sleep KO because of InterruptedException");
                }
                logger.warn("Thread sleeping interrupted", e);
            }
        }
    }
    
  /*  private static void checkForEDRRecovery() {
        File[] edrRecoveryFiles = getMagicNumbersRecoveryFiles();
        if (edrRecoveryFiles != null && edrRecoveryFiles.length > 0) {
            logger.info("Recovery edr files found in recovery directory. Loading magic numbers from them...");
            loadMagicNumbersFromFiles(edrRecoveryFiles);
        }
    }
    */
    private static void scheduleBatchJobs() throws ParseException {
        try {
            logger.info("Schedule medina batch jobs");
            
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            scheduler.start();
            
//            JobDetail aggregationJobDetail = new JobDetail("aggregationBatchJobTask", null, AggregationBatchJobTask.class);
//            Trigger aggregationTrigger = new CronTrigger("aggregationTrigger", "myGroup", MedinaConfig.getAggregationBatchJobCron());
//            aggregationTrigger.setStartTime(new Date());
//            scheduler.scheduleJob(aggregationJobDetail, aggregationTrigger);
            
//            JobDetail rejectedTicketsJobDetail = new JobDetailImpl("rejectedTicketsBatchJobTask", null, RejectedTicketsBatchJobTask.class);
//            Trigger rejectedTicketsTrigger = new CronTriggerImpl("rejectedTicketsTrigger", "myGroup", MedinaConfig.getRejectedTicketsBatchJobCron());
            //rejectedTicketsTrigger.setStartTime(new Date());
//            scheduler.scheduleJob(rejectedTicketsJobDetail, rejectedTicketsTrigger);
            
//            JobDetail refreshDBPropertiesJobDetail = new JobDetail("refreshPropertiesJobTask", null, RefreshPropertiesJobTask.class);
//            Trigger refreshPropertiesTrigger = new CronTrigger("refreshPropertiesTrigger", "myGroup", MedinaConfig.getRefreshPropertiesBatchJobCron());
//            refreshPropertiesTrigger.setStartTime(new Date());
//            scheduler.scheduleJob(refreshDBPropertiesJobDetail, refreshPropertiesTrigger);

            logger.info("Medina batch jobs scheduling successfull!");

        } catch (SchedulerException se) {
            logger.error("Medina batch jobs schedule failed!", se);
        }
    }

    /**
     * Gets magic numbers recovery files if they exists. If no configured
     * recovery directory exists it terminates application.
     * 
     * @return Magic number recovery files.
     */
  /*  private static File[] getMagicNumbersRecoveryFiles() {
        if (MedinaConfig.getMagicNumbersRecoveryDir() == null) {
            logger.info("Unknown magic numers recovery directory. "
                    + "Please use property 'medina.magicNumbersRecoveryDir' in medina.properties to specify it.");
            System.exit(0);
        }
        File magicNumbersRecoveryDir = new File(MedinaConfig.getMagicNumbersRecoveryDir());
        if (!magicNumbersRecoveryDir.exists() || !magicNumbersRecoveryDir.isDirectory()) {
            logger.info("Magic numbers recovery directory does not exist. "
                    + "Please create such a directory before running Medina.");
            System.exit(0);
        }
        File[] files = magicNumbersRecoveryDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                if (name.endsWith(RECOVERY_EDR_EXTENSION)) {
                    return true;
                }
                return false;
            }

        });
        return files;
    }
*/
    /**
     * Loads magic numbers to CacheManager and database.
     * 
     * @param files
     *            EDR files which contains magic numbers need to recover.
     * @throws IOException
     */
/*    private static void loadMagicNumbersFromFiles(File[] files) {
        CacheTransaction cacheTransaction = TransactionalMagicNumberCache.getInstance().getTransaction();
        try {
        	MedinaPersistence.getEntityManager().getTransaction().begin();
            for (int i = 0; i < files.length; i++) {
                BufferedReader in = new BufferedReader(new FileReader(files[i]));
                String line = null;
                while ((line = in.readLine()) != null) {
                    String[] values = line.split(";");
                    if (values.length < 3) {
                    	throw new IllegalStateException("Not enought columns in recovery file. There should be at least 3 columns.");
                    }
                    // third column is magic number
                    byte[] magicNumber = MagicNumberConverter.convertToArray(values[2]);
                    cacheTransaction.addToCache(magicNumber);
                }
            }
            TransactionalMagicNumberCache.getInstance().persistCacheTransaction(cacheTransaction);
            MedinaPersistence.getEntityManager().getTransaction().commit();
            cacheTransaction.commit();
            logger.info("Magic numbers from recovery edr files were loaded successfully!");
        } catch (Throwable e) {
        	logger.error("Failed to load magic numbers from edr recovery files", e);
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            cacheTransaction.rollback();
        }
    }
*/
}
