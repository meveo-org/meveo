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
package org.meveo.bayad.bankfile;

import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.bayad.tip.CreatePaymentTask;
import org.meveo.model.billing.Invoice;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.BankOperation;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.MatchingTypeEnum;
import org.meveo.model.payments.OCCTemplate;
import org.meveo.model.payments.PaymentMethodEnum;
import org.meveo.model.payments.RecordedInvoice;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.service.payments.remote.CustomerAccountServiceRemote;

public class ProcessLineTask implements Callable<Object> {
	private static final Logger log = Logger.getLogger(ProcessLineTask.class);

	EntityManager em = null;

	private BankOperation bankOp = null;

	private CustomerAccountServiceRemote customerAccountServiceRemote;
	private ResourceBundle resource = ResourceBundle.getBundle("messages");

	private String fileName;

	public Object call() throws Exception {
		log.info("BankOperation to process:" + bankOp.toString());
		if (!bankOp.isValid()) {
			log.warn("current bankOperation is not valid:" + bankOp.getErrorMessage());
			throw new Exception(bankOp.getErrorMessage());
		}
		log.info("current operation is valid");
		Invoice invoice = getInvoice(bankOp.getInvocieId());
		if (invoice == null) {
			log.warn("cannot found invoice by id:" + bankOp.getInvocieId());
			throw new Exception(String.format(resource.getString("bankFile.cannotFoundInvoiceById"), bankOp.getInvocieId()));
			
		}

		RecordedInvoice recordedInvoice = getRecordedInvoice(invoice.getInvoiceNumber(), invoice.getProvider().getCode());

		if (recordedInvoice == null) {
			log.warn("cannot found recordedInvoice by reference:" + invoice.getInvoiceNumber());
			throw new Exception(String.format(resource.getString("bankFile.cannotFoundRecordedInvoice"), invoice.getInvoiceNumber()));
		}
		Provider provider = recordedInvoice.getProvider();
		if (provider == null) {
			log.warn("provider is null");
			throw new Exception(resource.getString("bankFile.providerIsNull"));
		}
		log.info("recordedInvoice :" + recordedInvoice.getReference());
		log.info("customerAccount code	:" + recordedInvoice.getCustomerAccount().getCode());
		if (recordedInvoice.getMatchingStatus() == MatchingStatusEnum.L) {
			log.warn("recordedInvoice already matched");
			throw new Exception(resource.getString("bankFile.recordedInvoiceAlreadyMatched"));
		}

		PaymentMethodEnum paymentMethodEnum = PaymentMethodEnum.WIRETRANSFER;

		OCCTemplate occTemplate = null;
		occTemplate = getOCCTemplate(provider.getCode());

		if (occTemplate == null) {
			log.warn("cannot found occTemplate");
			throw new Exception(resource.getString("bankFile.cannotFoundOCCTemplate"));
		}

		CreatePaymentTask createPaymentTask = new CreatePaymentTask();
		createPaymentTask.setCustomerAccountServiceRemote(customerAccountServiceRemote);
		createPaymentTask.setAmount(bankOp.getCredit());
		createPaymentTask.setBankCollectionDate(bankOp.getDateOp());
		createPaymentTask.setBankLot(fileName);
		createPaymentTask.setCustomerAccount(recordedInvoice.getCustomerAccount());
		createPaymentTask.setDepositDate(bankOp.getDateVal());
		createPaymentTask.setDueDate(bankOp.getDateVal());
		createPaymentTask.setEm(em);
		createPaymentTask.setMatchingTypeEnum(MatchingTypeEnum.A);
		createPaymentTask.setOccTemplate(occTemplate);
		createPaymentTask.setPaymentMethodEnum(paymentMethodEnum);
		createPaymentTask.setProvider(provider);
		createPaymentTask.setReference(recordedInvoice.getReference());
		createPaymentTask.setTransactionDate(bankOp.getDateOp());
		createPaymentTask.call();
		return null;
	}

	private RecordedInvoice getRecordedInvoice(String invoiceId, String providerCode) {
		RecordedInvoice recordedInvoice = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			recordedInvoice = (RecordedInvoice) em
					.createQuery("from " + RecordedInvoice.class.getSimpleName() + " where reference=:code and provider.code=:providerCode ")
					.setParameter("code", invoiceId)
					.setParameter("providerCode", providerCode)
					.getSingleResult();
		} catch (Exception e) {
		}
		return recordedInvoice;
	}

	private Invoice getInvoice(String invoiceId) {
		Invoice invoice = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			invoice = (Invoice) em.createQuery("from " + Invoice.class.getSimpleName() + " where invoiceNumber=:invoiceId ")
					.setParameter("invoiceId", invoiceId).getSingleResult();
		} catch (Exception e) {
		}
		return invoice;
	}


	private OCCTemplate getOCCTemplate(String providerCode) {
		return getOCCTemplate(providerCode, BayadConfig.getBankFileOccCode());
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

	public EntityManager getEm() {
		return em;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}



	public BankOperation getBankOp() {
		return bankOp;
	}

	public void setBankOp(BankOperation bankOp) {
		this.bankOp = bankOp;
	}

	public CustomerAccountServiceRemote getCustomerAccountServiceRemote() {
		return customerAccountServiceRemote;
	}

	public void setCustomerAccountServiceRemote(CustomerAccountServiceRemote customerAccountServiceRemote) {
		this.customerAccountServiceRemote = customerAccountServiceRemote;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
