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
import java.util.Date;

import org.manaty.model.resource.telecom.BillingStatusEnum;
import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.model.telecom.mediation.cdr.DATACDRWrapper;
import org.manaty.telecom.mediation.context.MediationContext;
import org.manaty.utils.CDRUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for IPAddressAccessStepTest.
 * 
 * @author Ignas
 *
 */
public class IPAddressAccessStepTest {
	
	@Test(groups = { "db" })
	public void testFindAccess() {
		IPAddressAccessStep step = new IPAddressAccessStep(null);
        CDR cdr = new DATACDRWrapper(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_DATA).addIPBinV4Address("1.1.1.1").build());
        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        boolean success = step.execute(context);
        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertNotNull(context.getAccess());
        Assert.assertEquals(context.getAccess().getId(), Long.valueOf(9009L));
        Assert.assertEquals(context.getAccess().getOfferCode(), "OFFER9009");
        Assert.assertEquals(context.getAccess().getLastPLMN(), "20820");
        Calendar activationAndStatusDate = Calendar.getInstance();
        activationAndStatusDate.set(2009, Calendar.MARCH, 1, 0, 0, 0);
        activationAndStatusDate.set(Calendar.MILLISECOND, 0);
        Date time = activationAndStatusDate.getTime();
        Assert.assertEquals(context.getAccess().getActivationDate(), time);
        Assert.assertEquals(context.getAccess().getBillingStatus(), BillingStatusEnum.ACTIVATED);
        Calendar activationDate = Calendar.getInstance();
        activationDate.set(2009, Calendar.MARCH, 1);
        Assert.assertEquals(context.getAccess().getBillingStatusDate(), time);
        Assert.assertEquals(context.getAccess().getOfferGroupId().longValue(), 1L);
	}
	
}
