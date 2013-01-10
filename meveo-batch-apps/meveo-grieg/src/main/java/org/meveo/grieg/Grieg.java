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
package org.meveo.grieg;

import java.text.ParseException;

import org.grieg.GriegConfig;
import org.grieg.communication.Communicator;
import org.meveo.core.launcher.TaskLauncher;
import org.meveo.grieg.dunning.config.GriegDunningModule;
import org.meveo.grieg.invoiceConverter.config.GriegModule;
import org.meveo.grieg.invoiceConverter.config.GriegValidatedInvoicesModule;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Main Grieg class.
 * 
 * Note. Provide a system property -Dgrieg.properties=%URL%/grieg.properties
 * when running.
 * 
 * @author Ignas Lelys
 * @created Dec 20, 2010
 */
public class Grieg {

    public static void main(String[] args) throws ParseException {
        try {
            MeveoPersistence.init(GriegConfig.getPersistenceUnitName(), GriegConfig.getPersistenceProperties());

        } catch (Exception e) {
            e.printStackTrace();
        }
        Injector injector = Guice.createInjector(new GriegModule());
        injector.getInstance(TaskLauncher.class).launchTasks();
        Injector validatedInvoicesInjector = Guice.createInjector(new GriegValidatedInvoicesModule());
        validatedInvoicesInjector.getInstance(TaskLauncher.class).launchTasks();
        Injector validatedDunningInjector = Guice.createInjector(new GriegDunningModule());
        validatedDunningInjector.getInstance(TaskLauncher.class).launchTasks();
        Communicator.launch();
    }

}
