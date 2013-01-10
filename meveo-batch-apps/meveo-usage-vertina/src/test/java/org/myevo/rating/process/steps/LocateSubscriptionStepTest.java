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

import java.util.HashMap;
import java.util.Map;

import org.meveo.core.inputhandler.TaskExecution;
import org.myevo.rating.model.EDR;
import org.myevo.rating.ticket.EDRTicket;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "db")
public class LocateSubscriptionStepTest {

    @Test
    public void testFindSubscription() {

        EDR edr = new EDR();
        edr.setAccessPointId("101");
        EDRTicket ticket = new EDRTicket(edr, "");

        Map<String, Object> contextParameters = new HashMap<String, Object>();
        TaskExecution<EDRTicket> taskExecution = new TaskExecution<EDRTicket>(null, null, null, null, null);

        LocateSubscriptionStep locateSubscriptionStep = new LocateSubscriptionStep(null, null);
        locateSubscriptionStep.process(ticket, taskExecution, contextParameters);
        Assert.assertNotNull(ticket.getSubscription(), "Subscription should not be null");
        Assert.assertEquals(ticket.getSubscription().getCode(), "SUBS-PA-101");
    }
}