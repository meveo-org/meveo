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
package org.meveo.core.process.step;

import java.util.Date;
import java.util.HashMap;

import org.meveo.core.inputhandler.TaskExecution;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Process step tests.
 * 
 * @author Ignas Lelys
 * @created Aug 4, 2010
 *
 */
public class AbstractProcessStepTest {
    
    @SuppressWarnings("unchecked")
    @Test(groups = { "unit" })
    public void testProcess() {
        AbstractProcessStep steps = new ProcessStepImpl(new ProcessStepImpl(null));
        // execute() should be invoked two times
        TaskExecution taskExecution = new TaskExecution("", new Date(), null, null, null);
        HashMap<String, Object> contextParameters = new HashMap<String, Object>();
        steps.process(new Object(), taskExecution, contextParameters);
        Assert.assertEquals(contextParameters.get("1"), "1");
        Assert.assertEquals(contextParameters.get("2"), "2");
    }

    @SuppressWarnings("unchecked")
    private class ProcessStepImpl extends AbstractProcessStep {
        public ProcessStepImpl(AbstractProcessStep nextStep) {
            super(nextStep, null);
        }
        @Override
        protected boolean execute(StepExecution stepExecution) {
            if (stepExecution.getParameter("1") != null) {
                stepExecution.addParameter("2", "2");
            } else {
                stepExecution.addParameter("1", "1");
            }
            return true;
        }
        
    }
}
