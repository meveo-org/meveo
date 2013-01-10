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
package org.meveo.bayad.invoices;

import java.io.File;
import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.bayad.invoices.exception.ImportInvoiceException;
import org.meveo.bayad.invoices.exception.InvoiceExistException;
import org.meveo.bayad.util.BayadUtils;
import org.meveo.commons.utils.DateUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.billing.Invoice;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.OCCTemplate;
import org.meveo.model.payments.PaymentMethodEnum;
import org.meveo.model.payments.RecordedInvoice;
import org.meveo.persistence.MeveoPersistence;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ImportInvoice {
	private static final Logger logger = Logger.getLogger(ImportInvoice.class);

	public Provider execute(File file) throws ImportInvoiceException, InvoiceExistException {
		EntityManager em = MeveoPersistence.getEntityManager();
		em.getTransaction().begin();
		try {

			InputSource source = new InputSource(file.getAbsolutePath());
			XPathFactory fabrique = XPathFactory.newInstance();
			XPath xpath = fabrique.newXPath();

			NodeList list = (NodeList) xpath.evaluate("//invoice", source, javax.xml.xpath.XPathConstants.NODESET);
			logger.info("list.getLength():" + list.getLength());

			Provider providerForHistory = null;
			for (int i = 0; i < list.getLength(); i++) {
				CustomerAccount customerAccount = null;
				OCCTemplate invoiceTemplate = null;
				RecordedInvoice recordedInvoice = new RecordedInvoice();
				Node node = list.item(i);
				String invoiceId = xpath.evaluate("@id", node);
				if (StringUtils.isBlank(invoiceId)) {
					throw new ImportInvoiceException("Invoice id is null or empty");
				}

				Invoice invoice = getInvoice(new Long(invoiceId));
				if (invoice == null) {
					throw new ImportInvoiceException("Invoice id ," + invoiceId + ", not found");
				}

				if (isRecordedInvoiceExist(invoice.getInvoiceNumber(), invoice.getProvider())) {
					throw new InvoiceExistException("Invoice id" + invoiceId + " already exist");
				}
				try {
					customerAccount = getCustomerAccount(xpath.evaluate("@customerAccountCode", node), invoice.getProvider());
					recordedInvoice.setCustomerAccount(customerAccount);
					recordedInvoice.setProvider(customerAccount.getProvider());
					// set first provider from first customer account
					if (providerForHistory != null) {
						providerForHistory = customerAccount.getProvider();
					}
				} catch (Exception e) {
					throw new ImportInvoiceException("Cannot found customerAccount");
				}

				try {
					invoiceTemplate = getInvoiceTemplate(customerAccount.getProvider().getCode());
				} catch (Exception e) {
					// TODO message fr|en
					throw new ImportInvoiceException("Cannot found OCC Template for invoice");
				}
				recordedInvoice.setReference(invoice.getInvoiceNumber());
				recordedInvoice.setAccountCode(invoiceTemplate.getAccountCode());
				recordedInvoice.setOccCode(invoiceTemplate.getCode());
				recordedInvoice.setOccDescription(invoiceTemplate.getDescription());
				recordedInvoice.setTransactionCategory(invoiceTemplate.getOccCategory());
				recordedInvoice.setAccountCodeClientSide(invoiceTemplate.getAccountCodeClientSide());

				try {
					recordedInvoice.setAmount(getAmountValue(xpath.evaluate("amount/amountWithTax", node)));
					recordedInvoice.setUnMatchingAmount(getAmountValue(xpath.evaluate("amount/amountWithTax", node)));
					recordedInvoice.setMatchingAmount(BigDecimal.ZERO);
				} catch (Exception e) {
					throw new ImportInvoiceException("Error on amountWithTax");
				}
				try {
					recordedInvoice.setAmountWithoutTax(getAmountValue(xpath.evaluate("amount/amountWithoutTax", node)));
				} catch (Exception e) {
					throw new ImportInvoiceException("Error on amountWithoutTax");
				}
				try {
					recordedInvoice.setNetToPay(getAmountValue(xpath.evaluate("amount/netToPay", node)));
				} catch (Exception e) {
					throw new ImportInvoiceException("Error on netToPay");
				}

				try {
					recordedInvoice.setDueDate(DateUtils.parseDateWithPattern(xpath.evaluate("header/dueDate", node), BayadConfig.getDateFormatInvoicesFile()));
				} catch (Exception e) {
					throw new ImportInvoiceException("Error on DueDate");
				}
				try {
					recordedInvoice.setInvoiceDate(DateUtils.parseDateWithPattern(xpath.evaluate("header/invoiceDate", node),
							BayadConfig.getDateFormatInvoicesFile()));
					recordedInvoice.setTransactionDate(DateUtils.parseDateWithPattern(xpath.evaluate("header/invoiceDate", node),
							BayadConfig.getDateFormatInvoicesFile()));

				} catch (Exception e) {
					throw new ImportInvoiceException("Error on invoiceDate");
				}
				try {
					recordedInvoice.setPaymentMethod(PaymentMethodEnum.valueOf(xpath.evaluate("header/billingAccount/paymentMethod/@type", node)));
				} catch (Exception e) {
					throw new ImportInvoiceException("Error on paymentMethod");
				}
				try {
					recordedInvoice.setTaxAmount(getAmountValue(xpath.evaluate("amount/taxes/@total", node)));
				} catch (Exception e) {
					throw new ImportInvoiceException("Error on total tax");
				}
				recordedInvoice.setPaymentInfo(xpath.evaluate("header/billingAccount/paymentMethod/bankCoordinates/IBAN", node));
				recordedInvoice.setPaymentInfo1(xpath.evaluate("header/billingAccount/paymentMethod/bankCoordinates/bankCode", node));
				recordedInvoice.setPaymentInfo2(xpath.evaluate("header/billingAccount/paymentMethod/bankCoordinates/branchCode", node));
				recordedInvoice.setPaymentInfo3(xpath.evaluate("header/billingAccount/paymentMethod/bankCoordinates/accountNumber", node));
				recordedInvoice.setPaymentInfo4(xpath.evaluate("header/billingAccount/paymentMethod/bankCoordinates/key", node));
				recordedInvoice.setPaymentInfo5(xpath.evaluate("header/billingAccount/paymentMethod/bankCoordinates/bankName", node));

				recordedInvoice.setBillingAccountName(xpath.evaluate("header/billingAccount/paymentMethod/bankCoordinates/accountOwner", node));
				recordedInvoice.setMatchingStatus(MatchingStatusEnum.O);
				recordedInvoice.setAuditable(BayadUtils.getAuditable(BayadUtils.getUserBayadSystem()));

				em.persist(recordedInvoice);
				logger.info("persit invoice ok");

			}
			em.getTransaction().commit();

			return providerForHistory;
		} catch (Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
			if (e instanceof InvoiceExistException) {
				throw new InvoiceExistException(e.getMessage());
			}
			throw new ImportInvoiceException(e.getMessage());
		}

	}

	private CustomerAccount getCustomerAccount(String customerAccountCode, Provider provider) throws Exception {
		EntityManager em = MeveoPersistence.getEntityManager();
		return (CustomerAccount) em.createQuery("from " + CustomerAccount.class.getSimpleName() + " where code =:code and provider=:provider")
				.setParameter("code", customerAccountCode).setParameter("provider", provider).getSingleResult();
	}

	private Invoice getInvoice(Long invoiceId) {
		Invoice invoice = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			invoice = (Invoice) em.createQuery("from " + Invoice.class.getSimpleName() + " where id =:id")
					.setParameter("id", invoiceId).getSingleResult();
		} catch (Exception e) {
		}
		return invoice;
	}

	private boolean isRecordedInvoiceExist(String reference, Provider provider) {
		RecordedInvoice recordedInvoice = null;
		try {
			EntityManager em = MeveoPersistence.getEntityManager();
			recordedInvoice = (RecordedInvoice) em
					.createQuery("from " + RecordedInvoice.class.getSimpleName() + " where reference =:reference and provider=:provider")
					.setParameter("reference", reference).setParameter("provider", provider).getSingleResult();
		} catch (Exception e) {
		}
		return recordedInvoice != null;
	}

	private OCCTemplate getInvoiceTemplate(String providerCode) throws Exception {
		EntityManager em = MeveoPersistence.getEntityManager();
		return (OCCTemplate) em.createQuery("from " + OCCTemplate.class.getSimpleName() + " where code=:code and provider.code=:providerCode")
				.setParameter("code", BayadConfig.getInvoiceOccCode()).setParameter("providerCode", providerCode).getSingleResult();

	}

	private BigDecimal getAmountValue(String amount) throws Exception {
		return new BigDecimal(amount.replaceAll(",", ".").replaceAll(" ", "").replaceAll("\u00A0", "").replaceAll("\u0020", ""));
	}
}