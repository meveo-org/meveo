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
package org.meveo.core.output;

import java.util.Map;

/**
 * Output object interface. Implementation must provide output string for
 * ticket. For example if we need to write tickets as EDRs as csv lines in file,
 * it would return a string with all EDR fields separated with comma.
 * 
 * @author Ignas Lelys
 * @created Sep 20, 2010
 * 
 */
public interface Output {

    /**
     * String output. It can be a line for csv file, or some text for text based
     * output, or xml tags for xml output file.
     */
    public String getTicketOutput();

    /**
     * Additional parameters for output if needed. It can be used by output
     * producer when constructing output. For example it can be used by jasper
     * reports when constructing pdf output.
     */
    public Map<String, Object> getParameters();

}
