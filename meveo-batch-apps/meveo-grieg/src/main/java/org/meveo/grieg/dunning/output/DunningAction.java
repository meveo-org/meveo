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

import org.meveo.core.outputhandler.OutputHandler;
import org.meveo.grieg.dunning.ticket.DunningTicket;

/**
 * Holder that has file name and all the information needed for that file
 * handling in {@link OutputHandler}.
 * 
 * @author anasseh
 * @created 04.04.2011
 * 
 */
public class DunningAction {

    private String fileName;
    private DunningTicket dunningTicket;

    public DunningAction() {

    }

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

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}