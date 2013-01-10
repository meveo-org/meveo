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
package org.meveo.config.integration;


/**
 * Tests full process of MeveoTask. With InputLoader InputHandler Processor etc.
 * 
 * @author Ignas Lelys
 * @created Apr 23, 2011
 * 
 */
// TODO finish this test
public class FullProcessTest {

//    @Test(groups = "db")
//    public void testFullProcess() {
//        try {
//            MeveoTask task = new MeveoTaskImpl(new InputLoaderProviderImpl(), new InputHandlerProviderImpl(), new OutputHandlerProviderImpl());
//            task.run();
//            
//        } finally {
//            
//        }
//    }
//
//    private class MeveoTaskImpl extends MeveoTask<Object> {
//        public MeveoTaskImpl(Provider<InputLoader> inputLoaderProvider, Provider<InputHandler<Object>> inputHandlerProvider, Provider<OutputHandler> outputHandlerProvider) {
//            super(inputLoaderProvider, inputHandlerProvider, outputHandlerProvider);
//        }
//        @Override
//        protected void persistInputHistory(TaskExecution<Object> taskExecution) {
//            InputHistory preSavedInputHistory = taskExecution.getInputHistory();
//            InputHistory InputHistory = new MedinaInputHistory(preSavedInputHistory);
//            InputHistory.setName(taskExecution.getInputObject().getName());
//            InputHistory.setParsedTickets(10);
//            InputHistory.setSucceededTickets(8);
//            InputHistory.setRejectedTickets(2);
//            MeveoPersistence.getEntityManager().merge(InputHistory);
//        }
//    }
}
