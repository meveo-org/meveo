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
package org.meveo.bayad.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.naming.NamingException;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.meveo.bayad.BayadConfig;
import org.meveo.bayad.tip.CreatePayment;
import org.meveo.bayad.util.BayadUtils;
import org.meveo.bayad.util.DDRequestFileBuilder;
import org.meveo.commons.utils.DateUtils;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.model.admin.User;
import org.meveo.model.crm.Provider;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.DDRequestItem;
import org.meveo.model.payments.DDRequestLOT;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.MatchingTypeEnum;
import org.meveo.model.payments.OCCTemplate;
import org.meveo.model.payments.PaymentMethodEnum;
import org.meveo.model.payments.RecordedInvoice;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.service.payments.remote.CustomerAccountServiceRemote;

/**
 * 
 * @author anasseh
 * @created 06.12.2010
 * 
 */

public class BayadServices {
	private final Logger logger = Logger.getLogger(BayadServices.class);

	private CustomerAccountServiceRemote customerAccountServiceRemote = null;
	private ResourceBundle resource = ResourceBundle.getBundle("messages");

	public BayadServices() {
		try {
			customerAccountServiceRemote = (CustomerAccountServiceRemote) EjbUtils.getRemoteInterface(BayadConfig.getMeveoCustomerAccountServiceJndiName(),
					BayadConfig.getMeveoProviderUrl());
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create DDRequestLOT for all invoices have a DERICT_DEBIT payment method
	 * and due date between fromDueDate and toDueDate.
	 * 
	 * @param fromDueDate
	 * @param toDueDate
	 * @param user
	 * @param provider
	 * @throws Exception
	 */

	public void createDDRquestLot(Date fromDueDate, Date toDueDate, User user, Provider provider) throws Exception {
		logger.info("createDDRquestLot fromDueDate:" + fromDueDate + "  toDueDate:" + toDueDate + "  user:" + (user == null ? "null" : user.getUserName()));

		if (provider == null) {
			throw new Exception(resource.getString("bayadServices.providerIsNull"));
		}
		if (fromDueDate == null) {
			throw new Exception(resource.getString("createDDRquestLot.fromDueDateIsNull"));
		}
		if (toDueDate == null) {
			throw new Exception(resource.getString("createDDRquestLot.toDueDateIsNull"));
		}
		if (fromDueDate.after(toDueDate)) {
			throw new Exception(resource.getString("createDDRquestLot.fromDueDateAfterToDueDate"));
		}
		if (user == null) {
			throw new Exception(resource.getString("bayadServices.userIsNull"));
		}

		List<RecordedInvoice> invoices = getInvoices(fromDueDate, toDueDate, provider.getCode());
		if (invoices == null || invoices.isEmpty()) {
			throw new Exception(resource.getString("createDDRquestLot.noInvoices"));
		}
		logger.info("number invoices : " + invoices.size());
		if (customerAccountServiceRemote == null) {
			throw new Exception(resource.getString("bayadServices.cannotGetBalance"));
		}
		EntityManager em = MeveoPersistence.getEntityManager();
		BigDecimal totalAmount = BigDecimal.ZERO;
		try {
			em.getTransaction().begin();
			DDRequestLOT ddRequestLOT = new DDRequestLOT();
			ddRequestLOT.setProvider(provider);
			ddRequestLOT.setAuditable(BayadUtils.getAuditable(user));
			ddRequestLOT.setInvoicesNumber(invoices.size());
			List<DDRequestItem> ddrequestItems = new ArrayList<DDRequestItem>();
			Map<CustomerAccount, List<RecordedInvoice>> customerAccountInvoices = new HashMap<CustomerAccount, List<RecordedInvoice>>();
			for (RecordedInvoice invoice : invoices) {
				if (customerAccountInvoices.containsKey(invoice.getCustomerAccount())) {
					customerAccountInvoices.get(invoice.getCustomerAccount()).add(invoice);
				} else {
					List<RecordedInvoice> tmp = new ArrayList<RecordedInvoice>();
					tmp.add(invoice);
					customerAccountInvoices.put(invoice.getCustomerAccount(), tmp);
				}
			}
			for (Map.Entry<CustomerAccount, List<RecordedInvoice>> e : customerAccountInvoices.entrySet()) {
				DDRequestItem ddrequestItem = new DDRequestItem();
				BigDecimal totalInvoices = BigDecimal.ZERO;
				/*
				 * BigDecimal balanceDue = customerAccountServiceRemote.
				 * customerAccountBalanceDueWithoutLitigation
				 * (e.getValue().get(0).getCustomerAccount().getId(), null,
				 * e.getValue().get(0).getDueDate());
				 * if(BigDecimal.ZERO.compareTo(balanceDue) == 0){ continue; }
				 * totalAmount = totalAmount.add(balanceDue);
				 */
				BigDecimal amount = e.getValue().get(0).getNetToPay();
				if (amount == null) {
					amount = customerAccountServiceRemote.customerAccountBalanceDueWithoutLitigation(e.getValue().get(0).getCustomerAccount().getId(), null, e
							.getValue().get(0).getDueDate());
				}
				if (BigDecimal.ZERO.compareTo(amount) == 0) {
					continue;
				}
				totalAmount = totalAmount.add(amount);
				ddrequestItem.setAmount(amount);
				ddrequestItem.setBillingAccountName(e.getValue().get(0).getBillingAccountName());
				ddrequestItem.setCustomerAccount(e.getValue().get(0).getCustomerAccount());
				ddrequestItem.setDdRequestLOT(ddRequestLOT);
				ddrequestItem.setDueDate(e.getValue().get(0).getDueDate());
				ddrequestItem.setPaymentInfo(e.getValue().get(0).getPaymentInfo());
				ddrequestItem.setPaymentInfo1(e.getValue().get(0).getPaymentInfo1());
				ddrequestItem.setPaymentInfo2(e.getValue().get(0).getPaymentInfo2());
				ddrequestItem.setPaymentInfo3(e.getValue().get(0).getPaymentInfo3());
				ddrequestItem.setPaymentInfo4(e.getValue().get(0).getPaymentInfo4());
				ddrequestItem.setPaymentInfo5(e.getValue().get(0).getPaymentInfo5());
				ddrequestItem.setProvider(e.getValue().get(0).getProvider());
				ddrequestItem.setReference(e.getValue().get(0).getReference());
				ddrequestItem.setAuditable(BayadUtils.getAuditable(user));
				ddrequestItem.setInvoices(e.getValue());
				for (RecordedInvoice invoice : e.getValue()) {
					totalInvoices = totalInvoices.add(invoice.getAmount());
					invoice.setDdRequestLOT(ddRequestLOT);
					invoice.setDdRequestItem(ddrequestItem);
					invoice.getAuditable().setUpdated(new Date());
					invoice.getAuditable().setUpdater(user);
					em.merge(invoice);
					ddRequestLOT.getInvoices().add(invoice);
					logger.debug("invoice reference : " + invoice.getReference() + " processed");
				}
				ddrequestItem.setAmountInvoices(totalInvoices);
				ddrequestItems.add(ddrequestItem);
				em.persist(ddrequestItem);
			}
			if (!ddrequestItems.isEmpty()) {
				ddRequestLOT.setDdrequestItems(ddrequestItems);
				ddRequestLOT.setInvoicesAmount(totalAmount);
				ddRequestLOT.setFileName(exportDDRequestLot(ddRequestLOT.getId(), ddrequestItems, totalAmount, provider));
				ddRequestLOT.setSendDate(new Date());
				ddRequestLOT.getAuditable().setUpdated(new Date());
				ddRequestLOT.getAuditable().setUpdater(user);
				em.merge(ddRequestLOT);
			} else {
				throw new Exception(resource.getString("createDDRquestLot.amount.zero"));
			}

			createPaymentsForDDRequestLot(ddRequestLOT, user, em);
			em.getTransaction().commit();
			logger.info("ddRequestLOT created , totalAmount:" + ddRequestLOT.getInvoicesAmount());
			logger.info("Successful createDDRquestLot fromDueDate:" + fromDueDate + "  toDueDate:" + toDueDate + " provider:" + provider.getCode());

		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		}
	}

	/**
	 * Export file for DDRequestLOT
	 * 
	 * @param ddRequestLotId
	 * @return fileName generated
	 * @throws Exception
	 */

	public String exportDDRequestLot(Long ddRequestLotId) throws Exception {
		logger.info("exportDDRequestLot ddRequestLotId:" + ddRequestLotId);

		if (ddRequestLotId == null) {
			throw new Exception(resource.getString("bayadServices.ddRequestLotIdIsNull"));
		}
		EntityManager em = MeveoPersistence.getEntityManager();
		DDRequestLOT ddRequestLOT = (DDRequestLOT) em.createQuery("from " + DDRequestLOT.class.getSimpleName() + " where id=:id")
				.setParameter("id", ddRequestLotId).getSingleResult();
		if (ddRequestLOT == null) {
			throw new Exception(String.format(resource.getString("bayadServices.cannotFindDDRequestLot"), ddRequestLotId));
		}
		String fileName = exportDDRequestLot(ddRequestLOT.getId(), ddRequestLOT.getDdrequestItems(), ddRequestLOT.getInvoicesAmount(),
				ddRequestLOT.getProvider());
		logger.info("Successful exportDDRequestLot ddRequestLotId:" + ddRequestLOT.getId() + " , fileName:" + fileName);
		return fileName;
	}

	/**
	 * Create payment for each invoice in DDRequestLOT
	 * 
	 * @param ddRequestLotId
	 * @param provider
	 * @throws Exception
	 */

	private void createPaymentsForDDRequestLot(DDRequestLOT ddRequestLOT, User user, EntityManager em) throws Exception {
		logger.info("createPaymentsForDDRequestLot ddRequestLotId:" + ddRequestLOT.getId() + " user :" + (user == null ? "null" : user.getUserName()));

		if (user == null) {
			throw new Exception(resource.getString("bayadServices.userIsNull"));
		}

		if (ddRequestLOT.isPaymentCreated()) {
			throw new Exception(resource.getString("createPaymentsForDDRequestLot.paymentsAlreadyCreated"));
		}
		if (ddRequestLOT.getInvoices() == null || ddRequestLOT.getInvoices().isEmpty()) {
			throw new Exception(resource.getString("createPaymentsForDDRequestLot.noInvoicesFounded"));
		}
		OCCTemplate directDebitTemplate = getDirectDebitOCCTemplate(ddRequestLOT.getProvider().getCode());

		if (customerAccountServiceRemote == null) {
			throw new Exception(resource.getString("bayadServices.cannotGetBalance"));
		}

		try {
			CreatePayment createPayment = new CreatePayment(em);
			for (DDRequestItem ddrequestItem : ddRequestLOT.getDdrequestItems()) {
				if (BigDecimal.ZERO.compareTo(ddrequestItem.getAmount()) == 0) {
					logger.debug("invoice:" + ddrequestItem.getReference() + " balanceDue:" + BigDecimal.ZERO + " no DIRECTDEBIT transaction ");
					continue;
				}
				boolean addMatching = ddrequestItem.getAmountInvoices().compareTo(ddrequestItem.getAmount()) == 0;
				List list = ddrequestItem.getInvoices();
				createPayment.create(ddrequestItem.getProvider(), PaymentMethodEnum.DIRECTDEBIT, directDebitTemplate, ddrequestItem.getAmount(),
						ddrequestItem.getCustomerAccount(),
						ddrequestItem.getReference(), ddRequestLOT.getFileName(), ddRequestLOT.getSendDate(),
						DateUtils.addDaysToDate(new Date(), BayadConfig.getDateValueAfter()),
						ddRequestLOT.getSendDate(), ddRequestLOT.getSendDate(), list, addMatching, MatchingTypeEnum.A_DERICT_DEBIT);
			}
			ddRequestLOT.setPaymentCreated(true);
			ddRequestLOT.getAuditable().setUpdated(new Date());
			ddRequestLOT.getAuditable().setUpdater(user);
			em.merge(ddRequestLOT);
			logger.info("Successful createPaymentsForDDRequestLot ddRequestLotId:" + ddRequestLOT.getId());

		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * @param fromDueDate
	 * @param toDueDate
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<RecordedInvoice> getInvoices(Date fromDueDate, Date toDueDate, String providerCode) throws Exception {
		EntityManager em = MeveoPersistence.getEntityManager();
		return em
				.createQuery(
						"from " + RecordedInvoice.class.getSimpleName()
								+ " where ddRequestLOT is null and matchingStatus=:matchingStatus and dueDate >=:fromDueDate and"
								+ " dueDate<=:toDueDate and paymentMethod=:paymentMethod  and provider.code=:providerCode ")
				.setParameter("fromDueDate", fromDueDate).setParameter("toDueDate", toDueDate).setParameter("matchingStatus", MatchingStatusEnum.O)
				.setParameter("paymentMethod", PaymentMethodEnum.DIRECTDEBIT).setParameter("providerCode", providerCode).getResultList();
	}

	/**
	 * @param invoices
	 * @throws Exception
	 */
	private String exportDDRequestLot(Long ddRequestId, List<DDRequestItem> ddrequestItems, BigDecimal totalAmount, Provider provider) throws Exception {
		DDRequestFileBuilder ddRequestBuilder = new DDRequestFileBuilder(provider);
		Date dateValue = DateUtils.addDaysToDate(new Date(), BayadConfig.getDateValueAfter());
		String dateValueString = DateUtils.formatDateWithPattern(dateValue, "ddMM");
		String year = DateUtils.formatDateWithPattern(dateValue, "yy");
		year = year.substring(1, 2);
		ddRequestBuilder.addHeader(dateValueString + year, ddRequestId);

		if (customerAccountServiceRemote == null) {
			throw new Exception(resource.getString("bayadServices.cannotGetBalance"));
		}

		for (DDRequestItem ddrequestItem : ddrequestItems) {
			ddRequestBuilder.addLine(provider.getBankCoordinates().getIssuerNumber(), ddrequestItem.getReference(), ddrequestItem.getBillingAccountName(),
					ddrequestItem.getPaymentInfo5() == null ? "        " : ddrequestItem.getPaymentInfo5(), ddrequestItem.getPaymentInfo2(),
					ddrequestItem.getPaymentInfo3(),
							ddrequestItem.getAmount(), ddrequestItem.getPaymentInfo1());
		}
		ddRequestBuilder.addFooter(totalAmount);

		String fileName = String.format(BayadConfig.getDDRequestFileNamePrefix(), ddRequestId);
		fileName += "_" + provider.getCode();
		fileName += DateUtils.formatDateWithPattern(new Date(), BayadConfig.getDDRequestFileNameExtension());
		String outputDir = BayadConfig.getDDRequestOutputDirectory();
		ddRequestBuilder.toFile(outputDir + File.separator + fileName);
		return fileName;
	}

	private OCCTemplate getDirectDebitOCCTemplate(String providerCode) throws Exception {
		EntityManager em = MeveoPersistence.getEntityManager();
		return (OCCTemplate) em.createQuery("from " + OCCTemplate.class.getSimpleName() + " where code=:code and provider.code=:providerCode")
				.setParameter("code", BayadConfig.getDirectDebitOccCode()).setParameter("providerCode", providerCode).getSingleResult();

	}

}