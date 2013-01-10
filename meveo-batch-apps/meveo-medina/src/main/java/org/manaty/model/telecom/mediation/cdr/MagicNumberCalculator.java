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

import java.util.Calendar;
import java.util.Date;

import org.manaty.telecom.mediation.MedinaConfig;
import org.meveo.commons.utils.StringUtils;

/**
 * Singleton class for calculating CDR identifiers (magic numbers). Concrete
 * instance with strategy of how to calculate CDR identifiers are configured.
 * 
 * @author Ignas
 * @created Apr 20, 2009
 */
public abstract class MagicNumberCalculator {

    private static MagicNumberCalculator instance;

    static {
        String algorithm = MedinaConfig.getMagicNumberCalculationAlgorithm();
        if ("md5".equals(algorithm.toLowerCase())) {
            instance = new MD5MagicNumberCalculator();
        } else {
        	try{
        		instance = (MagicNumberCalculator) Class.forName(algorithm).newInstance();
        	} catch(Exception e){
        		instance = new MagicNumberCalculator() {
					
					@Override
					public int getMagicNumberLenght() {
						return 50;
					}
					
					@Override
					protected byte[] calculate(byte[]... fields) {
						return this.joinFields(fields);
					}
				};
        		System.err.println("Unknown magic number calculation alghorithm ("+algorithm+"), use the generic one");
        	}
        }
    }

    public MagicNumberCalculator() {

    }

    public static MagicNumberCalculator getInstance() {
        return instance;
    }
    
    public byte[] calculateForGenericCDR(CDR cdr) {
        byte[] cdrTypeBytes = cdr.getEventType() == null ? "0".getBytes() : cdr.getEventType().getBytes();
        byte[] originSensorIdBytes = cdr.getOriginSensorId() == null ? "0".getBytes() :(cdr.getOriginSensorId()).getBytes();
        byte[] originUserIdBytes = cdr.getOriginUserId() == null ? "0".getBytes() : (cdr.getOriginUserId()).getBytes();
        byte[] targetUserIdBytes = cdr.getTargetUserId() == null ? "0".getBytes() : (cdr.getTargetUserId()).getBytes();
    	Calendar calendar = Calendar.getInstance();
        calendar.setTime(cdr.getStartDate());
        byte[] recordOpeningTimeBytes = new byte[] { (byte) (calendar.get(Calendar.YEAR) % 100),
                 (byte) (calendar.get(Calendar.MONTH)), (byte) (calendar.get(Calendar.DAY_OF_MONTH)),
                 (byte) (calendar.get(Calendar.HOUR_OF_DAY)), (byte) (calendar.get(Calendar.MINUTE)),
                 (byte) (calendar.get(Calendar.SECOND)) };
    	return MagicNumberCalculator.getInstance().calculate(cdrTypeBytes, originSensorIdBytes, originUserIdBytes,
    			recordOpeningTimeBytes,targetUserIdBytes, new byte[] { 0 });
    }

    /**
     * Calculate magic number for VOICE ticket. 
     * 
     * @param iPBinV4Address IP address.
     * @param nodeID NodeID.
     * @param recordSequenceNumber sequence number.
     * @param recordOpeningTime opening time.
     * @return Magic number.
     */
    public byte[] calculateForDATA(String iPBinV4Address, String nodeID, String recordSequenceNumber,
            Date recordOpeningTime) {
        if (StringUtils.isBlank(iPBinV4Address))
            iPBinV4Address = "0.0.0.0";
        String[] ipAddrNumbers = iPBinV4Address.split("\\.");
        byte[] ipAddressBytes = { (byte) Integer.parseInt(ipAddrNumbers[0]), (byte) Integer.parseInt(ipAddrNumbers[1]),
                (byte) Integer.parseInt(ipAddrNumbers[2]), (byte) Integer.parseInt(ipAddrNumbers[3]) };

        if (StringUtils.isBlank(nodeID))
            nodeID = "GGSN0000";
        long nodeIDNum = Long.parseLong(getStringNumericalPart(nodeID));
        short nodeIDNumTwoBytes = (short) nodeIDNum;
        byte[] nodeIDBytes = new byte[] { (byte) (nodeIDNumTwoBytes >>> 8), (byte) nodeIDNumTwoBytes };

        if (StringUtils.isBlank(recordSequenceNumber))
            recordSequenceNumber = "0";
        long recordSequenceNum = Long.parseLong(recordSequenceNumber);
        short recordSequenceNumTwoBytes = (short) recordSequenceNum;
        byte[] recordSequenceNumberBytes = new byte[] { (byte) (recordSequenceNumTwoBytes >>> 8),
                (byte) recordSequenceNumTwoBytes };

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(recordOpeningTime);
        byte[] recordOpeningTimeBytes = new byte[] { (byte) (calendar.get(Calendar.YEAR) % 100),
                (byte) (calendar.get(Calendar.MONTH)), (byte) (calendar.get(Calendar.DAY_OF_MONTH)),
                (byte) (calendar.get(Calendar.HOUR_OF_DAY)), (byte) (calendar.get(Calendar.MINUTE)),
                (byte) (calendar.get(Calendar.SECOND)) };

        return MagicNumberCalculator.getInstance().joinFields(nodeIDBytes, ipAddressBytes, recordSequenceNumberBytes,
                recordOpeningTimeBytes, new byte[] { 0 }); // DATA
    }

