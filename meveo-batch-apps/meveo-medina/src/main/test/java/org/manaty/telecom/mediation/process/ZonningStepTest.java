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

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.manaty.model.mediation.NumberingPlan;
import org.manaty.model.mediation.ZonningPlan;
import org.manaty.model.mediation.ZonningPlan.CDRTypeEnum;
import org.manaty.model.mediation.ZonningPlan.DirectionEnum;
import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.model.telecom.mediation.cdr.SMSCDRWrapper;
import org.manaty.model.telecom.mediation.cdr.VOICECDRWrapper;
import org.manaty.telecom.mediation.ConfigurationException;
import org.manaty.telecom.mediation.cache.SimpleNumberingPlanCache;
import org.manaty.telecom.mediation.cache.SimpleNumberingPlanCache.Key;
import org.manaty.telecom.mediation.context.Access;
import org.manaty.telecom.mediation.context.MediationContext;
import org.manty.mock.MockPreparedStatement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for Zonning Step.
 * 
 * @author Donatas Remeika
 * @created Mar 14, 2009
 */
public class ZonningStepTest {

    @Test(groups = { "db" })
    public void testFindOriginAndTargetZoneForDATAUsage() {
        Access access = new Access(1L, "OFFER1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new BaseCDR.Builder().addOriginPLMN("ZONE1").build();

        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertNotNull(context.getOriginZone());
        Assert.assertNotNull(context.getTargetZone());
        Assert.assertEquals(context.getOriginZone(), context.getTargetZone());
        Assert.assertEquals(context.getOriginZone(), "DEST1");
    }
    
    @Test(groups = { "db" })
    public void testFindOriginAndTargetZoneForVOICEIncomingTicket() {
        Access access = new Access(1L, "7OFFER7", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new VOICECDRWrapper(new BaseCDR.Builder().addCDRType("MSF").addOriginPLMN("20820").addCallingNumber("123456").build());

        MediationContext context = new MediationContext(cdr, CDRType.VOICE, new CDRProcessor(null));
        context.setIncoming(true);
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertEquals(context.getOriginZone(), "ZONE_FOR_INCOMING_TICKET_7OFFER7");
        Assert.assertEquals(context.getTargetZone(), "7ZONE7");
    }

    @Test(groups = { "db" })
    public void testFindOriginZoneForWithCDRTypeAndDirection() {
        Access access = new Access(1L, "TYPEDIRECTIONOFFER", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new BaseCDR.Builder().addOriginPLMN("20821").addCalledNumber("123").addCallingNumber("321").build();

        MediationContext context = new MediationContext(cdr, CDRType.SMS, new CDRProcessor(null));
        context.setIncoming(true);
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertEquals(context.getTargetZone(), "SMS_INCOMING_ZONE");
        
        MediationContext context2 = new MediationContext(cdr, CDRType.VOICE, new CDRProcessor(null));
        context2.setIncoming(false);
        context2.setAccess(access);
        
        step = new ZonningStep(null);
        success = step.execute(context2);

        Assert.assertTrue(success);
        Assert.assertTrue(context2.isAccepted());
        Assert.assertEquals(context2.getOriginZone(), "VOICE_OUTGOING_ZONE");
        
        MediationContext context3 = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        context3.setIncoming(false);
        context3.setAccess(access);
        
        step = new ZonningStep(null);
        success = step.execute(context3);

        Assert.assertTrue(success);
        Assert.assertTrue(context3.isAccepted());
        Assert.assertEquals(context3.getOriginZone(), "DATA_ZONE");

    }

    @Test(groups = { "db" })
    public void testFindOriginAndTargetZoneForDATAUsageWithDefaultPLMN() {
        Access access = new Access(1L, "OFFER1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new BaseCDR.Builder().addOriginPLMN(null).build();

        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertNotNull(context.getOriginZone());
        Assert.assertNotNull(context.getTargetZone());
        Assert.assertEquals(context.getOriginZone(), context.getTargetZone());
        Assert.assertEquals(context.getOriginZone(), "DEST1");
    }

    @Test(groups = { "db" })
    public void testFindOriginAndTargetZoneNotExists() {
        Access access = new Access(1L, "OFFER1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new BaseCDR.Builder().addOriginPLMN("ZONE2").build();

        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertEquals(context.getOriginZone(), ZonningStep.DEFAULT_ZONE);
        Assert.assertEquals(context.getTargetZone(), ZonningStep.DEFAULT_ZONE);
    }
    
    @Test(groups = { "db" })
    public void testFindPlmnNotExists() {
        Access access = new Access(1L, "OFFER1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new BaseCDR.Builder().build();

        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertEquals(context.getOriginPlmn(), "20820");
    }
    
    @Test(groups = { "db" })
    public void testFindPlmn() {
        Access access = new Access(1L, "OFFER1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new BaseCDR.Builder().addOriginPLMN("ORIGIN_PLMN").build();

        MediationContext context = new MediationContext(cdr, CDRType.DATA, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertEquals(context.getOriginPlmn(), "ORIGIN_PLMN");
    }

    @Test(groups = { "unit" })
    public void testCacheSizeCacheClears() {
        ZonningStep step = new ZonningStep(null);
        for (int i = 0; i <= 100; i++) {
            step.putToCache(String.valueOf(i), new ZonningPlan());
        }
        ZonningPlan zone0 = step.getFromCache("0");
        Assert.assertNotNull(zone0);

        ZonningPlan zone100 = step.getFromCache(String.valueOf(100));
        Assert.assertNotNull(zone100);
        ZonningStep.clearCache();
        zone100 = step.getFromCache(String.valueOf(100));
        Assert.assertNull(zone100);
    }

    @Test(groups = { "unit" })
    public void testGetZoneWithNotEnoughData() {
        ZonningStep step = new ZonningStep(null);

        ZonningPlan zonningPlan = step.getZone(null, "OFFER", CDRTypeEnum.DATA, DirectionEnum.INCOMING, null);
        Assert.assertNull(zonningPlan);

        zonningPlan = step.getZone("PLMN", null, CDRTypeEnum.DATA, DirectionEnum.INCOMING, null);
        Assert.assertNull(zonningPlan);
    }
    
    @Test(groups = { "unit" }, expectedExceptions = { ConfigurationException.class })
    public void testGetZoneThrowsConfigurationException() {
        PreparedStatement statement = new MockPreparedStatement() {

            @Override
            public void setString(int parameterIndex, String x) throws SQLException {
                throw new SQLException("TEST");
            }
            
        };
        
        ZonningStep step = new ZonningStep(null);
        ZonningPlan zonningPlan = step.getZone("PLMN", "OFFER", CDRTypeEnum.DATA, DirectionEnum.INCOMING, statement);
        Assert.assertNull(zonningPlan);

    }
    
    @Test(groups = { "db" })
    public void testFindTargetZoneForVOICEOnNET() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        
        final Field orderedRegexpsFields = SimpleNumberingPlanCache.class.getDeclaredField("orderedRegexpsByOfferCode");
        final Field numberingPlansByPhoneRegexpField = SimpleNumberingPlanCache.class.getDeclaredField("numberingPlansByPhoneRegexp");
        orderedRegexpsFields.setAccessible(true);
        numberingPlansByPhoneRegexpField.setAccessible(true);
        
        Map<Key, NumberingPlan> plans = new HashMap<Key, NumberingPlan>();
        
        NumberingPlan sample = new NumberingPlan();
        sample.setPhoneNumberRegexp("9[0-9]{6}");
        sample.setZoneId("S2");
        Key key = new Key("9[0-9]{6}", "OFFER1", "VOICE", true);
        plans.put(key, sample);
        
        NumberingPlan sample2 = new NumberingPlan();
        sample2.setPhoneNumberRegexp("9[0-9]{6}");
        sample2.setZoneId("S4");
        Key key2 = new Key("2222*", null, "VOICE", true);
        plans.put(key2, sample2);
        
        Map<String, List<Key>> regexps = new HashMap<String, List<Key>>();
        regexps.put(null, new ArrayList<Key>());
        regexps.put("OFFER1", new ArrayList<Key>());
        regexps.get(null).add(key2);
        regexps.get("OFFER1").add(key);
        
        SimpleNumberingPlanCache instance = SimpleNumberingPlanCache.getInstance();
        numberingPlansByPhoneRegexpField.set(instance, plans);
        orderedRegexpsFields.set(instance, regexps);
        
        Access access = new Access(1L, "OFFER1", null, null, null, null, null, null, null, null, null, null, null, null);

        // non existent called number in numbering plan
        CDR cdr = new BaseCDR.Builder().addOriginPLMN("ZONE1").addCalledNumber("1111112")
	        .addOnNET("_ON").addOperator("BYTEL")
	        .build();

        MediationContext context = new MediationContext(cdr, CDRType.VOICE, new CDRProcessor(null));
        context.setAccess(access);
        context.setOnNET(true);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertNotNull(context.getOriginZone());
        Assert.assertNotNull(context.getTargetZone());
        Assert.assertEquals(context.getOriginZone(), "DEST1");
        Assert.assertEquals(context.getTargetZone(), "MOBILE_ON_NET");
        
        // called number in numbering plan
        CDR cdr2 = new BaseCDR.Builder().addOriginPLMN("ZONE1").addCalledNumber("9012345").addOnNET("_ON").build();

        MediationContext context2 = new MediationContext(cdr2, CDRType.VOICE, new CDRProcessor(null));
        context2.setAccess(access);
        context2.setOnNET(true);

        step = new ZonningStep(null);
        success = step.execute(context2);

        Assert.assertTrue(success);
        Assert.assertTrue(context2.isAccepted());
        Assert.assertNotNull(context2.getOriginZone());
        Assert.assertNotNull(context2.getTargetZone());
        Assert.assertEquals(context2.getOriginZone(), "DEST1");
        Assert.assertEquals(context2.getTargetZone(), "S2");
        
        // called number in public numbering plan, but ticket is onNet so still on net zone must be returned
        CDR cdr3 = new BaseCDR.Builder().addOriginPLMN("ZONE1").addCalledNumber("22222").addOnNET("_ON").build();

        MediationContext context3 = new MediationContext(cdr3, CDRType.VOICE, new CDRProcessor(null));
        context3.setAccess(access);
        context3.setOnNET(true);

        step = new ZonningStep(null);
        success = step.execute(context3);

        Assert.assertTrue(success);
        Assert.assertTrue(context3.isAccepted());
        Assert.assertNotNull(context3.getOriginZone());
        Assert.assertNotNull(context3.getTargetZone());
        Assert.assertEquals(context3.getOriginZone(), "DEST1");
        Assert.assertEquals(context3.getTargetZone(), "MOBILE_ON_NET");
        
        // called number in public numbering plan, and ticket is offNet zone from numbering plan must be returned
        CDR cdr4 = new BaseCDR.Builder().addOriginPLMN("ZONE1").addCalledNumber("22222").addOnNET("_OFF").build();

        MediationContext context4 = new MediationContext(cdr4, CDRType.VOICE, new CDRProcessor(null));
        context4.setAccess(access);
        context4.setOnNET(false);

        step = new ZonningStep(null);
        success = step.execute(context4);

        Assert.assertTrue(success);
        Assert.assertTrue(context4.isAccepted());
        Assert.assertNotNull(context4.getOriginZone());
        Assert.assertNotNull(context4.getTargetZone());
        Assert.assertEquals(context4.getOriginZone(), "DEST1");
        Assert.assertEquals(context4.getTargetZone(), "S4");
    }
    
    @Test(groups = { "db" })
    public void testFindTargetZoneForVOICE() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        
        final Field orderedRegexpsFields = SimpleNumberingPlanCache.class.getDeclaredField("orderedRegexpsByOfferCode");
        final Field numberingPlansByPhoneRegexpField = SimpleNumberingPlanCache.class.getDeclaredField("numberingPlansByPhoneRegexp");
        orderedRegexpsFields.setAccessible(true);
        numberingPlansByPhoneRegexpField.setAccessible(true);
        
        Map<Key, NumberingPlan> plans = new HashMap<Key, NumberingPlan>();
        NumberingPlan sample = new NumberingPlan();
        sample.setPhoneNumberRegexp("9[0-9]{6}");
        sample.setZoneId("S2");
        Key key = new Key("9[0-9]{6}", "OFFER1", "VOICE", true);
        plans.put(key, sample);
        
        Map<String, List<Key>> regexps = new HashMap<String, List<Key>>();
        regexps.put("OFFER1", new ArrayList<Key>());
        regexps.get("OFFER1").add(key);
        
        SimpleNumberingPlanCache instance = SimpleNumberingPlanCache.getInstance();
        numberingPlansByPhoneRegexpField.set(instance, plans);
        orderedRegexpsFields.set(instance, regexps);
        
        Access access = new Access(1L, "OFFER1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new BaseCDR.Builder().addOriginPLMN("ZONE1").addCalledNumber("9012345").build();

        MediationContext context = new MediationContext(cdr, CDRType.VOICE, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertNotNull(context.getOriginZone());
        Assert.assertNotNull(context.getTargetZone());
        Assert.assertEquals(context.getOriginZone(), "DEST1");
        Assert.assertEquals(context.getTargetZone(), "S2");
    }
    
    @Test(groups = { "db" })
    public void testTargetZoneForVOICENotFound() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        
        final Field numberingPlansByPhoneRegexpField = SimpleNumberingPlanCache.class.getDeclaredField("numberingPlansByPhoneRegexp");
        numberingPlansByPhoneRegexpField.setAccessible(true);
        
        Map<Key, NumberingPlan> plans = new HashMap<Key, NumberingPlan>();
        NumberingPlan sample = new NumberingPlan();
        sample.setPhoneNumberRegexp("9[0-9]{6}");
        sample.setZoneId("S2");
        plans.put(new Key("9[0-9]{6}", "OFFER_DIFFERENT_FROM_PA", "VOICE", true), sample);
        // with different offer no target zone should be found
        
        SimpleNumberingPlanCache instance = SimpleNumberingPlanCache.getInstance();
        numberingPlansByPhoneRegexpField.set(instance, plans);
        
        Access access = new Access(1L, "OFFER1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new BaseCDR.Builder().addOriginPLMN("ZONE1").addCalledNumber("9012345").build();

        MediationContext context = new MediationContext(cdr, CDRType.VOICE, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertNotNull(context.getOriginZone());
        Assert.assertNotNull(context.getTargetZone());
        Assert.assertEquals(context.getOriginZone(), "DEST1");
        Assert.assertEquals(context.getTargetZone(), "OUT_ZONE");
    }
    
    @Test(groups = { "db" })
    public void testFindTargetZoneForVOICEMVNORouting() {
        Access access = new Access(1L, "NC_OFFRE_VOIX1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new VOICECDRWrapper(new BaseCDR.Builder().addOriginPLMN("ZONE1").addCalledNumber("11111")
        	.addMVNORouting("7801").build());

        MediationContext context = new MediationContext(cdr, CDRType.VOICE, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertEquals(context.getTargetZone(), "APPELS_PREFIXE_NATIONAL");
    }
    
    @Test(groups = { "db" })
    public void testFindTargetZoneForSMSMVNORouting() {
        Access access = new Access(1L, "NC_OFFRE_VOIX1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new SMSCDRWrapper(new BaseCDR.Builder().addOriginPLMN("ZONE1").addCalledNumber("11111")
        	.addMVNORouting("7801").build());

        MediationContext context = new MediationContext(cdr, CDRType.SMS, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        // SMS tickets shouldnt care about MVNO routing
        Assert.assertNotSame(context.getTargetZone(), "APPELS_PREFIXE_NATIONAL");
    }
    
    @Test(groups = { "unit" })
    public void testFindTargetZoneSSPTicket() {
        Access access = new Access(1L, "NC_OFFRE_VOIX1", null, null, null, null, null, null, null, null, null, null, null, null);

        CDR cdr = new VOICECDRWrapper(new BaseCDR.Builder().addOriginPLMN("ZONE1").addCalledNumber("11111")
        	.addCDRType("SSP").build());

        MediationContext context = new MediationContext(cdr, CDRType.VOICE, new CDRProcessor(null));
        context.setAccess(access);

        ZonningStep step = new ZonningStep(null);
        boolean success = step.execute(context);

        Assert.assertTrue(success);
        Assert.assertTrue(context.isAccepted());
        Assert.assertEquals(context.getTargetZone(), "APPEL_SSP");
    }
}
