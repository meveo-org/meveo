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
package org.meveo.rating.ticket;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.meveo.model.billing.ChargeApplication;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.vertina.ticket.VertinaTicket;

/**
 * Vertina DATA ticket.
 * 
 * @author Ignas Lelys
 * @created Apr 21, 2010
 * 
 */
public class RatingTicket implements VertinaTicket {

	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
    public ChargeApplication chargeApplication;
    public RatedTransaction ratedTransaction;
    public String providerCode;
    public String chargeCode;
	public BigDecimal amountWithoutTax;
	public BigDecimal amount2;
	public Date applicationDate;
	public Date subscriptionDate;
	public String criteria1;
	public String criteria2;
	public String criteria3;

    public RatingTicket(ChargeApplication chargeApplication,RatedTransaction ratedTransaction) {
        this.chargeApplication = chargeApplication;
        this.ratedTransaction = ratedTransaction;
    }

    public String toString(){
    	return String.format("ChargeCode = %s, AmountWithoutTax = %s, Amount2 = %s, "
				+ "C1 = %s, C2 = %s, C3 = %s, ApplicationDate = %s, SubscriptionDate = %s", chargeCode,
				amountWithoutTax, amount2, criteria1, criteria2,
				criteria3, applicationDate, subscriptionDate);
    }
}
