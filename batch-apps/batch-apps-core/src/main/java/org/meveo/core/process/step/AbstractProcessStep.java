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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.meveo.config.MeveoConfig;
import org.meveo.core.inputhandler.TaskExecution;

/**
 * @author Ignas Lelys
 * @created Jul 8, 2010
 *
 * @param <T>
 */
public abstract class AbstractProcessStep<T> implements ProcessStep<T> {
    
    protected final Logger logger = Logger.getLogger(this.getClass());
    
    private AbstractProcessStep<T> nextStep;
    
    protected MeveoConfig config;

    private long timeInMills = 0L;
    private long times = 0L;
    
    /**
     * Constructor.
     * 
     * @param nextStep
     *            Next step to be executed.
     */
    public AbstractProcessStep(AbstractProcessStep<T> nextStep, MeveoConfig config) {
        this.nextStep = nextStep;
        this.config = config;
    }
    
    /**
     * Steps should override this to add their logic.
     * 
     * @param context
     * @return true if next step can be executed.
     */
    protected abstract boolean execute(StepExecution<T> stepExecution);

    /**
     * Process in a Chain of Steps, starting from this step.
     * 
     * @param context
     *            Execution context
     */
    public final void process(T ticket, TaskExecution<T> taskExecution, Map<String, Object> contextParameters) {
        long start = System.currentTimeMillis();
        StepExecution<T> stepExecution = new StepExecution<T>(ticket, 
                this.getClass().getSimpleName(), contextParameters, taskExecution);
        
        boolean proceed = execute(stepExecution);
        
        long end = System.currentTimeMillis();
        times++;
        timeInMills += (end - start);
        
        // set duration to step execution
        stepExecution.setDurationInMillis(timeInMills);
        
        if (proceed && nextStep != null) {
            nextStep.process(ticket, taskExecution, contextParameters);
        }
    }
    
    /**
     * Return how many times step was executed. For example if there were 100
     * tickets and this step was reached by 95 tickets (others were rejected before) - it will return 95.
     * 
     * @return How many times this step was executed.
     */
    protected long getExecutionCount() {
        return times;
    }
    
    /**
     * Returns how much time does it took for this step to process all tickets.
     * For example - if there were 100 tickets and each ticket spent 1s in this
     * step it will return 100s.
     * 
     * @return How much time it took to execute step logic.
     */
    protected long getExecutionTime() {
        return timeInMills;
    }
    
    /**
     * Sets current ticket as accepted. (Probably not needed?).
     * 
     * @param stepExecution
     *            Step execution context.
     */
    protected void setAccepted(StepExecution<T> stepExecution) {
        stepExecution.addParameter(Constants.ACCEPTED, Boolean.TRUE);
    }
    
    /**
     * Process step implementation can invoke this method to set current ticket
     * as rejected.
     * 
     * @param stepExecution
     *            Step execution context.
     * @param reason
     *            Rejection reason.
     */
    protected void setNotAccepted(StepExecution<T> stepExecution, String reason) {
        logger.warn(new StringBuilder().append("Ticket rejected in ").append(stepExecution.getName()).append(
                " with reason ").append(reason));
        stepExecution.addParameter(Constants.STATUS, reason);
        stepExecution.addParameter(Constants.ACCEPTED, Boolean.FALSE);
    }
    
    /**
     * @param parameterKey
     * @param parameterValue
     * @param taskExecution
     */
    protected <E> void putToTaskExecutionContextParameter(String parameterKey, E parameterValue, TaskExecution<T> taskExecution) {
        taskExecution.addExecutionContextParameter(parameterKey, parameterValue);
    }
    
    /**
     * @param parameterKey
     * @param taskExecution
     */
    @SuppressWarnings("unchecked")
    protected <E> E getFromTaskExecutionContextParameter(String parameterKey, TaskExecution<T> taskExecution) {
        return (E)taskExecution.getExecutionContextParameter(parameterKey);
    }
    
    /**
     * @param <E>
     * @param parameterKey
     * @param parameterValue
     * @param taskExecution
     */
    protected <E> void putToTaskExecutionListContextParameter(String parameterKey, E parameterValue, TaskExecution<T> taskExecution) {
        List<E> list = getFromTaskExecutionContextParameter(parameterKey, taskExecution);
        if (list == null) {
            list = new ArrayList<E>();
        }
        list.add(parameterValue);
        putToTaskExecutionContextParameter(parameterKey, list, taskExecution);
    }
    
    /**
     * @param <K>
     * @param <V>
     * @param parameterKey
     * @param parameterMapKey
     * @param parameterMapValue
     * @param taskExecution
     */
    protected <K, V> void putToTaskExecutionMapContextParameter(String parameterKey, K parameterMapKey,
            V parameterMapValue, TaskExecution<T> taskExecution) {
        Map<K, V> map = getFromTaskExecutionContextParameter(parameterKey, taskExecution);
        if (map == null) {
            map = new HashMap<K, V>();
        }
        map.put(parameterMapKey, parameterMapValue);
        putToTaskExecutionContextParameter(parameterKey, map, taskExecution);
    }
    
    /**
     * @param <K>
     * @param <V>
     * @param parameterKey
     * @param mapParameterKey
     * @param taskExecution
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <K, V> V getFromTaskExecutionMapContextParameter(String parameterKey, K mapParameterKey,
            TaskExecution<T> taskExecution) {
        Map<K, V> map = (Map<K, V>) taskExecution.getExecutionContextParameter(parameterKey);
        if (map != null) {
            return map.get(mapParameterKey);
        } else {
            return null;
        }
    }
}
