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
package org.myevo.rating.process.steps;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.model.billing.Subscription;
import org.meveo.model.catalog.UsagePricePlanItem;
import org.meveo.persistence.MeveoPersistence;
import org.myevo.rating.model.EDR;
import org.myevo.rating.ticket.EDRTicket;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "db")
public class LocatePriceStepTest {

    @Test
    public void testValidateParametersNoDB() {
        LocatePriceStep locatePriceStep = new LocatePriceStep(null, null);
        Assert.assertTrue(locatePriceStep.validateParameters("A", "A", null, null));
        Assert.assertTrue(locatePriceStep.validateParameters(2L, null, 1L, 3L));
        Assert.assertTrue(locatePriceStep.validateParameters(2L, null, 2L, 3L));
        Assert.assertTrue(locatePriceStep.validateParameters(new GregorianCalendar(2010, 3, 2).getTime(), null, new GregorianCalendar(2010, 2, 2).getTime(), new GregorianCalendar(
            2010, 4, 2).getTime()));
        Assert.assertTrue(locatePriceStep.validateParameters(new GregorianCalendar(2010, 2, 2).getTime(), null, new GregorianCalendar(2010, 2, 2).getTime(), new GregorianCalendar(
            2010, 4, 2).getTime()));
        Assert.assertTrue(locatePriceStep.validateParameters("A", null, null, null));
        Assert.assertTrue(locatePriceStep.validateParameters(null, "__NULL", null, null));
        Assert.assertTrue(locatePriceStep.validateParameters(new Boolean(true), "true", null, null));
        Assert.assertTrue(locatePriceStep.validateParameters(new Boolean(true), String.valueOf(true), null, null));
    }

    @Test
    public void testGetPricePlanNoDB() {
        List<UsagePricePlanItem> pricePlanList = initPricePlanItems();
        EDR edrExisting = buildExistingPricePlanEDR();
        EDR edrNonExisting = buildNonExistingPricePlanEDR();

        LocatePriceStep locatePriceStep = new LocatePriceStep(null, null);

        Assert.assertEquals(locatePriceStep.getPricePlan(pricePlanList, edrExisting).getId().longValue(), 1L);
        Assert.assertEquals(locatePriceStep.getPricePlan(pricePlanList, edrNonExisting), null);
    }

