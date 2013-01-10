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
package org.meveo.core.launcher;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.meveo.commons.exceptions.ConfigurationException;
import org.meveo.config.MeveoConfig;
import org.meveo.config.task.MeveoTask;
import org.meveo.shutdown.ShutdownTask;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.google.inject.Inject;

/**
 * Launcher that launches provided MeveoTask (injected by guice), and starts
 * list of batch jobs (also provided by guice injection).
 * 
 * @author Ignas Lelys
 * @created Jan 21, 2011
 * 
 */
public class TaskAndBatchJobsLauncher implements TaskLauncher {

    private static final Logger logger = Logger.getLogger(TaskAndBatchJobsLauncher.class);

    @Inject
    private MeveoConfig config;

    @SuppressWarnings("unchecked")
    @Inject
    private MeveoTask task;

    @Inject
    private Set<Job> batchJobs;
    
    private Scheduler scheduler;

    /**
     * @see org.meveo.core.launcher.TaskLauncher#launchTasks()
     */
    @Override
    public void launchTasks() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(config.getThreadCount());

        logger.info("Launching tasks...");
        executor.scheduleWithFixedDelay(task, 0, config.getScanningInterval(), TimeUnit.MILLISECONDS);
        scheduleBatchJobs();
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownTask(scheduler, executor)));
    }

    /**
     * Schedule batch jobs.
     */
    private void scheduleBatchJobs() {
        try {
            logger.info("Schedule batch jobs");

            // Grab the Scheduler instance from the Factory
            scheduler = StdSchedulerFactory.getDefaultScheduler();

            // and start it off
            scheduler.start();

            int jobCount = 0;
            for (Job job : batchJobs) {
                jobCount++;
                String jobName = new StringBuilder("job").append("_").append(jobCount).toString();
                String triggerName = new StringBuilder("trigger").append("_").append(jobCount).toString();
                JobDetail jobDetail = new JobDetail(jobName, null, job.getClass());
                String jobClassName = job.getClass().getSimpleName();
                String batchJobCron = config.getBatchJobCron(jobClassName);
                Trigger jobTrigger;
                try {
                    jobTrigger = new CronTrigger(triggerName, "meveoJobs", batchJobCron);
                } catch (ParseException e) {
                    logger.error("Batch jobs not started.", e);
                    throw new ConfigurationException("Application could not start because batch jobs could not be started.");
                }
                jobTrigger.setStartTime(new Date());
                scheduler.scheduleJob(jobDetail, jobTrigger);
                logger.info(String.format(
                        "Batch job with name '%s' (class - %s) scheduling was successfull! Using cron - '%s'", jobName,
                        jobClassName, batchJobCron));
            }
            logger.info(String.format("All %s  batch job scheduled.", jobCount));

        } catch (SchedulerException se) {
            logger.error("Batch jobs scheduling failed!", se);
        }
    }

}
