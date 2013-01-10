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
package org.meveo.core.inputloader;

import java.io.File;

import org.meveo.core.inputhandler.AbstractInputHandler;
import org.meveo.core.inputhandler.TaskExecution;

/**
 * Input loader interface. Classes that implement this interface must provide
 * the way for application to load an input. The input could be file with some
 * tickets, webservice, jms message and so on.
 * 
 * @author Ignas Lelys
 * @created Apr 20, 2010
 * 
 */
public interface InputLoader {

    /**
     * This method must be implemented and return input object. Input object can
     * be file or some DTO object from JMS message or anything else. This object
     * should be later be passed to appropriate {@link AbstractInputHandler}
     * which knows how to convert and handle input object.
     * <p>
     * For example some kind of file input implementation would return
     * {@link File} object and concrete file input handler would later cast it
     * and parse the file.
     * 
     * @return Input object.
     */
    public Input loadInput();

    /**
     * Handles input after processing it. For example for file input, it can be
     * moved to accepted directory.
     */
    @SuppressWarnings("unchecked")
    public void handleInputAfterProcessing(Input input, TaskExecution taskExecution);

    /**
     * This method should be invoked if input processing failed and transaction
     * is roll backed. Examples could be send notification to input sender if
     * input was got by message or append some extension about failed file if
     * input was a file or something similar.
     */
    public void handleInputAfterFailure(Input input, Throwable e);

}
