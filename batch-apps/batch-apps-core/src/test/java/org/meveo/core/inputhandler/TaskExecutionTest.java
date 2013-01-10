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

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.core.process.step.StepExecution;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for TaskExecution class.
 * 
 * @author Ignas Lelys
 * @created Aug 9, 2010
 *
 */
public class TaskExecutionTest {
    
    @SuppressWarnings("unchecked")
    @Test(groups = {"unit"})
    public void testAddStepExecution() throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
        TaskExecution<String> taskExecution = new TaskExecution<String>("test", new Date(), null, null, null);
        // add step executions (do not use same taskExecution in stepExecution constructor, because stepExecution constructor
        // calls addStepExecution also, so we get doubled elements...
        taskExecution.addStepExecution("ticket1", new StepExecution<String>("ticket1", "1a", null, new TaskExecution<String>("test", new Date(), null, null, null)));
        taskExecution.addStepExecution("ticket1", new StepExecution<String>("ticket1", "2a", null, new TaskExecution<String>("test", new Date(), null, null, null)));
        taskExecution.addStepExecution("ticket2", new StepExecution<String>("ticket2", "1b", null, new TaskExecution<String>("test", new Date(), null, null, null)));
        taskExecution.addStepExecution("ticket3", new StepExecution<String>("ticket3", "1c", null, new TaskExecution<String>("test", new Date(), null, null, null)));
        Map<String, Collection<StepExecution<String>>> ticketsWithStepExecution = (Map<String, Collection<StepExecution<String>>>)ReflectionUtils.getPrivateField(TaskExecution.class, taskExecution, "ticketsWithStepExecution");
        Assert.assertEquals((Integer)ticketsWithStepExecution.keySet().size(), (Integer)3);
        Assert.assertEquals((Integer)((Collection<StepExecution<String>>)ticketsWithStepExecution.get("ticket1")).size(), (Integer)2);
        Assert.assertEquals((Integer)((Collection<StepExecution<String>>)ticketsWithStepExecution.get("ticket2")).size(), (Integer)1);
        Assert.assertEquals((Integer)((Collection<StepExecution<String>>)ticketsWithStepExecution.get("ticket3")).size(), (Integer)1);
        Assert.assertNull(ticketsWithStepExecution.get("ticket4"));
    }
    
    @Test(groups = {"unit"})
    public void testAddExecutionContextParameter() {
        TaskExecution<String> taskExecution = new TaskExecution<String>("test", new Date(), null, null, null);
        taskExecution.addExecutionContextParameter("1", "11");
        taskExecution.addExecutionContextParameter("2", "22");
        taskExecution.addExecutionContextParameter("3", "33");
        Assert.assertEquals(taskExecution.getExecutionContextParameter("1"), "11");
        Assert.assertEquals(taskExecution.getExecutionContextParameter("2"), "22");
        Assert.assertEquals(taskExecution.getExecutionContextParameter("3"), "33");
        Assert.assertNull(taskExecution.getExecutionContextParameter("4"));
    }
    
}
