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
package org.manaty.telecom.mediation.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.manaty.model.mediation.NumberingPlan;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.telecom.mediation.MedinaPersistence;
import org.manaty.telecom.mediation.cache.SimpleNumberingPlanCache.Key;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for simple cache manager.
 * 
 * @author Ignas Lelys
 * @created 2009.09.29
 */
public class SimpleCacheManagerTest {

    private SimpleNumberingPlanCache cacheManager;

    @SuppressWarnings("unchecked")
    @Test(groups = { "db" })
    public void testLoadCache() throws Exception {
        insertSampleDataToDatabase();
        cacheManager = SimpleNumberingPlanCache.getInstance();
        final Method loadCacheMethod = SimpleNumberingPlanCache.class.getDeclaredMethod("loadCache");
        loadCacheMethod.setAccessible(true);
        int numberOfNumberingPlans = (Integer) loadCacheMethod.invoke(cacheManager);
        Assert.assertEquals(numberOfNumberingPlans, 10); // 9 + 1 in test-data.xml
        // check regexps are sorted by length
        final Field orderedRegexpsByOfferCode = SimpleNumberingPlanCache.class.getDeclaredField("orderedRegexpsByOfferCode");
        orderedRegexpsByOfferCode.setAccessible(true);
        Map<String, List<Key>> regexps = (Map<String, List<Key>>)orderedRegexpsByOfferCode.get(cacheManager);
        Assert.assertEquals(regexps.size(), 2);
        Assert.assertEquals(regexps.get("OFFER").size(), 1);
        Assert.assertEquals(regexps.get("OFFER").get(0).phoneKey, "9[0-9]{5}");
        Assert.assertEquals(regexps.get(null).size(), 1);
        Assert.assertEquals(regexps.get(null).get(0).phoneKey, "9[0-9]{4}");

        
        final Field orderedStartWithRegexpsByOfferCode = SimpleNumberingPlanCache.class.getDeclaredField("orderedStartWithRegexpsByOfferCode");
        orderedStartWithRegexpsByOfferCode.setAccessible(true);
        Map<String, List<Key>> startWithPatterns = (Map<String, List<Key>>)orderedStartWithRegexpsByOfferCode.get(cacheManager);
        Assert.assertEquals(startWithPatterns.size(), 3);
        Assert.assertEquals(startWithPatterns.get("OFFER").size(), 1);
        Assert.assertEquals(startWithPatterns.get("OFFER").get(0).phoneKey, "338");
        Assert.assertEquals(startWithPatterns.get(null).size(), 2);
        Assert.assertEquals(startWithPatterns.get(null).get(0).phoneKey, "338");
        Assert.assertEquals(startWithPatterns.get(null).get(1).phoneKey, "33");
    }

    @Test(groups = { "db" }, dependsOnMethods = { "testLoadCache" })
    public void testGetNumberingPlanFromCache() {
        NumberingPlan plan1 = cacheManager.getNumberingPlanFromCache("331222222", "OFFER", CDRType.VOICE, true);
        Assert.assertNotNull(plan1);
        Assert.assertEquals(plan1.getZoneId(), "S1");
        NumberingPlan plan2 = cacheManager.getNumberingPlanFromCache("912345", "OFFER", CDRType.VOICE, true);
        Assert.assertNotNull(plan2);
        Assert.assertEquals(plan2.getZoneId(), "S2");
        NumberingPlan plan3 = cacheManager.getNumberingPlanFromCache("331222221", "OFFER", CDRType.VOICE, true);
        Assert.assertNotNull(plan3);
        Assert.assertEquals(plan3.getZoneId(), "S3");
        NumberingPlan plan4 = cacheManager.getNumberingPlanFromCache("337A22221", null, CDRType.VOICE, true);
        Assert.assertNotNull(plan4);
        Assert.assertEquals(plan4.getZoneId(), "S5");
        NumberingPlan plan5 = cacheManager.getNumberingPlanFromCache("338A22221", null, CDRType.VOICE, true);
        Assert.assertNotNull(plan5);
        Assert.assertEquals(plan5.getZoneId(), "S6");
    }

