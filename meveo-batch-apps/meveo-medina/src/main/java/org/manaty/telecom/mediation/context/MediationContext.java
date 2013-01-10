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

import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRStatus;
import org.manaty.telecom.mediation.process.Processor;
import org.meveo.model.mediation.Access;
import org.meveo.model.rating.EDR;

/**
 * Mediation context.
 * 
 * @author seb
 * @created Aug 6, 2012
 */
public class MediationContext {

    private Processor processor;
    private CDR cdr;
    private boolean accepted = true;
    private CDRStatus status;
    private EDR edr;
    private byte[] magicNumber;
    private String accessUserId;
    private String accessServiceId;
    private Access originAccess;
    private Access targetAccess;
    private String originZoneCode;
    private String targetZoneCode;
    private String originSubZoneCode;
    private String targetSubZoneCode;
    private String originTimeCode;
    private String targetTimeCode;
    private String originNumeringCode;
    private String targetNumeringCode;
    private UsageCount usageCount;
    
    public MediationContext(CDR cdr, Processor processor) {
        this.cdr = cdr;
        this.status = CDRStatus.ONGOING;
        this.processor = processor;
    }

	public Processor getProcessor() {
		return processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	public CDR getCdr() {
		return cdr;
	}

	public void setCdr(CDR cdr) {
		this.cdr = cdr;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public CDRStatus getStatus() {
		return status;
	}

	public void setStatus(CDRStatus status) {
		this.status = status;
	}

	public EDR getEdr() {
		return edr;
	}

	public void setEdr(EDR edr) {
		this.edr = edr;
	}

	public byte[] getMagicNumber() {
		return magicNumber;
	}

	public void setMagicNumber(byte[] magicNumber) {
		this.magicNumber = magicNumber;
	}

	public String getAccessUserId() {
		return accessUserId;
	}

	public void setAccessUserId(String accessUserId) {
		this.accessUserId = accessUserId;
	}

	public String getAccessServiceId() {
		return accessServiceId;
	}

	public void setAccessServiceId(String accessServiceId) {
		this.accessServiceId = accessServiceId;
	}

	public Access getOriginAccess() {
		return originAccess;
	}

	public void setOriginAccess(Access originAccess) {
		this.originAccess = originAccess;
	}

	public Access getTargetAccess() {
		return targetAccess;
	}

	public void setTargetAccess(Access targetAccess) {
		this.targetAccess = targetAccess;
	}

	public String getOriginZoneCode() {
		return originZoneCode;
	}

	public void setOriginZoneCode(String originZoneCode) {
		this.originZoneCode = originZoneCode;
	}

	public String getTargetZoneCode() {
		return targetZoneCode;
	}

	public void setTargetZoneCode(String targetZoneCode) {
		this.targetZoneCode = targetZoneCode;
	}

	public String getOriginSubZoneCode() {
		return originSubZoneCode;
	}

	public void setOriginSubZoneCode(String originSubZoneCode) {
		this.originSubZoneCode = originSubZoneCode;
	}

	public String getTargetSubZoneCode() {
		return targetSubZoneCode;
	}

	public void setTargetSubZoneCode(String targetSubZoneCode) {
		this.targetSubZoneCode = targetSubZoneCode;
	}

	public String getOriginTimeCode() {
		return originTimeCode;
	}

	public void setOriginTimeCode(String originTimeCode) {
		this.originTimeCode = originTimeCode;
	}

	public String getTargetTimeCode() {
		return targetTimeCode;
	}

	public void setTargetTimeCode(String targetTimeCode) {
		this.targetTimeCode = targetTimeCode;
	}

	public String getOriginNumeringCode() {
		return originNumeringCode;
	}

	public void setOriginNumeringCode(String originNumeringCode) {
		this.originNumeringCode = originNumeringCode;
	}

	public String getTargetNumeringCode() {
		return targetNumeringCode;
	}

	public void setTargetNumeringCode(String targetNumeringCode) {
		this.targetNumeringCode = targetNumeringCode;
	}

	public UsageCount getUsageCount() {
		return usageCount;
	}

	public void setUsageCount(UsageCount usageCount) {
		this.usageCount = usageCount;
	}
    
}
