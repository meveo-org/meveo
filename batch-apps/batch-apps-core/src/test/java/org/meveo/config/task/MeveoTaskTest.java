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
package org.meveo.config.task;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.meveo.commons.utils.FileUtils;
import org.meveo.core.inputhandler.AbstractInputHandler;
import org.meveo.core.inputhandler.InputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.AbstractInputLoader;
import org.meveo.core.inputloader.Input;
import org.meveo.core.inputloader.InputLoader;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.process.Processor;
import org.meveo.model.admin.Currency;
import org.meveo.model.admin.InputHistory;
import org.meveo.model.admin.MedinaInputHistory;
import org.meveo.persistence.MeveoPersistence;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.inject.Provider;

/**
 * MeveoTask class tests.
 * 
 * @author Ignas Lelys
 * @created Aug 4, 2010
 *
 */
public class MeveoTaskTest {
    
    private static final String TEST_DIR = "target/test-classes/files/";
    private static final String TEST_ACCEPTED_DIR = "target/test-classes/files/accepted/";
    private static final String TEST_REJECTED_DIR = "target/test-classes/files/rejected/";
    private static final String TEST_FILE_NAME = "meveoTaskTest.dummy";
    
    @SuppressWarnings("unchecked")
    @Test(groups = { "db" }, dependsOnMethods = {"testRunTaskTransactionRollback"})
    public void testRunTask() {
        File fileMovedToRejectedDir = null;
        try {
            fileMovedToRejectedDir = new File(TEST_ACCEPTED_DIR + TEST_FILE_NAME);
            
            MeveoTask task = new MeveoTaskImpl(new InputLoaderProviderImpl(), new InputHandlerProviderImpl(), new OutputHandlerProviderImpl());
            task.run();
            EntityManager em = MeveoPersistence.getEntityManager();
            // check if operations saved to database from InputHandler
            Query query = em.createQuery("from Currency where code = 'LTL'");
            List result = query.getResultList();
            Assert.assertEquals((Integer)result.size(), (Integer)1);
            // check if InputHistory saved to database
            Query queryInputHistory = em.createQuery("from MedinaInputHistory where name = 'aaa'");
            List resultInputHistory = queryInputHistory.getResultList();
            Assert.assertEquals((Integer)resultInputHistory.size(), (Integer)1);
            Assert.assertEquals(((MedinaInputHistory)resultInputHistory.get(0)).getParsedTickets(), (Integer)10);
            Assert.assertEquals(((MedinaInputHistory)resultInputHistory.get(0)).getSucceededTickets(), (Integer)8);
            Assert.assertEquals(((MedinaInputHistory)resultInputHistory.get(0)).getRejectedTickets(), (Integer)2);
        
            // test if handleInputAfterProcessing was invoked and file moved to accepted dir
            Assert.assertTrue(fileMovedToRejectedDir.exists());
        } finally {    
            // move file back for next test
            FileUtils.moveFile(TEST_DIR, fileMovedToRejectedDir, TEST_FILE_NAME);
        }
    }
    
    @Test(groups = { "db" })
    @SuppressWarnings("unchecked")
    public void testRunTaskTransactionRollback() {
        MeveoTask task = new MeveoTaskImpl(new InputLoaderProviderImpl(), new InputHandlerProviderImplThrowException(), new OutputHandlerProviderImpl());
        try {
        task.run();
        } catch (RuntimeException e){
        	//cool we caught an exception
        }
        EntityManager em = MeveoPersistence.getEntityManager();
        Query query = em.createQuery("from Currency where code = 'LTL'");
        List result = query.getResultList();
        // no currency inserted because transaction rollbacked
        Assert.assertEquals((Integer)result.size(), (Integer)0);
        Query queryInputHistory = em.createQuery("from MedinaInputHistory");
        List resultInputHistory = queryInputHistory.getResultList();
        // no new InputHistory inserted because transaction rollbacked (there is 1 in initial data)
        Assert.assertEquals((Integer)resultInputHistory.size(), (Integer)1);
        
        // test if handleInputAfterFailure was invoked and file moved to rejected dir
        File f = new File(TEST_REJECTED_DIR + TEST_FILE_NAME);
        Assert.assertTrue(f.exists());
        
        // move file back for next test
        FileUtils.moveFile(TEST_DIR, f, TEST_FILE_NAME);
    }
    
