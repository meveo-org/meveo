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
package org.meveo.service;

import org.manaty.BaseIntegrationTest;
import org.meveo.admin.exception.IncorrectSusbcriptionException;
import org.meveo.service.api.dto.ConsumptionDTO;
import org.meveo.service.billing.local.RatedTransactionServiceLocal;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RatedTransactionServiceTest extends BaseIntegrationTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation("dataSets/BaseData.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/AllAccountHierarchy.dbunit.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Wallet.dbunit.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/Subscription.dbunit.xml"));
        beforeTestOperations.add(new DataSetOperation("dataSets/RatedTransaction.dbunit.xml"));
    }

    @Test(expectedExceptions = IncorrectSusbcriptionException.class)
    public void invalidSubscriptionTest() throws Exception {

        RatedTransactionServiceLocal ratedTransactionService = (RatedTransactionServiceLocal) getLocalInterfaceFromEmbededServer("RatedTransactionService/local");
        ratedTransactionService.getConsumption("nonexistent", null, null, true);
    }

    @Test
    public void summarizedConsumptionTest() throws Exception {

        RatedTransactionServiceLocal ratedTransactionService = (RatedTransactionServiceLocal) getLocalInterfaceFromEmbededServer("RatedTransactionService/local");
        ConsumptionDTO consumptionDTO = ratedTransactionService.getConsumption("Code_2", "DATA", null, true);

        Assert.assertNotNull(consumptionDTO);

        Assert.assertEquals(consumptionDTO.getAmountCharged().doubleValue(), 416.16D);
        Assert.assertEquals(consumptionDTO.getAmountUncharged().doubleValue(), 3620.25D);
        Assert.assertEquals(consumptionDTO.getConsumptionCharged().intValue(), 2084);
        Assert.assertEquals(consumptionDTO.getConsumptionUncharged().intValue(), 2086);

    }

    @Test
    public void notSummarizedConsumptionTest() throws Exception {

        RatedTransactionServiceLocal ratedTransactionService = (RatedTransactionServiceLocal) getLocalInterfaceFromEmbededServer("RatedTransactionService/local");
        ConsumptionDTO consumptionDTO = ratedTransactionService.getConsumption("Code_2", "DATA", null, false);

        Assert.assertNotNull(consumptionDTO);

        Assert.assertEquals(consumptionDTO.getAmountCharged().doubleValue(), 416.16D);
        Assert.assertEquals(consumptionDTO.getAmountUncharged().doubleValue(), 3620.25D);
        Assert.assertEquals(consumptionDTO.getConsumptionCharged().intValue(), 2084);
        Assert.assertEquals(consumptionDTO.getConsumptionUncharged().intValue(), 2086);

        Assert.assertEquals(consumptionDTO.getIncomingNationalConsumptionCharged().intValue(), 684);
        Assert.assertEquals(consumptionDTO.getIncomingNationalConsumptionUncharged().intValue(), 342);
        Assert.assertEquals(consumptionDTO.getIncomingRoamingConsumptionCharged().intValue(), 347);
        Assert.assertEquals(consumptionDTO.getIncomingRoamingConsumptionUncharged().intValue(), 690);

        Assert.assertEquals(consumptionDTO.getOutgoingNationalConsumptionCharged().intValue(), 700);
        Assert.assertEquals(consumptionDTO.getOutgoingNationalConsumptionUncharged().intValue(), 348);
        Assert.assertEquals(consumptionDTO.getOutgoingRoamingConsumptionCharged().intValue(), 353);
        Assert.assertEquals(consumptionDTO.getOutgoingRoamingConsumptionUncharged().intValue(), 706);

    }

}