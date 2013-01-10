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
package org.meveo.bayad.dunning.process;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.util.BayadUtils;
import org.meveo.bayad.util.DunningLotBuilder;
import org.meveo.model.admin.BayadDunningInputHistory;
import org.meveo.model.admin.DunningHistory;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.ActionDunning;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.DunningActionTypeEnum;
import org.meveo.model.payments.DunningLOT;
import org.meveo.model.payments.OtherCreditAndCharge;
import org.meveo.persistence.MeveoPersistence;

/**
 * update customerAccounts and actions whene dunninglevel are changed
 * 
 * @author anasseh
 * @created 03.12.2010
 * 
 */
public class CommitStep {

    private static final Logger logger = Logger.getLogger(CommitStep.class);

    private DunningHistory dunningHistory = null;
    private BayadDunningInputHistory bayadDunningInputHistory = null;
    private List<CustomerAccount> listCustomerAccountUpdated = new ArrayList<CustomerAccount>();
    private List<ActionDunning> listActionDunning = new ArrayList<ActionDunning>();
    private List<OtherCreditAndCharge> listOCC = new ArrayList<OtherCreditAndCharge>();
    private Provider provider = null;

    public void doCommit() throws Exception {
        logger.info("doCommit ...");
        EntityManager em = MeveoPersistence.getEntityManager();        
        em.getTransaction().begin();        
        if (listCustomerAccountUpdated != null && !listCustomerAccountUpdated.isEmpty()) {
            for (CustomerAccount customerAccount : listCustomerAccountUpdated) {
                em.merge(customerAccount);
                logger.info("doCommit update customerAccount.id " + customerAccount.getId() + " ok");
            }
        }
        if (dunningHistory != null) {
            em.persist(dunningHistory);
        }
        if (bayadDunningInputHistory != null) {
            em.persist(bayadDunningInputHistory);
        }

        if (listActionDunning != null && !listActionDunning.isEmpty()) {
            for (DunningActionTypeEnum actionType : DunningActionTypeEnum.values()) {
                DunningLOT dunningLOT = new DunningLOT();
                dunningLOT.setActionType(actionType);
                dunningLOT.setAuditable(BayadUtils.getAuditable(BayadUtils.getUserBayadSystem()));
                dunningLOT.setProvider(getProvider());

                em.persist(dunningLOT);
                logger.info("doCommit persist dunningLOT ok");
                for (ActionDunning actionDunning : listActionDunning) {
                    if (actionDunning.getTypeAction() == actionType) {
                        actionDunning.setDunningLOT(dunningLOT);
                        actionDunning.setAuditable(BayadUtils.getAuditable(BayadUtils.getUserBayadSystem()));
                        em.persist(actionDunning);
                        dunningLOT.getActions().add(actionDunning);
                        em.merge(dunningLOT);
                    }
                }
                if (dunningLOT.getActions().isEmpty()) {
                    em.remove(dunningLOT);
                } else {
                    try {
                        dunningLOT.setFileName(buildFile(dunningLOT));
                        logger.info("doCommit dunningLOT.setFileName ok");
                        dunningLOT.setDunningHistory(dunningHistory);
                        em.merge(dunningLOT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        em.getTransaction().rollback();//
                    }
                }
            }
        }
        if (listOCC != null && !listOCC.isEmpty()) {
            for (OtherCreditAndCharge occ : listOCC) {
                em.persist(occ);
            }
        }
        em.getTransaction().commit();
        logger.info("doCommit done");
    }

    private String buildFile(DunningLOT dunningLOT) throws Exception {
        DunningLotBuilder bunningLotBuilder = new DunningLotBuilder(dunningLOT);
        bunningLotBuilder.exportToFile();
        return bunningLotBuilder.getFileName();
    }

    /**
     * @return the dunningHistory
     */
    public DunningHistory getDunningHistory() {
        return dunningHistory;
    }

    /**
     * @param dunningHistory
     *            the dunningHistory to set
     */
    public void setDunningHistory(DunningHistory dunningHistory) {
        this.dunningHistory = dunningHistory;
    }
    
    /**
     * @return
     */
    public BayadDunningInputHistory getBayadDunningInputHistory() {
        return bayadDunningInputHistory;
    }

    /**
     * @param bayadDunningInputHistory
     */
    public void setBayadDunningInputHistory(BayadDunningInputHistory bayadDunningInputHistory) {
        this.bayadDunningInputHistory = bayadDunningInputHistory;
    }

    /**
     * @return the listCustomerAccountUpdated
     */
    public List<CustomerAccount> getListCustomerAccountUpdated() {
        return listCustomerAccountUpdated;
    }

    /**
     * @param listCustomerAccountUpdated
     *            the listCustomerAccountUpdated to set
     */
    public void setListCustomerAccountUpdated(List<CustomerAccount> listCustomerAccountUpdated) {
        this.listCustomerAccountUpdated = listCustomerAccountUpdated;
    }

    /**
     * @return the listActionDunning
     */
    public List<ActionDunning> getListActionDunning() {
        return listActionDunning;
    }

    /**
     * @param listActionDunning
     *            the listActionDunning to set
     */
    public void setListActionDunning(List<ActionDunning> listActionDunning) {
        this.listActionDunning = listActionDunning;
    }

    public void setListOCC(List<OtherCreditAndCharge> listOCC) {
        this.listOCC = listOCC;
    }

    public List<OtherCreditAndCharge> getListOCC() {
        return listOCC;
    }

    /**
     * @return the provider
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * @param provider
     *            the provider to set
     */
    public void setProvider(Provider provider) {
        this.provider = provider;
    }

}
