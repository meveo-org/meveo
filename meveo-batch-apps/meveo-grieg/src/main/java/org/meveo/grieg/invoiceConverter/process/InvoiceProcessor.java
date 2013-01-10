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
package org.meveo.grieg.invoiceConverter.process;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.grieg.ticket.GriegTicket;
import org.meveo.core.inputhandler.TaskExecution;
import org.meveo.core.process.AbstractProcessor;
import org.meveo.core.process.step.AbstractProcessStep;

import com.google.inject.Inject;

/**
 * Class responsible for processing a single CDR.
 * 
 * @author Ignas Lelys
 * @created Dec 22, 2010
 */
public class InvoiceProcessor extends AbstractProcessor<GriegTicket> {
    
    /**
     * @param processStepsChain
     */
    @Inject
    public InvoiceProcessor(AbstractProcessStep<GriegTicket> processStepsChain) {
        super(processStepsChain);
    }
    
    /**
     * @see org.meveo.core.process.AbstractProcessor#doCommit(org.meveo.core.inputhandler.InputContext)
     */
    @Override
    protected void doCommit(TaskExecution<GriegTicket> taskExecution) throws SQLException {
    }
    
    /**
     * @see org.meveo.core.process.AbstractProcessor#getNamedQueries()
     */
    @Override
    protected Map<String, String> getNamedQueries() {
        Map<String, String> queries = new HashMap<String, String>();
        return queries;
    }

}
