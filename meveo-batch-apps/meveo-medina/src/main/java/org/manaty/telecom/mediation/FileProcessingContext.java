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
package org.manaty.telecom.mediation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.meveo.model.mediation.RejectedCDR;
import org.manaty.model.telecom.mediation.cdr.CDRStatus;

/**
 * File processing context.
 * 
 * @author Ignas Lelys
 * @created 2009.07.30
 */
public class FileProcessingContext {
    
    private List<RejectedCDR> rejectedCDRs;
    
    private List<Long> processedRejectedTicketsIds;

    private Map<Long, CDRStatus> failedRejectedTicketsIds;
    
    
    public FileProcessingContext() {
        super();
        rejectedCDRs = new ArrayList<RejectedCDR>();
        processedRejectedTicketsIds = new ArrayList<Long>();
        failedRejectedTicketsIds = new HashMap<Long, CDRStatus>();
    }

	public List<RejectedCDR> getRejectedCDRs() {
		return rejectedCDRs;
	}

	public List<Long> getProcessedRejectedTicketsIds() {
		return processedRejectedTicketsIds;
	}

	public Map<Long, CDRStatus> getFailedRejectedTicketsIds() {
		return failedRejectedTicketsIds;
	}

}
