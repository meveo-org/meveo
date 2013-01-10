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
package org.manaty.telecom.mediation.process;

import java.sql.PreparedStatement;
import java.util.Date;
import java.util.Map;

import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.telecom.mediation.FileProcessingContext;
import org.manaty.telecom.mediation.context.MediationContext;
import org.meveo.model.mediation.Access;

/**
 * Interface for CDR Processor.
 * 
 * @author Donatas Remeika
 * @created Mar 19, 2009
 */
public interface Processor {

    /**
     * Finish processor work. Commit data.
     */
    public CommitResult commit(Date ticketDate, FileProcessingContext context);

    /**
     * Process single CDR.
     * 
     * @param cdr
     *            CDR to process.
     * @param type
     *            Type of CDR.
     *        
     * @return MediationContext object.
     */
    public MediationContext process(CDR cdr, String eventType);
    
    public AbstractProcessStep getUniquenessStep();
    public AbstractProcessStep getAccessStep();
    public AbstractProcessStep getZonningStep();
    public AbstractProcessStep getTimeStep();
    public AbstractProcessStep getNumberStep();

    /**
     * Get processed AccessPoints.
     * 
     * @return Map of Long->AccessPoint
     */
    public Map<Long, Access> getAccesses();
    
    public Map<String, Access> getAccessCacheByUserId();
    
    public PreparedStatement getStatementFindAccessByUserId();

    public PreparedStatement getStatementFindZone();
    
    public PreparedStatement getStatementInsertRejectedTicket();
    
}
