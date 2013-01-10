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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.bayad.util.BayadUtils;
import org.meveo.commons.utils.DateUtils;
import org.meveo.model.payments.ActionDunning;
import org.meveo.model.payments.ActionPlanItem;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.DunningActionStatusEnum;
import org.meveo.model.payments.DunningActionTypeEnum;
import org.meveo.model.payments.DunningLevelEnum;
import org.meveo.model.payments.DunningPlan;
import org.meveo.model.payments.DunningPlanTransition;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.OCCTemplate;
import org.meveo.model.payments.OtherCreditAndCharge;
import org.meveo.model.payments.RecordedInvoice;
import org.meveo.persistence.MeveoPersistence;

/**
 * Upgrade dunninglevel for one customerAccount
 * 
 * @author anasseh
 * @created 03.12.2010
 * 
 */
public class UpgradeDunningLevelStep extends DunningStep {

	private static final Logger logger = Logger.getLogger(UpgradeDunningLevelStep.class);

	private List<ActionDunning> listActionDunning = new ArrayList<ActionDunning>();
	private List<OtherCreditAndCharge> listOCC = new ArrayList<OtherCreditAndCharge>();

	public boolean execute() throws Exception {
		logger.info("UpgradeDunningLevelStep ...");
		boolean isUpgradelevel = false;
		CustomerAccount customerAccount = getDunningTicket().getCustomerAccount();
		logger.info("UpgradeDunningLevelStep customerAccount.code:" + customerAccount.getCode());

		if (getDunningTicket().getBalanceExigible().compareTo(BigDecimal.ZERO) == BayadConfig.getDunningBlanceFlag()) {
			logger.info("UpgradeDunningLevelStep balance in dunning");
			logger.info("UpgradeDunningLevelStep customerAccount.dunningLevel:" + customerAccount.getDunningLevel());
			DunningLevelEnum nextLevel = BayadUtils.getNextDunningLevel(customerAccount.getDunningLevel());
			if (nextLevel == null) {
				logger.info("UpgradeDunningLevelStep  max DunningLevel");
				return false;
			} else {
				logger.info("UpgradeDunningLevelStep nextLevel:" + nextLevel);
				DunningPlanTransition dunningPlanTransition = getDunningPlanTransition(customerAccount.getDunningLevel(), nextLevel, getDunningTicket()
						.getDunningPlan());
				if (dunningPlanTransition == null) {
					logger.info("UpgradeDunningLevelStep dunningPlanTransition not found fromLevel:" + customerAccount.getDunningLevel() + " , toLevel:"
							+ nextLevel + ", dunningplan:" + getDunningTicket().getDunningPlan().getCode());
					return false;
				}
				if (DateUtils.addDaysToDate(customerAccount.getDateDunningLevel(), dunningPlanTransition.getWaitDuration()).before(new Date())) {
					List<RecordedInvoice> recordedInvoices = getRecordedInvoices(customerAccount, MatchingStatusEnum.O);
					if (recordedInvoices != null && !recordedInvoices.isEmpty()) {
						RecordedInvoice recordedInvoice = recordedInvoices.get(0);
						if (DateUtils.addDaysToDate(recordedInvoices.get(0).getDueDate(), dunningPlanTransition.getDelayBeforeProcess()).before(new Date())) {
							if (getDunningTicket().getBalanceExigible().compareTo(dunningPlanTransition.getThresholdAmount()) == 1) {
								for (ActionPlanItem actionPlanItem : getActionPlanItems(getDunningTicket().getDunningPlan(), dunningPlanTransition)) {
									BigDecimal amoutDue = getDunningTicket().getBalanceExigible();
									ActionDunning actionDunning = new ActionDunning();
									actionDunning.setCustomerAccount(customerAccount);
									actionDunning.setRecordedInvoice(recordedInvoice);
									actionDunning.setCreationDate(new Date());
									actionDunning.setTypeAction(actionPlanItem.getActionType());
									actionDunning.setStatus(DunningActionStatusEnum.E);
									actionDunning.setStatusDate(new Date());
									actionDunning.setFromLevel(customerAccount.getDunningLevel());
									actionDunning.setToLevel(dunningPlanTransition.getDunningLevelTo());
									actionDunning.setActionPlanItem(actionPlanItem);
									actionDunning.setProvider(customerAccount.getProvider());
									listActionDunning.add(actionDunning);
									if (actionPlanItem.getActionType() == DunningActionTypeEnum.CHARGE) {
										listOCC.add(addOCC(customerAccount, actionPlanItem.getChargeAmount()));
										amoutDue = amoutDue.add(actionPlanItem.getChargeAmount());
									}
									actionDunning.setAmountDue(amoutDue);
								}

								customerAccount.setDunningLevel(dunningPlanTransition.getDunningLevelTo());
								customerAccount.setDateDunningLevel(new Date());

								setCustomerAccountUpdated(customerAccount);
								isUpgradelevel = true;
								logger.info("UpgradeDunningLevelStep   upgrade ok");
							} else {
								logger.info("UpgradeDunningLevelStep   ThresholdAmount < invoice.amount");
							}
						} else {
							logger.info("UpgradeDunningLevelStep DelayBeforeProcess : notYet");
						}
					} else {
						logger.info("UpgradeDunningLevelStep  no invoice founded");
					}
				} else {
					logger.info("UpgradeDunningLevelStep  in WaitDuration");
				}
			}
		}

		return isUpgradelevel;
	}

