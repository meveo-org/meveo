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
 * Abstract CDR which implements {@link CDR} interface.
 * 
 * @author seb
 * @created Aug 6, 2012
 * 
 */
public class BaseCDR implements CDR {

	private String eventType;
	private String ticketId;
	private String originSensorId;
	private String targetSensorId;
	private String originUserId;
	private String targetUserId;
	private Date startDate;
	private Date endDate;
	private BigDecimal volumeUp;
	private BigDecimal volumeDown;
	private BigDecimal volumeTotal;
	private boolean finishedTicket=true;
	private Long retryRejectedID;
	private String causeForRecordClosing;
	
	@Override
	public String getEventType() {
		return eventType;
	}

	@Override
	public String getTicketID() {
		return ticketId;
	}

	@Override
	public String getOriginSensorId() {
		return originSensorId;
	}

	@Override
	public String getTargetSensorId() {
		return targetSensorId;
	}

	@Override
	public String getOriginUserId() {
		return originUserId;
	}

	@Override
	public String getTargetUserId() {
		return targetUserId;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public BigDecimal getVolumeUp() {
		return volumeUp;
	}

	@Override
	public BigDecimal getVolumeDown() {
		return volumeDown;
	}

	@Override
	public BigDecimal getVolumeTotal() {
		return volumeTotal;
	}

	@Override
	public boolean isFinishedTicket() {
		return finishedTicket;
	}

	@Override
	public Long getRetryRejectedID() {
		return retryRejectedID;
	}

	@Override
	public String getCauseForRecordClosing() {
		return causeForRecordClosing;
	}

	@Override
	public byte[] getMagicNumber() {
		return MagicNumberCalculator.getInstance().calculateForGenericCDR(this);
	}

	@Override
	public Object getDelegate() {
		return this;
	}

	public String getTicketId() {
		return ticketId;
	}

	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public void setOriginSensorId(String originSensorId) {
		this.originSensorId = originSensorId;
	}

	public void setTargetSensorId(String targetSensorId) {
		this.targetSensorId = targetSensorId;
	}

	public void setOriginUserId(String originUserId) {
		this.originUserId = originUserId;
	}

	public void setTargetUserId(String targetUserId) {
		this.targetUserId = targetUserId;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setVolumeUp(BigDecimal volumeUp) {
		this.volumeUp = volumeUp;
	}

	public void setVolumeDown(BigDecimal volumeDown) {
		this.volumeDown = volumeDown;
	}

	public void setVolumeTotal(BigDecimal volumeTotal) {
		this.volumeTotal = volumeTotal;
	}

	public void setFinishedTicket(boolean finishedTicket) {
		this.finishedTicket = finishedTicket;
	}

	public void setRetryRejectedID(Long retryRejectedID) {
		this.retryRejectedID = retryRejectedID;
	}

	public void setCauseForRecordClosing(String causeForRecordClosing) {
		this.causeForRecordClosing = causeForRecordClosing;
	}

	
 
}