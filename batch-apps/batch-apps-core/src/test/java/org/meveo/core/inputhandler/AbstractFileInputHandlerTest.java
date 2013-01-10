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
package org.meveo.core.inputhandler;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.meveo.config.task.TestConfig;
import org.meveo.core.inputloader.Input;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.parser.AbstractTextParser;
import org.meveo.core.parser.Parser;
import org.meveo.core.parser.ParserException;
import org.meveo.core.process.AbstractProcessor;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.Constants;
import org.meveo.persistence.MeveoPersistence;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link AbstractFileInputHandler} tests.
 *  
 * @author Ignas Lelys
 * @created Aug 4, 2010
 *
 */
public class AbstractFileInputHandlerTest {
    
    private static final String TEST_FILE_NAME = "test.csv";
    private static final String TEST_DIR = "target/test-classes/files/";
    
    @SuppressWarnings("rawtypes")
    @Test(groups = { "db" })
    public void testExecuteInputHandling() throws Exception {
        
        MeveoPersistence.getEntityManager().getTransaction().begin();
        TestParser testParser = new TestParser();
        AbstractFileInputHandler fileInputHandler = new FileInputHandlerImpl(new TestProcessor(null), null, testParser);
        Input input = new Input(TEST_FILE_NAME, new File(TEST_DIR + TEST_FILE_NAME));
        TaskExecution taskExecution = fileInputHandler.handleInput(input);
        
        // check if all tickets from parser got there
        Assert.assertEquals(taskExecution.getParsedTicketsCount(), (Integer)10);
        Assert.assertEquals(taskExecution.getProcessedTicketsCount(), (Integer)9);
        Assert.assertEquals(taskExecution.getRejectedTicketsCount(), (Integer)1);
        
        // check if parser got closed after handling
        Assert.assertTrue(testParser.isClosed());
        
        //
        Assert.assertEquals(((FileInputHandlerImpl)fileInputHandler).getRejectCalled(), (Integer)1);
        
        MeveoPersistence.getEntityManager().getTransaction().commit();
    }
    
    @SuppressWarnings({"unchecked","rawtypes"})
    private class FileInputHandlerImpl extends AbstractFileInputHandler {
        int rejectCalled = 0;
        public FileInputHandlerImpl(Processor processor, OutputProducer outputProducer, Parser parser) {
            super(processor, outputProducer, parser);
            super.config = new TestConfig();
        }
        @Override
        protected void rejectTicket(Input input, Object ticket, String status) {
            rejectCalled++;
        }
        public Integer getRejectCalled() {
            return rejectCalled;
        }
    }
    
    @SuppressWarnings({"unchecked","rawtypes"})
    private class TestProcessor extends AbstractProcessor {
        public TestProcessor(AbstractProcessStep processStepsChain) {
            super(processStepsChain);
        }
        
        @Override
        public Map process(Object ticket, TaskExecution taskExecution) {
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            if ("ticket1".equals(ticket.toString())) {
                parameters.put(Constants.ACCEPTED, Boolean.FALSE);
            } else {
                parameters.put(Constants.ACCEPTED, Boolean.TRUE);
            }
            return parameters;
        }
        @Override
        protected void doCommit(TaskExecution taskExecution) throws SQLException {
        }
        @Override
        protected Map getNamedQueries() {
            return new HashMap();
        }
    }
    
    @SuppressWarnings("rawtypes")
    private class TestParser extends AbstractTextParser {
        int counter = 0;
        boolean closed = false;
        @Override
        public void setParsingFile(String fileName) throws ParserException {
        }
        // return 10 strings as tickets
        @Override
        public Object next() throws ParserException {
            counter++;
            if (counter <= 10) {
                return "ticket" + counter;
            } else {
                return null;
            }
        }
        @Override
        public void close() {
            closed = true;
        }
        public boolean isClosed() {
            return closed;
        }
    }
    
}
