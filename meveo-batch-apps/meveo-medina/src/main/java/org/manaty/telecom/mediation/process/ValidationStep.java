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
import org.manaty.telecom.mediation.context.MediationContext;
import org.manaty.telecom.mediation.validator.Validator;
import org.manaty.telecom.mediation.validator.ValidatorFactory;

/**
 * Get validator according to CDR type and validate CDR.
 * 
 * @author Donatas Remeika
 * @created Mar 10, 2009
 */
public class ValidationStep extends AbstractProcessStep {

    public ValidationStep(AbstractProcessStep nextStep) {
        super(nextStep);
    }

    /**
     * Does validation logic.
     */
    @Override
    protected boolean execute(MediationContext context) {
        Validator validator = ValidatorFactory.getValidator(context.getType());
        CDR cdr = context.getCDR();

        if (validator == null) {
            return true;
        }

        boolean valid = validator.validate(cdr);

        if (!valid) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ticket rejected in Validation: invalid format.");
            }
            context.setAccepted(false);
            // AK
            // if (SMSValidator.isSmsmoRoamingGuepard(cdr)) {
            // context.setStatus(CDRStatus.SMSMO_ROAMING_GUEPARD);
            // } else {
            context.setStatus(CDRStatus.INVALID_FORMAT);
            // }
        }

        return valid;
    }

}
