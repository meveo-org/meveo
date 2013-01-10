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

import java.sql.PreparedStatement;
import java.util.Map;

import org.meveo.config.task.TestConfig;
import org.meveo.core.inputloader.Input;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.process.Processor;
import org.meveo.persistence.MeveoPersistence;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link AbstractInputHandler} tests.
 * 
 * @author Ignas Lelys
 * @created Aug 9, 2010
 *
 */
public class AbstractInputHandlerTest {
    
    @SuppressWarnings("unchecked")
    @Test(groups = {"db"})
    public void testHandleInput() throws Exception {
        MeveoPersistence.getEntityManager().getTransaction().begin();
        
        AbstractInputHandler inputHandler = new InputHandlerImpl(new Processor<Object>() {
            @Override
            public void commit(TaskExecution<Object> taskExecution) {
            }
            @Override
            public PreparedStatement getStatementByName(String name) {
                return null;
            }
            @Override
            public Map<String, Object> process(Object ticket, TaskExecution<Object> taskExecution) {
                return null;
            }
        }, null);
        Input input = new Input("target/test-classes/files/test.csv", new Object());
        TaskExecution taskExecution = inputHandler.handleInput(input);
        Assert.assertNotNull(taskExecution.getStartTime());
        Assert.assertNotNull(taskExecution.getEndTime());
        
        MeveoPersistence.getEntityManager().getTransaction().commit();
    }
    
    @SuppressWarnings("unchecked")
    @Test(groups = {"db"})
    public void testCreateNewTaskExecution() {
        MeveoPersistence.getEntityManager().getTransaction().begin();
        
        AbstractInputHandler inputHandler = new InputHandlerImpl(new Processor<Object>() {
            @Override
            public void commit(TaskExecution<Object> taskExecution) {
            }
            @Override
            public PreparedStatement getStatementByName(String name) {
                return null;
            }
            @Override
            public Map<String, Object> process(Object ticket, TaskExecution<Object> taskExecution) {
                return null;
            }
        }, null);
        Input input = new Input("target/test-classes/files/test.csv", new Object());
        TaskExecution taskExecution = inputHandler.createNewTaskExecution(input);
        Assert.assertNotNull(taskExecution.getStartTime());
        Assert.assertNotNull(taskExecution.getInputHistory().getId());
        Assert.assertNull(taskExecution.getEndTime());
        Assert.assertEquals(taskExecution.getName(), "target/test-classes/files/test.csv task");
        
        MeveoPersistence.getEntityManager().getTransaction().commit();
    }

    @SuppressWarnings("unchecked")
    private class InputHandlerImpl extends AbstractInputHandler {
        public InputHandlerImpl(Processor processor, OutputProducer outputProducer) {
            super(processor, outputProducer);
            super.config = new TestConfig();
        }
        @Override
        public TaskExecution executeInputHandling(Input input, TaskExecution taskExecution) throws Exception {
            return taskExecution;
        }
    }
}
