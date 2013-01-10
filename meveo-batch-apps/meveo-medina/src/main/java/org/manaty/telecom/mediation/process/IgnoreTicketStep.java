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
package org.manaty.telecom.mediation.process;

import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRStatus;
import org.manaty.telecom.mediation.MedinaConfig;
import org.manaty.telecom.mediation.context.MediationContext;
import org.manaty.utils.CDRUtils;
import org.manaty.utils.StringUtils;

/**
 * Step that checks if ticket should be ignored.
 * 
 * @author Ignas
 *
 */
public class IgnoreTicketStep extends AbstractProcessStep {

	public IgnoreTicketStep(AbstractProcessStep nextStep) {
		super(nextStep);
	}

	@Override
	protected boolean execute(MediationContext context) {
		CDR cdr = context.getCDR();
		String offerCode = context.getAccess().getSubscription().getOffer().getCode();
		String[] mvnoOfferCodes = MedinaConfig.getMVNOOffers().split(",");
		if (CDRUtils.isSSPTicket(cdr) && !StringUtils.isArrayContainingString(offerCode, mvnoOfferCodes)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ticket IGNORED. SSP ticket offer is not MVNO offer");
            }
            context.setAccepted(false);
            context.setStatus(CDRStatus.IGNORED);
            return false;
        }else if(CDRUtils.isCFTicket(cdr) && !StringUtils.isArrayContainingString(offerCode, mvnoOfferCodes)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ticket IGNORED. CF ticket is ignored for M2M");
            }
            context.setAccepted(false);
            context.setStatus(CDRStatus.IGNORED);
            return false;
        }
		
		return true;
	}

}
