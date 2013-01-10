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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.meveo.config.MeveoConfig;
import org.meveo.config.task.MeveoTask;
import org.meveo.shutdown.ShutdownTask;

import com.google.inject.Inject;

/**
 * Task launcher that just starts {@link MeveoTask} with executor. If additionally to
 * that application needs to start batch jobs - it should use
 * {@link TaskAndBatchJobsLauncher}.
 * 
 * @author Ignas Lelys
 * @created Jan 21, 2011
 * 
 */
public class SimpleTaskLauncher implements TaskLauncher {

    private static final Logger logger = Logger.getLogger(SimpleTaskLauncher.class);

    @Inject
    private MeveoConfig config;

    // TODO solve generics thing
    @SuppressWarnings("unchecked")
    @Inject
    private MeveoTask task;

    /**
     * @see org.meveo.core.launcher.TaskLauncher#launchTasks()
     */
    public void launchTasks() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(config.getThreadCount());

        logger.info("Launching tasks...");
        executor.scheduleWithFixedDelay(task, 0, config.getScanningInterval(), TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownTask(executor)));
    }

}
