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
package org.meveo.core.parser;

/**
 * Parser interface.
 * 
 * @author Ignas
 */
public interface Parser<T> {

    /**
     * Parses and returns next available EDR.
     * 
     * @return next ticket from file.
     * @throws ParserException
     *             If parsing fails.
     */
    public T next() throws ParserException;

    /**
     * Release allocated resources.
     */
    public void close();
    
    /**
     * Parsing file must be set before start of retrieving tickets with next() method.
     */
    public void setParsingFile(String fileName) throws ParserException;
}
