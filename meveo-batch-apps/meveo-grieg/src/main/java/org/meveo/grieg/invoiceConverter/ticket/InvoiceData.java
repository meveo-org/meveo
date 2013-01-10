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
package org.meveo.grieg.invoiceConverter.ticket;

import java.math.BigDecimal;
import java.util.Date;

import org.grieg.ticket.GriegTicket;


/**
 * Invoice data ticket.
 * 
 * @author Ignas Lelys
 * @created Dec 20, 2010
 *
 */
public class InvoiceData implements GriegTicket {

    private long invoiceId;
    private String invoiceNumber;
    private String billingCycleCode;
    private Date invoiceDate;
    private Date dueDate;
    private String customerAccountCode;
    private BigDecimal amountWithTax;
    private String billingAccountCode;
    private BigDecimal balance;
    private BigDecimal netToPay;
    private String paymentMethod;
    private String source;
    private boolean accountTerminated;
    private String billingTemplateName;
    private String customerAccountTitle;
    
    private InvoiceData(Builder builder) {
        this.invoiceId =builder.invoiceId;
        this.billingCycleCode = builder.billingCycleCode;
        this.invoiceDate = builder.invoiceDate;
        this.dueDate = builder.dueDate;
        this.customerAccountCode = builder.customerAccountCode;
        this.amountWithTax = builder.amountWithTax;
        this.invoiceNumber = builder.invoiceNumber;
        this.billingAccountCode = builder.billingAccountCode;
        this.balance = builder.balance;
        this.netToPay = builder.netToPay;
        this.paymentMethod = builder.paymentMethod;
        this.source = builder.source;
        this.accountTerminated=builder.accountTerminated;
        this.billingTemplateName=builder.billingTemplateName;
        this.customerAccountTitle=customerAccountTitle;
    }
    
    public static class Builder {

        private long invoiceId;
        private String invoiceNumber;
        private String billingCycleCode;
        private Date invoiceDate;
        private Date dueDate;
        private String customerAccountCode;
        private BigDecimal amountWithTax;
        private String billingAccountCode;
        private BigDecimal balance;
        private BigDecimal netToPay;
        private String paymentMethod;
        private String source;
        private boolean accountTerminated;
        private String billingTemplateName;
        private String customerAccountTitle;

        public Builder() {
            // set default values if needed
        }
        public Builder addPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }
        public Builder addBillingCycleCode(String billingCycleCode) {
            this.billingCycleCode = billingCycleCode;
            return this;
        }
		public Builder addInvoiceDate(Date invoiceDate) {
			this.invoiceDate = invoiceDate;
			return this;
		}
        public Builder addDueDate(Date dueDate) {
            this.dueDate = dueDate;
            return this;
        }
        public Builder addCustomerAccountCode(String customerAccountCode) {
            this.customerAccountCode = customerAccountCode;
            return this;
        }
        public Builder addAmountWithTax(BigDecimal amountWithTax) {
            this.amountWithTax = amountWithTax;
            return this;
        }
        public Builder addSource(String source) {
            this.source = source;
            return this;
        }
        public Builder addInvoiceId(long invoiceId) {
            this.invoiceId = invoiceId;
            return this;
        }
        public Builder addInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }
        public Builder addBillingAccountCode(String billingAccountCode) {
            this.billingAccountCode = billingAccountCode;
            return this;
        }
        public Builder addNetToPay(BigDecimal netToPay) {
            this.netToPay = netToPay;
            return this;
        }
        public Builder addBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }
        public Builder addAccountTerminated(boolean accountTerminated) {
            this.accountTerminated = accountTerminated;
            return this;
        }
        public Builder addBillingTemplateName( String billingTemplateName) {
            this.billingTemplateName = billingTemplateName;
            return this;
        }
        public Builder addCustomerAccountTitle( String customerAccountTitle) {
            this.customerAccountTitle = customerAccountTitle;
            return this;
        }
        public GriegTicket build() {
            return new InvoiceData(this);
        }
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public String getBillingCycleCode() {
        return billingCycleCode;
    }
    
    public Date getInvoiceDate(){
    	return invoiceDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getCustomerAccountCode() {
        return customerAccountCode;
    }

    public BigDecimal getAmountWithTax() {
        return amountWithTax;
    }
    
    public String getBillingAccountCode() {
        return billingAccountCode;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getNetToPay() {
        return netToPay;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public long getInvoiceId() {
        return invoiceId;
    }
    
    
    
    public boolean isAccountTerminated() {
		return accountTerminated;
	}

	public String getBillingTemplateName() {
		return billingTemplateName;
	}

	public String getCustomerAccountTitle() {
		return customerAccountTitle;
	}

	@Override
    public String toString() {
        return "InvoiceData [amountWithTax=" + amountWithTax + ", billingCycleCode=" + billingCycleCode
                + ", customerAccountCode=" + customerAccountCode + ", invoiceDate=" + invoiceDate+ ", dueDate=" + dueDate + ", invoiceNumber="
                + invoiceNumber + ",invoiceId= "+invoiceId+", source=" + source + "]";
    }
    

}
