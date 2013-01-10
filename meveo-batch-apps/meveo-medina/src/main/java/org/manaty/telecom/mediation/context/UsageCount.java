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
package org.manaty.telecom.mediation.context;


/**
 * Bean for storing single CDR usage count.
 * 
 * @author Donatas Remeika
 * @created Mar 20, 2009
 */
public class UsageCount {

    private String eventType;
    
    private Long count;
    
    private Long countUp;
    
    private Long countDown;
    
    public UsageCount(String eventType, Long count, Long countUp, Long countDown) {
        this.eventType = eventType;
        this.count = count;
        this.countUp = countUp;
        this.countDown = countDown;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getCount() {
        return count;
    }

	public Long getCountUp() {
		return countUp;
	}

	public Long getCountDown() {
		return countDown;
	}

}
