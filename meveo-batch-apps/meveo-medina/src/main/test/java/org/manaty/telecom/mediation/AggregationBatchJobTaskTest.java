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
package org.manaty.telecom.mediation;


/**
 * Tests for ticket aggregation batch job.
 * 
 * @author Ignas Lelys
 * @created 2009.12.24
 */
public class AggregationBatchJobTaskTest {

//    @Test(groups = { "db" })
//    public void testExecute() throws SQLException, UnknownCDRTypeFieldException {
//        Query query = MedinaPersistence.getEntityManager().createQuery("select count(*) from CDRTicket");
//        long numberOfSavedCDRTickets = (Long)query.getSingleResult();
//        
//        AggregationBatchJobTask task = new AggregationBatchJobTask();
//        task.initialize(MedinaPersistence.getEntityManager());
//        List<CDRTicket> finalTickets = task.getAllFinalTickets();
//        Assert.assertEquals(finalTickets.size(), 4);
//        
//        /* final MEDINA_CDR_TICKET with ID = 4 (full partial tickets)*/
//        List<CDRTicket> aggregated = task.aggregate(finalTickets.get(0));
//        Assert.assertEquals(aggregated.size(), 4);
//        CDR cdr = task.joinAggregatedTickets(aggregated);
//        Assert.assertEquals(cdr.getIPBinV4Address(), "192.168.1.1");
//        Assert.assertEquals((long)cdr.getDuration(), 2400L);
//        Assert.assertEquals((long)cdr.getUploadedDataVolume(), 400L);
//        Assert.assertEquals((long)cdr.getDownloadedDataVolume(), 400L);
//        Assert.assertEquals(cdr.getPDPConnectionStatus(), "CLOSED");
//        Assert.assertEquals(cdr.getSequenceNumber(), "0");
//        Assert.assertEquals(cdr.getNature(), "GUE");
//        Assert.assertEquals(cdr.getPDPIpAddress(), "9.9.9.9");
//        Calendar openingTime = Calendar.getInstance();
//        openingTime.set(2009, Calendar.JANUARY, 1, 10, 0, 0);
//        openingTime.set(Calendar.MILLISECOND, 0);
//        Assert.assertEquals(cdr.getRecordOpeningTime(), openingTime.getTime());
//        task.deleteAggregatedTicketsFromDatabase(aggregated);
//        long ticketsLeft = numberOfSavedCDRTickets - aggregated.size();
//        Assert.assertEquals(query.getSingleResult(), ticketsLeft);
//        
//        task = new AggregationBatchJobTask();
//        task.initialize(MedinaPersistence.getEntityManager());
//        /* final MEDINA_CDR_TICKET with ID = 4 (missing partial tickets, but it still should be aggregated because of time limit)*/
//        List<CDRTicket> aggregated2 = task.aggregate(finalTickets.get(1));
//        Assert.assertEquals(aggregated2.size(), 3);
//        CDR cdr2 = task.joinAggregatedTickets(aggregated2);
//        Assert.assertEquals(cdr2.getIPBinV4Address(), "192.168.1.2");
//        Assert.assertEquals((long)cdr2.getDuration(), 2400L);
//        Assert.assertEquals((long)cdr2.getUploadedDataVolume(), 180L);
//        Assert.assertEquals((long)cdr2.getDownloadedDataVolume(), 150L);
//        Assert.assertEquals(cdr2.getSequenceNumber(), "0");
//        Assert.assertEquals(cdr2.getNature(), "GUE");
//        Calendar openingTime2 = Calendar.getInstance();
//        openingTime2.set(2009, Calendar.JANUARY, 1, 10, 30, 0);
//        openingTime2.set(Calendar.MILLISECOND, 0);
//        Assert.assertEquals(cdr2.getRecordOpeningTime(), openingTime2.getTime());
//        task.deleteAggregatedTicketsFromDatabase(aggregated2);
//        ticketsLeft = ticketsLeft - aggregated2.size();
//        Assert.assertEquals(query.getSingleResult(), ticketsLeft);
//        
//        task = new AggregationBatchJobTask();
//        task.initialize(MedinaPersistence.getEntityManager());
//        /* final MEDINA_CDR_TICKET with ID = 9 */
//        List<CDRTicket> aggregated3 = task.aggregate(finalTickets.get(2));
//        Assert.assertEquals(aggregated3.size(), 2);
//        CDR cdr3 = task.joinAggregatedTickets(aggregated3);
//        Assert.assertEquals((long)cdr3.getDuration(), 1200L);
//        Assert.assertEquals(cdr3.getSequenceNumber(), "0");
//        Assert.assertEquals(cdr3.getIOT(), BigDecimal.ZERO);
//        Assert.assertEquals(cdr3.getNature(), "GUE");
//        Assert.assertEquals(cdr3.getCallingNumber(), "6331180");
//        Assert.assertEquals(cdr3.getOperator(), "BYTEL");
//        Assert.assertEquals(cdr3.getSSCode(), "91");
//        Assert.assertEquals(cdr3.getIdnCom(), "111-111");
//        Assert.assertEquals(cdr3.getMSISDN(), "863301110");
//        Assert.assertEquals(cdr3.getCellId(), "66");
//        Calendar cellTime3 = Calendar.getInstance();
//        cellTime3.set(2010, Calendar.NOVEMBER, 10, 0, 0, 0);
//        cellTime3.set(Calendar.MILLISECOND, 0);
//        Assert.assertEquals(cdr3.getCellChangeDate(), cellTime3.getTime());
//        Calendar openingTime3 = Calendar.getInstance();
//        openingTime3.set(2009, Calendar.JANUARY, 1, 10, 30, 0);
//        openingTime3.set(Calendar.MILLISECOND, 0);
//        Assert.assertEquals(cdr3.getRecordOpeningTime(), openingTime3.getTime());
//        Assert.assertEquals(cdr3.getOnNET(), "_ON");
//        Assert.assertEquals(cdr3.getMVNORouting(), "7801");
//        task.deleteAggregatedTicketsFromDatabase(aggregated3);
//        ticketsLeft = ticketsLeft - aggregated3.size();
//        Assert.assertEquals(query.getSingleResult(), ticketsLeft);
//        
//        task = new AggregationBatchJobTask();
//        task.initialize(MedinaPersistence.getEntityManager());
//        /* final MEDINA_CDR_TICKET with ID = 11 */
//        List<CDRTicket> aggregated4 = task.aggregate(finalTickets.get(3));
//        Assert.assertEquals(aggregated4.size(), 1);
//        CDR cdr4 = task.joinAggregatedTickets(aggregated4);
//        Assert.assertEquals((long)cdr4.getDuration(), 600L);
//        Assert.assertEquals(cdr4.getSequenceNumber(), "0");
//        Assert.assertEquals(cdr4.getIOT(), BigDecimal.ZERO);
//        Assert.assertEquals(cdr4.getNature(), "GUE");
//        Assert.assertEquals(cdr4.getCallingNumber(), "62222222");
//        Assert.assertEquals(cdr4.getMSISDN(), "81111111");
//        Assert.assertEquals(cdr4.getOperator(), "TELE2");
//        Calendar openingTime4 = Calendar.getInstance();
//        openingTime4.set(2009, Calendar.JANUARY, 1, 11, 0, 0);
//        openingTime4.set(Calendar.MILLISECOND, 0);
//        Assert.assertEquals(cdr4.getRecordOpeningTime(), openingTime4.getTime());
//        Assert.assertEquals(cdr4.getOnNET(), "_OFF");
//        Assert.assertEquals(cdr4.getMVNORouting(), "7802");
//        task.deleteAggregatedTicketsFromDatabase(aggregated4);
//        ticketsLeft = ticketsLeft - aggregated4.size();
//        Assert.assertEquals(query.getSingleResult(), ticketsLeft);
//    }
//    
//	@Test(groups = { "unit" })
//	public void testJoinAggregatedTicketsWithoutTicketsWithSequenceNumber1() throws UnknownCDRTypeFieldException {
//		AggregationBatchJobTask task = new AggregationBatchJobTask();
//		CDRTicket cdr1 = new CDRTicket();
//		cdr1.setUploadedDataVolume(123L);
//		cdr1.setDownloadedDataVolume(321L);
//		cdr1.setDuration(120L);
//		Calendar openingTime = Calendar.getInstance();
//		openingTime.set(2010, Calendar.JANUARY, 1, 21, 0, 0);
//		openingTime.set(Calendar.MILLISECOND, 0);
//		cdr1.setRecordOpeningTime(openingTime.getTime());
//		Calendar closingTime = Calendar.getInstance();
//		closingTime.set(2010, Calendar.JANUARY, 1, 21, 2, 0);
//		closingTime.set(Calendar.MILLISECOND, 0);
//		cdr1.setRecordClosingTime(closingTime.getTime());
//		cdr1.setSequenceNumber("2");
//		cdr1.setEtatELU("P");
//		cdr1.setCdrType("GPRS");
//		CDRTicket cdr2 = new CDRTicket();
//		cdr2.setUploadedDataVolume(123L);
//		cdr2.setDownloadedDataVolume(321L);
//		cdr2.setDuration(240L);
//		openingTime = Calendar.getInstance();
//		openingTime.set(2010, Calendar.JANUARY, 1, 21, 2, 0);
//		cdr2.setRecordOpeningTime(openingTime.getTime());
//		closingTime = Calendar.getInstance();
//		closingTime.set(2010, Calendar.JANUARY, 1, 21, 6, 0);
//		closingTime.set(Calendar.MILLISECOND, 0);
//		cdr2.setRecordClosingTime(closingTime.getTime());		
//		cdr2.setSequenceNumber("3");
//		cdr2.setEtatELU("F");
//		cdr2.setCdrType("GPRS");
//		
//		List<CDRTicket> aggregated = new ArrayList<CDRTicket>();
//		
//		aggregated.add(cdr1);
//		aggregated.add(cdr2);
//		CDR cdr = task.joinAggregatedTickets(aggregated);
//		
//		Calendar expectedOpeningTime = Calendar.getInstance();
//		expectedOpeningTime.set(2010, Calendar.JANUARY, 1, 21, 0, 0);
//		expectedOpeningTime.set(Calendar.MILLISECOND, 0);
//		Assert.assertEquals(cdr.getRecordOpeningTime(), expectedOpeningTime.getTime());
//		Assert.assertEquals((long)cdr.getDownloadedDataVolume(), 642L);
//		Assert.assertEquals((long)cdr.getUploadedDataVolume(), 246L);
//		Assert.assertEquals((long)cdr.getDuration(), 360L);
//	}

}
