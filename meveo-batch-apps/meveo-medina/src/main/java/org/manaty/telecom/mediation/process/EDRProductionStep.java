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

import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.model.telecom.mediation.cdr.CDRType.CDRSubtype;
import org.manaty.model.telecom.mediation.edr.EDR;
import org.manaty.telecom.mediation.context.MediationContext;
import org.manaty.utils.CDRUtils;
import org.manaty.utils.MagicNumberConverter;

/**
 * Convert data to EDR bean.
 * 
 * @author Donatas Remeika
 * @created Mar 6, 2009
 */
public class EDRProductionStep extends AbstractProcessStep {

    private static final String NO_MAGIC_NUMBER_MESSAGE = "NO_IDENTIFIER";

    public EDRProductionStep(AbstractProcessStep nextStep) {
        super(nextStep);
    }

    /**
     * Convert data to EDR format.
     */
    @Override
    protected boolean execute(MediationContext context) {
        CDR cdr = context.getCDR();
        CDRType type = context.getType();
        byte[] magicNumber = context.getMagicNumber();
        
        EDR edr = new EDR();
        edr.setUserId(String.valueOf(context.getAccessUserId()));
        edr.setServiceId(String.valueOf(context.getAccessServiceId()));
        edr.setId(magicNumber != null ? MagicNumberConverter.convertToString(magicNumber) : NO_MAGIC_NUMBER_MESSAGE);
        edr.setConsumptionDate(cdr.getRecordOpeningTime());
        edr.setIMSI(cdr.getIMSI());
        edr.setMSISDN(cdr.getMSISDN());
        edr.setOriginZone(context.getOriginZone());
        edr.setTargetZone(context.getTargetZone());
        edr.setAccessPointId(context.getAccess().getId());
        edr.setPlmn(context.getOriginPlmn());
        edr.setPlmnFromTicket(cdr.getOriginPLMN());
        edr.setRoaming(context.getRoaming());
        if (type.getCDRSubType() == CDRSubtype.VOICE) {
            edr.setIOT(context.getCDR().getIOT());
            edr.setSpecialCalledNumber(context.getSpecialNumberType());
        }
        if (type.getCDRSubType() == CDRSubtype.DATA) {
            edr.setDownloadVolume(cdr.getDownloadedDataVolume());
            edr.setUploadVolume(cdr.getUploadedDataVolume());
            edr.setAccessPointNameNI(cdr.getAccessPointNameNI());
        }
        if (type.getCDRSubType() == CDRSubtype.DATA || type.getCDRSubType() == CDRSubtype.VOICE) {
            edr.setDuration(cdr.getDuration());
        }
        if (type.getCDRSubType() == CDRSubtype.SMS || type.getCDRSubType() == CDRSubtype.VOICE ) {
            edr.setCalledNumber(CDRUtils.getRealCalledNumberForEDR(cdr, context.getAccess().getAccessUserId()));
            edr.setIncoming(context.getIncoming());
            edr.setSpecialCalledNumber(context.getSpecialNumberType());
            edr.setCallingNumber(cdr.getCallingNumber());
        }
        
        context.setEDR(edr);
        return true;
    }
}