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

import java.util.Calendar;

import org.manaty.model.resource.telecom.BillingStatusEnum;
import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.telecom.mediation.cache.TransactionalCellCache;
import org.manaty.telecom.mediation.context.Access;
import org.manaty.telecom.mediation.context.MediationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for CountingStep.
 * 
 * @author Ignas Lelys
 * @created JAN 31, 2011
 * 
 */
public class CellManagementStepTest {

	@Test(groups = { "db" })
	public void testAddUsageCount() {
		Calendar cal = Calendar.getInstance();
		cal.set(2011, Calendar.JANUARY, 31, 1, 10, 0);
		cal.set(Calendar.MILLISECOND, 0);
		CDR cdr = new BaseCDR.Builder().addDownloadedDataVolume(10L)
				.addUploadedDataVolume(11L).addCellChangeDate(cal.getTime())
				.addCellId("CELL_2").addServedIMSI("1111").build();
		TransactionalCellCache.getInstance().beginTransaction();
		MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
		Access access = new Access(1L, "offer", 1L, null, cal.getTime(), BillingStatusEnum.ACTIVATED, cal.getTime(), 
				null, null, null, null, null, 1L, null);
		context.setAccess(access);
		Assert.assertNull(context.getUsageCount());
		ProcessStep step = new CellManagementStep(null);
		step.process(context);
		Assert.assertTrue(context.isAccepted());
		TransactionalCellCache.getInstance().rollbackTransaction();
	}

}
