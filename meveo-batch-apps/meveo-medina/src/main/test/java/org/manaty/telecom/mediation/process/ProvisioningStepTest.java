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
import org.manaty.telecom.mediation.context.Access;
import org.manaty.telecom.mediation.context.MediationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for ProvisioningStep.
 * 
 * @author Ignas
 * @created Apr 30, 2009
 */
public class ProvisioningStepTest {
	
	@Test(groups = { "db" })
    public void testProvisioning() {
        
        long accessPointId = 2001L;
        String offerCode = "TEST_INGENICO_BY_PA_OFFER_CODE";
        
        Calendar activationDate = Calendar.getInstance();
        activationDate.set(2009, Calendar.JANUARY, 1);
        Access access = new Access(accessPointId, offerCode, 2222L, null, activationDate.getTime(),
                BillingStatusEnum.ACTIVATED, activationDate.getTime(), null, null, null, null, null, null, null);
        
        Calendar consuptionDate = Calendar.getInstance();
        consuptionDate.set(2009, Calendar.MARCH, 31);
        CDR cdr = new BaseCDR.Builder().addOriginPLMN("20821").addRecordOpeningTime(consuptionDate.getTime()).build();
        
        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        context.setAccess(access);
        context.setOriginZone("TEST_INGENICO_BY_PA_ZONE");

        ProvisioningStep step = new ProvisioningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertEquals(context.getAccessUserId(), "2222");
        Assert.assertEquals(context.getAccessServiceId(), "PA_DATA");
        
    }
    
}
