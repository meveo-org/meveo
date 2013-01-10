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

import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRStatus;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.telecom.mediation.context.MediationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for CountingStep.
 * 
 * @author Ignas Lelys
 * @created Mar 12, 2009
 * 
 */
public class CountingStepTest {

    @Test(groups = { "unit" })
    public void testAddUsageCount() {
        CDR cdr = new BaseCDR.Builder().addDownloadedDataVolume(10L).addUploadedDataVolume(11L).build();
        MediationContext context = new MediationContext(cdr, CDRType.DATA, null);
        Assert.assertNull(context.getUsageCount());
        ProcessStep step = new CountingStep(null);
        step.process(context);
        Assert.assertNotNull(context.getUsageCount());
        Assert.assertEquals(context.getUsageCount().getUsage(), CDRType.DATA);
        Assert.assertEquals(context.getUsageCount().getCount(), (Long)21L);
        Assert.assertEquals(context.getUsageCount().getCountUp(), (Long)11L);
        Assert.assertEquals(context.getUsageCount().getCountDown(), (Long)10L);
    }
    
    @Test(groups = { "unit" })
    public void testIgnoreTicket() {
        CDR cdr = new BaseCDR.Builder().addDownloadedDataVolume(0L).addUploadedDataVolume(0L).build();
        MediationContext context = new MediationContext(cdr, CDRType.DATA, null);
        Assert.assertNull(context.getUsageCount());
        ProcessStep step = new CountingStep(null);
        step.process(context);
        Assert.assertNull(context.getUsageCount());
        Assert.assertEquals(context.getStatus(), CDRStatus.IGNORED);
    }

//    /**
//     * VOICE SSP tickets shouldnt be ignored
//     */
//    @Test(groups = { "unit" })
//    public void testIgnoreVOICESSPTicket() {
//        CDR cdr = new BaseCDR.Builder().addDuration(0L).addCDRType("SSP").build();
//        MediationContext context = new MediationContext(cdr, CDRType.VOICE, null);
//        Assert.assertNull(context.getUsageCount());
//        ProcessStep step = new CountingStep(null);
//        step.process(context);
//        // not ignored
//        Assert.assertEquals(context.getStatus(), CDRStatus.ONGOING);
//    }
}
