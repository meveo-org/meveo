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
package org.meveo.shutdown;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.meveo.persistence.MeveoPersistence;
import org.quartz.Scheduler;

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

    /** List of excecutors to shutdown. */
    private List<ExecutorService> executors;
    
    /** Do application need to shutdown quartz batch jobs (if scheduler is not null then it probably is started and need shutdown). */
    private Scheduler scheduler;

    public ShutdownTask(ExecutorService... executors) {
        super();
        this.executors = Collections.unmodifiableList(Arrays.asList(executors));
    }
    
    public ShutdownTask(Scheduler scheduler, ExecutorService... executors) {
        super();
        this.scheduler = scheduler;
        this.executors = Collections.unmodifiableList(Arrays.asList(executors));
    }

    public void run() {
        try {
            logger.info("Shutting down all threads.");
            for (ExecutorService executor : executors) {
                executor.shutdown();
                while (!executor.isTerminated()) {
                    Thread.sleep(100);
                }
            }
            logger.info("Threads shut down successfully.");
            if (scheduler != null) {
                if (scheduler.isStarted()) {
                    logger.info(String.format("Shutting down all batch jobs. Shecduler name: %s", scheduler.getSchedulerName()));
                    scheduler.shutdown();
                    logger.info("Quartz batch jobs shut down successfully.");
                }
            }
            
            logger.info("Close EntityManagerFactory. Releasing db resources.");
            MeveoPersistence.closeAll();
            logger.info("EntityManagerFactory Closed.");
        } catch (Exception e) {
            logger.warn("Unnexpected exception on application shutdown.", e);
        }
    }

}
