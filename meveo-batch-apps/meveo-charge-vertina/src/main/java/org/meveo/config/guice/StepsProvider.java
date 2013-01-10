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
package org.meveo.config.guice;

import org.meveo.config.MeveoConfig;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.ValidationStep;
import org.meveo.core.validator.Validator;
import org.meveo.rating.process.RatingPriceStep;
import org.meveo.rating.process.TransactionCreationStep;
import org.meveo.rating.ticket.RatingTicket;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class StepsProvider implements Provider<AbstractProcessStep<RatingTicket>> {

    private final Validator<RatingTicket> validator;

    private final MeveoConfig meveoConfig;

    @Inject
    public StepsProvider(Validator<RatingTicket> validator, MeveoConfig meveoConfig) {
        this.validator = validator;
        this.meveoConfig = meveoConfig;
    }

    public AbstractProcessStep<RatingTicket> get() {
        AbstractProcessStep<RatingTicket> transactionCreationStep = new TransactionCreationStep(null, meveoConfig);
        AbstractProcessStep<RatingTicket> ratingPriceStep = new RatingPriceStep(transactionCreationStep, meveoConfig);
        ValidationStep<RatingTicket> validationStep = new ValidationStep<RatingTicket>(ratingPriceStep, meveoConfig, validator);
        return validationStep;
    }
}