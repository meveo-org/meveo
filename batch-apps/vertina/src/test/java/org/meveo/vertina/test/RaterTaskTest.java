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
package org.meveo.vertina.test;


/**
 * Rater task tests.
 * 
 * @author Ignas Lelys
 * @created Jul 19, 2010
 *
 */
public class RaterTaskTest {
    
//    @SuppressWarnings("unchecked")
//    @Test(groups = { "db" })
//    public void testPersistTaskExecution() {
//        RaterTask raterTask = new RaterTask(new InputLoader() {
//            @Override
//            public Input loadInput() {
//                return null;
//            }
//            @Override
//            public void handleInputFailure() {
//            }
//        }, new InputHandler<VertinaTicket>() {
//            @Override
//            public TaskExecution<VertinaTicket> handleInput(Input input) throws Exception {
//                return null;
//            }
//        }, new OutputHandler() {
//            @Override
//            public void handleOutput(Object outputObject) {
//            }
//        });
//        
//        TaskExecution<VertinaTicket> taskExecution = new TaskExecution<VertinaTicket>("test", new Date(), null);
//        taskExecution.setParsedTicketsCount(10);
//        taskExecution.setProcessedTicketsCount(8);
//        taskExecution.setRejectedTicketsCount(2);
//        
//        VertinaInputInfo inputInfo = new VertinaInputInfo();
//        Calendar analysisStartTime = Calendar.getInstance();
//        analysisStartTime.set(2010, Calendar.JANUARY, 2, 12, 10, 0);
//        analysisStartTime.set(Calendar.MILLISECOND, 0);
//        inputInfo.setAnalysisStartDate(analysisStartTime.getTime());
//        inputInfo.setName("testFileName.txt");
//        
//        EntityManager em = MeveoPersistence.getEntityManager();
//        em.getTransaction().begin();
//        raterTask.persistInputInfo(inputInfo, taskExecution);
//        em.getTransaction().commit();
//        
//        Query query = em.createQuery("from VertinaInputInfo");
//        List<VertinaInputInfo> vertinaInputInfos = query.getResultList();
//        
//        Assert.assertEquals(vertinaInputInfos.size(), 1);
//        VertinaInputInfo loadedInputInfo = vertinaInputInfos.get(0);
//        Assert.assertEquals(loadedInputInfo.getParsedTickets(), (Integer)10);
//        Assert.assertEquals(loadedInputInfo.getSucceededTickets(), (Integer)8);
//        Assert.assertEquals(loadedInputInfo.getRejectedTickets(), (Integer)2);
//        Assert.assertEquals(loadedInputInfo.getName(), "testFileName.txt");
//        Assert.assertEquals(loadedInputInfo.getAnalysisStartDate(), analysisStartTime.getTime());
//    }

}
