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
package org.meveo.bayad.dunning.process;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.DunningLevelEnum;

/**
 * downgrade dunninglevel for one customerAccount
 * 
 * @author anasseh
 * @created 03.12.2010
 * 
 */
public class DowngradeDunningLevelStep extends DunningStep {

    private static final Logger logger = Logger.getLogger(DowngradeDunningLevelStep.class);

    public boolean execute()throws Exception {
        logger.info("DowngradeDunningLevelStep ...");
        boolean isDowngradelevel = false;
        CustomerAccount customerAccount = getDunningTicket().getCustomerAccount();        
        if (getDunningTicket().getBalanceExigible().compareTo(BigDecimal.ZERO) <= 0 && customerAccount.getDunningLevel() != DunningLevelEnum.R0) {
            customerAccount.setDunningLevel(DunningLevelEnum.R0);
            customerAccount.setDateDunningLevel(new Date());

            setCustomerAccountUpdated(customerAccount);
            isDowngradelevel = true;
            logger.info("customerAccount code:"+customerAccount.getCode()+" updated to R0");
        }
            // attente besoin pour par exp : R3--> R2 avec actions
        
        return isDowngradelevel;
    }
}
