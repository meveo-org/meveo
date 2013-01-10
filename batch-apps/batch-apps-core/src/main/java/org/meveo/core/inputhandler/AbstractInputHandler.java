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

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.meveo.config.MeveoConfig;
import org.meveo.core.inputhandler.TaskExecution.TaskExecutionStatus;
import org.meveo.core.inputloader.Input;
import org.meveo.core.output.Output;
import org.meveo.core.outputproducer.OutputProducer;
import org.meveo.core.process.Processor;
import org.meveo.core.process.step.Constants;
import org.meveo.model.admin.InputHistory;
import org.meveo.model.crm.Provider;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;

/**
 * Input processor class. Most often it processes files, but can also be a
 * different implementation (like webservices etc...).
 * 
 * @author Ignas Lelys
 * @created 2009.07.16
 */
public abstract class AbstractInputHandler<T> implements InputHandler<T> {

    private static final Logger logger = Logger.getLogger(AbstractInputHandler.class);
    
    /** Processor responsible of processing single ticket. */
    protected Processor<T> processor;
    protected OutputProducer outputProducer;
    
    @Inject
    protected MeveoConfig config;

    /**
     * Constructor with parameters for guice injection.
     * 
     * @param processor
     *            Injected {@link Processor} implementation.
     * @param outputProducer
     *            Injected {@link OutputProducer} implementation.
     */
    @Inject
    public AbstractInputHandler(Processor<T> processor, OutputProducer outputProducer) {
        super();
        this.processor = processor;
        this.outputProducer = outputProducer;
    }

    /**
     * @see org.meveo.core.inputhandler.InputHandler#handleInput(org.meveo.core.inputloader.Input)
     */
    @SuppressWarnings("unchecked")
    public TaskExecution<T> handleInput(Input input) throws Exception {
        TaskExecution<T> taskExecution = createNewTaskExecution(input);
        
        long processStart = System.currentTimeMillis();
        taskExecution = executeInputHandling(input, taskExecution);
        logger.info("Processor process took: " + (System.currentTimeMillis() - processStart));
        
        taskExecution.setEndTime(new Date());
        taskExecution.setStatus(TaskExecutionStatus.COMPLETED);
        
        long commitStart = System.currentTimeMillis();
        processor.commit(taskExecution);
        logger.info("Processor commit took: " + (System.currentTimeMillis() - commitStart));
        
        Object outputs = taskExecution.getExecutionContextParameter(Constants.OUTPUT_TICKETS);
        if (outputs != null) {
            List<Output> outputList = (List<Output>)outputs;
            logger.info(String.format("Producing output for %s output tickets", outputList.size()));
            Object outputObject = outputProducer.produceOutput(outputList);
            taskExecution.setOutputObject(outputObject);
        }
        return taskExecution;
    }
    
    /**
     * Template method  for actual input handling, where processor processes all tickets.
     * 
     * @param input Input object, with name, InputInfo and Input object itself (e.g. file).
     * @param taskExecution Created task execution for input.
     * 
     * @return Task execution after process.
     * @throws Exception
     */
    public abstract TaskExecution<T> executeInputHandling(Input input, TaskExecution<T> taskExecution) throws Exception;

    /**
     * Create {@link TaskExecution} object.
     * 
     * @param input Input object.
     * @return Newly created task execution object.
     */
    public TaskExecution<T> createNewTaskExecution(Input input) {
        Date startTime = new Date();
        String inputName = input.getName();
        InputHistory inputHistory = new InputHistory();
        inputHistory.setName(inputName);
        inputHistory.setAnalysisStartDate(startTime);
        inputHistory.setVersion(1);
        inputHistory.setProvider(MeveoPersistence.getEntityManager().getReference(Provider.class, config.getDefaultProviderId()));
        
        MeveoPersistence.getEntityManager().persist(inputHistory);
        MeveoPersistence.getEntityManager().flush();
        StringBuilder taskNameBuilder = new StringBuilder();
        taskNameBuilder.append(inputName).append(" ").append("task");
        TaskExecution<T> taskExecution = new TaskExecution<T>(taskNameBuilder.toString(), startTime, input, inputHistory, processor);
        taskExecution.setStatus(TaskExecutionStatus.ONGOING);
        return taskExecution;
    }
    
}
