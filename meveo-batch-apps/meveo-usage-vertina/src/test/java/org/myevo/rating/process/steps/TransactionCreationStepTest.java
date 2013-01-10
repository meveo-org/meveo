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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.model.billing.Subscription;
import org.meveo.model.billing.UserAccount;
import org.meveo.model.billing.Wallet;
import org.meveo.model.catalog.OneShotChargeTemplate;
import org.meveo.model.catalog.PriceCode;
import org.meveo.model.catalog.UsagePricePlanItem;
import org.meveo.model.crm.Provider;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.vertina.constants.VertinaConstants;
import org.myevo.rating.model.EDR;
import org.myevo.rating.ticket.EDRTicket;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "db")
public class TransactionCreationStepTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateTransaction() {

        EntityManager em = MeveoPersistence.getEntityManager();

        EDR edr = new EDR();
        edr.setAccessPointId("101");
        edr.setUploadVolume(100L);
        edr.setDownloadVolume(300L);
        edr.setRoaming(true);

        EDRTicket ticket = new EDRTicket(edr, "");

        Wallet wallet = new Wallet();
        wallet.setId(15L);
        UserAccount userAccount = new UserAccount();
        userAccount.setWallet(wallet);
        Subscription subscription = new Subscription();
        subscription.setProvider(em.getReference(Provider.class, 1L));
        subscription.setUserAccount(userAccount);

        PriceCode priceCode = new PriceCode();
        priceCode.setChargeInPrice1(new BigDecimal(3.9));
        priceCode.setChargeInPrice2(new BigDecimal(2.0));
        priceCode.setChargeOutPrice1(new BigDecimal(1.8));
        priceCode.setChargeOutPrice2(new BigDecimal(1.5));
        priceCode.setChargeTemplateIn(em.find(OneShotChargeTemplate.class, 1L));
        priceCode.setChargeTemplateOut(em.find(OneShotChargeTemplate.class, 2L));

        UsagePricePlanItem usagePricePlanItem = new UsagePricePlanItem();
        usagePricePlanItem.setPriceCode(priceCode);

        ticket.setSubscription(subscription);
        ticket.setUsagePricePlanItem(usagePricePlanItem);

        Map<String, Object> contextParameters = new HashMap<String, Object>();
        TaskExecution<EDRTicket> taskExecution = new TaskExecution<EDRTicket>(null, null, null, null, null);

        TransactionCreationStep transactionCreationStep = new TransactionCreationStep(null, null);
        transactionCreationStep.process(ticket, taskExecution, contextParameters);
        Assert.assertNotNull(taskExecution.getExecutionContextParameter(VertinaConstants.LIST_OF_TRANSACTIONS_KEY));
        Assert.assertEquals(((List) taskExecution.getExecutionContextParameter(VertinaConstants.LIST_OF_TRANSACTIONS_KEY)).size(), 2);

        RatedTransaction transaction1 = (RatedTransaction) ((List) taskExecution.getExecutionContextParameter(VertinaConstants.LIST_OF_TRANSACTIONS_KEY)).get(0);
        RatedTransaction transaction2 = (RatedTransaction) ((List) taskExecution.getExecutionContextParameter(VertinaConstants.LIST_OF_TRANSACTIONS_KEY)).get(1);

