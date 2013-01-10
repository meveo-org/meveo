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


/**
 * Tests for CDRUniquenessCheckStep.
 * 
 * @author Donatas Remeika
 * @created Apr 9, 2009
 */
public class CDRUniquenessCheckStepTest {

//    @Test(groups = { "unit" })
//    public void testAddUniqueCDRS() {
//
//        CDRUniquenessCheckStep step = new CDRUniquenessCheckStep(null);
//        for (int i = 101; i < 106; i++) {
//            CDR cdr = new BaseCDR.Builder().addDownloadedDataVolume(100L).addUploadedDataVolume(1L).addOriginPLMN(
//                    "20820").addServedIMSI("10000" + i).addNodeID("GGSN" + i)
//                    .addRecordSequenceNumber(String.valueOf(i)).addIPBinV4Address("101.23.78." + i).addDuration(
//                            i + 100L).addRecordOpeningTime(new Date()).build();
//            MediationContext context = new MediationContext(new DATACDRWrapper(cdr), CDRType.DATA, null);
//            boolean success = step.execute(context);
//            Assert.assertTrue(success);
//            Assert.assertTrue(context.isAccepted());
//            Assert.assertNotNull(context.getMagicNumber());
//        }
//    }
//
//    @Test(groups = { "unit" })
//    public void testAddNonUniqueCDR() {
//
//        CDRUniquenessCheckStep step = new CDRUniquenessCheckStep(null);
//        CDR cdr = new BaseCDR.Builder().addDownloadedDataVolume(100L).addUploadedDataVolume(1L).addOriginPLMN("20820")
//                .addServedIMSI("100001").addNodeID("GGSN1").addRecordSequenceNumber(String.valueOf(1))
//                .addIPBinV4Address("101.23.78.1").addDuration(100L).addRecordOpeningTime(new Date()).build();
//        MediationContext context = new MediationContext(new DATACDRWrapper(cdr), CDRType.DATA, null);
//        // add same magic number to cache
//        CacheTransaction transaction = TransactionalMagicNumberCache.getInstance().getTransaction();
//        transaction.addToCache(new DATACDRWrapper(cdr).getMagicNumber());
//        transaction.commit();
//        // try to add same cdr again
//        boolean success = step.execute(context);
//        Assert.assertFalse(success);
//        Assert.assertFalse(context.isAccepted());
//        Assert.assertNull(context.getMagicNumber());
//    }
//    
//    @Test(groups = { "unit" })
//    public void testDoNotCheckPartialTickets() {
//
//        CDRUniquenessCheckStep step = new CDRUniquenessCheckStep(null);
//        CDR cdr = new BaseCDR.Builder().addOriginPLMN("20820")
//                .addServedIMSI("100001").addNodeID("GGSN1").addRecordSequenceNumber(String.valueOf(2))
//                .addServedMSISDN("111").addIPBinV4Address("101.23.78.1").addCalledNumber("111111").addDuration(100L).addRecordOpeningTime(new Date()).build();
//        MediationContext context = new MediationContext(new VOICECDRWrapper(cdr), CDRType.VOICE, null);
//        // add same magic number to cache
//        CacheTransaction transaction = TransactionalMagicNumberCache.getInstance().getTransaction();
//        transaction.addToCache(new VOICECDRWrapper(cdr).getMagicNumber());
//        transaction.commit();
//        // try to add same cdr again
//        boolean success = step.execute(context);
//        Assert.assertTrue(success);
//        Assert.assertTrue(context.isAccepted());
//        Assert.assertNotNull(context.getMagicNumber());
//    }
//    
//    @Test(groups = { "unit" })
//    public void testIncomingOutgoingTickets() {
//    	Date now = new Date();
//        CDRUniquenessCheckStep step = new CDRUniquenessCheckStep(null);
//        CDR outgoing = new BaseCDR.Builder().addCDRType("OUTGOING").addCalledNumber("111")
//        				.addServedMSISDN("222").addRecordOpeningTime(now).addRecordSequenceNumber("1").build();
//        CDR incoming = new BaseCDR.Builder().addCDRType("MST").addCallingNumber("222")
//        				.addServedMSISDN("111").addRecordOpeningTime(now).addRecordSequenceNumber("1").build();
//        MediationContext context = new MediationContext(new VOICECDRWrapper(outgoing), CDRType.VOICE, null);
//        // add incoming to cache
//        CacheTransaction transaction = TransactionalMagicNumberCache.getInstance().getTransaction();
//        transaction.addToCache(new VOICECDRWrapper(incoming).getMagicNumber());
//        transaction.commit();
//        // try to add same cdr again
//        boolean success = step.execute(context);
//        Assert.assertTrue(success);
//        Assert.assertTrue(context.isAccepted());
//        Assert.assertNotNull(context.getMagicNumber());
//    }

}
