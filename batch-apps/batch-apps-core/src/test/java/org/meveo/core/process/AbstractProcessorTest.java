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
package org.meveo.core.process;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.Constants;
import org.meveo.core.process.step.StepExecution;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link AbstractProcessor} tests.
 * 
 * @author Ignas Lelys
 * @created Aug 4, 2010
 * 
 */
public class AbstractProcessorTest {

    @SuppressWarnings("unchecked")
    @Test(groups = { "db" })
    public void testInstantiate() {
        ProcessStepImpl processStepsChain = new ProcessStepImpl(null);
        AbstractProcessor processor = new ProcessorImpl(processStepsChain);
        Assert.assertTrue(processor.processStepsChain instanceof ProcessStepImpl);
        Assert.assertSame(processor.processStepsChain, processStepsChain);
        Map preparedStatements = processor.statements;
        Assert.assertEquals((Integer)preparedStatements.size(), (Integer)2);
        Assert.assertTrue(preparedStatements.get("TEST_1") instanceof PreparedStatement);
        Assert.assertTrue(preparedStatements.get("TEST_2") instanceof PreparedStatement);
    }

    @SuppressWarnings("unchecked")
    @Test(groups = { "db" })
    public void testProcess() {
        AbstractProcessor processor = new ProcessorImpl(new ProcessStepImpl(null));
        Map<String, Object> contextParams = processor.process("ticket1", new TaskExecution<String>("ticket1.source",
                new Date(), null, null, processor));
        Assert.assertTrue((Boolean) contextParams.get(Constants.ACCEPTED));
        Assert.assertEquals(contextParams.get(Constants.STATUS), Constants.ONGOING_STATUS);
        Assert.assertEquals(contextParams.get("processStep"), "in");
    }

    // TODO
    @SuppressWarnings("unchecked")
    @Test(groups = { "db" })
    public void testCommit() {
        AbstractProcessor processor = new ProcessorImpl(new ProcessStepImpl(null));
        processor.commit(null);
    }

    @SuppressWarnings("unchecked")
    private class ProcessorImpl extends AbstractProcessor<String> {
        public ProcessorImpl(AbstractProcessStep processStepsChain) {
            super(processStepsChain);
        }

        @Override
        protected void doCommit(TaskExecution taskExecution) throws SQLException {
        }

        @Override
        protected Map getNamedQueries() {
            HashMap queries = new HashMap();
            queries.put("TEST_1", "SELECT * FROM BILLING_WALLET");
            queries.put("TEST_2", "SELECT * FROM BILLING_RATED_TRANSACTION");
            return queries;
        }
    }

    private class ProcessStepImpl extends AbstractProcessStep<String> {
        public ProcessStepImpl(AbstractProcessStep<String> nextStep) {
            super(nextStep, null);
        }

        @Override
        protected boolean execute(StepExecution<String> stepExecution) {
            stepExecution.addParameter("processStep", "in");
            return true;
        }

    }

}
