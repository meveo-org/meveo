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
package org.meveo.grieg.invoiceConverter.config;

import org.grieg.GriegConfig;
import org.grieg.ticket.GriegTicket;
import org.meveo.config.MeveoConfig;
import org.meveo.config.MeveoFileConfig;
import org.meveo.config.task.MeveoTask;
import org.meveo.core.inputhandler.InputHandler;
import org.meveo.core.inputloader.InputLoader;
import org.meveo.core.inputloader.SimpleFileInputLoader;
import org.meveo.core.launcher.SimpleTaskLauncher;
import org.meveo.core.launcher.TaskLauncher;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.parser.Parser;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.validator.Validator;
import org.meveo.grieg.invoiceConverter.input.InvoiceValidator;
import org.meveo.grieg.invoiceConverter.input.InvoiceXMLFileHandler;
import org.meveo.grieg.invoiceConverter.input.XMLParser;
import org.meveo.grieg.invoiceConverter.output.PDFFilesOutputProducer;
import org.meveo.grieg.invoiceConverter.output.PDFsToDBOutputHandler;
import org.meveo.grieg.invoiceConverter.process.InvoiceProcessor;
import org.meveo.grieg.invoiceConverter.task.InvoiceConverterTask;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * @author Ignas Lelys
 * @created Dec 17, 2010
 * 
 */
public class GriegModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TaskLauncher.class).to(SimpleTaskLauncher.class);
        bind(MeveoTask.class).to(InvoiceConverterTask.class);
        bind(MeveoConfig.class).to(GriegConfig.class);
        bind(MeveoFileConfig.class).to(GriegConfig.class);
        bind(GriegConfig.class).asEagerSingleton();
        bind(InputLoader.class).to(SimpleFileInputLoader.class);
        bind(new TypeLiteral<InputHandler<GriegTicket>>() {}).to(InvoiceXMLFileHandler.class);
        bind(new TypeLiteral<Processor<GriegTicket>>() {}).to(InvoiceProcessor.class);
        bind(new TypeLiteral<Parser<GriegTicket>>() {}).to(XMLParser.class);
        bind(new TypeLiteral<Validator<GriegTicket>>() {}).to(InvoiceValidator.class);
        bind(OutputProducer.class).to(PDFFilesOutputProducer.class);
        bind(OutputHandler.class).to(PDFsToDBOutputHandler.class);
        bind(new TypeLiteral<AbstractProcessStep<GriegTicket>>() {}).toProvider(StepsProvider.class);
    }

}
