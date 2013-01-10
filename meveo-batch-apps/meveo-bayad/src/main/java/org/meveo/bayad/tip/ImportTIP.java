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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.Invoice;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.AccountOperation;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.MatchingTypeEnum;
import org.meveo.model.payments.OCCTemplate;
import org.meveo.model.payments.OperationCategoryEnum;
import org.meveo.model.payments.PaymentMethodEnum;
import org.meveo.model.payments.RecordedInvoice;
import org.meveo.persistence.MeveoPersistence;

public class ImportTIP {
	private static final Logger log = Logger.getLogger(ImportTIP.class);

	public Provider execute(File file) throws Exception {
		EntityManager em = MeveoPersistence.getEntityManager();
		em.getTransaction().begin();
		try {
			BufferedReader buf = null;
			buf = new BufferedReader(new FileReader(file));
			String line = null;
			int numLine = 0;
			OCCTemplate occTemplate = null;
			Provider provider = null;
			CreatePayment createPayment = new CreatePayment(em);

			while ((line = buf.readLine()) != null) {
				numLine++;
				if (!line.startsWith("06")) {
					continue;
				}
				TIPLine tipLine = new TIPLine(line);
				if (!tipLine.isValid()) {
					log.warn("current line is not valid:" + tipLine.getCause());
					throw new Exception(tipLine.getCause());
				}
				log.info("current line is valid");
				Invoice invoice = getInvoice(tipLine.getOperationReference());
				if (invoice == null) {
					log.warn("cannot found invoice by id:" + tipLine.getOperationReference());
					throw new Exception("cannot found invoice by id:" + tipLine.getOperationReference());
				}

				RecordedInvoice recordedInvoice = getRecordedInvoice(invoice.getInvoiceNumber());

				if (recordedInvoice == null) {
					log.warn("cannot found recordedInvoice by reference:" + invoice.getInvoiceNumber());
					throw new Exception("cannot found recordedInvoice by reference:" + invoice.getInvoiceNumber());
				}
				provider = recordedInvoice.getProvider();
				if (provider == null) {
					log.warn("provider is null");
					throw new Exception("provider is null");
				}
				log.debug("recordedInvoice :" + recordedInvoice.getReference());
				if (recordedInvoice.getMatchingStatus() == MatchingStatusEnum.L) {
					log.warn("recordedInvoice already matched");
					throw new Exception("recordedInvoice already matched");
				}
				if (tipLine.isRIBModified()) {
					log.info("RIB is Modified");
					BillingAccount billingAccount = invoice.getBillingAccount();
					billingAccount.getBankCoordinates().setAccountNumber(tipLine.getNumCompte());
					billingAccount.getBankCoordinates().setBankCode(tipLine.getBankCode());
					billingAccount.getBankCoordinates().setBranchCode(tipLine.getCodeGuichet());
					billingAccount.getBankCoordinates().setKey(tipLine.getCleRIB());
					billingAccount.getBankCoordinates().setAccountOwner(tipLine.getAccountName());
					em.merge(billingAccount);
					log.info("update rib in billingAccunt ok");
				}
				PaymentMethodEnum paymentMethodEnum = tipLine.getPaymentMethod();

				if (paymentMethodEnum == PaymentMethodEnum.TIP) {
					log.info("PaymentMethod:" + PaymentMethodEnum.TIP);
					occTemplate = getTipOCCTemplate(provider.getCode());
				} else {
					log.info("PaymentMethod:" + PaymentMethodEnum.CHECK);
					occTemplate = getCheckOCCTemplate(provider.getCode());
				}
				if (occTemplate == null) {
					log.warn("cannot found occTemplate");
					throw new Exception("cannot found occTemplate ");
				}
				boolean isToMatching = false;
				List list = new ArrayList();
				log.info("amout Tip:" + tipLine.getAmountTip());
				if (tipLine.getAmountTip().compareTo(recordedInvoice.getUnMatchingAmount()) == 0) {
					list.add(recordedInvoice);
					log.info("amout Tip = amount 1 invoice : matching this 2 occ");
					isToMatching = true;
				} else {
					if (tipLine.getAmountTip().compareTo(recordedInvoice.getUnMatchingAmount()) > 0) {
						log.info("amout Tip > amount 1 invoice : search others invoices to matching ...");
						List<AccountOperation> occs = getOCC(recordedInvoice.getCustomerAccount(), OperationCategoryEnum.DEBIT,
										true, new Date(), MatchingStatusEnum.O, MatchingStatusEnum.P, MatchingStatusEnum.I);
						List<AccountOperation> occToMatch = getOccForAmout(occs, tipLine.getAmountTip());
						if (occToMatch != null) {
							log.info("amout Tip > amount 1 invoice : found " + occToMatch.size() + " occ for matching");
							list.addAll(occToMatch);
							isToMatching = true;
						} else {
							log.info("amout Tip > amount 1 invoice :  occ for matching not found");
						}
					}
					log.info("amout Tip < amount  invoice");
				}
				createPayment.create(provider, paymentMethodEnum, occTemplate, tipLine.getAmountTip(), recordedInvoice.getCustomerAccount(),
						recordedInvoice.getReference(), file.getName(), tipLine.getProcessDate(), tipLine.getProcessDate(), tipLine.getDueDate(),
						tipLine.getProcessDate(), list, isToMatching, MatchingTypeEnum.A_TIP);

			}
			em.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				em.getTransaction().rollback();
			} catch (Exception et) {
				et.printStackTrace();
			}
			throw e;
		}
		return null;

	}

	private RecordedInvoice getRecordedInvoice(String invoiceId) {
		RecordedInvoice recordedInvoice = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			recordedInvoice = (RecordedInvoice) em.createQuery("from " + RecordedInvoice.class.getSimpleName() + " where reference=:code ")
					.setParameter("code", invoiceId).getSingleResult();
		} catch (Exception e) {
		}
		return recordedInvoice;
	}

	private Invoice getInvoice(Long invoiceId) {
		Invoice invoice = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			invoice = (Invoice) em.createQuery("from " + Invoice.class.getSimpleName() + " where id=:invoiceId ")
					.setParameter("invoiceId", invoiceId).getSingleResult();
		} catch (Exception e) {
		}
		return invoice;
	}

	private OCCTemplate getCheckOCCTemplate(String providerCode) {
		return getOCCTemplate(providerCode, BayadConfig.getCheckOccCode());
	}

	private OCCTemplate getTipOCCTemplate(String providerCode) {
		return getOCCTemplate(providerCode, BayadConfig.getTIPOccCode());
	}

	private OCCTemplate getOCCTemplate(String providerCode, String codeOCC) {
		OCCTemplate occTemplate = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			occTemplate = (OCCTemplate) em.createQuery("from " + OCCTemplate.class.getSimpleName() + " where code=:code and provider.code=:providerCode")
					.setParameter("code", codeOCC).setParameter("providerCode", providerCode).getSingleResult();
		} catch (Exception e) {
		}
		return occTemplate;
	}

	private List<AccountOperation> getOccForAmout(List<AccountOperation> occs, BigDecimal amountTip) {
		List<AccountOperation> occsResult = new ArrayList<AccountOperation>();
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (AccountOperation occ : occs) {
			totalAmount = totalAmount.add(occ.getUnMatchingAmount());
			occsResult.add(occ);
			if (totalAmount.equals(amountTip)) {
				return occsResult;
			}
		}
		return null;
	}

	public List<AccountOperation> getOCC(CustomerAccount customerAccount, OperationCategoryEnum operationCategoryEnum, boolean isDue, Date to,
			MatchingStatusEnum... status) throws Exception {
		log.info("getOCC  customerAccount:" + (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:" + to + "  operationCategoryEnum:"
				+ operationCategoryEnum);
		List<AccountOperation> listOCC = new ArrayList<AccountOperation>();
		QueryBuilder queryBuilder = new QueryBuilder("from AccountOperation");
		queryBuilder.addCriterionEnum("transactionCategory", operationCategoryEnum);
		if (isDue) {
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
		if (isDue) {
			queryBuilder.addOrderCriterion("dueDate", false);
		} else {
			queryBuilder.addOrderCriterion("transactionDate", false);
		}
		Query query = queryBuilder.getQuery(MeveoPersistence.getEntityManager());
		listOCC = (List<AccountOperation>) query.getResultList();
		return listOCC;
	}

}
