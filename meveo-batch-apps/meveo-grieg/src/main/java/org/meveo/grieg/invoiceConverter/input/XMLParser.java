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
package org.meveo.grieg.invoiceConverter.input;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

import org.grieg.ticket.GriegTicket;
import org.meveo.core.parser.AbstractXMLParser;
import org.meveo.core.parser.ParserException;
import org.meveo.grieg.invoiceConverter.ticket.InvoiceData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Parser implementation for xml invoice file format.
 * 
 * @author Ignas Lelys
 * @created Dec 17, 2010
 * 
 */
public class XMLParser extends AbstractXMLParser<GriegTicket> {

    private static final String ID_ATTRIBUTE_TAG_NAME = "id";
    
    private static final String NUMBER_ATTRIBUTE_TAG_NAME = "number";

    private static final String AMOUNT_WITH_TAX_TAG_NAME = "amountWithTax";
    
    private static final String BALANCE_TAG_NAME = "balance";
    
    private static final String NET_TO_PAY_TAG_NAME = "netToPay";

    private static final String AMOUNT_TAG_NAME = "amount";

    private static final String INVOICE_DATE_TAG_NAME = "invoiceDate";

    private static final String DUE_DATE_TAG_NAME = "dueDate";
    
    private static final String CUSTOMER_ACCOUNT_TAG_NAME ="customerAccount";

    private static final String BILLING_ACCOUNT_TAG_NAME = "billingAccount";

    private static final String BILLING_CYCLE_CODE_ATTRIBUTE_NAME = "billingCycleCode";

    private static final String CUSTOMER_ACCOUNT_CODE_ATTRIBUTE_NAME = "customerAccountCode";

    private static final String INVOICE_TAG_NAME = "invoice";

    private static final String BILLING_ACCOUNT_CODE_ATTRIBUTE_NAME = "code";

    private static final String PAYMENT_METHOD_TAG_NAME = "paymentMethod";

    private static final String PAYMENT_METHOD_TYPE_ATTRIBUTE_NAME = "type";

    private static final String dueDateFormat = "dd/MM/yyyy";
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat(dueDateFormat);
    
    private NodeList invoiceElements;
    
    private int elementIndex = 0;
    
    @Override
    public GriegTicket next() throws ParserException {
        if (invoiceElements == null) {
            invoiceElements = xmlDocument.getElementsByTagName(INVOICE_TAG_NAME);
        }
        Node invoiceNode = invoiceElements.item(elementIndex++);
        if (invoiceNode != null) {
            InvoiceData.Builder invoiceBuilder = new InvoiceData.Builder();
            
            Element invoiceElement = (Element) invoiceNode;
            invoiceElement.normalize();
            invoiceBuilder.addCustomerAccountCode(invoiceElement.getAttribute(CUSTOMER_ACCOUNT_CODE_ATTRIBUTE_NAME));
            invoiceBuilder.addInvoiceId(Long.parseLong(invoiceElement.getAttribute(ID_ATTRIBUTE_TAG_NAME)));
            invoiceBuilder.addInvoiceNumber(invoiceElement.getAttribute(NUMBER_ATTRIBUTE_TAG_NAME));
            invoiceBuilder.addBillingTemplateName(invoiceElement.getAttribute("templateName"));
            
            Element customerAccountElement = getFirstElement(invoiceElement.getElementsByTagName(CUSTOMER_ACCOUNT_TAG_NAME));
            invoiceBuilder.addAccountTerminated(Boolean.valueOf(customerAccountElement.getAttribute("accountTerminated")));
            String customerAccountTitle = customerAccountElement.getElementsByTagName("quality").item(0).getFirstChild().getNodeValue();
            invoiceBuilder.addCustomerAccountTitle(customerAccountTitle);
            
            
            
            Element billingAccountElement = getFirstElement(invoiceElement.getElementsByTagName(BILLING_ACCOUNT_TAG_NAME));
            invoiceBuilder.addBillingCycleCode(getFieldValue(billingAccountElement.getAttribute(BILLING_CYCLE_CODE_ATTRIBUTE_NAME)));
            invoiceBuilder.addBillingAccountCode(getFieldValue(billingAccountElement.getAttribute(BILLING_ACCOUNT_CODE_ATTRIBUTE_NAME)));

            Element paymentMethodElement = getFirstElement(billingAccountElement.getElementsByTagName(PAYMENT_METHOD_TAG_NAME));
            invoiceBuilder.addPaymentMethod(paymentMethodElement.getAttribute(PAYMENT_METHOD_TYPE_ATTRIBUTE_NAME));
            
            Element invoiceDateElement = getFirstElement(invoiceElement.getElementsByTagName(INVOICE_DATE_TAG_NAME));
            String invoiceDateString = invoiceDateElement.getFirstChild().getNodeValue();
            invoiceBuilder.addInvoiceDate(getDateFieldValue(invoiceDateString, dateFormat, dueDateFormat));

            
            Element dueDatetElement = getFirstElement(invoiceElement.getElementsByTagName(DUE_DATE_TAG_NAME));
            String dueDateString = dueDatetElement.getFirstChild().getNodeValue();
            invoiceBuilder.addDueDate(getDateFieldValue(dueDateString, dateFormat, dueDateFormat));

            Element amountElement = getFirstElement(invoiceElement.getElementsByTagName(AMOUNT_TAG_NAME));
            
            String amountWithTaxValue = amountElement.getElementsByTagName(AMOUNT_WITH_TAX_TAG_NAME).item(0).getFirstChild().getNodeValue();
            amountWithTaxValue = amountWithTaxValue.replaceAll(",", ".").replaceAll("\u00a0", ""); // TODO fixxxx!!!!
            BigDecimal amountWithTax = new BigDecimal(amountWithTaxValue);
            amountWithTax = amountWithTax.setScale(2, RoundingMode.HALF_UP);
            invoiceBuilder.addAmountWithTax(amountWithTax);

            String balanceValue = amountElement.getElementsByTagName(BALANCE_TAG_NAME).item(0).getFirstChild().getNodeValue();
            balanceValue = balanceValue.replaceAll(",", ".").replaceAll("\u00a0", "");
            BigDecimal balance = new BigDecimal(balanceValue);
            balance = balance.setScale(2, RoundingMode.HALF_UP);
            invoiceBuilder.addBalance(balance);

            String netToPayValue = amountElement.getElementsByTagName(NET_TO_PAY_TAG_NAME).item(0).getFirstChild().getNodeValue();
            netToPayValue = netToPayValue.replaceAll(",", ".").replaceAll("\u00a0", "");
            BigDecimal netToPay = new BigDecimal(netToPayValue);
            netToPay = netToPay.setScale(2, RoundingMode.HALF_UP);
            invoiceBuilder.addNetToPay(netToPay);
            
            invoiceBuilder.addSource(getNodeXmlString(invoiceNode));

            InvoiceData data = (InvoiceData)invoiceBuilder.build();
            return data;
        }
        
        elementIndex = 0;
        return null;
    }
    
    
    
}
