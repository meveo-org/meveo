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
package org.meveo.oudaya;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.config.guice.OudayaModule;
import org.meveo.config.task.MeveoTask;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.shutdown.ShutdownTask;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author R.AITYAAZZA
 */
public class Oudaya {

    private static final Logger logger = Logger.getLogger(Oudaya.class);

    @Inject
    private OudayaConfig oudayaConfig;

    @SuppressWarnings("unchecked")
    @Inject
    private MeveoTask task;

    public static void main(String[] args) {
        System.out.println("Starting invoicing tasks1...");
        MeveoPersistence.init(OudayaConfig.getPersistenceUnitName(), OudayaConfig.getPersistenceProperties());
        Injector injector = Guice.createInjector(new OudayaModule());
        injector.getInstance(Oudaya.class).startApplication();
    }

    private void startApplication() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(oudayaConfig.getThreadCount());

        System.out.println("Starting invoicing tasks...");
        executor.scheduleWithFixedDelay(task, 0, oudayaConfig.getScanningInterval(), TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownTask(executor)));
    }

}
