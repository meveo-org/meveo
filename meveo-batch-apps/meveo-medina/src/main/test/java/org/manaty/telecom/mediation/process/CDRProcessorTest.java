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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.manaty.model.mediation.RejectedCDR;
import org.manaty.model.mediation.RejectedCDR.RejectedCDRFlag;
import org.manaty.model.resource.alarm.AlarmType;
import org.manaty.model.resource.telecom.BillingStatusEnum;
import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRStatus;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.model.telecom.mediation.cdr.DATACDRWrapper;
import org.manaty.model.telecom.mediation.cdr.VOICECDRWrapper;
import org.manaty.telecom.mediation.FileProcessingContext;
import org.manaty.telecom.mediation.MedinaPersistence;
import org.manaty.telecom.mediation.cache.TransactionalCellCache;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache;
import org.manaty.telecom.mediation.cache.TransactionalMagicNumberCache.CacheTransaction;
import org.manaty.telecom.mediation.context.Access;
import org.manaty.telecom.mediation.context.AccessUpdateType;
import org.manaty.telecom.mediation.context.MediationContext;
import org.manaty.utils.ListUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for CDRProcessor class.
 * 
 * @author Donatas Remeika
 * @created Mar 20, 2009
 */
public class CDRProcessorTest {

    @SuppressWarnings("unchecked")
    @Test(groups = "db", dependsOnMethods = { "testProcessingRollsback" })
    public void testProcessingAndCommit() throws Throwable {
        try {
            Query cdrTicketQuery = MedinaPersistence.getEntityManager()
                    .createQuery("select count(*) from CDRTicket");
            Long insertedCDRTickets = (Long) cdrTicketQuery.getSingleResult();

            MedinaPersistence.getEntityManager().getTransaction().begin();
            TransactionalCellCache.getInstance().beginTransaction();
            CacheTransaction cacheTransaction = TransactionalMagicNumberCache
                    .getInstance().getTransaction();

//            int cacheSizeBeforeProcessing = TransactionalMagicNumberCache
//                    .getInstance().getCache().size();

            List<CDR> cdrs = new ArrayList<CDR>();

            for (int i = 1; i < 6; i++) {
                CDR cdr = new DATACDRWrapper(new BaseCDR.Builder()
                        .addDownloadedDataVolume(100L)
                        .addUploadedDataVolume(1L).addOriginPLMN("20820")
                        .addServedIMSI("10000" + i).addNodeID("GGSN" + i)
                        .addRecordSequenceNumber(String.valueOf(1))
                        .addIPBinV4Address("101.23.78." + i).addDuration(
                                i + 100L).addEtatELU("F").addRecordOpeningTime(
                                new Date()).addOriginPLMN("20820")
                        .addAccessPointNameNI("iphone").build());
                cdrs.add(cdr);
            }
            CDR cdr = new DATACDRWrapper(new BaseCDR.Builder()
                    .addDownloadedDataVolume(100L).addUploadedDataVolume(1L)
                    .addOriginPLMN("20820").addServedMSISDN("10000" + 6)
                    .addNodeID("GGSN" + 11).addRecordSequenceNumber(
                            String.valueOf(1)).addIPBinV4Address(
                            "101.23.78." + 6).addDuration(6 + 100L)
                    .addRecordOpeningTime(new Date()).addEtatELU("F")
                    .addAccessPointNameNI("iphone").addOriginPLMN("20820")
                    .build());
            cdrs.add(cdr);

            cdr = new DATACDRWrapper(new BaseCDR.Builder()
                    .addDownloadedDataVolume(100L).addUploadedDataVolume(1L)
                    .addOriginPLMN("20820").addServedMSISDN("10000" + 6)
                    .addNodeID("GGSN" + 12).addRecordSequenceNumber(
                            String.valueOf(1)).addIPBinV4Address(
                            "101.23.78." + 7).addDuration(7 + 100L)
                    .addRecordOpeningTime(new Date()).addEtatELU("F")
                    .addAccessPointNameNI("iphone").addOriginPLMN("20820")
                    .build());
            cdrs.add(cdr);

            cdr = new DATACDRWrapper(new BaseCDR.Builder()
                    .addDownloadedDataVolume(100L).addUploadedDataVolume(1L)
                    .addOriginPLMN("20820").addServedIMSI("10000" + 6)
                    .addNodeID("GGSN" + 13).addRecordSequenceNumber(
                            String.valueOf(1)).addIPBinV4Address(
                            "101.23.78." + 8).addDuration(7 + 100L)
                    .addRecordOpeningTime(new Date()).addEtatELU("F")
                    .addAccessPointNameNI("iphone").addOriginPLMN("20820")
                    .build());
            cdrs.add(cdr);
            // Cdr with no plmn and no zonningPlan in database (so OUT_ZONE
            // zonning
            // plan used).
            cdr = new DATACDRWrapper(new BaseCDR.Builder()
                    .addDownloadedDataVolume(100L).addUploadedDataVolume(1L)
                    .addServedMSISDN("10000" + 9).addNodeID("GGSN" + 14)
                    .addRecordSequenceNumber(String.valueOf(1))
                    .addIPBinV4Address("101.123.78." + 9).addDuration(7 + 100L)
                    .addRecordOpeningTime(new Date()).addEtatELU("F")
                    .addAccessPointNameNI("iphone").addOriginPLMN("20820")
                    .addPDPIpAddress("1.2.3.4").build());
            cdrs.add(cdr);

            // Partial cdrs. Those cdrs must be saved to MEDINA_CDR_TICKET
            // table.
            CDR partialCDR1 = new VOICECDRWrapper(new BaseCDR.Builder()
                    .addDownloadedDataVolume(10L).addUploadedDataVolume(10L)
                    .addServedMSISDN("100009").addRecordSequenceNumber(
                            String.valueOf(9)).addIPBinV4Address(
                            "111.111.111.111").addDuration(10 + 100L)
                    .addRecordOpeningTime(new Date()).addEtatELU("N")
                    .addCalledNumber("333333").addAccessPointNameNI(
                            "my_computer").addTicketID("1111").addOriginPLMN(
                            "20820").addOnNET("onnet").addMVNORouting("7801")
                    .addNature("GUE").addCallingNumber("123123").addOperator(
                            "BYTEL").addSSCode("91").addIdnCom("222-222")
                    .addNodeID("BS30").addCellId("66").addPDPIpAddress(
                            "1.2.3.4").addCellChangeDate(new Date()).build());

            CDRProcessor processor = new CDRProcessor(cacheTransaction);
            long start = System.currentTimeMillis();
            MediationContext context = null;
            for (int i = 0; i < 9; i++) {
                context = processor.process(cdrs.get(i), CDRType.DATA);
                Assert.assertNotNull(context);
                Assert.assertTrue(context.isAccepted());
                Assert.assertNotNull(context.getEDR());
//                Assert.assertNotNull(context.getMagicNumber());
                Assert.assertEquals(context.getStatus(), CDRStatus.ONGOING);
                Assert.assertEquals(context.getUsageCount().getUsage(),
                        CDRType.DATA);
                Assert.assertEquals(context.getUsageCount().getCount(), Long
                        .valueOf(101L));
                Assert.assertEquals(context.getUsageCount().getCountDown(),
                        Long.valueOf(100L));
                Assert.assertEquals(context.getUsageCount().getCountUp(), Long
                        .valueOf(1L));
                Assert.assertNotNull(context.getAccessUserId());
                Assert.assertEquals(context.getAccessServiceId(), "PA_DATA");
            }
            // Test that cache is filled
            Assert.assertEquals(processor.getAccesses().size(), 7);
            Assert.assertEquals(processor.getAccessCacheByMSISDN().size(), 2);
            Assert.assertEquals(processor.getAccessCacheByIMSI().size(), 6);

            FileProcessingContext fileProcessingContext = new FileProcessingContext();
            // fileProcessingContext.getPAMatchesToRetryInsert().addAll(
            // ListUtils.createList(context.getNotInsertedPaMatch()));
            fileProcessingContext.getPartialCDRs().addAll(
                    ListUtils.createList(partialCDR1));

            // process partial ticket for aggregation
            context = processor.process(partialCDR1, CDRType.VOICE);
            Assert.assertNotNull(context);
            Assert.assertFalse(context.isAccepted());
            Assert.assertEquals(context.getStatus(), CDRStatus.AGGREGATED);
            Assert.assertEquals(context.getType(), CDRType.VOICE);

            CommitResult commitResult = processor.commit(new Date(),
                    fileProcessingContext);
            Assert.assertNotNull(commitResult);
            Assert.assertEquals(commitResult.getUsageCountDATA(), 909L);
            // Test that caches are cleared
            Assert.assertEquals(processor.getAccesses().size(), 0);
            Assert.assertEquals(processor.getAccessCacheByMSISDN().size(), 0);
            Assert.assertEquals(processor.getAccessCacheByIMSI().size(), 0);
            MedinaPersistence.getEntityManager().getTransaction().commit();
            TransactionalCellCache.getInstance().commitTransaction();

            EntityManager em = MedinaPersistence.getEntityManager();

            Query query = em
                    .createNativeQuery("SELECT sum (PARTITION_ID) from RM_ALARM_INSTANCE WHERE ACCESS_POINT_ID between 1001 and 1006 AND (ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME
                            + "' OR ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME_ROUNDED + "')");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    20);

            query = em
                    .createNativeQuery("SELECT sum (CCV1) from RM_ALARM_INSTANCE WHERE ACCESS_POINT_ID between 1001 and 1006 AND (ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME
                            + "' OR ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME_ROUNDED + "')");
            Number alarmInstanceCounterSum = (Number) query.getSingleResult();
            Assert.assertEquals(alarmInstanceCounterSum.intValue(), 707);

            query = em
                    .createNativeQuery("SELECT sum (CCV1_UP) from RM_ALARM_INSTANCE WHERE ACCESS_POINT_ID between 1001 and 1006 AND (ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME
                            + "' OR ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME_ROUNDED + "')");
            Number alarmInstanceCounterUPSum = (Number) query.getSingleResult();
            Assert.assertEquals(alarmInstanceCounterUPSum.intValue(), 7);

            query = em
                    .createNativeQuery("SELECT sum (CCV1_DOWN) from RM_ALARM_INSTANCE WHERE ACCESS_POINT_ID between 1001 and 1006 AND (ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME
                            + "' OR ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME_ROUNDED + "')");
            Number alarmInstanceCounterDOWNSum = (Number) query
                    .getSingleResult();
            Assert.assertEquals(alarmInstanceCounterDOWNSum.intValue(), 700);

            query = em
                    .createNativeQuery("SELECT sum (CCV1) from RM_ALARM_INSTANCE WHERE ACCESS_POINT_ID between 1001 and 1006 AND ALARM_TYPE = '"
                            + AlarmType.UNDER_CONSUMPTION_VOLUME + "'");
            Number alarmInstanceCounterSumUnder = (Number) query
                    .getSingleResult();
            Assert.assertEquals(alarmInstanceCounterSumUnder.intValue(), 101);

            query = em
                    .createNativeQuery("SELECT CCV1 from RM_ALARM_INSTANCE WHERE ACCESS_POINT_ID = 1006 AND (ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME
                            + "' OR ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME_ROUNDED + "')");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    303);

            query = em
                    .createNativeQuery("SELECT sum (COUNTER1) from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID between 1002 and 1006");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    707);

            query = em
                    .createNativeQuery("SELECT sum (COUNTER1_UP) from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID between 1002 and 1006");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    7);

            query = em
                    .createNativeQuery("SELECT sum (COUNTER1_DOWN) from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID between 1002 and 1006");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    700);

            // Usage counter for 1001 was not updated because ticket date was
            // before usage date.
            query = em
                    .createNativeQuery("SELECT sum (COUNTER1) from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID = 1001");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    0);

            query = em
                    .createNativeQuery("SELECT sum (COUNTER1_UP) from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID = 1001");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    0);

            query = em
                    .createNativeQuery("SELECT sum (COUNTER1_DOWN) from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID = 1001");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    0);

            query = em
                    .createNativeQuery("SELECT COUNTER1 from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID = 1006");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    303);

