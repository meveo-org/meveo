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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.meveo.bayad.util.BayadUtils;
import org.meveo.commons.utils.QueryBuilder;
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
import org.meveo.persistence.MeveoPersistence;
import org.meveo.service.payments.remote.CustomerAccountServiceRemote;

public class CreatePaymentTask implements Callable<Object> {
	private static final Logger log = Logger.getLogger(CreatePaymentTask.class);

	EntityManager em = null;
	CustomerAccountServiceRemote customerAccountServiceRemote = null;
	Provider provider;
	PaymentMethodEnum paymentMethodEnum;
	OCCTemplate occTemplate;
	BigDecimal amount;
	CustomerAccount customerAccount;
	String reference;
	String bankLot;
	Date depositDate;
	Date bankCollectionDate;
	Date dueDate;
	Date transactionDate;
	MatchingTypeEnum matchingTypeEnum;

	@Override
	public Object call() throws Exception {

		log.info("create payment for amount:" + amount + " paymentMethodEnum:" + paymentMethodEnum + "  customerAccount:"
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

		boolean isToMatching = false;
		List listOCCforMatching = new ArrayList();

		Date dateBalance = new Date();
		BigDecimal balance = customerAccountServiceRemote.customerAccountBalanceDueWithoutLitigation(customerAccount.getId(), null, dateBalance);
		if (amount.compareTo(balance) == 0) {
			isToMatching = true;
			listOCCforMatching = getOCCs(customerAccount, false, dateBalance, MatchingStatusEnum.O, MatchingStatusEnum.P);
		}
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
		return null;
	}

	private List<AccountOperation> getOCCs(CustomerAccount customerAccount, boolean isDueDate, Date to, MatchingStatusEnum... status) throws Exception {
		log.info("getOCC  customerAccount:" + (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:" + to);
		List<AccountOperation> listOCC = new ArrayList<AccountOperation>();
		QueryBuilder queryBuilder = new QueryBuilder("from AccountOperation");
		if (isDueDate) {
			queryBuilder.addCriterion("dueDate", "<=", to, false);
		} else {
			queryBuilder.addCriterion("transactionDate", "<=", to, false);
		}
		queryBuilder.addCriterionEntity("customerAccount", customerAccount);
		if (status.length == 1) {
			queryBuilder.addCriterionEnum("matchingStatus", status[0]);
		} else {
			queryBuilder.startOrClause();
			for (MatchingStatusEnum st : status) {
				queryBuilder.addCriterionEnum("matchingStatus", st);
			}
			queryBuilder.endOrClause();
		}
		if (isDueDate) {
			queryBuilder.addOrderCriterion("dueDate", false);
		} else {
			queryBuilder.addOrderCriterion("transactionDate", false);
		}
		Query query = queryBuilder.getQuery(MeveoPersistence.getEntityManager());
		listOCC = (List<AccountOperation>) query.getResultList();
		return listOCC;
	}

	public EntityManager getEm() {
		return em;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public CustomerAccountServiceRemote getCustomerAccountServiceRemote() {
		return customerAccountServiceRemote;
	}

	public void setCustomerAccountServiceRemote(CustomerAccountServiceRemote customerAccountServiceRemote) {
		this.customerAccountServiceRemote = customerAccountServiceRemote;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public PaymentMethodEnum getPaymentMethodEnum() {
		return paymentMethodEnum;
	}

	public void setPaymentMethodEnum(PaymentMethodEnum paymentMethodEnum) {
		this.paymentMethodEnum = paymentMethodEnum;
	}

	public OCCTemplate getOccTemplate() {
		return occTemplate;
	}

	public void setOccTemplate(OCCTemplate occTemplate) {
		this.occTemplate = occTemplate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public CustomerAccount getCustomerAccount() {
		return customerAccount;
	}

	public void setCustomerAccount(CustomerAccount customerAccount) {
		this.customerAccount = customerAccount;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getBankLot() {
		return bankLot;
	}

	public void setBankLot(String bankLot) {
		this.bankLot = bankLot;
	}

	public Date getDepositDate() {
		return depositDate;
	}

	public void setDepositDate(Date depositDate) {
		this.depositDate = depositDate;
	}

	public Date getBankCollectionDate() {
		return bankCollectionDate;
	}

	public void setBankCollectionDate(Date bankCollectionDate) {
		this.bankCollectionDate = bankCollectionDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public MatchingTypeEnum getMatchingTypeEnum() {
		return matchingTypeEnum;
	}

	public void setMatchingTypeEnum(MatchingTypeEnum matchingTypeEnum) {
		this.matchingTypeEnum = matchingTypeEnum;
	}

	public static Logger getLog() {
		return log;
	}


}