    @Test
    public void testGetPricePlanDB() {

        EntityManager em = MeveoPersistence.getEntityManager();

        EDR edr1 = new EDR();
        edr1.setServiceId("DATA_TEST");
        edr1.setOriginZone("1");
        edr1.setRoaming(false);
        EDRTicket ticket1 = new EDRTicket(edr1, "");
        ticket1.setSubscription(em.find(Subscription.class, 1L));

        EDR edr2 = new EDR();
        edr2.setServiceId("DATA_TEST");
        edr2.setOriginZone("1");
        edr2.setRoaming(true);
        EDRTicket ticket2 = new EDRTicket(edr2, "");
        ticket2.setSubscription(em.find(Subscription.class, 1L));

        EDR edr3 = new EDR();
        edr3.setServiceId("DATA_TEST");
        edr3.setOriginZone("10");
        edr3.setRoaming(true);
        EDRTicket ticket3 = new EDRTicket(edr3, "");
        ticket3.setSubscription(em.find(Subscription.class, 2L));

        // Roaming should be true
        EDR edrF1 = new EDR();
        edrF1.setServiceId("DATA_TEST");
        edrF1.setOriginZone("2");
        edrF1.setRoaming(false);
        EDRTicket ticketF1 = new EDRTicket(edrF1, "");
        ticketF1.setSubscription(em.find(Subscription.class, 1L));

        // Non-existing service code
        EDR edrF2 = new EDR();
        edrF2.setServiceId("DATA_TESTS");
        edrF2.setOriginZone("1");
        edrF2.setRoaming(false);
        EDRTicket ticketF2 = new EDRTicket(edrF2, "");
        ticketF2.setSubscription(em.find(Subscription.class, 1L));

        // Wrong customer account code
        EDR edrF3 = new EDR();
        edrF3.setServiceId("DATA_TEST");
        edrF3.setOriginZone("10");
        edrF3.setRoaming(true);
        EDRTicket ticketF3 = new EDRTicket(edrF3, "");
        ticketF3.setSubscription(em.find(Subscription.class, 1L));

        Map<String, Object> contextParameters = new HashMap<String, Object>();
        TaskExecution<EDRTicket> taskExecution = new TaskExecution<EDRTicket>(null, null, null, null, null);

        LocatePriceStep locatePriceStep = new LocatePriceStep(null, null);

        locatePriceStep.process(ticket1, taskExecution, contextParameters);
        Assert.assertNotNull(ticket1.getUsagePricePlanItem());
        Assert.assertEquals(ticket1.getUsagePricePlanItem().getId(), (Long) 2L);

        locatePriceStep.process(ticket2, taskExecution, contextParameters);
        Assert.assertNotNull(ticket2.getUsagePricePlanItem());
        Assert.assertEquals(ticket2.getUsagePricePlanItem().getId(), (Long) 1L);

        locatePriceStep.process(ticket3, taskExecution, contextParameters);
        Assert.assertNotNull(ticket3.getUsagePricePlanItem());
        Assert.assertEquals(ticket3.getUsagePricePlanItem().getId(), (Long) 4L);
        
        locatePriceStep.process(ticketF1, taskExecution, contextParameters);
        Assert.assertNull(ticketF1.getUsagePricePlanItem());

        locatePriceStep.process(ticketF2, taskExecution, contextParameters);
        Assert.assertNull(ticketF2.getUsagePricePlanItem());

        locatePriceStep.process(ticketF3, taskExecution, contextParameters);
        Assert.assertNull(ticketF3.getUsagePricePlanItem());
    }

    private EDR buildNonExistingPricePlanEDR() {
        EDR edr = new EDR();

        edr.setOriginZone("getOriginZone non existing");
        edr.setAccessPointNameNI("getAccessPointNameNI non existing");
        edr.setPlmn("getPlmn non existing");
        edr.setPlmnFromTicket("getPlmnFromTicket non existing");
        edr.setRoaming(Boolean.TRUE);

        return edr;
    }

    private EDR buildExistingPricePlanEDR() {
        EDR edr = new EDR();

        edr.setOriginZone("getOriginZone");
        edr.setAccessPointNameNI("getAccessPointNameNI");
        edr.setPlmn("getPlmn");
        edr.setPlmnFromTicket("getPlmnFromTicket");
        edr.setRoaming(Boolean.TRUE);

        return edr;
    }

    private List<UsagePricePlanItem> initPricePlanItems() {
        List<UsagePricePlanItem> pricePlanItems = new ArrayList<UsagePricePlanItem>();

        UsagePricePlanItem pricePlan = new UsagePricePlanItem();
        pricePlan.setId(1L);
        pricePlan.setStringParam1("GETORIGINZONE");
        pricePlan.setStringParam2("getAccessPointNameNI");
        pricePlan.setStringParam3("getPlmn");
        pricePlan.setStringParam4("getPlmnFromTicket");
        pricePlan.setBooleanParam1(true);

        pricePlanItems.add(pricePlan);

        pricePlan = new UsagePricePlanItem();
        pricePlan.setId(2L);
        pricePlan.setStringParam1("getOriginZone 1");
        pricePlan.setStringParam2("getAccessPointNameNI 2");
        pricePlan.setStringParam3("getPlmn 3");
        pricePlan.setStringParam4("getPlmnFromTicket 4");
        pricePlan.setBooleanParam1(true);

        pricePlanItems.add(pricePlan);

        return pricePlanItems;
    }
}
