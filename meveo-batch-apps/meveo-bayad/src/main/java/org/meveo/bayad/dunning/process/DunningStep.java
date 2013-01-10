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
/**
 * 
 */
package org.meveo.bayad.dunning.process;

import org.meveo.bayad.ticket.DunningTicket;
import org.meveo.model.payments.CustomerAccount;

/**
 * @author anasseh
 * 
 */
public class DunningStep {

    private DunningTicket dunningTicket;
    private CustomerAccount customerAccountUpdated;

    public DunningStep() {

    }

    public void setDunningTicket(DunningTicket dunningTicket) {
        this.dunningTicket = dunningTicket;
    }

    public DunningTicket getDunningTicket() {
        return dunningTicket;
    }

    public void setCustomerAccountUpdated(CustomerAccount customerAccountUpdated) {
        this.customerAccountUpdated = customerAccountUpdated;
    }

    public CustomerAccount getCustomerAccountUpdated() {
        return customerAccountUpdated;
    }

}
