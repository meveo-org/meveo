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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.manaty.model.resource.telecom.BillingStatusEnum;
import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRStatus;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.model.telecom.mediation.cdr.DATACDRWrapper;
import org.manaty.telecom.mediation.ConfigurationException;
import org.manaty.telecom.mediation.context.Access;
import org.manaty.telecom.mediation.context.MediationContext;
import org.manaty.utils.CDRUtils;
import org.manty.mock.MockPreparedStatement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for Access step.
 * 
 * @author Donatas Remeika
 * @created Mar 13, 2009
 */
public class AccessStepTest {
    
    @Test(groups = { "db" })
    public void testAccessStepFindByIMSI() {
        AccessStep step = new AccessStep(null);
        CDR cdr = new DATACDRWrapper(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_DATA).addServedIMSI("1111").build());
        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        boolean success = step.execute(context);
        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertNotNull(context.getAccess());
        Assert.assertEquals(context.getAccess().getId(), Long.valueOf(1L));
        Assert.assertEquals(context.getAccess().getOfferCode(), "1111");
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
        Assert.assertEquals(context.getAccess().getPreviousOfferGroupId().longValue(), 12345L);
        Calendar previousOfferGoupUpdateDate = Calendar.getInstance();
        previousOfferGoupUpdateDate.set(2009, Calendar.FEBRUARY, 1, 0, 0, 0);
        previousOfferGoupUpdateDate.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(context.getAccess().getPreviousOfferGroupUpdate(), previousOfferGoupUpdateDate.getTime());
    }

    @Test(groups = { "db" })
    public void testAccessStepFindByMSISDN() {
        AccessStep step = new AccessStep(null);
        CDR cdr = new DATACDRWrapper(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_DATA).addServedMSISDN("1111").build());
        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        boolean success = step.execute(context);
        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertNotNull(context.getAccess());
        Assert.assertEquals(context.getAccess().getId(), Long.valueOf(2L));
        Assert.assertEquals(context.getAccess().getOfferCode(), "2222");
        Assert.assertEquals(context.getAccess().getLastPLMN(), "20821");
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
        Assert.assertEquals(context.getAccess().getPreviousOfferGroupId().longValue(), 12345L);
        Calendar previousOfferGoupUpdateDate = Calendar.getInstance();
        previousOfferGoupUpdateDate.set(2009, Calendar.FEBRUARY, 1, 0, 0, 0);
        previousOfferGoupUpdateDate.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(context.getAccess().getPreviousOfferGroupUpdate(), previousOfferGoupUpdateDate.getTime());
    }
    
    
    @Test(groups = { "db" })
    public void testAccessStepFindByPreviousMSISDN() {
        AccessStep step = new AccessStep(null);
        CDR cdr = new DATACDRWrapper(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_DATA).addServedMSISDN("3333").build());
        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        boolean success = step.execute(context);
        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertNotNull(context.getAccess());
        Assert.assertEquals(context.getAccess().getId(), Long.valueOf(44L));
        Assert.assertEquals(context.getAccess().getOfferCode(), "9999");
        Assert.assertEquals(context.getAccess().getLastPLMN(), "20821");
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
        Assert.assertEquals(context.getAccess().getPreviousOfferGroupId().longValue(), 12345L);
        Calendar previousOfferGoupUpdateDate = Calendar.getInstance();
        previousOfferGoupUpdateDate.set(2009, Calendar.FEBRUARY, 1, 0, 0, 0);
        previousOfferGoupUpdateDate.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(context.getAccess().getPreviousOfferGroupUpdate(), previousOfferGoupUpdateDate.getTime());
    }
    
//    @Test(groups = { "db" })
//    public void testAccessStepFindByMSISDNWhenNotFoundByIMSI() {
//        AccessStep step = new AccessStep(null);
//        ASN1CDR cdr = new ASN1CDR();
//        ContexteAppel contexteAppel = new ContexteAppel();
//        cdr.setContexteAppel(contexteAppel);
//        contexteAppel.setIMSI("1234");
//        contexteAppel.setMSISDN("1111");
//        MediationContext context = new MediationContext(new ASN1DATAWrapper(cdr), CDRType.DATA, new CDRProcessor(null));
//        boolean success = step.execute(context);
//        Assert.assertTrue(success);
//        Assert.assertTrue(context.isAccepted());
//        Assert.assertNotNull(context.getAccess());
//        Assert.assertEquals(context.getAccess().getId(), Long.valueOf(2L));
//        Assert.assertEquals(context.getAccess().getOfferCode(), "2222");
//        Assert.assertEquals(context.getAccess().getLastPLMN(), "20821");
//        Calendar activationAndStatusDate = Calendar.getInstance();
//        activationAndStatusDate.set(2009, Calendar.MARCH, 1, 0, 0, 0);
//        activationAndStatusDate.set(Calendar.MILLISECOND, 0);
//        Date time = activationAndStatusDate.getTime();
//        Assert.assertEquals(context.getAccess().getActivationDate(), time);
//        Assert.assertEquals(context.getAccess().getBillingStatus(), BillingStatusEnum.ACTIVATED);
//        Calendar activationDate = Calendar.getInstance();
//        activationDate.set(2009, Calendar.MARCH, 1);
//        Assert.assertEquals(context.getAccess().getBillingStatusDate(), time);
//        Assert.assertEquals(context.getAccess().getOfferGroupId().longValue(), 1L);
//        Assert.assertEquals(context.getAccess().getPreviousOfferGroupId().longValue(), 12345L);
//        Calendar previousOfferGoupUpdateDate = Calendar.getInstance();
//        previousOfferGoupUpdateDate.set(2009, Calendar.FEBRUARY, 1, 0, 0, 0);
//        previousOfferGoupUpdateDate.set(Calendar.MILLISECOND, 0);
//        Assert.assertEquals(context.getAccess().getPreviousOfferGroupUpdate(), previousOfferGoupUpdateDate.getTime());
//    }

    @Test(groups = { "db" })
    public void testAccessStepInvalidIMSI() {
        AccessStep step = new AccessStep(null);
        CDR cdr = new DATACDRWrapper(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_DATA).addServedMSISDN("99999").build());
        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        boolean success = step.execute(context);
        Assert.assertFalse(success);
        Assert.assertFalse(context.isAccepted());
        Assert.assertEquals(CDRStatus.NO_ACCESS, context.getStatus());
        Assert.assertNull(context.getAccess());
    }
    
    @Test(groups = { "db" })
    public void testAccessStepLineNotAllocated() {
        AccessStep step = new AccessStep(null);
        CDR cdr = new DATACDRWrapper(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_DATA).addServedIMSI("2222").build());
        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        boolean success = step.execute(context);
        Assert.assertFalse(success);
        Assert.assertFalse(context.isAccepted());
        Assert.assertEquals(CDRStatus.NO_ACCESS, context.getStatus());
        Assert.assertNull(context.getAccess());
    }
    
    @Test(groups = { "unit" }, expectedExceptions = { ConfigurationException.class })
    public void testAccessStepThrowsConfigurationException() {
        AccessStep step = new AccessStep(null);
        PreparedStatement statement = new MockPreparedStatement() {

            @Override
            public void setString(int parameterIndex, String x) throws SQLException {
                throw new SQLException("TEST");
            }
        };
        step.getAccess(Collections.<Long, Access> emptyMap(), Collections.<String, Access> emptyMap(), "236589754",
                statement);
    }
    
    
}