	private OtherCreditAndCharge addOCC(CustomerAccount customerAccount, BigDecimal chargeAmount) throws Exception {

		OCCTemplate dunningOccTemplate = getDunningOCCTemplate(customerAccount.getProvider().getCode());
		OtherCreditAndCharge occ = new OtherCreditAndCharge();
		occ.setAccountCode(dunningOccTemplate.getAccountCode());
		occ.setOccCode(dunningOccTemplate.getCode());
		occ.setOccDescription(dunningOccTemplate.getDescription());
		occ.setTransactionCategory(dunningOccTemplate.getOccCategory());
		occ.setAccountCodeClientSide(dunningOccTemplate.getAccountCodeClientSide());
		occ.setAmount(chargeAmount);
		occ.setUnMatchingAmount(chargeAmount);
		occ.setMatchingAmount(BigDecimal.ZERO);
		occ.setCustomerAccount(customerAccount);
		occ.setMatchingStatus(MatchingStatusEnum.O);
		occ.setTransactionDate(new Date());
		occ.setOperationDate(new Date());
		occ.setDueDate(new Date());
		occ.setProvider(customerAccount.getProvider());
		occ.setAuditable(BayadUtils.getAuditable(BayadUtils.getUserBayadSystem()));
		return occ;

	}

	@SuppressWarnings("unchecked")
	private List<ActionPlanItem> getActionPlanItems(DunningPlan dunningPlan, DunningPlanTransition dunningPlanTransition) {
		List<ActionPlanItem> actionPlanItems = new ArrayList<ActionPlanItem>();
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			actionPlanItems = (List<ActionPlanItem>) em
					.createQuery(
							"from " + ActionPlanItem.class.getSimpleName()
									+ " where dunningPlan.id=:dunningPlanId and dunningLevel=:dunningLevel order by itemOrder")
					.setParameter("dunningPlanId", dunningPlan.getId()).setParameter("dunningLevel", dunningPlanTransition.getDunningLevelTo()).getResultList();
		} catch (Exception e) {
		}
		return actionPlanItems;
	}

	@SuppressWarnings("unchecked")
	private List<RecordedInvoice> getRecordedInvoices(CustomerAccount customerAccount, MatchingStatusEnum o) {
		List<RecordedInvoice> invoices = new ArrayList<RecordedInvoice>();
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			invoices = (List<RecordedInvoice>) em
					.createQuery(
							"from " + RecordedInvoice.class.getSimpleName()
									+ " where customerAccount.id=:customerAccountId and matchingStatus=:matchingStatus order by dueDate")
					.setParameter("customerAccountId", customerAccount.getId()).setParameter("matchingStatus", MatchingStatusEnum.O).getResultList();
		} catch (Exception e) {

		}
		return invoices;
	}

	private DunningPlanTransition getDunningPlanTransition(DunningLevelEnum dunningLevelFrom, DunningLevelEnum dunningLevelTo, DunningPlan dunningPlan) {
		DunningPlanTransition dunningPlanTransition = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			dunningPlanTransition = (DunningPlanTransition) em
					.createQuery(
							"from " + DunningPlanTransition.class.getSimpleName()
									+ " where dunningLevelFrom=:dunningLevelFrom and dunningLevelTo=:dunningLevelTo and dunningPlan.id=:dunningPlanId")
					.setParameter("dunningLevelFrom", dunningLevelFrom).setParameter("dunningLevelTo", dunningLevelTo)
					.setParameter("dunningPlanId", dunningPlan.getId()).getSingleResult();
		} catch (Exception e) {
		}
		return dunningPlanTransition;
	}

	private OCCTemplate getDunningOCCTemplate(String providerCode) throws Exception {
		EntityManager em = MeveoPersistence.getEntityManager();
		return (OCCTemplate) em.createQuery("from " + OCCTemplate.class.getSimpleName() + " where code=:code and provider.code=:providerCode")
				.setParameter("code", BayadConfig.getDunningOccCode()).setParameter("providerCode", providerCode).getSingleResult();

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

	/**
	 * @return the listOCC
	 */
	public List<OtherCreditAndCharge> getListOCC() {
		return listOCC;
	}

	/**
	 * @param listOCC
	 *            the listOCC to set
	 */
	public void setListOCC(List<OtherCreditAndCharge> listOCC) {
		this.listOCC = listOCC;
	}

}
