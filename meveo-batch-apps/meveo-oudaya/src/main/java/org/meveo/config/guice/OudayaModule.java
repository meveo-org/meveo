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
package org.meveo.config.guice;

import org.meveo.config.MeveoConfig;
import org.meveo.config.task.MeveoTask;
import org.meveo.core.inputhandler.InputHandler;
import org.meveo.core.inputloader.InputLoader;
import org.meveo.core.outputhandler.NoOutputHandler;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.core.outputproducer.NoOutputProducer;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.validator.Validator;
import org.meveo.oudaya.InvoicerTask;
import org.meveo.oudaya.InvoicingTicket;
import org.meveo.oudaya.OudayaConfig;
import org.meveo.oudaya.inputhandler.OudayaInvoicingInputHandler;
import org.meveo.oudaya.inputloader.db.OudayaDatabaseInputLoader;
import org.meveo.oudaya.processor.InvoicingProcessor;
import org.meveo.oudaya.validator.InvoicingValidator;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * Oudaya Vertina modules configuration.
 * 
 * @author Ignas Lelys
 * @created Jun 16, 2010
 * 
 */
public class OudayaModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MeveoTask.class).to(InvoicerTask.class);
        bind(OudayaConfig.class).asEagerSingleton();
        bind(MeveoConfig.class).to(OudayaConfig.class);
        bind(InputLoader.class).to(OudayaDatabaseInputLoader.class);
        bind(new TypeLiteral<InputHandler<InvoicingTicket>>() {
        }).to(OudayaInvoicingInputHandler.class);
        bind(new TypeLiteral<Processor<InvoicingTicket>>() {
        }).to(InvoicingProcessor.class);
        bind(new TypeLiteral<Validator<InvoicingTicket>>() {
        }).to(InvoicingValidator.class);
        bind(OutputProducer.class).to(NoOutputProducer.class);
        bind(OutputHandler.class).to(NoOutputHandler.class);
        bind(new TypeLiteral<AbstractProcessStep<InvoicingTicket>>() {
        }).toProvider(StepsProvider.class);
    }

}