    @Test(groups = { "db" })
    public void testGetZoneFromRouting() {
    	Assert.assertEquals(cacheManager.getZoneFromRouting("NC_OFFRE_VOIX1", "7802", false), "APPELS_PREFIXE_INTERNATIONAL");
    }
    
    @Test(groups = { "db" })
    public void testGetZoneFromRoutingNational() {
    	Assert.assertEquals(cacheManager.getZoneFromRouting("NC_OFFRE_VOIX1", "7803", false), "APPELS_PREFIXE_NATIONAL3");
    }
    
    @Test(groups = { "db" })
    public void testGetZoneFromRoutingInternational() {
    	Assert.assertEquals(cacheManager.getZoneFromRouting("NC_OFFRE_VOIX1", "7803", true), "APPELS_PREFIXE_INTERNATIONAL3");
    }
    
    private void insertSampleDataToDatabase() throws Exception {
    	try {
	        EntityManager em = MedinaPersistence.getEntityManager();
	        em.getTransaction().begin();
	        
	        NumberingPlan plan1 = new NumberingPlan();
	        plan1.setPhoneNumber("331222222");
	        plan1.setZoneId("S1");
	        plan1.setOfferCode("OFFER");
	        plan1.setCdrType("VOICE");
	        plan1.setOutgoing(true);
	        em.persist(plan1);
	        
	        NumberingPlan plan2 = new NumberingPlan();
	        plan2.setPhoneNumberRegexp("9[0-9]{5}");
	        plan2.setZoneId("S2");
	        plan2.setOfferCode("OFFER");
	        plan2.setCdrType("VOICE");
	        plan2.setOutgoing(true);
	        em.persist(plan2);
	        
	        NumberingPlan plan3 = new NumberingPlan();
	        plan3.setPhonePrefix("331");
	        plan3.setZoneId("S3");
	        plan3.setOfferCode("OFFER");
	        plan3.setCdrType("VOICE");
	        plan3.setOutgoing(true);
	        em.persist(plan3);
	        
	        // this numbering plan prefix should not be used, because there is
	        // longer prefix that starts with 3
	        NumberingPlan plan4 = new NumberingPlan();
	        plan4.setPhonePrefix("3");
	        plan4.setZoneId("S4");
	        plan4.setOfferCode("OFFER");
	        plan4.setCdrType("VOICE");
	        plan4.setOutgoing(true);
	        em.persist(plan4);
	        
	        NumberingPlan plan5 = new NumberingPlan();
	        plan5.setPhoneNumberRegexp("33*");
	        plan5.setZoneId("S5");
	        plan5.setCdrType("VOICE");
	        plan5.setOutgoing(true);
	        em.persist(plan5);
	        
	        NumberingPlan plan6 = new NumberingPlan();
	        plan6.setPhoneNumberRegexp("338*");
	        plan6.setZoneId("S6");
	        plan6.setCdrType("VOICE");
	        plan6.setOutgoing(true);
	        em.persist(plan6);
	        
	        NumberingPlan plan7 = new NumberingPlan();
	        plan7.setPhoneNumberRegexp("9[0-9]{4}");
	        plan7.setZoneId("S7");
	        plan7.setOfferCode(null);
	        plan7.setCdrType("VOICE");
	        plan7.setOutgoing(true);
	        em.persist(plan7);
	        
	        NumberingPlan plan8 = new NumberingPlan();
	        plan8.setPhoneNumberRegexp("338*");
	        plan8.setZoneId("S8");
	        plan8.setCdrType("VOICE");
	        plan8.setOfferCode("OFFER");
	        plan8.setOutgoing(true);
	        em.persist(plan8);
	        
	        em.getTransaction().commit();
    	} catch (Exception e) {
			MedinaPersistence.getEntityManager().getTransaction().rollback();
			throw e;
		}
    }

}
