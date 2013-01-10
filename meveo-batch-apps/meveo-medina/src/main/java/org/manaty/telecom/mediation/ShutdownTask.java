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

import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Shutdown hook. Shuts down all threads and saves all cached CDRHashes to
 * database. Invoked on application shutdown.
 * 
 * @author Ignas Lelys
 * @created Mar 27, 2009
 * 
 */
public class ShutdownTask implements Runnable {

    private static final Logger logger = Logger.getLogger(ShutdownTask.class);

    private ExecutorService executor;

    public ShutdownTask(ExecutorService executor) {
        super();
        this.executor = executor;
    }

    public void run() {
        try {
            logger.info("Shutting down all threads.");
            executor.shutdown();
            logger.info("Threads shut down successfully.");
            while (!executor.isTerminated()) {
                Thread.sleep(100);
            }
            logger.info("Shutting down all batch jobs.");
            StdSchedulerFactory.getDefaultScheduler().shutdown();
            logger.info("Batch jobs shut down successfully.");
            
            logger.info("Close EntityManagerFactory. Releasing db resources.");
            MedinaPersistence.closeEntityManagerFactory();
            logger.info("EntityManagerFactory Closed.");
        } catch (Exception e) {
            logger.warn("Unnexpected exception on Medina shutdown.", e);
        }
    }

}
