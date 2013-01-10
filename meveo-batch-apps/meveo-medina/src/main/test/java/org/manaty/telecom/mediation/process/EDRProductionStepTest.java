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

import java.util.Date;

import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.model.telecom.mediation.edr.EDR;
import org.manaty.telecom.mediation.context.Access;
import org.manaty.telecom.mediation.context.MediationContext;
import org.manaty.utils.MagicNumberConverter;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for EDRProductionStep.
 * 
 * @author Donatas Remeika
 * @created Mar 16, 2009
 */
public class EDRProductionStepTest {

    @Test(groups = { "unit" })
    public void testEDRProduction() {
        long time = System.currentTimeMillis();
        long duration = 10000;
        CDR cdr = new BaseCDR.Builder().addRecordOpeningTime(new Date(time)).addDuration(duration).addServedIMSI("IMSI1").addServedMSISDN("MSISD1")
                .addDownloadedDataVolume(1000L).addUploadedDataVolume(2000L).build();
        MediationContext context = new MediationContext(cdr, CDRType.DATA, null);
        String magicNumber = "aaa43543265456734563563456";
        context.setMagicNumber(MagicNumberConverter.convertToArray(magicNumber));
        context.setOriginZone("DEST1");
        Access access = new Access(1L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        context.setAccess(access);
        EDRProductionStep step = new EDRProductionStep(null);
        boolean success = step.execute(context);
        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        
        EDR edr = context.getEDR();
        
        Assert.assertNotNull(edr);
        Assert.assertEquals(edr.getId(), magicNumber);
        Assert.assertEquals(edr.getIMSI(), cdr.getIMSI());
        Assert.assertEquals(edr.getMSISDN(), cdr.getMSISDN());
        Assert.assertEquals(edr.getConsumptionDate(), cdr.getRecordOpeningTime());
        Assert.assertEquals(edr.getDownloadVolume(), cdr.getDownloadedDataVolume());
        Assert.assertEquals(edr.getUploadVolume(), cdr.getUploadedDataVolume());
        Assert.assertEquals(edr.getDuration(), Long.valueOf(10000L));
        Assert.assertEquals(edr.getOriginZone(), "DEST1");
        Assert.assertEquals(edr.getAccessPointId(), access.getId());
    }
    
}