    private class InputHandlerProviderImpl implements Provider<InputHandler<Object>> {
        @SuppressWarnings("unchecked")
        @Override
        public InputHandler<Object> get() {
            return new InputHandlerImpl(null, null);
        }
        
    }
    
    private class InputLoaderProviderImpl implements Provider<InputLoader> {
        @Override
        public InputLoader get() {
            return new InputLoaderImpl();
        }
        
    }
    
    private class InputHandlerProviderImplThrowException implements Provider<InputHandler<Object>> {
        @SuppressWarnings("unchecked")
        @Override
        public InputHandler<Object> get() {
            return new InputHandlerImplThrowException(null, null);
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private class InputHandlerImplThrowException extends InputHandlerImpl {
        public InputHandlerImplThrowException(Processor processor, OutputProducer outputProducer) {
            super(processor, outputProducer);
        }
        @Override
        public TaskExecution handleInput(Input input) throws Exception {
            super.handleInput(input);
            // throws exception so currency should not be commited to database
            throw new RuntimeException();
        }
    }
    
    @SuppressWarnings("unchecked")
    private class InputHandlerImpl extends AbstractInputHandler {
        public InputHandlerImpl(Processor processor, OutputProducer outputProducer) {
            super(processor, outputProducer);
        }
        @Override
        public TaskExecution handleInput(Input input) throws Exception {
            // lets say handle input saves new currency (to test if MeveoTask.run() commits DB transaction) 
            EntityManager em = MeveoPersistence.getEntityManager();
            Currency currency = new Currency();
            currency.setCode("LTL");
            currency.setIsoCode("111");
            currency.setName("Litas");
            currency.setSystemCurrency(false);
            em.persist(currency);
            return new TaskExecution(input.getName(), new Date(), input, null, null);
        }
        @Override
        public TaskExecution executeInputHandling(Input input, TaskExecution taskExecution) throws Exception {
            return null;
        }
    }
    
    private class InputLoaderImpl extends AbstractInputLoader {
        @Override
        public Input loadInput() {
            return new Input("aaa", null);
        }
        @Override
        public void handleInputAfterFailure(Input input, Throwable e) {
            String rejectedFilesDirectory = TEST_REJECTED_DIR;
            FileUtils.moveFile(rejectedFilesDirectory, new File(TEST_DIR, TEST_FILE_NAME), TEST_FILE_NAME);
        }
        @SuppressWarnings("unchecked")
        @Override
        public void handleInputAfterProcessing(Input input, TaskExecution taskExecution) {
            String acceptedFilesDirectory = TEST_ACCEPTED_DIR;
            FileUtils.moveFile(acceptedFilesDirectory, new File(TEST_DIR, TEST_FILE_NAME), TEST_FILE_NAME);

        }
    }
    
    private class OutputHandlerProviderImpl implements Provider<OutputHandler> {
        @Override
        public OutputHandler get() {
            return new OutputHandlerImpl();
        }
    }
    
    private class OutputHandlerImpl implements OutputHandler {
        @SuppressWarnings("unchecked")
        @Override
        public void handleOutput(TaskExecution taskExecution) {
        }
    }
    
    private class MeveoTaskImpl extends MeveoTask<Object> {
        public MeveoTaskImpl(Provider<InputLoader> inputLoaderProvider, Provider<InputHandler<Object>> inputHandlerProvider, Provider<OutputHandler> outputHandlerProvider) {
            super(inputLoaderProvider, inputHandlerProvider, outputHandlerProvider);
        }
        @Override
        protected void persistInputHistory(TaskExecution<Object> taskExecution) {
            InputHistory InputHistory = new MedinaInputHistory();
            InputHistory.setName(taskExecution.getInputObject().getName());
            InputHistory.setParsedTickets(10);
            InputHistory.setSucceededTickets(8);
            InputHistory.setRejectedTickets(2);
            MeveoPersistence.getEntityManager().persist(InputHistory);
        }
    }

}
