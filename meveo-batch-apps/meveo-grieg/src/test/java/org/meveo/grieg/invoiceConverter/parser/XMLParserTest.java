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
package org.meveo.grieg.invoiceConverter.parser;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.meveo.commons.utils.DateUtils;
import org.meveo.core.parser.ParserException;
import org.meveo.grieg.invoiceConverter.input.XMLParser;
import org.meveo.grieg.invoiceConverter.ticket.InvoiceData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Ignas Lelys
 * @created Dec 21, 2010
 *
 */
public class XMLParserTest {
    
    @Test(groups={"unit"})
    public void testNext() throws ParserException {
        XMLParser parser = new XMLParser();
        parser.setParsingFile("src/test/resources/test.xml");
        InvoiceData data1 = (InvoiceData)parser.next();
        Assert.assertEquals(data1.getInvoiceNumber(), "0989362-8");
        Assert.assertEquals(data1.getCustomerAccountCode(), "25-018623");
        Assert.assertEquals(data1.getBillingCycleCode(), "PART1");
        Assert.assertEquals(data1.getAmountWithTax(), new BigDecimal("306.14"));
        Assert.assertEquals(data1.getBalance(), new BigDecimal("111.11"));
        Assert.assertEquals(data1.getNetToPay(), new BigDecimal("124.58"));
        Assert.assertEquals(data1.getPaymentMethod(), "DIRECT_DEBIT");
        Date dueDateExpected = DateUtils.newDate(2010, Calendar.SEPTEMBER, 14, 0, 0, 0);
        Assert.assertEquals(data1.getDueDate(), dueDateExpected);
        InvoiceData data2 = (InvoiceData)parser.next();
        Assert.assertEquals(data2.getInvoiceNumber(), "0989362-9");
        Assert.assertEquals(data2.getCustomerAccountCode(), "25-018623");
        Assert.assertEquals(data2.getBillingCycleCode(), "PRO1");
        Assert.assertEquals(data2.getAmountWithTax(), new BigDecimal("0.00"));
        Assert.assertEquals(data2.getBalance(), new BigDecimal("12.13"));
        Assert.assertEquals(data2.getNetToPay(), new BigDecimal("22.00"));
        Assert.assertEquals(data2.getPaymentMethod(), "TIP");
        Date dueDate2Expected = DateUtils.newDate(2099, Calendar.JANUARY, 1, 0, 0, 0);
        Assert.assertEquals(data2.getDueDate(), dueDate2Expected);
        InvoiceData data3 = (InvoiceData)parser.next();
        Assert.assertNull(data3);
    }

}
