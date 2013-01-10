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
package org.meveo.core.outputproducer;

import java.util.List;

import org.meveo.core.output.Output;

/**
 * Output producer interface. Classes that implement this interface can produce
 * file output, web service output or whatever is needed for requirements. After producing output is later handled (file 
 * copied to output dir, message sent etc.). OutputProducer and OutputHandler are separate, because producing is done 
 * incrementally, for each ticket (for example each ticket processed adds new line to output file, and only when all tickets
 * are finished file is moved to output dir).
 * 
 * @author Ignas Lelys
 * @created Sep 20, 2010
 *
 */
public interface OutputProducer {
    
    /**
     * Produce output and return its name. By it output can be retrieved in output handling phase.
     * 
     * @param outputTickets List of output tickets to add to output.
     * 
     * @return Name of output (e.g. file name). Name will be used in later handling of
     * output. For example file moved from tmp dir to final destination, or webservice xml
     * is sent to server. Output is handled after commit phase when we know it was successful.
     */
    public Object produceOutput(List<Output> outputTickets);

}
