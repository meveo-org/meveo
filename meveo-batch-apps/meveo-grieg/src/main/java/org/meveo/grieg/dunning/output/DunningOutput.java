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
package org.meveo.grieg.dunning.output;

import java.util.Map;

import org.meveo.core.output.Output;
import org.meveo.grieg.dunning.ticket.DunningTicket;

/**
 * @author Ignas Lelys
 * @created Dec 28, 2010
 * 
 */
public class DunningOutput implements Output {
    DunningTicket dunningTicket;

    /**
     * @return the dunningTicket
     */
    public DunningTicket getDunningTicket() {
        return dunningTicket;
    }

    /**
     * @param dunningTicket
     *            the dunningTicket to set
     */
    public void setDunningTicket(DunningTicket dunningTicket) {
        this.dunningTicket = dunningTicket;
    }

    public String getTicketOutput() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Object> getParameters() {
        // TODO Auto-generated method stub
        return null;
    }

}