            query = em
                    .createQuery("SELECT paOnActiveOffer from UsageCounter where accessPoint.id = 1002");
            Assert.assertTrue(((Boolean) query.getSingleResult()));

            query = em
                    .createNativeQuery("SELECT LAST_COMMUNICATION_DATE from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID between 1002 and 1006 and PARTITION_ID between 1 and 6");

            List<Timestamp> resultList = query.getResultList();
            Assert.assertTrue(resultList.size() == 5);
            for (Timestamp timestamp : resultList) {
                Assert.assertTrue(timestamp.before(new Timestamp(System
                        .currentTimeMillis() + 1000)));
                Assert.assertTrue(timestamp.after(new Timestamp(start - 1000)));
            }

            // test if ticket for aggregation was saved to database
            // successfully.
            query = em.createQuery("select count(*) from CDRTicket");
            Assert
                    .assertEquals(query.getSingleResult(),
                            insertedCDRTickets + 1);

            query = em
                    .createQuery("select count(*) from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), 1L);

            query = em
                    .createQuery("select t.createdOn from CDRTicket t where t.ticketID = '1111'");
            Assert.assertNotNull(query.getSingleResult());

            query = em
                    .createQuery("select t.onNET from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), "onnet");

            query = em
                    .createQuery("select t.mvnoRouting from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), "7801");

            query = em
                    .createQuery("select t.nature from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), "GUE");

            query = em
                    .createQuery("select t.iot from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), BigDecimal.ZERO);

            query = em
                    .createQuery("select t.callingNumber from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), "123123");

            query = em
                    .createQuery("select t.operator from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), "BYTEL");

            query = em
                    .createQuery("select t.ssCode from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), "91");

            query = em
                    .createQuery("select t.idnCom from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), "222-222");

            query = em
                    .createQuery("select t.nodeId from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), "BS30");

            query = em
                    .createQuery("select t.cellId from CDRTicket t where t.ticketID = '1111'");
            Assert.assertEquals(query.getSingleResult(), "66");

            query = em
                    .createQuery("select t.cellChangeDate from CDRTicket t where t.ticketID = '1111'");
            Assert.assertNotNull(query.getSingleResult());

            // test magic numbers cache commit
            cacheTransaction.commit();

            // TODO
//            Assert.assertEquals(TransactionalMagicNumberCache.getInstance()
//                    .getCache().size(), cacheSizeBeforeProcessing + 10);

        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }

    }

    @Test(groups = "db")
    public void testProcessingRollsback() throws Exception {
        try {
            MedinaPersistence.getEntityManager().getTransaction().begin();
            CacheTransaction cacheTransaction = TransactionalMagicNumberCache
                    .getInstance().getTransaction();

            int cacheSizeBeforeProcessing = TransactionalMagicNumberCache
                    .getInstance().getCache().size();

            List<CDR> cdrs = new ArrayList<CDR>();

            for (int i = 1; i < 6; i++) {
                CDR cdr = new DATACDRWrapper(new BaseCDR.Builder()
                        .addDownloadedDataVolume(100L)
                        .addUploadedDataVolume(1L).addOriginPLMN("20820")
                        .addServedIMSI("10000" + i).addNodeID("GGSN" + i)
                        .addRecordSequenceNumber("1").addIPBinV4Address(
                                "101.123.78." + i).addDuration(i + 100L)
                        .addEtatELU("F").addOriginPLMN("20820")
                        .addRecordOpeningTime(new Date()).addAccessPointNameNI(
                                "iphone").build());
                cdrs.add(cdr);
            }
            CDR cdr = new DATACDRWrapper(new BaseCDR.Builder()
                    .addDownloadedDataVolume(100L).addUploadedDataVolume(1L)
                    .addOriginPLMN("20820").addServedMSISDN("10000" + 6)
                    .addNodeID("GGSN" + 11).addRecordSequenceNumber(
                            String.valueOf(1)).addIPBinV4Address(
                            "101.123.78." + 6).addDuration(6 + 100L)
                    .addRecordOpeningTime(new Date()).addAccessPointNameNI(
                            "iphone").addOriginPLMN("20820").addEtatELU("F")
                    .build());
            cdrs.add(cdr);

            cdr = new DATACDRWrapper(new BaseCDR.Builder()
                    .addDownloadedDataVolume(100L).addUploadedDataVolume(1L)
                    .addOriginPLMN("20820").addServedMSISDN("10000" + 6)
                    .addNodeID("GGSN" + 12).addRecordSequenceNumber(
                            String.valueOf(1)).addIPBinV4Address(
                            "101.123.78." + 6).addDuration(7 + 100L)
                    .addRecordOpeningTime(new Date()).addAccessPointNameNI(
                            "iphone").addOriginPLMN("20820").addEtatELU("F")
                    .build());
            cdrs.add(cdr);

            cdr = new DATACDRWrapper(new BaseCDR.Builder()
                    .addDownloadedDataVolume(0L).addUploadedDataVolume(0L)
                    .addOriginPLMN("20820").addServedMSISDN("10000" + 6)
                    .addNodeID("GGSN" + 13).addRecordSequenceNumber(
                            String.valueOf(1)).addIPBinV4Address(
                            "101.123.78." + 6).addDuration(7 + 100L)
                    .addRecordOpeningTime(new Date()).addAccessPointNameNI(
                            "iphone").addOriginPLMN("20820").addEtatELU("F")
                    .build());
            cdrs.add(cdr);

            CDRProcessor processor = new CDRProcessor(cacheTransaction);
            for (int i = 0; i < 7; i++) {
                MediationContext context = processor.process(cdrs.get(i),
                        CDRType.DATA);
                Assert.assertNotNull(context);
                Assert.assertTrue(context.isAccepted());
                Assert.assertNotNull(context.getEDR());
//                Assert.assertNotNull(context.getMagicNumber());
                Assert.assertEquals(context.getStatus(), CDRStatus.ONGOING);
                Assert.assertEquals(context.getUsageCount().getUsage(),
                        CDRType.DATA);
                Assert.assertEquals(context.getUsageCount().getCount(), Long
                        .valueOf(101L));
                Assert.assertNotNull(context.getAccessUserId());
                Assert.assertEquals(context.getAccessServiceId(), "PA_DATA");
                Assert.assertEquals(context.getAccess().getUpdateType(),
                        AccessUpdateType.ALL);
            }

            MediationContext context = processor.process(cdrs.get(7),
                    CDRType.DATA);
            Assert.assertNotNull(context);
            Assert.assertFalse(context.isAccepted());
            Assert.assertNull(context.getEDR());
//            Assert.assertNotNull(context.getMagicNumber());
            Assert.assertEquals(context.getStatus(), CDRStatus.IGNORED);
            Assert.assertNull(context.getUsageCount());
            Assert.assertNull(context.getAccessUserId());
            Assert.assertNull(context.getAccessServiceId());
            Assert.assertEquals(context.getAccess().getUpdateType(),
                    AccessUpdateType.ALL);

            // Test that cache is filled
            Assert.assertEquals(processor.getAccesses().size(), 6);
            Assert.assertEquals(processor.getAccessCacheByMSISDN().size(), 1);
            Assert.assertEquals(processor.getAccessCacheByIMSI().size(), 5);

            FileProcessingContext fileProcessingContext = new FileProcessingContext();
            processor.commit(new Date(), fileProcessingContext);
            // Test that caches are cleared
            Assert.assertEquals(processor.getAccesses().size(), 0);
            Assert.assertEquals(processor.getAccessCacheByMSISDN().size(), 0);
            Assert.assertEquals(processor.getAccessCacheByIMSI().size(), 0);

            MedinaPersistence.getEntityManager().getTransaction().rollback();

            EntityManager em = MedinaPersistence.getEntityManager();

            Query query = em
                    .createNativeQuery("SELECT sum (CCV1) from RM_ALARM_INSTANCE WHERE ACCESS_POINT_ID between 1001 and 1006 AND ALARM_TYPE = '"
                            + AlarmType.OVER_CONSUMPTION_VOLUME + "'");
            Number alarmInstanceCounterSum = (Number) query.getSingleResult();
            Assert.assertEquals(alarmInstanceCounterSum.intValue(), 0);

            query = em
                    .createNativeQuery("SELECT sum (COUNTER1) from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID between 1001 and 1006");
            Assert.assertEquals(((Number) query.getSingleResult()).intValue(),
                    0);

            query = em
                    .createNativeQuery("SELECT LAST_COMMUNICATION_DATE from MEDINA_USAGE_COUNTER WHERE ACCESS_POINT_ID = 1001 AND PARTITION_ID = 1");
            Assert.assertNull(query.getSingleResult());


            // test magic numbers cache rollback
            cacheTransaction.rollback();

            Assert.assertEquals(TransactionalMagicNumberCache.getInstance()
                    .getCache().size(), cacheSizeBeforeProcessing);
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }

    }

    @Test(groups = { "db" })
    public void testUpdateAccessPLMN() {
        CDRProcessor processor = new CDRProcessor(null);
        Access access = new Access(1L, "OFFER", 1L, "LAST1", new Date(),
                BillingStatusEnum.ACTIVATED, new Date(), null, null, null,
                null, null, null, null);
        processor.updateLastPLMNIfNeeded("LAST2", access, "IDENTIFIER");

        Assert.assertEquals(access.getLastPLMN(), "LAST2");
        Assert.assertEquals(access.getLastPLMNs().size(), 1);
        Assert.assertNotNull(access.getLastPLMNs().get("LAST2"));
        Assert.assertEquals(access.getLastPLMNs().get("LAST2"), "IDENTIFIER");
    }

    @Test(groups = { "db" })
    public void testUpdateOverAndUnderConsumptionAlarms() throws Exception {
        Calendar ticketDate1 = Calendar.getInstance();
        ticketDate1.set(2010, Calendar.JANUARY, 5, 0, 0, 0);
        ticketDate1.set(Calendar.MILLISECOND, 0);
        Calendar ticketDate2 = Calendar.getInstance();
        ticketDate2.set(2010, Calendar.JANUARY, 2, 0, 0, 0);
        ticketDate2.set(Calendar.MILLISECOND, 0);
        Calendar ticketDate3 = Calendar.getInstance();
        ticketDate3.set(2010, Calendar.JANUARY, 3, 0, 0, 0);
        ticketDate3.set(Calendar.MILLISECOND, 0);

        CDR cdr1 = new DATACDRWrapper(new BaseCDR.Builder()
                .addDownloadedDataVolume(1L).addUploadedDataVolume(1L)
                .addServedIMSI("88888").addNodeID("GGSN01")
                .addRecordSequenceNumber("1").addIPBinV4Address("1.1.1.1")
                .addDuration(5000L).addEtatELU("F").addOriginPLMN("20820")
                .addRecordOpeningTime(ticketDate1.getTime())
                .addAccessPointNameNI("iphone").build());
        CDR cdr2 = new DATACDRWrapper(new BaseCDR.Builder()
                .addDownloadedDataVolume(1L).addUploadedDataVolume(1L)
                .addServedIMSI("88888").addNodeID("GGSN01")
                .addRecordSequenceNumber("1").addIPBinV4Address("1.1.1.2")
                .addDuration(5000L).addEtatELU("F").addOriginPLMN("20820")
                .addRecordOpeningTime(ticketDate2.getTime())
                .addAccessPointNameNI("iphone").build());
        CDR cdr3 = new DATACDRWrapper(new BaseCDR.Builder()
                .addDownloadedDataVolume(1L).addUploadedDataVolume(1L)
                .addServedIMSI("88888").addNodeID("GGSN01")
                .addRecordSequenceNumber("1").addIPBinV4Address("1.1.1.2")
                .addDuration(5000L).addEtatELU("F").addOriginPLMN("20820")
                .addRecordOpeningTime(ticketDate3.getTime())
                .addAccessPointNameNI("iphone").build());

        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();
            CDRProcessor processor = new CDRProcessor(null);
            MediationContext context = processor.process(cdr1, CDRType.DATA);
            Assert.assertTrue(context.isAccepted());
            context = processor.process(cdr2, CDRType.DATA);
            Assert.assertTrue(context.isAccepted());
            context = processor.process(cdr3, CDRType.DATA);

            processor.commit(new Date(), new FileProcessingContext());

            Date expectedFirstTicketDate = ticketDate2.getTime();
            Calendar expectedUnchangedDate = Calendar.getInstance();
            expectedUnchangedDate.set(2010, Calendar.FEBRUARY, 1, 0, 0, 0);
            expectedUnchangedDate.set(Calendar.MILLISECOND, 0);

            Query q = em
                    .createQuery("select ai.firstTicketDate from AlarmInstance ai where ai.id = 8001");
            Assert.assertEquals((Date) q.getSingleResult(),
                    expectedFirstTicketDate);

            // q =
            // em.createQuery("select ai.firstTicketDate from AlarmInstance ai where ai.id = 8002");
            // Assert.assertEquals((Date)q.getSingleResult(),
            // expectedFirstTicketDate);

            // because first ticket date is updated only for under and over
            // consumption tickets this one shouldn't be updated
            q = em
                    .createQuery("select ai.firstTicketDate from AlarmInstance ai where ai.id = 8003");
            Assert.assertEquals((Date) q.getSingleResult(),
                    expectedUnchangedDate.getTime());

            // test counters updates
            q = em
                    .createQuery("select ai.ccv1 from AlarmInstance ai where ai.id = 8001");
            Assert.assertEquals((long) (Long) q.getSingleResult(), 6L);

            q = em
                    .createQuery("select ai.ccv1 from AlarmInstance ai where ai.id = 8002");
            Assert.assertEquals((long) (Long) q.getSingleResult(), 6L);

            em.getTransaction().commit();
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

    // @Test(groups = { "db" })
    // public void testUpdatePLMNCounters() {
    // CDR cdr1 = new DATACDRWrapper(new BaseCDR.Builder()
    // .addDownloadedDataVolume(1L).addUploadedDataVolume(1L)
    // .addServedIMSI("111111").addNodeID("GGSN01")
    // .addRecordSequenceNumber("1").addIPBinV4Address("1.1.1.1")
    // .addDuration(5000L).addEtatELU("F").addOriginPLMN("20821")
    // .addRecordOpeningTime(new Date())
    // .addAccessPointNameNI("iphone").build());
    // CDR cdr2 = new DATACDRWrapper(new BaseCDR.Builder()
    // .addDownloadedDataVolume(1L).addUploadedDataVolume(1L)
    // .addServedIMSI("111111").addNodeID("GGSN01")
    // .addRecordSequenceNumber("1").addIPBinV4Address("1.1.1.2")
    // .addDuration(5000L).addEtatELU("F").addOriginPLMN("20822")
    // .addRecordOpeningTime(new Date())
    // .addAccessPointNameNI("iphone").build());
    // CDR cdr3 = new DATACDRWrapper(new BaseCDR.Builder()
    // .addDownloadedDataVolume(1L).addUploadedDataVolume(1L)
    // .addServedIMSI("111111").addNodeID("GGSN01")
    // .addRecordSequenceNumber("1").addIPBinV4Address("1.1.1.2")
    // .addDuration(5000L).addEtatELU("F").addOriginPLMN("20823")
    // .addRecordOpeningTime(new Date())
    // .addAccessPointNameNI("iphone").build());
    //
    // EntityManager em = MedinaPersistence.getEntityManager();
    // em.getTransaction().begin();
    // CDRProcessor processor = new CDRProcessor(null);
    // MediationContext context = processor.process(cdr1, CDRType.DATA);
    // Assert.assertTrue(context.isAccepted());
    // context = processor.process(cdr2, CDRType.DATA);
    // Assert.assertTrue(context.isAccepted());
    // // cdr3 with plmn 20823 should not be updated to counter because it is
    // // not in the alarm (id=1) list.
    // context = processor.process(cdr3, CDRType.DATA);
    // Assert.assertTrue(context.isAccepted());
    // processor.commit(new Date(), new FileProcessingContext());
    // Query query = em
    // .createQuery("SELECT a.ccv1 from AlarmInstance a where a.id = 4001");
    // Assert.assertEquals(((Long) query.getSingleResult()).longValue(), 54L);
    // em.getTransaction().commit();
    // }

    // TODO uncomment it
//    @Test(groups = { "db" })
//    public void testRejectNonUniquePartialTicket() throws Exception {
//        Date recordOpeningTime = new Date();
//        CDR cdr = new VOICECDRWrapper(new BaseCDR.Builder()
//                .addDownloadedDataVolume(1L).addUploadedDataVolume(1L)
//                .addServedIMSI("111111").addServedMSISDN("111").addNodeID(
//                        "GGSN0111").addRecordSequenceNumber("2")
//                .addIPBinV4Address("1.1.1.1").addDuration(5000L)
//                .addEtatELU("P").addOriginPLMN("20821").addRecordOpeningTime(
//                        recordOpeningTime).addAccessPointNameNI("iphone")
//                .addCalledNumber("333333").build());
//        CDR cdrCopy = new VOICECDRWrapper(new BaseCDR.Builder()
//                .addDownloadedDataVolume(1L).addUploadedDataVolume(1L)
//                .addServedIMSI("111111").addServedMSISDN("111").addNodeID(
//                        "GGSN0111").addRecordSequenceNumber("2")
//                .addIPBinV4Address("1.1.1.1").addDuration(5000L)
//                .addEtatELU("P").addOriginPLMN("20821").addRecordOpeningTime(
//                        recordOpeningTime).addAccessPointNameNI("iphone")
//                .addCalledNumber("333333").build());
//
//        try {
//            EntityManager em = MedinaPersistence.getEntityManager();
//            em.getTransaction().begin();
//            CacheTransaction cacheTransaction = TransactionalMagicNumberCache
//                    .getInstance().getTransaction();
//            CDRProcessor processor = new CDRProcessor(cacheTransaction);
//            MediationContext context = processor.process(cdr, CDRType.VOICE);
//            Assert.assertFalse(context.isAccepted());
//            Assert.assertEquals(context.getStatus(), CDRStatus.AGGREGATED);
//            context = processor.process(cdrCopy, CDRType.VOICE);
//            Assert.assertFalse(context.isAccepted());
//            Assert.assertEquals(context.getStatus(), CDRStatus.DUPLICATE);
//            cacheTransaction.rollback();
//            em.getTransaction().rollback();
//        } catch (Exception e) {
//            MedinaPersistence.getEntityManager().getTransaction().rollback();
//            throw e;
//        } finally {
//            MedinaPersistence.closeEntityManager();
//        }
//    }

    @Test(groups = { "db" })
    public void testRejectPartialTicketWithUnknownPA() throws Exception {
        CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
                "0000000").addServedMSISDN("00").addNodeID("GGSN01")
                .addRecordSequenceNumber("1").addIPBinV4Address("1.1.1.1")
                .addDuration(5000L).addEtatELU("F").addOriginPLMN("20821")
                .addRecordOpeningTime(new Date())
                .addAccessPointNameNI("iphone").addCalledNumber("333333")
                .build());

        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();
            CDRProcessor processor = new CDRProcessor(null);
            MediationContext context = processor.process(cdr1, CDRType.VOICE);
            Assert.assertFalse(context.isAccepted());
            Assert.assertEquals(context.getStatus(), CDRStatus.NO_ACCESS);
            em.getTransaction().rollback();
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

    @Test(groups = { "db" })
    public void testLastComunicationDateForSpecificUsagesUpdated()
            throws Exception {
        Calendar recordOpeningTime = Calendar.getInstance();
        recordOpeningTime.set(2009, Calendar.JANUARY, 21, 0, 0, 0);
        recordOpeningTime.set(Calendar.MILLISECOND, 0);
        CDR cdr1 = new DATACDRWrapper(new BaseCDR.Builder()
                .addDownloadedDataVolume(1L).addUploadedDataVolume(1L)
                .addServedIMSI("55555").addNodeID("GGSN01")
                .addRecordSequenceNumber("1").addIPBinV4Address("1.1.1.1")
                .addDuration(5000L).addEtatELU("F").addOriginPLMN("20820")
                .addRecordOpeningTime(recordOpeningTime.getTime())
                .addAccessPointNameNI("iphone").build());

        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();
            CDRProcessor processor = new CDRProcessor(null);
            MediationContext context = processor.process(cdr1, CDRType.DATA);
            Assert.assertTrue(context.isAccepted());
            processor.commit(recordOpeningTime.getTime(),
                    new FileProcessingContext());
            Query q = em
                    .createQuery("select u.lastDATAComunication from UsageCounter u where u.id = 5001");
            Calendar c = Calendar.getInstance();
            c.set(2009, Calendar.JANUARY, 20, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            Date unupdatedDate = c.getTime();
            Assert.assertEquals((Date) q.getSingleResult(), recordOpeningTime
                    .getTime());
            q = em
                    .createQuery("select u.lastOutgoingVOICEComunication from UsageCounter u where u.id = 5001");
            Assert.assertEquals((Date) q.getSingleResult(), unupdatedDate);
            em.getTransaction().commit();
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

    // TODO
//    @Test(groups = { "db" })
//    public void testVOICEPartialTicketUniquenessCheck() {
//        CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
//                "666666").addServedMSISDN("666666").addRecordSequenceNumber(
//                "333").addDuration(5000L).addEtatELU("P")
//                .addOriginPLMN("20820").addRecordOpeningTime(new Date())
//                .addCalledNumber("333333").build());
//        try {
//            // add same magic number to cache
//            CacheTransaction transaction = TransactionalMagicNumberCache
//                    .getInstance().getTransaction();
//            transaction.addToCache(cdr1.getMagicNumber());
//
//            EntityManager em = MedinaPersistence.getEntityManager();
//            em.getTransaction().begin();
//            CDRProcessor processor = new CDRProcessor(transaction);
//            MediationContext context = processor
//                    .process(cdr1, CDRType.DATA);
//            Assert.assertFalse(context.isAccepted());
//            Assert.assertEquals(context.getStatus(), CDRStatus.DUPLICATE);
//        } finally {
//            MedinaPersistence.getEntityManager().getTransaction().rollback();
//        }
//    }

//    @Test(groups = { "db" })
//    public void testOnNetZoneNumbersHaveSameOwner() {
//        CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
//                "55555").addRecordSequenceNumber("1").addCalledNumber("666666")
//                .addDuration(5000L).addEtatELU("F").addOriginPLMN("20820")
//                .addRecordOpeningTime(new Date()).addCDRType("MSO").addIOT(
//                        new BigDecimal("1.1")).addServedMSISDN("55555")
//                .addOnNET("_ON").build());
//
//        CDRProcessor processor = new CDRProcessor(null);
//        MediationContext context = processor.process(cdr1, CDRType.VOICE);
//        Assert.assertTrue(context.isAccepted());
//        Assert.assertFalse(context.getIncoming());
//        Assert.assertEquals(context.getOriginZone(), "5ZONE5");
//        Assert.assertEquals(context.getTargetZone(), "MOBILE_SUPER_ON_NET");
//
//        // same DATA_CSD test
//        context = processor.process(cdr1, CDRType.DATA);
//        Assert.assertTrue(context.isAccepted());
//        Assert.assertFalse(context.getIncoming());
//        Assert.assertEquals(context.getOriginZone(), "5ZONE5");
//        Assert.assertEquals(context.getTargetZone(), "MOBILE_SUPER_ON_NET");
//    }
//
//    @Test(groups = { "db" })
//    public void testOnNetZoneNumbersHaveDifferentOwner() {
//        CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
//                "55555").addRecordSequenceNumber("1").addCalledNumber("77777")
//                .addDuration(5000L).addEtatELU("F").addOriginPLMN("20820")
//                .addRecordOpeningTime(new Date()).addCDRType("MSO").addIOT(
//                        new BigDecimal("1.1")).addServedMSISDN("55555")
//                .addOnNET("_ON").build());
//
//        CDRProcessor processor = new CDRProcessor(null);
//        MediationContext context = processor.process(cdr1, CDRType.VOICE);
//        Assert.assertTrue(context.isAccepted());
//        Assert.assertFalse(context.getIncoming());
//        Assert.assertEquals(context.getOriginZone(), "5ZONE5");
//        Assert.assertEquals(context.getTargetZone(), "MOBILE_ON_NET");
//
//        context = processor.process(cdr1, CDRType.DATA);
//        Assert.assertTrue(context.isAccepted());
//        Assert.assertFalse(context.getIncoming());
//        Assert.assertEquals(context.getOriginZone(), "5ZONE5");
//        Assert.assertEquals(context.getTargetZone(), "MOBILE_ON_NET");
//    }

//    @Test(groups = { "db" })
//    public void testOnNetZoneCalledNumberHasNoLineInSystem() {
//        CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
//                "55555").addRecordSequenceNumber("1")
//                .addCalledNumber("5435435").addDuration(5000L).addEtatELU("F")
//                .addOriginPLMN("20820").addRecordOpeningTime(new Date())
//                .addCDRType("MSO").addIOT(new BigDecimal("1.1"))
//                .addServedMSISDN("55555").addOnNET("_ON").build());
//
//        CDRProcessor processor = new CDRProcessor(null);
//        MediationContext context = processor.process(cdr1, CDRType.VOICE);
//        Assert.assertTrue(context.isAccepted());
//        Assert.assertFalse(context.getIncoming());
//        Assert.assertEquals(context.getOriginZone(), "5ZONE5");
//        Assert.assertEquals(context.getTargetZone(), "MOBILE_ON_NET");
//
//        context = processor.process(cdr1, CDRType.DATA);
//        Assert.assertTrue(context.isAccepted());
//        Assert.assertFalse(context.getIncoming());
//        Assert.assertEquals(context.getOriginZone(), "5ZONE5");
//        Assert.assertEquals(context.getTargetZone(), "MOBILE_ON_NET");
//    }

    @Test(groups = { "db" })
    public void testIncomingTicketZone() {
        CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
                "77777").addRecordSequenceNumber("1").addCalledNumber("77777")
                .addDuration(5000L).addEtatELU("F").addOriginPLMN("20820")
                .addRecordOpeningTime(new Date()).addCDRType("TAPSMSMT").addIOT(
                        new BigDecimal("1.1")).addServedMSISDN("77777")
                .addOnNET("_OFF").addCallingNumber("123456").build());

        CDRProcessor processor = new CDRProcessor(null);
        MediationContext context = processor.process(cdr1, CDRType.VOICE);
        Assert.assertTrue(context.isAccepted());
        Assert.assertTrue(context.getIncoming());
        Assert.assertEquals(context.getOriginZone(),
                "ZONE_FOR_INCOMING_TICKET_7OFFER7");
        Assert.assertEquals(context.getTargetZone(), "7ZONE7");

        // same test for DATA_CSD
        context = processor.process(cdr1, CDRType.VOICE);
        Assert.assertTrue(context.isAccepted());
        Assert.assertTrue(context.getIncoming());
        Assert.assertEquals(context.getOriginZone(),
                "ZONE_FOR_INCOMING_TICKET_7OFFER7");
        Assert.assertEquals(context.getTargetZone(), "7ZONE7");
    }

    /**
     * NO_ACCESS and DUPLICATE reasons is in medina-test.properties file for
     * rejected tickets that need to be retried, so it should be rejected with
     * flag REJECTED_FOR_RETRY
     * 
     * @throws Exception
     */
    @Test(groups = { "db" })
    public void testInsertRejectedTicketsForRetry() throws Exception {
        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();

            Calendar openingTime = Calendar.getInstance();
            openingTime.set(2010, 0, 1, 12, 0, 0);

            RejectedCDR rejected1 = new RejectedCDR();
            rejected1.setFileName("failas1.txt");
            rejected1.setRejectionReason("NO_ACCESS");
            rejected1.setDate(new Date());
            rejected1
                    .setTicketData("ggsnPDPRecord.accessPointNameNI = nameNI\n"
                            + "ggsnPDPRecord.sgsnPLMNIdentifier = 20280\n"
                            + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSUplink = 2\n"
                            + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSDownlink = 1\n"
                            + "ggsnPDPRecord.recordSequenceNumber = 3\n"
                            + "ggsnPDPRecord.servedPDPAddress.iPAddress.iPBinaryAddress.iPBinV4Address = 123.255.255.255\n"
                            + "ggsnPDPRecord.nodeID = sss\n"
                            + "ggsnPDPRecord.recordOpeningTime = 100101120000\n"
                            + "ggsnPDPRecord.duration = 10\n"
                            + "ggsnPDPRecord.chargingID = 5\n"
                            + "ggsnPDPRecord.servedMSISDN = 3333\n"
                            + "ggsnPDPRecord.servedIMSI = 2222\n"
                            + "ggsnPDPRecord.servedIMEISV = 1111\n"
                            + "etatELU = aaa\n"
                            + "ggsnPDPRecord.causeForRecClosing = N\n"
                            + "typeAppeal = GPRS\n" + "ticketID = 1234\n"
                            + "nature = GUE\n");

            CDRProcessor processor = new CDRProcessor(null);
            FileProcessingContext fileProcessingContext = new FileProcessingContext();
            fileProcessingContext.getRejectedCDRs().add(rejected1);
            processor.commit(new Date(), fileProcessingContext);

            em.getTransaction().commit();

            Query q = em
                    .createQuery("select rc from RejectedCDR rc where rc.fileName = 'failas1.txt'");
            RejectedCDR rejectedCDR = (RejectedCDR) q.getSingleResult();
            Assert
                    .assertEquals(
                            rejectedCDR.getTicketData(),
                            "ggsnPDPRecord.accessPointNameNI = nameNI\n"
                                    + "ggsnPDPRecord.sgsnPLMNIdentifier = 20280\n"
                                    + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSUplink = 2\n"
                                    + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSDownlink = 1\n"
                                    + "ggsnPDPRecord.recordSequenceNumber = 3\n"
                                    + "ggsnPDPRecord.servedPDPAddress.iPAddress.iPBinaryAddress.iPBinV4Address = 123.255.255.255\n"
                                    + "ggsnPDPRecord.nodeID = sss\n"
                                    + "ggsnPDPRecord.recordOpeningTime = 100101120000\n"
                                    + "ggsnPDPRecord.duration = 10\n"
                                    + "ggsnPDPRecord.chargingID = 5\n"
                                    + "ggsnPDPRecord.servedMSISDN = 3333\n"
                                    + "ggsnPDPRecord.servedIMSI = 2222\n"
                                    + "ggsnPDPRecord.servedIMEISV = 1111\n"
                                    + "etatELU = aaa\n"
                                    + "ggsnPDPRecord.causeForRecClosing = N\n"
                                    + "typeAppeal = GPRS\n"
                                    + "ticketID = 1234\n" + "nature = GUE\n");

            Assert.assertEquals(rejectedCDR.getRejectedFlag(),
                    RejectedCDRFlag.REJECTED_FOR_RETRY);
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }

    }

    /**
     * NO_ACCESS and DUPLICATE reasons is in medina-test.properties file for
     * rejected tickets that need to be retried, so it should be rejected with
     * flag REJECTED_FOR_RETRY. (The problem is that duplication also has magic
     * number attached).
     * 
     * @throws Exception
     */
    @Test(groups = { "db" })
    public void testInsertRejectedTicketsForRetryDuplication() throws Exception {
        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();

            Calendar openingTime = Calendar.getInstance();
            openingTime.set(2010, 0, 1, 12, 0, 0);

            RejectedCDR rejected1 = new RejectedCDR();
            rejected1.setFileName("failas2.txt");
            rejected1.setRejectionReason("DUPLICATE_1234567890");
            rejected1.setDate(new Date());
            rejected1
                    .setTicketData("ggsnPDPRecord.accessPointNameNI = nameNI\n"
                            + "ggsnPDPRecord.sgsnPLMNIdentifier = 20280\n"
                            + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSUplink = 2\n"
                            + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSDownlink = 1\n"
                            + "ggsnPDPRecord.recordSequenceNumber = 3\n"
                            + "ggsnPDPRecord.servedPDPAddress.iPAddress.iPBinaryAddress.iPBinV4Address = 123.255.255.255\n"
                            + "ggsnPDPRecord.nodeID = sss\n"
                            + "ggsnPDPRecord.recordOpeningTime = 100101120000\n"
                            + "ggsnPDPRecord.duration = 10\n"
                            + "ggsnPDPRecord.chargingID = 5\n"
                            + "ggsnPDPRecord.servedMSISDN = 3333\n"
                            + "ggsnPDPRecord.servedIMSI = 2222\n"
                            + "ggsnPDPRecord.servedIMEISV = 1111\n"
                            + "etatELU = aaa\n"
                            + "ggsnPDPRecord.causeForRecClosing = N\n"
                            + "typeAppeal = GPRS\n" + "ticketID = 1234\n"
                            + "nature = GUE\n");

            CDRProcessor processor = new CDRProcessor(null);
            FileProcessingContext fileProcessingContext = new FileProcessingContext();
            fileProcessingContext.getRejectedCDRs().add(rejected1);
            processor.commit(new Date(), fileProcessingContext);

            em.getTransaction().commit();

            Query q = em
                    .createQuery("select rc from RejectedCDR rc where rc.fileName = 'failas2.txt'");
            RejectedCDR rejectedCDR = (RejectedCDR) q.getSingleResult();
            Assert
                    .assertEquals(
                            rejectedCDR.getTicketData(),
                            "ggsnPDPRecord.accessPointNameNI = nameNI\n"
                                    + "ggsnPDPRecord.sgsnPLMNIdentifier = 20280\n"
                                    + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSUplink = 2\n"
                                    + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSDownlink = 1\n"
                                    + "ggsnPDPRecord.recordSequenceNumber = 3\n"
                                    + "ggsnPDPRecord.servedPDPAddress.iPAddress.iPBinaryAddress.iPBinV4Address = 123.255.255.255\n"
                                    + "ggsnPDPRecord.nodeID = sss\n"
                                    + "ggsnPDPRecord.recordOpeningTime = 100101120000\n"
                                    + "ggsnPDPRecord.duration = 10\n"
                                    + "ggsnPDPRecord.chargingID = 5\n"
                                    + "ggsnPDPRecord.servedMSISDN = 3333\n"
                                    + "ggsnPDPRecord.servedIMSI = 2222\n"
                                    + "ggsnPDPRecord.servedIMEISV = 1111\n"
                                    + "etatELU = aaa\n"
                                    + "ggsnPDPRecord.causeForRecClosing = N\n"
                                    + "typeAppeal = GPRS\n"
                                    + "ticketID = 1234\n" + "nature = GUE\n");

            Assert.assertEquals(rejectedCDR.getRejectedFlag(),
                    RejectedCDRFlag.REJECTED_FOR_RETRY);
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

    /**
     * INVALID_FORMAT reason is not in medina-test.properties file for rejected
     * tickets that need to be retried, so it should be rejected with flag
     * REJECTED_FINALLY.
     * 
     * @throws Exception
     */
    @Test(groups = { "db" })
    public void testInsertRejectedTicketsForFinalRejection() throws Exception {
        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();

            Calendar openingTime = Calendar.getInstance();
            openingTime.set(2010, 0, 1, 12, 0, 0);

            RejectedCDR rejected1 = new RejectedCDR();
            rejected1.setFileName("failas3.txt");
            rejected1.setRejectionReason("INVALID_FORMAT");
            rejected1.setDate(new Date());
            rejected1
                    .setTicketData("ggsnPDPRecord.accessPointNameNI = nameNI\n"
                            + "ggsnPDPRecord.sgsnPLMNIdentifier = 20280\n"
                            + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSUplink = 2\n"
                            + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSDownlink = 1\n"
                            + "ggsnPDPRecord.recordSequenceNumber = 3\n"
                            + "ggsnPDPRecord.servedPDPAddress.iPAddress.iPBinaryAddress.iPBinV4Address = 123.255.255.255\n"
                            + "ggsnPDPRecord.nodeID = sss\n"
                            + "ggsnPDPRecord.recordOpeningTime = 100101120000\n"
                            + "ggsnPDPRecord.duration = 10\n"
                            + "ggsnPDPRecord.chargingID = 5\n"
                            + "ggsnPDPRecord.servedMSISDN = 3333\n"
                            + "ggsnPDPRecord.servedIMSI = 2222\n"
                            + "ggsnPDPRecord.servedIMEISV = 1111\n"
                            + "etatELU = aaa\n"
                            + "ggsnPDPRecord.causeForRecClosing = N\n"
                            + "typeAppeal = GPRS\n" + "ticketID = 1234\n"
                            + "nature = GUE\n");

            CDRProcessor processor = new CDRProcessor(null);
            FileProcessingContext fileProcessingContext = new FileProcessingContext();
            fileProcessingContext.getRejectedCDRs().add(rejected1);
            processor.commit(new Date(), fileProcessingContext);

            em.getTransaction().commit();

            Query q = em
                    .createQuery("select rc from RejectedCDR rc where rc.fileName = 'failas3.txt'");
            RejectedCDR rejectedCDR = (RejectedCDR) q.getSingleResult();
            Assert
                    .assertEquals(
                            rejectedCDR.getTicketData(),
                            "ggsnPDPRecord.accessPointNameNI = nameNI\n"
                                    + "ggsnPDPRecord.sgsnPLMNIdentifier = 20280\n"
                                    + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSUplink = 2\n"
                                    + "ggsnPDPRecord.listOfTrafficVolumes.[0].dataVolumeGPRSDownlink = 1\n"
                                    + "ggsnPDPRecord.recordSequenceNumber = 3\n"
                                    + "ggsnPDPRecord.servedPDPAddress.iPAddress.iPBinaryAddress.iPBinV4Address = 123.255.255.255\n"
                                    + "ggsnPDPRecord.nodeID = sss\n"
                                    + "ggsnPDPRecord.recordOpeningTime = 100101120000\n"
                                    + "ggsnPDPRecord.duration = 10\n"
                                    + "ggsnPDPRecord.chargingID = 5\n"
                                    + "ggsnPDPRecord.servedMSISDN = 3333\n"
                                    + "ggsnPDPRecord.servedIMSI = 2222\n"
                                    + "ggsnPDPRecord.servedIMEISV = 1111\n"
                                    + "etatELU = aaa\n"
                                    + "ggsnPDPRecord.causeForRecClosing = N\n"
                                    + "typeAppeal = GPRS\n"
                                    + "ticketID = 1234\n" + "nature = GUE\n");

            Assert.assertEquals(rejectedCDR.getRejectedFlag(),
                    RejectedCDRFlag.REJECTED_FINALLY);
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

    @Test(groups = { "db" })
    public void testSetFinallyRejectedTicketFlagIfProcessed() throws Exception {
        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();

            CDRProcessor processor = new CDRProcessor(null);
            FileProcessingContext fileProcessingContext = new FileProcessingContext();
            fileProcessingContext.getProcessedRejectedTicketsIds().add(2L);
            processor.commit(new Date(), fileProcessingContext);

            em.getTransaction().commit();

            // finally with id = 2 and not finally rejected with id = 1
            Query q = em
                    .createQuery("select rc.rejectedFlag from RejectedCDR rc where rc.id = 2");
            Assert.assertEquals((RejectedCDRFlag) q.getSingleResult(),
                    RejectedCDRFlag.PROCESSED);
            q = em
                    .createQuery("select rc.rejectedFlag from RejectedCDR rc where rc.id = 1");
            Assert.assertEquals((RejectedCDRFlag) q.getSingleResult(),
                    RejectedCDRFlag.REJECTED_FOR_RETRY);
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

    @Test(groups = { "db" })
    public void testUpdateFailedRejectecetTicketReason() throws Exception {
        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();

            CDRProcessor processor = new CDRProcessor(null);
            FileProcessingContext fileProcessingContext = new FileProcessingContext();
            fileProcessingContext.getFailedRejectedTicketsIds().put(7L,
                    CDRStatus.DUPLICATE);
            processor.commit(new Date(), fileProcessingContext);

            em.getTransaction().commit();

            Query q = em
                    .createQuery("select rc.rejectionReason from RejectedCDR rc where rc.id = 7");
            Assert.assertEquals((String) q.getSingleResult(), "DUPLICATE");
            q = em
                    .createQuery("select rc.rejectionReason from RejectedCDR rc where rc.id = 8");
            Assert.assertEquals((String) q.getSingleResult(), "NO_ACCESS");
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

    @Test(groups = { "db" })
    public void testUpdateCellIdAndInsertHistory() throws Throwable {
        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();
            TransactionalCellCache.getInstance().beginTransaction();

            Calendar cellChangeDate = Calendar.getInstance();
            cellChangeDate.set(2010, Calendar.NOVEMBER, 11, 0, 1, 0);
            cellChangeDate.set(Calendar.MILLISECOND, 0);
            CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
                    "77777").addRecordSequenceNumber("1").addCalledNumber(
                    "77777").addDuration(5000L).addEtatELU("F").addOriginPLMN(
                    "20820").addRecordOpeningTime(new Date()).addCDRType("MSF")
                    .addIOT(new BigDecimal("1.1")).addServedMSISDN("77777")
                    .addOnNET("_OFF").addCallingNumber("12345611").addCellId(
                            "CELL_CHANGED").addCellChangeDate(
                            cellChangeDate.getTime()).build());

            CDRProcessor processor = new CDRProcessor(null);
            MediationContext context = processor.process(cdr1, CDRType.VOICE);
            Assert.assertTrue(context.isAccepted());

            FileProcessingContext fileProcessingContext = new FileProcessingContext();
            processor.commit(new Date(), fileProcessingContext);

            TransactionalCellCache.getInstance().commitTransaction();
            em.getTransaction().commit();

            Query q = em.createQuery("select count(*) from CurrentCell c where c.accessPoint.id = 7001");
            Assert.assertEquals((Long) q.getSingleResult(), (Long)1L);
            
            q = em.createQuery("select c.cellId from CurrentCell c where c.id = 7777");
            Assert.assertEquals((String) q.getSingleResult(), "CELL_CHANGED");
            
            q = em.createQuery("select c.cellChangeDate from CurrentCell c where c.id = 7777");
            Assert.assertEquals((Date) q.getSingleResult(), cellChangeDate.getTime());

            q = em.createQuery("select ch.cellChangeDate from CellHistory ch where ch.cellId = 'CELL_CHANGED'");
            Assert.assertEquals((Date) q.getSingleResult(), cellChangeDate.getTime());
            
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

    @Test(groups = { "db" })
    public void testInsertCellIdAndInsertHistory() throws Throwable {
        try {
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();
            TransactionalCellCache.getInstance().beginTransaction();

            Calendar cellChangeDate = Calendar.getInstance();
            cellChangeDate.set(2010, Calendar.NOVEMBER, 11, 0, 1, 0);
            cellChangeDate.set(Calendar.MILLISECOND, 0);
            CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
                    "666666").addRecordSequenceNumber("1").addCalledNumber(
                    "666666").addDuration(5000L).addEtatELU("F").addOriginPLMN(
                    "20820").addRecordOpeningTime(new Date()).addCDRType("MSF")
                    .addIOT(new BigDecimal("1.1")).addServedMSISDN("666666")
                    .addOnNET("_OFF").addCallingNumber("12345611").addCellId(
                            "NEW_CELL").addCellChangeDate(
                            cellChangeDate.getTime()).build());

            CDRProcessor processor = new CDRProcessor(null);
            MediationContext context = processor.process(cdr1, CDRType.VOICE);
            Assert.assertTrue(context.isAccepted());

            FileProcessingContext fileProcessingContext = new FileProcessingContext();
            processor.commit(new Date(), fileProcessingContext);

            TransactionalCellCache.getInstance().commitTransaction();
            em.getTransaction().commit();

            Query q = em.createQuery("select count(*) from CurrentCell c where c.accessPoint.id = 6001");
            Assert.assertEquals((Long) q.getSingleResult(), (Long)1L);
            
            q = em.createQuery("select c.accessPoint.id from CurrentCell c where c.cellId = 'NEW_CELL'");
            Assert.assertEquals((Long) q.getSingleResult(), (Long) 6001L);
            
            q = em.createQuery("select ch.cellChangeDate from CellHistory ch where ch.cellId = 'NEW_CELL'");
            Assert.assertEquals((Date) q.getSingleResult(), cellChangeDate.getTime());
            
            MedinaPersistence.closeEntityManager();
            
            em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();
            TransactionalCellCache.getInstance().beginTransaction();
            
            cellChangeDate.set(2010, Calendar.NOVEMBER, 11, 0, 2, 0);
            cellChangeDate.set(Calendar.MILLISECOND, 0);
            CDR cdr2 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
                "666666").addRecordSequenceNumber("1").addCalledNumber(
                "666666").addDuration(5000L).addEtatELU("F").addOriginPLMN(
                "20820").addRecordOpeningTime(new Date()).addCDRType("MSF")
                .addIOT(new BigDecimal("1.1")).addServedMSISDN("666666")
                .addOnNET("_OFF").addCallingNumber("12345611").addCellId(
                        "NEW_CELL2").addCellChangeDate(
                        cellChangeDate.getTime()).build());
            
            processor = new CDRProcessor(null);
            context = processor.process(cdr2, CDRType.VOICE);
            Assert.assertTrue(context.isAccepted());

            // second one to update
            fileProcessingContext = new FileProcessingContext();
            processor.commit(new Date(), fileProcessingContext);

            TransactionalCellCache.getInstance().commitTransaction();
            em.getTransaction().commit();

            q = em.createQuery("select count(*) from CurrentCell c where c.accessPoint.id = 6001");
            Assert.assertEquals((Long) q.getSingleResult(), (Long)1L);

            q = em.createQuery("select c.accessPoint.id from CurrentCell c where c.cellId = 'NEW_CELL2'");
            Assert.assertEquals((Long) q.getSingleResult(), (Long) 6001L);
            
            q = em.createQuery("select ch.cellChangeDate from CellHistory ch where ch.cellId = 'NEW_CELL2'");
            Assert.assertEquals((Date) q.getSingleResult(), cellChangeDate.getTime());
            

        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

    @Test(groups = { "db" })
    public void testTicketCellChangeDateBeforeCurrenCellChangeDate()
            throws Throwable {
        try {
            // no cell should be updated
            EntityManager em = MedinaPersistence.getEntityManager();
            em.getTransaction().begin();
            TransactionalCellCache.getInstance().beginTransaction();

            Calendar cellChangeDate = Calendar.getInstance();
            cellChangeDate.set(2010, Calendar.NOVEMBER, 11, 0, 0, 0);
            cellChangeDate.set(Calendar.MILLISECOND, 0);
            CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder().addServedIMSI(
                    "55555").addRecordSequenceNumber("1").addCalledNumber(
                    "55555").addDuration(5000L).addEtatELU("F").addOriginPLMN(
                    "20820").addRecordOpeningTime(new Date()).addCDRType("MSF")
                    .addIOT(new BigDecimal("1.1")).addServedMSISDN("55555")
                    .addOnNET("_OFF").addCallingNumber("12345611").addCellId(
                            "NEW_CELL_BEFORE_CHANGE_DATE").addCellChangeDate(
                            cellChangeDate.getTime()).build());

            CDRProcessor processor = new CDRProcessor(null);
            MediationContext context = processor.process(cdr1, CDRType.VOICE);
            Assert.assertTrue(context.isAccepted());
            TransactionalCellCache.getInstance().commitTransaction();
        } catch (Exception e) {
            MedinaPersistence.getEntityManager().getTransaction().rollback();
            throw e;
        } finally {
            MedinaPersistence.closeEntityManager();
        }
    }

//    @Test(groups = { "db" })
//    public void testProcessDummyPA() throws Exception {
//        Calendar recordOpeningTime = Calendar.getInstance();
//        recordOpeningTime.set(2011, Calendar.OCTOBER, 11, 0, 1, 0);
//        recordOpeningTime.set(Calendar.MILLISECOND, 0);
//
//        Calendar recordClosingTime = Calendar.getInstance();
//        recordClosingTime.set(2011, Calendar.OCTOBER, 12, 0, 1, 0);
//        recordClosingTime.set(Calendar.MILLISECOND, 0);
//
//        CDR cdr1 = new VOICECDRWrapper(new BaseCDR.Builder()
//                .addServedIMSI("02").addNodeID("GGSN01").addCalledNumber("123").addCDRType("MSO")
//                .addRecordSequenceNumber("1").addIPBinV4Address("3.3.3.3")
//                .addDuration(5000L).addEtatELU("F").addOriginPLMN("20820")
//                .addRecordOpeningTime(recordOpeningTime.getTime())
//                .addAccessPointNameNI("iphone").addPdpConnectionStatus("P")
//                .addCauseForRecordClosing("D").addIdnCom("77").addCellId("123")
//                .addPDPIpAddress("11.11.11.11").addRecordClosingTime(
//                        recordClosingTime.getTime()).addCallingNumber("321").build());
//
//        try {
//            EntityManager em = MedinaPersistence.getEntityManager();
//
//            em.getTransaction().begin();
//            CDRProcessor processor = new CDRProcessor(null);
//            MediationContext context = processor.process(cdr1, CDRType.VOICE);
//            Assert.assertTrue(context.isAccepted());
//
//            FileProcessingContext fileProcessingContext = new FileProcessingContext();
//            processor
//                    .commit(recordOpeningTime.getTime(), fileProcessingContext);
//
//            em.getTransaction().commit();
//            
//            Access access = context.getAccess();
//            Assert.assertEquals(access.getOfferCode(), "default_offer");
//            Assert.assertEquals(access.getOfferGroupId(), (Long)90l);
//            Assert.assertEquals(access.getBillingStatus(), BillingStatusEnum.ACTIVATED);
//            Assert.assertEquals(access.getBillingStatus(), BillingStatusEnum.ACTIVATED);
//            Assert.assertEquals(context.getAccessServiceId(), "PA_VOIX");
//            Assert.assertEquals(context.getAccessUserId(), "0");
//            Assert.assertEquals(context.getOriginZone(), "9ZONE");
//            Assert.assertEquals(context.getTargetZone(), "ZONE9");
//            Assert.assertEquals(context.getIncoming(), Boolean.FALSE);
//        } catch (Exception e) {
//            MedinaPersistence.getEntityManager().getTransaction().rollback();
//            throw e;
//        } finally {
//            MedinaPersistence.closeEntityManager();
//        }
//    }

}
