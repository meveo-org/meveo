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

import java.util.Map;

import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.Processor;

/**
 * Step execution object holds information about processing of ticket.
 * 
 * @author Ignas Lelys
 * @created Jun 25, 2010
 * 
 */
public class StepExecution <T> {

    /** Task execution instance. Holds information about performed task current step is in. */
    private TaskExecution<T> taskExecution;

    /** Current steps execution duration. */
    private Long durationInMillis = 0L;

    /** Current steps name. */
    private String name;

    /** Context parameters that might be used by current step, or new paramters can be added and passed to other steps. */
    private Map<String, Object> contextParameters;

    /** Ticket. */
    private T ticket;
    
    /**
     * Constructor.
     * 
     * @param name Name of step.
     * @param contextParameters
     *            Context parameters are taken from previous {@link Step}
     *            {@link StepExecution} and passed over. So after step is
     *            processed its parameters are accessible to the next Step.
     * @param taskExecution Task execution entity.
     */
    public StepExecution(T ticket, String name, Map<String, Object> contextParameters, TaskExecution<T> taskExecution) {
        super();
        this.ticket = ticket;
        this.name = name;
        this.contextParameters = contextParameters;
        this.taskExecution = taskExecution;
        taskExecution.addStepExecution(ticket, this);
    }

    public TaskExecution<T> getTaskExecution() {
        return taskExecution;
    }
    
    public Long getDurationInMillis() {
        return durationInMillis;
    }

    public void setDurationInMillis(Long durationInMillis) {
        this.durationInMillis = durationInMillis;
    }
    
    public String getName() {
        return name;
    }
    
    public T getTicket() {
        return ticket;
    }

    /**
     * Add parameter to context parameters.
     */
    public void addParameter(String key, Object value) {
        contextParameters.put(key, value);
    }

    /**
     * Get parameter from context parameters.
     */
    public Object getParameter(String key) {
        return contextParameters.get(key);
    }

    /**
     * Gets Processor instance that runs current task. Gets it from {@link TaskExecution}.
     */
    public Processor<T> getProcessor() {
        return taskExecution.getProcessor();
    }

}
