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
package org.myevo.rating.ticket;

import org.meveo.config.ticket.Ticket;
import org.meveo.model.billing.Subscription;
import org.meveo.model.catalog.UsagePricePlanItem;
import org.myevo.rating.model.EDR;

public class EDRTicket implements Ticket {

	private EDR edr;
	private Subscription subscription;
	private UsagePricePlanItem usagePricePlanItem;
	private String source;

	public EDRTicket(EDR edr, String source) {
		super();
		this.edr = edr;
		this.source = source;
	}

	public EDR getEdr() {
		return edr;
	}

	public void setEdr(EDR edr) {
		this.edr = edr;
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
	}

	public UsagePricePlanItem getUsagePricePlanItem() {
		return usagePricePlanItem;
	}

	public void setUsagePricePlanItem(UsagePricePlanItem usagePricePlanItem) {
		this.usagePricePlanItem = usagePricePlanItem;
	}

	public Object getSource() {
		return source;
	}

}
