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

/**
 * Info about committed data.
 * 
 * @author Donatas Remeika
 * @created May 6, 2009
 */
public class CommitResult {

    private long usageCountDATA;
    private long usageCountVOICE;
    private long usageCountSMS;

    public CommitResult(long usageCountDATA, long usageCountVOICE, long usageCountSMS) {
        super();
        this.usageCountDATA = usageCountDATA;
        this.usageCountVOICE = usageCountVOICE;
        this.usageCountSMS = usageCountSMS;
    }

    public long getUsageCountDATA() {
        return usageCountDATA;
    }
    
    public long getUsageCountVOICE() {
        return usageCountVOICE;
    }
    
    public long getUsageCountSMS() {
        return usageCountSMS;
    }
}
