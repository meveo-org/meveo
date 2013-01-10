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
package org.grieg.communication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.grieg.GriegConfig;
import org.grieg.communication.ticket.MessageTicket;
import org.meveo.config.task.MeveoTask;
import org.meveo.core.launcher.TaskLauncher;
import org.meveo.shutdown.ShutdownTask;

import com.google.inject.Inject;

/**
 * @author Sebastien Michea
 * @created March 23, 2011
 *
 */
public class MessageTaskLauncher implements TaskLauncher {
    
    private static final Logger logger = Logger.getLogger(MessageTaskLauncher.class);

    @Inject
    private GriegConfig griegConfig;
    
    @Inject
    private MeveoTask<MessageTicket> task;
    
    @Override
    public void launchTasks() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(griegConfig
                .getThreadCount());

        logger.info("Launching Communication Center task...");
        executor.scheduleWithFixedDelay(task, 0, griegConfig.getMessageScanningInterval(), TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownTask(executor)));
    }

}
