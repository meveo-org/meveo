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
package org.meveo.bayad.ticket;

import java.math.BigDecimal;

import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.DunningPlan;

/**
 * Ticket for dunningTask
 * 
 * @author anasseh
 * @created 03.12.2010
 */
public class DunningTicket {
	private CustomerAccount customerAccount;
	private BigDecimal balanceExigible;
	private DunningPlan dunningPlan;
	
	public DunningTicket(){
		
	}
	public CustomerAccount getCustomerAccount() {
		return customerAccount;
	}
	public void setCustomerAccount(CustomerAccount customerAccount) {
		this.customerAccount = customerAccount;
	}
	public BigDecimal getBalanceExigible() {
		return balanceExigible;
	}
	public void setBalanceExigible(BigDecimal balanceDue) {
		this.balanceExigible = balanceDue;
	}
	public DunningPlan getDunningPlan() {
		return dunningPlan;
	}
	public void setDunningPlan(DunningPlan dunningPlan) {
		this.dunningPlan = dunningPlan;
	}
    
	
}
