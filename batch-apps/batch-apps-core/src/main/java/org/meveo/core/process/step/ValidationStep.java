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
package org.meveo.core.process.step;

import org.meveo.config.MeveoConfig;
import org.meveo.core.validator.Validator;

/**
 * Get validator and validate ticket. Because validation step is always same it
 * can be reused, just with different validators which provides concrete
 * validation rules.
 * 
 * @author Ignas Lelys
 * @created May 3, 2010
 * 
 */
public class ValidationStep<T> extends AbstractProcessStep<T> {

    /** Injected validator. */
    private Validator<T> validator;

    /**
     * @param nextStep
     */
    public ValidationStep(AbstractProcessStep<T> nextStep, MeveoConfig config, Validator<T> validator) {
        super(nextStep, config);
        this.validator = validator;
    }

    /**
     * Does validation logic.
     */
    @Override
    protected boolean execute(StepExecution<T> stepExecution) {
        if (validator.validate(stepExecution.getTicket())) {
            return true;
        } else {
            stepExecution.addParameter(Constants.STATUS, Constants.VALIDATION_FAILED_STATUS);
            stepExecution.addParameter(Constants.ACCEPTED, false);
            return false;
        }
    }

}
