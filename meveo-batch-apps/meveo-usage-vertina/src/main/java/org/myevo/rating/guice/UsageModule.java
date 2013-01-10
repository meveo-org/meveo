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
package org.myevo.rating.guice;

import org.meveo.config.MeveoConfig;
import org.meveo.config.MeveoFileConfig;
import org.meveo.config.task.MeveoTask;
import org.meveo.core.inputhandler.InputHandler;
import org.meveo.core.inputloader.InputLoader;
import org.meveo.core.inputloader.SimpleFileInputLoader;
import org.meveo.core.outputhandler.NoOutputHandler;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.core.outputproducer.NoOutputProducer;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.parser.Parser;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.validator.Validator;
import org.meveo.vertina.Vertina;
import org.myevo.rating.config.UsageConfig;
import org.myevo.rating.inputhandler.EDRInputHandler;
import org.myevo.rating.process.EDRProcessor;
import org.myevo.rating.task.EDRParser;
import org.myevo.rating.task.EDRRaterTask;
import org.myevo.rating.ticket.EDRTicket;
import org.myevo.rating.validator.EDRRatingValidator;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class UsageModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Vertina.class);
        bind(MeveoTask.class).to(EDRRaterTask.class);
        bind(UsageConfig.class).asEagerSingleton();
        bind(MeveoFileConfig.class).to(UsageConfig.class);
        bind(MeveoConfig.class).to(UsageConfig.class);
        bind(InputLoader.class).to(SimpleFileInputLoader.class);
        bind(new TypeLiteral<InputHandler<EDRTicket>>() {}).to(EDRInputHandler.class);
        bind(new TypeLiteral<Parser<EDRTicket>>() {}).to(EDRParser.class);
        bind(new TypeLiteral<Processor<EDRTicket>>() {}).to(EDRProcessor.class);
        bind(new TypeLiteral<Validator<EDRTicket>>() {}).to(EDRRatingValidator.class);
        bind(OutputProducer.class).to(NoOutputProducer.class);
        bind(OutputHandler.class).to(NoOutputHandler.class);
        bind(new TypeLiteral<AbstractProcessStep<EDRTicket>>() {}).toProvider(StepsProvider.class);
    }

}
