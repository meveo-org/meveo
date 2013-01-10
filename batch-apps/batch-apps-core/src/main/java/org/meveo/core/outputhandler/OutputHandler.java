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
package org.meveo.core.outputhandler;

import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.outputproducer.OutputProducer;

/**
 * OutputHandler interface. Processing thread will use OutputHandler to deal
 * with it after it was produced with {@link OutputProducer}.
 * 
 * @author Ignas Lelys
 * @created Sep 29, 2010
 * 
 */
public interface OutputHandler {

    /**
     * Output handling method. For example copy constructed output file from
     * temp dir to output dir, or sends constructed xml message.
     * {@link OutputProducer} and {@link OutputHandler} are separated, because
     * output is produced inside transaction (for example we run through tickets
     * and add data to output file line by line). So in case of error and
     * transaction rollback output should not be provided to user. So output
     * handling happens only after all processing right before transaction
     * commit. If error happens in handling output, transaction is still
     * rollbacked.
     * 
     * @param outputObject
     *            Object of output. For example for file it can be string with file
     *            name. This name can be used to retrieve it. It can be list of
     *            file names if there are multiple files.
     */
    @SuppressWarnings("unchecked")
    public void handleOutput(TaskExecution taskExecution);

}
