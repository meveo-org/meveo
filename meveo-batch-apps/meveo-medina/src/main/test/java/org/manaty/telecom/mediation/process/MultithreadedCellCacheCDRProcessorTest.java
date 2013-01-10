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
package org.manaty.telecom.mediation.process;

import org.testng.annotations.Test;

public class MultithreadedCellCacheCDRProcessorTest {
    
    @Test(groups = { "db" })
    public void testMultithreadedCellIdAndInsertHistoryInsert() throws Exception {
        
    }
    
//    private class CDRProcessorThread implements Runnable {
//
//        public void run() {
//            EntityManager em = MedinaPersistence.getEntityManager();
//            em.getTransaction().begin();
//
//            Calendar cellChangeDate = Calendar.getInstance();
//            cellChangeDate.set(2011, Calendar.FEBRUARY, 1, 0, 0, 0);
//            cellChangeDate.set(Calendar.MILLISECOND, 0);
//            CDR cdr = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
//                    "666666").addRecordSequenceNumber("1").addCalledNumber(
//                    "666666").addDuration(5000L).addEtatELU("F").addOriginPLMN(
//                    "20820").addRecordOpeningTime(new Date()).addCDRType("MSF")
//                    .addIOT(new BigDecimal("1.1")).addServedIMSI("100001")
//                    .addOnNET("_OFF").addCallingNumber("12345611").addCellId(
//                            "CELL_OLDER").addCellChangeDate(
//                            cellChangeDate.getTime()).build());
//            
//            CDRProcessor processor = new CDRProcessor(null);
//            MediationContext context = processor.process(cdr, CDRType.VOICE);
//            Assert.assertTrue(context.isAccepted());
//        }
//        
//    }

}
