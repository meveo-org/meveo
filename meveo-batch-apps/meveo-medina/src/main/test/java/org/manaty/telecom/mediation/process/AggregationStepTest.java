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
import org.manaty.model.telecom.mediation.cdr.CDRStatus;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.model.telecom.mediation.cdr.DATACDRWrapper;
import org.manaty.model.telecom.mediation.cdr.SMSCDRWrapper;
import org.manaty.model.telecom.mediation.cdr.VOICECDRWrapper;
import org.manaty.telecom.mediation.context.MediationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for aggregation step.
 * 
 * @author Ignas Lelys
 * @created 2009.12.18
 */
public class AggregationStepTest {

    @Test(groups = { "unit" })
    public void testFinishedTicket() {

        AggregationStep step = new AggregationStep(null);
        CDR cdr = new BaseCDR.Builder().addDownloadedDataVolume(100L).addUploadedDataVolume(1L).addOriginPLMN(
                "20820").addServedIMSI("100001").addNodeID("GGSN1")
                .addRecordSequenceNumber("1").addIPBinV4Address("101.23.78.1").addDuration(
                        10000L).addRecordOpeningTime(new Date()).addEtatELU("F").build();
        MediationContext context = new MediationContext(new VOICECDRWrapper(cdr), CDRType.DATA, null);
        boolean success = step.execute(context);
        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertFalse(context.getStatus() == CDRStatus.AGGREGATED);
    }
    
    @Test(groups = { "unit" })
    public void testPartialTickets() {

        AggregationStep step = new AggregationStep(null);
        CDR cdr1 = new BaseCDR.Builder().addOriginPLMN(
                "20820").addServedIMSI("100001").addNodeID("GGSN1")
                .addRecordSequenceNumber("1").addIPBinV4Address("101.23.78.1").addDuration(
                        10000L).addRecordOpeningTime(new Date()).addEtatELU("P").addCalledNumber("333333").build();
        MediationContext context1 = new MediationContext(new SMSCDRWrapper(cdr1), CDRType.SMS, null);
        boolean success = step.execute(context1);
        Assert.assertFalse(success);
        Assert.assertFalse(context1.isAccepted());
        Assert.assertTrue(context1.getStatus() == CDRStatus.AGGREGATED);
        
        CDR cdr2 = new BaseCDR.Builder().addOriginPLMN(
                "20820").addServedIMSI("100001").addNodeID("GGSN1")
                .addRecordSequenceNumber("2").addIPBinV4Address("101.23.78.1").addDuration(
                        10000L).addRecordOpeningTime(new Date()).addEtatELU("F").addCalledNumber("333333").build();
        MediationContext context2 = new MediationContext(new VOICECDRWrapper(cdr2), CDRType.VOICE, null);
        success = step.execute(context2);
        Assert.assertFalse(success);
        Assert.assertFalse(context2.isAccepted());
        Assert.assertTrue(context2.getStatus() == CDRStatus.AGGREGATED);
    }
    
    @Test(groups = { "unit" })
    public void testDATAPartialTicketsNotAggregated() {

        AggregationStep step = new AggregationStep(null);
        CDR cdr1 = new BaseCDR.Builder().addOriginPLMN(
                "20820").addServedIMSI("100001").addNodeID("GGSN1")
                .addRecordSequenceNumber("1").addIPBinV4Address("101.23.78.1").addDuration(
                        10000L).addRecordOpeningTime(new Date()).addEtatELU("P").addCalledNumber("333333").build();
        MediationContext context1 = new MediationContext(new DATACDRWrapper(cdr1), CDRType.DATA, null);
        boolean success = step.execute(context1);
        Assert.assertTrue(success);
        Assert.assertTrue(context1.isAccepted());
        Assert.assertTrue(context1.getStatus() != CDRStatus.AGGREGATED);
    }
    
}
