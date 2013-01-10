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
package org.manaty.model.telecom.mediation.cdr;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Interface for accessing CDR properties.
 * 
 * @author seb
 * @created Aug 6, 2012
 * 
 */
public interface CDR {

	public String getEventType();

	public String getTicketID();

	public String getOriginSensorId();
	
	public String getTargetSensorId();
	
	public String getOriginUserId();
	
	public String getTargetUserId();
	
	public Date getStartDate();
	
	public Date getEndDate();
	
	public BigDecimal getVolumeUp();
	
	public BigDecimal getVolumeDown();
	
	public BigDecimal getVolumeTotal();
	
	/** Returns if ticket is finished, or is just a part of bigger ticket. */
	public boolean isFinishedTicket();

	/**
	 * Retry rejected Ticket ID. Field is used and can be parsed only when
	 * Medina creates text tickets file, for rejected tickets in Database.
	 * For those tickets this field is not null, and it shows ID of entry
	 * in MEDINA_REJECTED_CDR entry, which should be deleted if ticket was 
	 * processed successfully after retry. Also if this field is not null,
	 * ticket is also not saved again to database if it was rejected in the process.
	 */
	public Long getRetryRejectedID();

	/**
	 * The reason on disconnection is only define when the connection status moves to open to closed.
	 * if the value is "N" then the reason is "Normal communication end".
	 * if the value is "A" then the reason is "Abnormal communication end".
	 */
	public String getCauseForRecordClosing();
	
	/**
	 * Get calculated magic number.
	 */
	public byte[] getMagicNumber();

	/**
	 * Get wrapped data bean.
	 * 
	 * @return CDR data bean.
	 */
	public Object getDelegate();

}
