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
package org.meveo.bayad.dunning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.bayad.dunning.process.CommitStep;
import org.meveo.bayad.dunning.process.DowngradeDunningLevelStep;
import org.meveo.bayad.dunning.process.UpgradeDunningLevelStep;
import org.meveo.bayad.ticket.DunningTicket;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.model.admin.BayadDunningInputHistory;
import org.meveo.model.admin.DunningHistory;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.ActionDunning;
import org.meveo.model.payments.CreditCategoryEnum;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.CustomerAccountStatusEnum;
import org.meveo.model.payments.DunningPlan;
import org.meveo.model.payments.DunningPlanStatusEnum;
import org.meveo.model.payments.OtherCreditAndCharge;
import org.meveo.model.payments.PaymentMethodEnum;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.service.payments.remote.CustomerAccountServiceRemote;

/**
 * Dunning process
 * 
 * @author anasseh
 * @created 03.12.2010
 */

public class DunningProcess {
    
    private static final Logger logger = Logger.getLogger(DunningProcess.class);

    public void execute() {
        logger.info("execute DunningProcess ....");
        MeveoPersistence.closeEntityManager(); 
        Date startDate = new Date();
        CustomerAccountServiceRemote customerAccountServiceRemote = null;
        try {
            customerAccountServiceRemote = (CustomerAccountServiceRemote) EjbUtils.getRemoteInterface(BayadConfig.getMeveoCustomerAccountServiceJndiName(),
                    BayadConfig.getMeveoProviderUrl());
            logger.info("customerAccountServiceRemote:" + customerAccountServiceRemote);

        } catch (Exception e) {
            e.printStackTrace();
        }               
        for (DunningPlan dunningPlan : getDunningPlans()) {
            int loadedCustomerAccounts = 0;
            int errorCustomerAccounts = 0;
            int updatedCustomerAccounts = 0;
            List<CustomerAccount> listCustomerAccountUpdated = new ArrayList<CustomerAccount>();
            List<ActionDunning> listActionDunning = new ArrayList<ActionDunning>();
            List<OtherCreditAndCharge> listOCC = new ArrayList<OtherCreditAndCharge>();

            List<CustomerAccount> customerAccounts = getCustomerAccounts(dunningPlan.getCreditCategory(), dunningPlan.getPaymentMethod(), dunningPlan
                    .getProvider().getCode());
            logger.info(String.format("Found %s CustomerAccounts to check", (customerAccounts == null ? "null" : customerAccounts.size())));
            for (CustomerAccount customerAccount : customerAccounts) {
                try {
                    logger.info("Processing  customerAccounts code " + customerAccount.getCode());
                    loadedCustomerAccounts++;
                    BigDecimal balanceExigible = customerAccountServiceRemote.customerAccountBalanceExigibleWithoutLitigation(customerAccount.getId(), null,
                            new Date());
                    logger.info("balanceExigible " + balanceExigible);
                    DunningTicket dunningTicket = new DunningTicket();
                    dunningTicket.setCustomerAccount(customerAccount);
                    dunningTicket.setDunningPlan(dunningPlan);
                    dunningTicket.setBalanceExigible(balanceExigible);

                    DowngradeDunningLevelStep downLevel = new DowngradeDunningLevelStep();
                    downLevel.setDunningTicket(dunningTicket);
                    if (downLevel.execute()) {
                        updatedCustomerAccounts++;
                        listCustomerAccountUpdated.add(downLevel.getCustomerAccountUpdated());
                    } else {
                        UpgradeDunningLevelStep upgradeLevel = new UpgradeDunningLevelStep();
                        upgradeLevel.setDunningTicket(dunningTicket);
                        if (upgradeLevel.execute()) {
                            updatedCustomerAccounts++;
                            listCustomerAccountUpdated.add(upgradeLevel.getCustomerAccountUpdated());
                            listActionDunning.addAll(upgradeLevel.getListActionDunning());
                            listOCC.addAll(upgradeLevel.getListOCC());
                        }
                    }
                } catch (Exception e) {
                    errorCustomerAccounts++;
                    e.printStackTrace();
                }
            }
            try {
                CommitStep dunningCommit = new CommitStep();
                dunningCommit.setListActionDunning(listActionDunning);
                dunningCommit.setListCustomerAccountUpdated(listCustomerAccountUpdated);
                dunningCommit.setListOCC(listOCC);
                dunningCommit.setProvider(dunningPlan.getProvider());
                DunningHistory dunningHistory = new DunningHistory();
                dunningHistory.setExecutionDate(startDate);
                dunningHistory.setLinesRead(loadedCustomerAccounts);
                dunningHistory.setLinesRejected(errorCustomerAccounts);
                dunningHistory.setLinesInserted(updatedCustomerAccounts);
                dunningHistory.setProvider(dunningPlan.getProvider());
                dunningCommit.setDunningHistory(dunningHistory);
                dunningCommit.setBayadDunningInputHistory(createNewInputHistory(loadedCustomerAccounts, updatedCustomerAccounts, errorCustomerAccounts, startDate, dunningPlan.getProvider()));
                dunningCommit.doCommit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Creates input history object, to save it to DB.
     */
    private BayadDunningInputHistory createNewInputHistory(int nbTicketsParsed, int nbTicketsSucceeded, int nbTicketsRejected, Date startDate, Provider provider) {
        BayadDunningInputHistory inputHistory = new BayadDunningInputHistory();
        inputHistory.setName(startDate.toString());
        inputHistory.setParsedTickets(nbTicketsParsed);
        inputHistory.setRejectedTickets(nbTicketsRejected);
        inputHistory.setSucceededTickets(nbTicketsSucceeded);
        inputHistory.setAnalysisStartDate(startDate);
        inputHistory.setAnalysisEndDate(new Date());
        inputHistory.setProvider(provider);
        return inputHistory;
    }

    @SuppressWarnings("unchecked")
    private List<CustomerAccount> getCustomerAccounts(CreditCategoryEnum creditCategory, PaymentMethodEnum paymentMethod, String providerCode) {
        EntityManager em = MeveoPersistence.getEntityManager();       
        List<CustomerAccount> customerAccounts = em
                .createQuery(
                        "from " + CustomerAccount.class.getSimpleName()
                                + " where paymentMethod=:paymentMethod and creditCategory=:creditCategory and status=:status and provider.code=:providerCode ")
                .setParameter("paymentMethod", paymentMethod).setParameter("creditCategory", creditCategory)
                .setParameter("status", CustomerAccountStatusEnum.ACTIVE).setParameter("providerCode", providerCode).getResultList();
        return customerAccounts;
    }

    @SuppressWarnings("unchecked")
    private List<DunningPlan> getDunningPlans() {
        EntityManager em = MeveoPersistence.getEntityManager();
        return (List<DunningPlan>) em.createQuery("from " + DunningPlan.class.getSimpleName() + " where status=:status")
                .setParameter("status", DunningPlanStatusEnum.ACTIVE).getResultList();
    }

}