    /**
     * Calculate magic number for SMS and VOICE tickets. 
     * 
     * @param msisdn msisdn from ticket.
     * @param phoneNumber target phone number.
     * @param recordSequenceNumber sequence number.
     * @param recordOpeningTime sms opening time.
     * @param imsi imsi from ticket.
     * @param plmn plmn from ticket.
     * @param ssCode
     * @param codeFermenteur
     * @param idnCom
     * @param isIncoming is incoming or outgoing ticket.
     * @param isSMS
     * @param callIdentificationNumber customer field 21 (from ticket)
     * 
     * @return Magic number.
     */
    public byte[] calculateForSMSAndVOICE(String msisdn, String phoneNumber, String recordSequenceNumber,
            Date recordOpeningTime, String imsi, String plmn, String ssCode, String codeFermenteur, String idnCom, 
            boolean isIncoming, boolean isSMS, String callIdentificationNumber) {
        
        byte[] calledNumberBytes = phoneNumber == null ? "0".getBytes() : phoneNumber.getBytes();
        byte[] msisdnBytes = msisdn == null ? "0".getBytes() : msisdn.getBytes();

        if (StringUtils.isBlank(recordSequenceNumber))
            recordSequenceNumber = "0";
        long recordSequenceNum = Long.parseLong(recordSequenceNumber);
        short recordSequenceNumTwoBytes = (short) recordSequenceNum;
        byte[] recordSequenceNumberBytes = new byte[] { (byte) (recordSequenceNumTwoBytes >>> 8),
                (byte) recordSequenceNumTwoBytes };

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(recordOpeningTime);
        byte[] recordOpeningTimeBytes = new byte[] { (byte) (calendar.get(Calendar.YEAR) % 100),
                (byte) (calendar.get(Calendar.MONTH)), (byte) (calendar.get(Calendar.DAY_OF_MONTH)),
                (byte) (calendar.get(Calendar.HOUR_OF_DAY)), (byte) (calendar.get(Calendar.MINUTE)),
                (byte) (calendar.get(Calendar.SECOND)) };
        
        byte[] imsiBytes = imsi == null ? "0".getBytes() : imsi.getBytes();
        
        byte[] plmnBytes = plmn == null ? "0".getBytes() : plmn.getBytes();

        byte[] ssCodeBytesBytes = ssCode == null ? "0".getBytes() : ssCode.getBytes();

        byte[] codeFermenteurBytes = codeFermenteur == null ? "0".getBytes() : codeFermenteur.getBytes();
        
        byte[] idnComBytes = idnCom == null ? "0".getBytes() : idnCom.getBytes();
        
        byte[] isIncomingBytes = isIncoming ? new byte[] { 1 } : new byte[] { 0 }; // incoming - 1, outgoing - 0
        
        byte[] isSMSBytes = isSMS ? new byte[] { 1 } : new byte[] { 2 }; // SMS - 1, VOICE - 2

        // get the custom field 21 (ticketId) only if the tiket is a SMSMO (outgoing SMS) to calculate magic number
        byte[] callIdentificationNumberBytes = callIdentificationNumber==null||!isSMS||isIncoming?"0".getBytes():callIdentificationNumber.getBytes();
        
        return MagicNumberCalculator.getInstance().calculate(calledNumberBytes, msisdnBytes, recordSequenceNumberBytes,
                recordOpeningTimeBytes, imsiBytes, plmnBytes, isIncomingBytes, ssCodeBytesBytes, codeFermenteurBytes, 
                idnComBytes, isSMSBytes, callIdentificationNumberBytes); // SMS
    }

    protected abstract byte[] calculate(byte[]... fields);

    /**
     * Returns Magic number length in bytes. Length might be different for
     * every calculator.
     * 
     * @return Magic number length in bytes.
     */
    public abstract int getMagicNumberLenght();

    /**
     * Takes fields as byte arrays, and joins them to one array which is sum of
     * fields.
     * 
     * @param fields
     *            Fields used for identifier calculation
     * @return Fields joined in one array.
     */
    protected byte[] joinFields(byte[]... fields) {
        int length = 0;
        for (byte[] field : fields) {
            if (length != 0) length++; // no separator before first field
            length += field.length;
        }
        byte[] data = new byte[length];
        int i = 0;
        for (byte[] field : fields) {
            if (i != 0) { // no separator before first field
                data[i] = '&';
                i++;
            }
            for (byte num : field) {
                data[i] = num;
                i++;
            }
        }
        return data;
    }

    /**
     * Removes all non digit characters from string.
     * @param string String
     * 
     * @return Numerical string.
     */
    private String getStringNumericalPart(String string) {
        StringBuilder numericalPart = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (Character.isDigit(ch)) {
                numericalPart.append(ch);
            }
        }
        return numericalPart.toString();
    }
}
