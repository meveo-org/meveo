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
package org.meveo.config.task;

import org.apache.log4j.Logger;
import org.meveo.core.inputhandler.InputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputhandler.TaskExecution.TaskExecutionStatus;
import org.meveo.core.inputloader.Input;
import org.meveo.core.inputloader.InputLoader;
import org.meveo.core.inputloader.InputNotLoadedException;
import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.persistence.MeveoPersistence;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Task for Meveo application. Configured with {@link AbstractFactory}. 
 * It loads input with appropriate {@link InputLoader} then processes 
 * it with appropriate {@link InputProcessor}. One MeveoTask is executed
 * in one separate thread. So when Meveo application is configured to run
 * in multithreaded environment each MeveoTask is executed in 
 * separate thread.
 * 
 * @author Ignas Lelys
 * @created 2009.06.15
 */
public abstract class MeveoTask<T> implements Runnable {

    private static final Logger logger = Logger.getLogger(MeveoTask.class);
    
    private Provider<InputLoader> inputLoaderProvider;
    
    private Provider<InputHandler<T>> inputHandlerProvider;
    
    private Provider<OutputHandler> outputHandlerProvider;

    /**
     * Constructor. InputLoader and inputHandler are injected by Guice framework.
     * (Configured in implementation project Guice configuration class.)
     * 
     * @param inputLoaderProvider Provider for InputLoader configured in guice module.
     * @param inputHandlerProvider Provider for InputHandler configured in guice module.
     * @param outputHandlerProvider Provider for OutputHandler configured in guice module.
     */
    @Inject
    public MeveoTask(Provider<InputLoader> inputLoaderProvider, Provider<InputHandler<T>> inputHandlerProvider, Provider<OutputHandler> outputHandlerProvider) {
        this.inputLoaderProvider = inputLoaderProvider;
        this.inputHandlerProvider = inputHandlerProvider;
        this.outputHandlerProvider = outputHandlerProvider;
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            long startTime = System.currentTimeMillis();
            //logger.debug("Start task with thread id:"+Thread.currentThread().getId());
            Input input = null;
            InputLoader inputLoader = inputLoaderProvider.get();
            try {
                input = inputLoader.loadInput();
            } catch (InputNotLoadedException e) {
                logger.error("Input failed to load. No more action on this task.");
                return;
            } catch (Throwable e) {
                logger.error("Input failed to load. No more action on this task.", e);
                return;
            }
            TaskExecution<T> taskExecution = null;
            if (input != null) {
                try {
                    logger.info(String.format("Processing '%s'", input.getName()));
                    MeveoPersistence.getEntityManager().getTransaction().begin();
                    taskExecution = inputHandlerProvider.get().handleInput(input);
                    persistInputHistory(taskExecution);
                    outputHandlerProvider.get().handleOutput(taskExecution);
                    inputLoader.handleInputAfterProcessing(input, taskExecution);
                    // TODO maybe add method for persisting TaskExecution to db or other store
                    MeveoPersistence.getEntityManager().getTransaction().commit();
                } catch (Throwable e) {
                    logger.error("Exception occured. Transaction rolled back.", e);
                    MeveoPersistence.getEntityManager().getTransaction().rollback();
                    if (taskExecution != null) {
                        taskExecution.setStatus(TaskExecutionStatus.FAILED);
                    }
                    inputLoader.handleInputAfterFailure(input, e);
                } finally {
                    MeveoPersistence.closeEntityManager();
                    logger.info(String.format("Input processed in %s ms.", 
                            System.currentTimeMillis() - startTime));
                }
            }
        } catch (Throwable t) {
            logger.error("Unnexpected exeption in executed thread!", t);
        }
    }

    /**
     * Persist InputHistory with results of input processing.
     */
    protected abstract void persistInputHistory(TaskExecution<T> taskExecution);

}