        Assert.assertEquals(transaction1.getSubUsageCode1(), "DATA");
        Assert.assertEquals(transaction1.getUsageCode(), "USAGE_CHARGE_2");
        Assert.assertEquals(transaction1.getGroupingId().intValue(), 3);
        Assert.assertEquals(transaction1.getDescription(), "Usage description 2");
        Assert.assertEquals(transaction1.getUsageAmount().intValue(), edr.getUploadVolume().intValue());
        Assert.assertEquals(transaction1.getUsageQuantity().intValue(), edr.getUploadVolume().intValue());
        Assert.assertEquals(transaction1.getUnitPrice1(), new BigDecimal(1.8));
        Assert.assertEquals(transaction1.getUnitPrice2(), new BigDecimal(1.5));
        Assert.assertEquals(transaction1.getTaxCode(), "1");
        Assert.assertEquals(transaction1.getTaxPercent().doubleValue(), new BigDecimal(19.6).doubleValue());
        Assert.assertEquals(transaction1.getAmount1().doubleValue(), (new BigDecimal(1.8)).multiply(new BigDecimal(edr.getUploadVolume())).doubleValue());
        Assert.assertEquals(transaction1.getAmount1WithoutTax().doubleValue(), (new BigDecimal(1.8)).multiply(new BigDecimal(edr.getUploadVolume())).doubleValue());
        Assert.assertEquals(transaction1.getAmount1Tax().floatValue(), (new BigDecimal(19.6)).multiply(transaction1.getAmount1()).floatValue());
        Assert.assertEquals(transaction1.getAmount1WithTax().doubleValue(), transaction1.getAmount1WithoutTax().add(transaction1.getAmount1Tax()).doubleValue());
        Assert.assertEquals(transaction1.getAmount2().doubleValue(), (new BigDecimal(1.5)).multiply(new BigDecimal(edr.getUploadVolume())).doubleValue());
        Assert.assertEquals(transaction1.getAmount2WithoutTax().doubleValue(), (new BigDecimal(1.5)).multiply(new BigDecimal(edr.getUploadVolume())).doubleValue());
        Assert.assertEquals(transaction1.getAmount2Tax().doubleValue(), (new BigDecimal(19.6)).multiply(transaction1.getAmount2()).doubleValue());
        Assert.assertEquals(transaction1.getAmount2WithTax().doubleValue(), transaction1.getAmount2WithoutTax().add(transaction1.getAmount2Tax()).doubleValue());

        Assert.assertEquals(transaction2.getSubUsageCode1(), "DATA");
        Assert.assertEquals(transaction2.getUsageCode(), "USAGE_CHARGE_1");
        Assert.assertEquals(transaction2.getGroupingId().intValue(), 1);
        Assert.assertEquals(transaction2.getDescription(), "Usage description 1");
        Assert.assertEquals(transaction2.getUsageAmount().intValue(), edr.getDownloadVolume().intValue());
        Assert.assertEquals(transaction2.getUsageQuantity().intValue(), edr.getDownloadVolume().intValue());
        Assert.assertEquals(transaction2.getUnitPrice1(), new BigDecimal(3.9));
        Assert.assertEquals(transaction2.getUnitPrice2(), new BigDecimal(2.0));
        Assert.assertEquals(transaction2.getTaxCode(), "1");
        Assert.assertEquals(transaction2.getTaxPercent().doubleValue(), new BigDecimal(19.6).doubleValue());
        Assert.assertEquals(transaction2.getAmount1().doubleValue(), (new BigDecimal(3.9)).multiply(new BigDecimal(edr.getDownloadVolume())).doubleValue());
        Assert.assertEquals(transaction2.getAmount1WithoutTax().doubleValue(), (new BigDecimal(3.9)).multiply(new BigDecimal(edr.getDownloadVolume())).doubleValue());
        Assert.assertEquals(transaction2.getAmount1Tax().floatValue(), (new BigDecimal(19.6)).multiply(transaction2.getAmount1()).floatValue());
        Assert.assertEquals(transaction2.getAmount1WithTax().doubleValue(), transaction2.getAmount1WithoutTax().add(transaction2.getAmount1Tax()).doubleValue());
        Assert.assertEquals(transaction2.getAmount2().doubleValue(), (new BigDecimal(2.0)).multiply(new BigDecimal(edr.getDownloadVolume())).doubleValue());
        Assert.assertEquals(transaction2.getAmount2WithoutTax().doubleValue(), (new BigDecimal(2.0)).multiply(new BigDecimal(edr.getDownloadVolume())).doubleValue());
        Assert.assertEquals(transaction2.getAmount2Tax().doubleValue(), (new BigDecimal(19.6)).multiply(transaction2.getAmount2()).doubleValue());
        Assert.assertEquals(transaction2.getAmount2WithTax().doubleValue(), transaction2.getAmount2WithoutTax().add(transaction2.getAmount2Tax()).doubleValue());

    }
}