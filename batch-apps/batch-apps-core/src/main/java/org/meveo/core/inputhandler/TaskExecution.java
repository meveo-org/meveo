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
package org.meveo.core.inputhandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.meveo.core.inputloader.Input;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.StepExecution;
import org.meveo.model.admin.InputHistory;
import org.meveo.model.crm.Provider;

/**
 * Object that represents execution of task, and holds all of the context about
 * that task
 * 
 * @author Ignas Lelys
 * @created Jun 25, 2010
 * 
 */
public class TaskExecution<T> {

    public enum TaskExecutionStatus {
        COMPLETED,
        FAILED,
        ONGOING
    };

    private TaskExecutionStatus status;
    
    /** Execution provider. */
    private Provider provider;

    /** Processor implementation for this task execution. */
    private Processor<T> processor;

    /**
     * Name of the task. Almost always (if AbstractInputHandler is used and
     * createNewTaskExecution method is not overriden), task name is
     * inputObject.name + ' task'.
     */
    private String name;

    /** Start time of task execution. */
    private Date startTime = null;

    /** End time of task execution. */
    private Date endTime = null;

    /** Number of tickets parsed. */
    private Integer parsedTicketsCount = 0;

    /** Number of tickets successfully processed. */
    private Integer processedTicketsCount = 0;

    /** Number of tickets rejected. */
    private Integer rejectedTicketsCount = 0;

    /** Input object thats was loaded by loader. */
    private Input inputObject;
    
    /** Pre saved input history. Its id can be used to link some entities with current task and show it in GUI. */
    private InputHistory inputHistory;

    /**
     * Output object. It is set after execution of task is finished
     * successfully. If application does not provide any output it can be null.
     */
    private Object outputObject;

    /** Step execution that were executed per task. */
    private Map<T, Collection<StepExecution<T>>> ticketsWithStepExecution = new HashMap<T, Collection<StepExecution<T>>>();

    /**
     * Execution context hold values that will be used after all steps is
     * completed (in commit for example).
     */
    private Map<String, Object> executionContext = new HashMap<String, Object>();

    /**
     * @param startTime
     */
    public TaskExecution(String name, Date startTime, Input input, InputHistory inputHistory, Processor<T> processor) {
        super();
        this.name = name;
        this.startTime = startTime;
        this.processor = processor;
        this.inputObject = input;
        this.inputHistory = inputHistory;
    }

    /**
     * @param stepExecution
     */
    public void addStepExecution(T ticket, StepExecution<T> stepExecution) {
        Collection<StepExecution<T>> stepExecutions = ticketsWithStepExecution.get(ticket);
        if (stepExecutions == null) {
            stepExecutions = new ArrayList<StepExecution<T>>();
        }
        stepExecutions.add(stepExecution);
        ticketsWithStepExecution.put(ticket, stepExecutions);
    }

    /**
     * @param key
     * @param value
     */
    public void addExecutionContextParameter(String key, Object value) {
        executionContext.put(key, value);
    }

    /**
     * @param key
     * @return
     */
    public Object getExecutionContextParameter(String key) {
        return executionContext.get(key);
    }

    public Integer getParsedTicketsCount() {
        return parsedTicketsCount;
    }

    public void setParsedTicketsCount(Integer parsedTicketsCount) {
        this.parsedTicketsCount = parsedTicketsCount;
    }

    public Integer getProcessedTicketsCount() {
        return processedTicketsCount;
    }

    public void setProcessedTicketsCount(Integer processedTicketsCount) {
        this.processedTicketsCount = processedTicketsCount;
    }

    public Integer getRejectedTicketsCount() {
        return rejectedTicketsCount;
    }

    public void setRejectedTicketsCount(Integer rejectedTicketsCount) {
        this.rejectedTicketsCount = rejectedTicketsCount;
    }

    public Processor<T> getProcessor() {
        return processor;
    }

    public String getName() {
        return name;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public TaskExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(TaskExecutionStatus status) {
        this.status = status;
    }

    public Object getOutputObject() {
        return outputObject;
    }

    public void setOutputObject(Object outputObject) {
        this.outputObject = outputObject;
    }

    public Input getInputObject() {
        return inputObject;
    }
    
    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }
    
    public InputHistory getInputHistory() {
        return inputHistory;
    }

    /**
     * @see org.springframework.batch.core.domain.Entity#toString()
     */
    public String toString() {
        StringBuilder ticketsBuilder = new StringBuilder();
        Set<T> tickets = ticketsWithStepExecution.keySet();
        for (T ticket : tickets) {
            ticketsBuilder.append(ticket.toString()).append(":").append("\n");
            for (StepExecution<T> stepExecution : ticketsWithStepExecution.get(ticket)) {
                ticketsBuilder.append(stepExecution.toString());
            }
        }
        return String.format(
                "Task execution info: startTime=%s, endTime=%s, number of tickets=%s [Step executions: %s]", startTime,
                endTime, tickets.size(), ticketsBuilder.toString());
    }

}