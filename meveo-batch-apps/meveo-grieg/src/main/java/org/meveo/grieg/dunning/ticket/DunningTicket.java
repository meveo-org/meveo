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
package org.meveo.grieg.dunning.ticket;

import org.grieg.ticket.GriegTicket;


/**
 * @author R.AITYAAZZA
 * @created 23 mars 11
 */
public class DunningTicket implements GriegTicket {

    private String actionType;
    private String providerCode;
    private Long idCustomerAccount;
    private String customerAccountCode;
    private String customerAccountDescription;
    private String title;
    private String firstName;
    private String lastName;
    private String invoiceNumber;
    private String sold;
    private String amountWithTax;
    private String invoiceDate;
    private String processDate;
    private String template;
    private String mail;
    private String mailCC;
    private String address1;
    private String address2;
    private String address3;
    private String zipCode;
    private String city;
    private String state;
    private String country;
    private String source;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public Long getIdCustomerAccount() {
        return idCustomerAccount;
    }

    public void setIdCustomerAccount(String idCustomerAccount) {
        this.idCustomerAccount = new Long(idCustomerAccount);
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getAmountWithTax() {
        return amountWithTax;
    }

    public void setAmountWithTax(String amountWithTax) {
        this.amountWithTax = amountWithTax;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getProcessDate() {
        return processDate;
    }

    public void setProcessDate(String processDate) {
        this.processDate = processDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setIdCustomerAccount(Long idCustomerAccount) {
        this.idCustomerAccount = idCustomerAccount;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMailCC() {
        return mailCC;
    }

    public void setMailCC(String mailCC) {
        this.mailCC = mailCC;
    }
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCustomerAccountCode(String customerAccountCode) {
        this.customerAccountCode = customerAccountCode;
    }

    public String getCustomerAccountCode() {
        return customerAccountCode;
    }
    
    public void setCustomerAccountDescription(String customerAccountDescription) {
        this.customerAccountDescription = customerAccountDescription;
    }

    public String getCustomerAccountDescription() {
        return customerAccountDescription;
    }

	public void setSold(String sold) {
		this.sold = sold;
	}

	public String getSold() {
		return sold;
	}

}
