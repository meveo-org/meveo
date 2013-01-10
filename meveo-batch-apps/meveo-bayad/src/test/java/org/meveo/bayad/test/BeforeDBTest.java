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
package org.meveo.bayad.test;
import org.meveo.bayad.BayadConfig;
import org.meveo.persistence.MeveoPersistence;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * steup db and set data
 * 
 * @author anasseh
 * @created 07.12.2010
 */
public class BeforeDBTest {

    @BeforeGroups(groups = { "db" })
    public void setUp() throws Exception {
        MeveoPersistence.init(BayadConfig.getPersistenceUnitName(), BayadConfig.getPersistenceProperties());
    }

    // TODO mo
    @Test(groups = { "db" })
    public void setData() {
//        EntityManager em = MeveoPersistence.getEntityManager();
//        em.getTransaction().begin();
//        User user = new User();
//        user.setName(new Name(Title.MR, "tester", "tester"));
//        user.setUserName("userName");
//        Auditable auditable = new Auditable();
//        auditable.setCreated(new Date());
//        auditable.setCreator(user);
//        user.setAuditable(auditable);
//        em.persist(user);
//
//        CustomerAccount customerAccount = new CustomerAccount();
//        customerAccount.setCode("customerAccountCode");
//        customerAccount.setStatus(CustomerAccountStatusEnum.ACTIVE);
//        customerAccount.setAuditable(auditable);
//        customerAccount.setCreditCategory(CreditCategoryEnum.NORMAL);
//        customerAccount.setDateDunningLevel(DateUtils.parseDateWithPattern("12/12/2009", "dd/MM/yyyy"));
//        customerAccount.setDunningLevel(DunningLevelEnum.R0);
//        customerAccount.setPaymentMethod(PaymentMethodEnum.DIRECTDEBIT);
//        em.persist(customerAccount);
//
//        DunningPlan dunningPlan = new DunningPlan();
//        dunningPlan.setCode("dunningPlanCode1");
//        dunningPlan.setCreditCategory(CreditCategoryEnum.NORMAL);
//        dunningPlan.setPaymentMethod(PaymentMethodEnum.DIRECTDEBIT);
//        dunningPlan.setStatus(DunningPlanStatusEnum.ACTIVE);
//        dunningPlan.setAuditable(auditable);
//        em.persist(dunningPlan);
//
//        DunningPlanTransition dunningPlanTransition = new DunningPlanTransition();
//        dunningPlanTransition.setDelayBeforeProcess(1);
//        dunningPlanTransition.setDunningLevelFrom(DunningLevelEnum.R0);
//        dunningPlanTransition.setDunningLevelTo(DunningLevelEnum.R1);
//        dunningPlanTransition.setDunningPlan(dunningPlan);
//        dunningPlanTransition.setThresholdAmount(BigDecimal.ONE);
//        dunningPlanTransition.setWaitDuration(1);
//        dunningPlanTransition.setAuditable(auditable);
//        em.persist(dunningPlanTransition);
//
//        DunningPlanTransition dunningPlanTransition1 = new DunningPlanTransition();
//        dunningPlanTransition1.setDelayBeforeProcess(1);
//        dunningPlanTransition1.setDunningLevelFrom(DunningLevelEnum.R1);
//        dunningPlanTransition1.setDunningLevelTo(DunningLevelEnum.R2);
//        dunningPlanTransition1.setDunningPlan(dunningPlan);
//        dunningPlanTransition1.setThresholdAmount(BigDecimal.ONE);
//        dunningPlanTransition1.setWaitDuration(1);
//        dunningPlanTransition1.setAuditable(auditable);
//        em.persist(dunningPlanTransition1);
//
//        ActionPlanItem actionPlanItem = new ActionPlanItem();
//        actionPlanItem.setActionType(DunningActionTypeEnum.FILE);
//        actionPlanItem.setAuditable(auditable);
//        actionPlanItem.setDunningLevel(DunningLevelEnum.R1);
//        actionPlanItem.setDunningPlan(dunningPlan);
//        actionPlanItem.setItemOrder(1);
//        actionPlanItem.setLetterTemplate("letterTemplate");
//        actionPlanItem.setThresholdAmount(BigDecimal.ONE);
//        em.persist(actionPlanItem);
//        em.getTransaction().commit();
    }

}
