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
package org.meveo.rating.inputloader;

import java.util.List;

import org.meveo.model.billing.ChargeApplication;
import org.meveo.model.billing.RatedTransaction;

/**
 * @author Seb
 * @created Apr 12, 2011
 * 
 */
public class ChargeAndTransactionInputObject {

    private List<ChargeApplication> chargeApplications;
    
    private List<RatedTransaction> ratedTransactions;

    
    public List<ChargeApplication> getChargeApplications() {
        return chargeApplications;
    }

    public void setChargeApplications(List<ChargeApplication> chargeApplications) {
        this.chargeApplications = chargeApplications;
    }

    public List<RatedTransaction> getRatedTransactions() {
        return ratedTransactions;
    }

    public void setRatedTransactions(List<RatedTransaction> ratedTransactions) {
        this.ratedTransactions = ratedTransactions;
    }

}
