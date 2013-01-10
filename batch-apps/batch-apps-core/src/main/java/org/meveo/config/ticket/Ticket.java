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
package org.meveo.config.ticket;

/**
 * 
 * @author Ignas Lelys
 * @created Dec 23, 2010
 * 
 */
public interface Ticket {

    /**
     * Every ticket must be able to return its source. For example, for csv file
     * ticket, it would return whole string of the line which represents ticket
     * (while comma separated values in that string represents different fields
     * that are parsed and set to ticket).
     */
    public Object getSource();

}
