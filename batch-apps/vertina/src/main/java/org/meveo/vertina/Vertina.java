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
package org.meveo.vertina;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.config.task.MeveoTask;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.shutdown.ShutdownTask;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Main Vertina class.
 * 
 * Note. Provide a system property -Dvertina.properties=%URL%/vertina.properties
 * when running.
 * 
 * @author Ignas Lelys
 * @created 2009.06.11
 */
public class Vertina {

    private static final Logger logger = Logger.getLogger(Vertina.class);

//    /** Recurring job executor. */
//    private static final ScheduledExecutorService recurringJobExecutor = Executors.newScheduledThreadPool(1);
    
    @Inject
    private VertinaConfig vertinaConfig;

    @SuppressWarnings("unchecked")
    @Inject
    private MeveoTask task;

    public static void main(String[] args) {
        MeveoPersistence.init(VertinaConfig.getPersistenceUnitName(), VertinaConfig.getPersistenceProperties());
        AbstractModule guiceConfiguration = (AbstractModule) ReflectionUtils.createObject(VertinaConfig
                .getVertinaConfigurationModule());
        Injector injector = Guice.createInjector(guiceConfiguration);
        injector.getInstance(Vertina.class).startApplication();
    }

    private void startApplication() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(vertinaConfig.getThreadCount());

        logger.info("Starting rater tasks...");
        executor.scheduleAtFixedRate(task, 0, vertinaConfig.getScanningInterval(), TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownTask(executor)));
    }
}
