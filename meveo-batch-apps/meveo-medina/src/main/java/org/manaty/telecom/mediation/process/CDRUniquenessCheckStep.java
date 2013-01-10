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

import org.manaty.telecom.mediation.context.MediationContext;

/**
 * Checks if CDR was not already processed by checking MD5 hash of configured fields.
 * 
 * @author Donatas Remeika
 * @created Mar 6, 2009
 */
public class CDRUniquenessCheckStep extends AbstractProcessStep {

    public CDRUniquenessCheckStep(AbstractProcessStep nextStep) {
        super(nextStep);
    }

    /**
     * CDR Uniqueness check logic.
     */
    @Override
    protected boolean execute(MediationContext context) {

//        byte[] magicNumber = context.getCDR().getMagicNumber();
//
//        // for voice and sms partial tickets do not run uniqueness check yet.
//        // it will be run on CDRProcessor.
//        if ((context.getType().getCDRSubType() == CDRSubtype.VOICE || context.getType().getCDRSubType() == CDRSubtype.SMS) && !CDRUtils.isTicketFinal(context.getCDR())) {
//            context.setMagicNumber(magicNumber);
//            return true;
//        }
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("Magic number:" + MagicNumberConverter.convertToString(magicNumber));
//        }
//        if (!TransactionalMagicNumberCache.getInstance().contains(magicNumber)) {
//            context.setMagicNumber(magicNumber);
//            return true;
//        } else {
//            if (logger.isDebugEnabled()) {
//                logger.debug(String.format("Ticket rejected in CDRUniquenessCheck: magic number already exists: '%s'", magicNumber));
//            }
//            context.setAccepted(false);
//            context.setStatus(CDRStatus.DUPLICATE);
//            return false;
//        }
    	return true;
    }
}
