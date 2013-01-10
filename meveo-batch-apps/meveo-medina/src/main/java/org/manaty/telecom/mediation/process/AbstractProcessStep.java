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

import org.apache.log4j.Logger;
import org.manaty.telecom.mediation.context.MediationContext;

/**
 * Abstract CDRProcessor step which is executed in a chain.
 * 
 * @author Donatas Remeika
 * @created Mar 6, 2009
 */
public abstract class AbstractProcessStep implements ProcessStep {
    
    protected final Logger logger = Logger.getLogger(this.getClass());
    
    private AbstractProcessStep nextStep;

    private long timeInMills = 0L;
    private long times = 0L;
    
    /**
     * Constructor.
     * 
     * @param nextStep
     *            Next step to be executed.
     */
    public AbstractProcessStep(AbstractProcessStep nextStep) {
        this.nextStep = nextStep;
    }

    /**
     * Steps should override this to add their logic.
     * 
     * @param context
     * @return true if next step can be executed.
     */
    protected abstract boolean execute(MediationContext context);

    /**
     * Process in a Chain of Steps, starting from this step.
     * 
     * @param context
     *            Execution context
     */
    public final void process(MediationContext context) {
        long start = System.currentTimeMillis();
        boolean proceed = execute(context);
        long end = System.currentTimeMillis();
        times++;
        timeInMills += (end - start);
        if (proceed && nextStep != null) {
            nextStep.process(context);
        }
    }
    
    public long getExecutionCount() {
        return times;
    }
    
    public long getExecutionTime() {
        return timeInMills;
    }
}
