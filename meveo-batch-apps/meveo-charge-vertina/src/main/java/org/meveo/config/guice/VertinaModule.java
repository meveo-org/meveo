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
import org.meveo.rating.inputHandler.RatingInputHandler;
import org.meveo.rating.inputloader.RatingDatabaseInputLoader;
import org.meveo.rating.process.RatingProcessor;
import org.meveo.rating.task.RaterTask;
import org.meveo.rating.ticket.RatingTicket;
import org.meveo.rating.validator.RatingValidator;
import org.meveo.vertina.Vertina;
import org.meveo.vertina.VertinaConfig;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * Rating Vertina modules configuration.
 * 
 * @author Ignas Lelys
 * @created Jun 16, 2010
 * 
 */
public class VertinaModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Vertina.class);
        bind(MeveoTask.class).to(RaterTask.class);
        bind(VertinaConfig.class).asEagerSingleton();
        bind(MeveoConfig.class).to(VertinaConfig.class);
        bind(InputLoader.class).to(RatingDatabaseInputLoader.class);
        bind(new TypeLiteral<InputHandler<RatingTicket>>() {}).to(RatingInputHandler.class);
        bind(new TypeLiteral<Processor<RatingTicket>>() {}).to(RatingProcessor.class);
        bind(new TypeLiteral<Validator<RatingTicket>>() {}).to(RatingValidator.class);
        bind(OutputProducer.class).to(NoOutputProducer.class);
        bind(OutputHandler.class).to(NoOutputHandler.class);
        bind(new TypeLiteral<AbstractProcessStep<RatingTicket>>() {}).toProvider(StepsProvider.class);
    }

}
