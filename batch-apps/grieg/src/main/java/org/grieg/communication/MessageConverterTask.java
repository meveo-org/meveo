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
package org.grieg.communication;

import org.grieg.communication.ticket.MessageTicket;
import org.meveo.config.task.MeveoTask;
import org.meveo.core.inputhandler.InputHandler;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.inputloader.InputLoader;
import org.meveo.core.outputhandler.OutputHandler;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Converter task. Takes invoice xmls and processed them.
 * 
 * @author Sebastien Michea
 * @created 
 *
 */
public class MessageConverterTask extends MeveoTask<MessageTicket> {

    @Inject
    public MessageConverterTask(Provider<InputLoader> inputLoaderProvider, Provider<InputHandler<MessageTicket>> inputHandlerProvider, Provider<OutputHandler> outputHandlerProvider) {
        super(inputLoaderProvider, inputHandlerProvider, outputHandlerProvider);
    }
    
    @Override
    protected void persistInputHistory(TaskExecution<MessageTicket> taskExecution) {
        // TODO Auto-generated method stub
        
    }
    
}
