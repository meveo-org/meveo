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
package org.meveo.bayad.tip;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.util.BayadUtils;
import org.meveo.model.AuditableEntity;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.AccountOperation;
import org.meveo.model.payments.AutomatedPayment;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.MatchingAmount;
import org.meveo.model.payments.MatchingCode;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.MatchingTypeEnum;
import org.meveo.model.payments.OCCTemplate;
import org.meveo.model.payments.PaymentMethodEnum;

/** 
 *
 * @deprecated  use CreatePaymentTask
 *
 */
@Deprecated
public class CreatePayment {
	private static final Logger log = Logger.getLogger(CreatePayment.class);

	EntityManager em = null;

	public CreatePayment(EntityManager em) {
		this.em = em;
	}

	public void create(Provider provider, PaymentMethodEnum paymentMethodEnum, OCCTemplate occTemplate, BigDecimal amount, CustomerAccount customerAccount,
			String reference, String bankLot, Date depositDate, Date bankCollectionDate, Date dueDate, Date transactionDate,
			List listOCCforMatching, boolean isToMatching, MatchingTypeEnum matchingTypeEnum)
			throws Exception {
		log.info("create payment for amount:" + amount + " paymentMethodEnum:" + paymentMethodEnum + " isToMatching:" + isToMatching + "  customerAccount:"
				+ customerAccount.getCode() + "...");
		AutomatedPayment automatedPayment = new AutomatedPayment();
		automatedPayment.setProvider(provider);
		automatedPayment.setPaymentMethod(paymentMethodEnum);
		automatedPayment.setAmount(amount);
		automatedPayment.setUnMatchingAmount(amount);
		automatedPayment.setMatchingAmount(BigDecimal.ZERO);
		automatedPayment.setAccountCode(occTemplate.getAccountCode());
		automatedPayment.setOccCode(occTemplate.getCode());
		automatedPayment.setOccDescription(occTemplate.getDescription());
		automatedPayment.setTransactionCategory(occTemplate.getOccCategory());
		automatedPayment.setAccountCodeClientSide(occTemplate.getAccountCodeClientSide());
		automatedPayment.setCustomerAccount(customerAccount);
		automatedPayment.setReference(reference);
		automatedPayment.setBankLot(bankLot);
		automatedPayment.setDepositDate(depositDate);
		automatedPayment.setBankCollectionDate(bankCollectionDate);
		automatedPayment.setDueDate(dueDate);
		automatedPayment.setTransactionDate(transactionDate);
		automatedPayment.setAuditable(BayadUtils.getAuditable(BayadUtils.getUserBayadSystem()));
		automatedPayment.setMatchingStatus(MatchingStatusEnum.O);
		em.persist(automatedPayment);
		listOCCforMatching.add(automatedPayment);

		if (isToMatching) {
			MatchingCode matchingCode = new MatchingCode();
			BigDecimal amountToMatch = BigDecimal.ZERO;
			for (int i = 0; i < listOCCforMatching.size(); i++) {
				AccountOperation accountOperation = (AccountOperation) listOCCforMatching.get(i);
				amountToMatch = accountOperation.getUnMatchingAmount();
				accountOperation.setMatchingAmount(accountOperation.getMatchingAmount().add(amountToMatch));
				accountOperation.setUnMatchingAmount(accountOperation.getUnMatchingAmount().subtract(amountToMatch));
				accountOperation.setMatchingStatus(MatchingStatusEnum.L);
				accountOperation.getAuditable().setUpdated(new Date());
				accountOperation.getAuditable().setUpdater(BayadUtils.getUserBayadSystem());
				em.merge(accountOperation);
				MatchingAmount matchingAmount = new MatchingAmount();
				matchingAmount.setProvider(accountOperation.getProvider());
				((AuditableEntity) matchingAmount).updateAudit(BayadUtils.getUserBayadSystem());
				matchingAmount.setAccountOperation(accountOperation);
				matchingAmount.setMatchingCode(matchingCode);
				matchingAmount.setMatchingAmount(amountToMatch);
				accountOperation.getMatchingAmounts().add(matchingAmount);
				matchingCode.getMatchingAmounts().add(matchingAmount);
			}
			matchingCode.setMatchingAmountDebit(amount);
			matchingCode.setMatchingAmountCredit(amount);
			matchingCode.setMatchingDate(new Date());
			matchingCode.setMatchingType(matchingTypeEnum);
			matchingCode.setAuditable(BayadUtils.getAuditable(BayadUtils.getUserBayadSystem()));
			matchingCode.setProvider(provider);
			em.persist(matchingCode);
			log.info("matching created  for 1 automatedPayment and " + (listOCCforMatching.size() - 1) + " occ");
		} else {
			log.info("no matching created ");
		}
		log.info("automatedPayment created for amount:" + automatedPayment.getAmount());
	}

}
