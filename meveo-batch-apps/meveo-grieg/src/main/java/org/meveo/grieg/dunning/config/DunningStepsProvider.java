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
package org.meveo.grieg.dunning.config;

import org.grieg.ticket.GriegTicket;
import org.meveo.config.MeveoConfig;
import org.meveo.core.process.step.AbstractProcessStep;
import org.meveo.core.process.step.ValidationStep;
import org.meveo.core.validator.Validator;
import org.meveo.grieg.dunning.process.DunningOutputProductionStep;
import org.meveo.grieg.dunning.process.ProviderLoadingStep;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author R.AITYAAZZA
 * @created 23 mars 11
 */
public class DunningStepsProvider implements Provider<AbstractProcessStep<GriegTicket>> {

    private final Validator<GriegTicket> validator;

    private final MeveoConfig meveoConfig;

    @Inject
    public DunningStepsProvider(Validator<GriegTicket> validator, MeveoConfig meveoConfig) {
        this.validator = validator;
        this.meveoConfig = meveoConfig;
    }

    public AbstractProcessStep<GriegTicket> get() {
        DunningOutputProductionStep dunningOutputProductionStep = new DunningOutputProductionStep(null, meveoConfig);
        ProviderLoadingStep providerLoadingStep = new ProviderLoadingStep(dunningOutputProductionStep, meveoConfig);
        ValidationStep<GriegTicket> validationStep = new ValidationStep<GriegTicket>(providerLoadingStep, meveoConfig, validator);
        return validationStep;
    }
}