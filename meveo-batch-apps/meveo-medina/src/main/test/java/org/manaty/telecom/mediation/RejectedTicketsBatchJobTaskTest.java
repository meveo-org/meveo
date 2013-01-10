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

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for ticket rejected tickets batch job.
 * 
 * @author Ignas Lelys
 * @created 2009.12.24
 */
public class RejectedTicketsBatchJobTaskTest {

	@Test(groups = { "db" })
    public void testGetTicketsToRetry() throws SQLException {
    	EntityManager em = MedinaPersistence.getEntityManager();
    	RejectedTicketsBatchJobTask task = new RejectedTicketsBatchJobTask();
    	task.initialize(em, true);
    	Calendar thresholdCal = Calendar.getInstance();
    	thresholdCal.set(2010, Calendar.JANUARY, 31, 0, 0);
    	List<Object[]> tickets = task.getTicketsToRetry(new Timestamp(thresholdCal.getTimeInMillis()));
//    	Assert.assertEquals(tickets.size(), 4);
    	Assert.assertEquals(tickets.get(0)[1], "ticket3");
    	Assert.assertEquals(tickets.get(1)[1], "ticket4");
    	Assert.assertEquals(tickets.get(2)[1], "manualRetryTicket1");
    	Assert.assertEquals(tickets.get(3)[1], "manualRetryTicket2");
    }

    @Test(groups = { "db" })
    public void testGetTicketsToReject() throws SQLException {
    	EntityManager em = MedinaPersistence.getEntityManager();
    	RejectedTicketsBatchJobTask task = new RejectedTicketsBatchJobTask();
    	task.initialize(em, true);
    	Calendar thresholdCal = Calendar.getInstance();
    	thresholdCal.set(2010, Calendar.JANUARY, 31, 0, 0);
    	List<Object[]> tickets = task.getTicketsToReject(new Timestamp(thresholdCal.getTimeInMillis()));
    	Assert.assertEquals(tickets.size(), 2);
    	Assert.assertEquals(tickets.get(0)[1], "ticket1");
    	Assert.assertEquals(tickets.get(1)[1], "ticket2");
    }
    
    @Test(groups = { "unit" })
	public void testGetThresholdDate() {
    	Calendar thresholdCal = Calendar.getInstance();
    	thresholdCal.set(2010, Calendar.JANUARY, 4, 0, 0, 0);
    	thresholdCal.set(Calendar.MILLISECOND, 0);
    	RejectedTicketsBatchJobTask task = new RejectedTicketsBatchJobTask();
    	
    	Timestamp thresholdTimestamp = task.getThresholdDate(thresholdCal.getTimeInMillis());
    	
    	Calendar expectedValue = Calendar.getInstance();
    	expectedValue.set(2010, Calendar.JANUARY, 1, 0, 0, 0);
    	expectedValue.set(Calendar.MILLISECOND, 0);
    	
    	Assert.assertEquals(expectedValue.getTime(), new Date(thresholdTimestamp.getTime()));
	}
    
}
