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
package org.meveo.grieg.invoiceConverter.output;

import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.Invoice;
import org.meveo.model.payments.CustomerAccount;

/**
 * Holder that has file name and all the information needed for that file handling in {@link OutputHandler}.
 * 
 * @author Ignas Lelys
 * @created Jan 12, 2011
 *
 */
public class FileInfoHolder {

    private String fileName;
    
    private Invoice invoice;
    
    private BillingAccount billingAccount;
    
    private CustomerAccount customerAccount;
    
    private String billingTemplate;
    
    
    public FileInfoHolder(String fileName, Invoice invoice, BillingAccount billingAccount, CustomerAccount customerAccount, String billingTemplate) {
        super();
        this.fileName = fileName;
        this.invoice = invoice;
        this.billingAccount = billingAccount;
        this.customerAccount = customerAccount;
        this.billingTemplate = billingTemplate;
    }
    

    public String getFileName() {
        return fileName;
    }
    
    public Invoice getInvoice() {
        return invoice;
    }
    
    public BillingAccount getBillingAccount() {
        return billingAccount;
    }

    public String getBillingTemplate() {
        return billingTemplate;
    }

    public CustomerAccount getCustomerAccount() {
        return customerAccount;
    }
    
    @Override
    public String toString() {
        return "FileInfoHolder [fileName=" + fileName + ", invoice=" + invoice.getInvoiceNumber() + ", billingAccount=" + billingAccount.getCode() + ", customerAccount=" + customerAccount.getCode()
                + ", billingTemplate=" + billingTemplate + "]";
    }
}