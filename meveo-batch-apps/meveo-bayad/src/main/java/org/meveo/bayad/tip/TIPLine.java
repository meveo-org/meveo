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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.meveo.bayad.BayadConfig;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.payments.PaymentMethodEnum;

public class TIPLine {

	private String invoiceId;
	private Date processDate;
	private Date dueDate;
	private String bankCode;
	private String accountName;
	private String codeGuichet;
	private String numCompte;
	private BigDecimal amountTip;
	private Long operationReference = null;
	private boolean RIBModified;
	private String cleRIB;
	private String checkNum;
	private PaymentMethodEnum paymentMethod;
	private boolean valid;
	private String cause;

	public TIPLine(String line) {
		try {
			if (StringUtils.isBlank(line) || line.length() != 240) {
				valid = false;
				cause = "La ligne doit faire 242 caractere";
				return;
			}
			processDate = getDateField(line, 25, 6, new Date());
			if (processDate == null) {
				valid = false;
				cause = "Format processDate est invalide";
				return;
			}
			dueDate = getDateField(line, 13, 6, processDate);
			if (dueDate == null) {
				valid = false;
				cause = "Format dueDate is invalide";
				return;
			}
			bankCode = getField(line, 82, 5);
			accountName = getField(line, 31, 24);
			codeGuichet = getField(line, 87, 5);
			numCompte = getField(line, 92, 11);
			try {
				amountTip = new BigDecimal(getField(line, 103, 16)).divide(new BigDecimal(100));
			} catch (Exception e) {
			}
			if (amountTip == null) {
				valid = false;
				cause = "amountTip is null ";
				return;
			}
			try {
				operationReference = new Long(getField(line, 126, 24).trim());
			} catch (Exception e) {
			}
			if (operationReference == null) {
				valid = false;
				cause = "operationReference is invalid " + (getField(line, 126, 24).trim());
				return;
			}
			RIBModified = !"1".equals(getField(line, 155, 1));
			cleRIB = getField(line, 156, 2);
			paymentMethod = PaymentMethodEnum.TIP;
			if ("F".equals(getField(line, 158, 1))) {
				paymentMethod = PaymentMethodEnum.CHECK;
			}
			if (paymentMethod == null) {
				valid = false;
				cause = "paymentMethod is null";
				return;
			}

			checkNum = getField(line, 163, 15);
			valid = true;
		} catch (Exception e) {
			e.printStackTrace();
			valid = false;
			cause = e.getMessage();
		}

	}

	public Date getDateField(String line, int pos, int length, Date defaultDate) {
		return parseDateWithPattern(getField(line, pos, length), BayadConfig.getTIPDateFormat(), defaultDate);
	}

	public String getField(String line, int pos, int length) {
		return line.substring((pos - 1), ((pos - 1) + length));
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public Date getProcessDate() {
		return processDate;
	}

	public void setProcessDate(Date processDate) {
		this.processDate = processDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getCodeGuichet() {
		return codeGuichet;
	}

	public void setCodeGuichet(String codeGuichet) {
		this.codeGuichet = codeGuichet;
	}

	public BigDecimal getAmountTip() {
		return amountTip;
	}

	public void setAmountTip(BigDecimal amountTip) {
		this.amountTip = amountTip;
	}

	public Long getOperationReference() {
		return operationReference;
	}

	public void setOperationReference(Long operationReference) {
		this.operationReference = operationReference;
	}

	public boolean isRIBModified() {
		return RIBModified;
	}

	public void setRIBModified(boolean rIBModified) {
		RIBModified = rIBModified;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getCleRIB() {
		return cleRIB;
	}

	public void setCleRIB(String cleRIB) {
		this.cleRIB = cleRIB;
	}

	public String getCheckNum() {
		return checkNum;
	}

	public void setCheckNum(String checkNum) {
		this.checkNum = checkNum;
	}

	public String getNumCompte() {
		return numCompte;
	}

	public void setNumCompte(String numCompte) {
		this.numCompte = numCompte;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public PaymentMethodEnum getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethodEnum paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public static Date parseDateWithPattern(String dateValue, String pattern, Date defaultDate) {
		if (StringUtils.isBlank(dateValue)) {
			return null;
		}
		if ("000000".equals(dateValue)) {
			return defaultDate;
		}
		Date result = null;
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		try {
			result = sdf.parse(dateValue);
		} catch (Exception e) {
		}

		return result;
	}
}
