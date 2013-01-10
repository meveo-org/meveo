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
package org.manaty.utils;

import java.math.BigDecimal;
import java.util.Calendar;

import org.manaty.model.mediation.ZonningPlan.CDRTypeEnum;
import org.manaty.model.mediation.ZonningPlan.DirectionEnum;
import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.CDRType;
import org.manaty.model.telecom.mediation.cdr.DATACDRWrapper;
import org.manaty.model.telecom.mediation.cdr.SMSCDRWrapper;
import org.manaty.model.telecom.mediation.cdr.UnknownCDRTypeFieldException;
import org.manaty.telecom.mediation.parser.DigiParser;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * CDRUtils unit tests.
 * 
 * @author Ignas Lelys
 * @created 2010.02.10
 */
public class CDRUtilsTest {
		
	@Test(groups = { "unit" })
	public void testConvertToCDRTypeEnum() {
		Assert.assertEquals(CDRUtils.convertToCDRTypeEnum(CDRType.DATA), CDRTypeEnum.DATA);
        Assert.assertEquals(CDRUtils.convertToCDRTypeEnum(CDRType.ROAMING_DATA), CDRTypeEnum.DATA);
		Assert.assertEquals(CDRUtils.convertToCDRTypeEnum(CDRType.SMS), CDRTypeEnum.SMS);
		Assert.assertEquals(CDRUtils.convertToCDRTypeEnum(CDRType.ROAMING_SMS_IN), CDRTypeEnum.SMS);
		Assert.assertEquals(CDRUtils.convertToCDRTypeEnum(CDRType.ROAMING_SMS_OUT), CDRTypeEnum.SMS);		
		Assert.assertEquals(CDRUtils.convertToCDRTypeEnum(CDRType.VOICE), CDRTypeEnum.VOICE);
	}
	
	@Test(groups = { "unit" })
	public void testConvertToDirectionEnum() {
		Assert.assertEquals(CDRUtils.convertToDirectionEnum(true), DirectionEnum.INCOMING);
		Assert.assertEquals(CDRUtils.convertToDirectionEnum(false), DirectionEnum.OUTGOING);
	}
	
	@Test(groups = { "unit" })
    public void testIsIncoming() {
		 CDR cdr1 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROAMING_SMS_IN).build();
		 Assert.assertTrue(CDRUtils.isIncomingTicket(cdr1));
		 
		 CDR cdr3 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROAMING_SMS_OUT).build();
		 Assert.assertFalse(CDRUtils.isIncomingTicket(cdr3));

		 CDR cdr4 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_SMS).build();
		 Assert.assertFalse(CDRUtils.isIncomingTicket(cdr4));
	}
	
	@Test(groups = { "unit" })
    public void testGetCDRType() throws UnknownCDRTypeFieldException {
		 Assert.assertEquals(CDRUtils.getCDRType(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROAMING_SMS_IN).build()), CDRType.ROAMING_SMS_IN);
		 Assert.assertEquals(CDRUtils.getCDRType(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROAMING_SMS_OUT).build()), CDRType.ROAMING_SMS_OUT);
		 Assert.assertEquals(CDRUtils.getCDRType(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROAMING_DATA).build()), CDRType.ROAMING_DATA);
		 Assert.assertEquals(CDRUtils.getCDRType(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_DATA).build()), CDRType.DATA);
		 Assert.assertEquals(CDRUtils.getCDRType(new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_SMS).build()), CDRType.SMS);
	}
	
	@Test(groups = { "unit" })
    public void testGetRealCalledNumberForEDR() {
		 // incoming and roaming
		 CDR cdr1 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROAMING_SMS_IN).addCalledNumberOriginalRoaming("1120820").build();
		 Assert.assertEquals(CDRUtils.getRealCalledNumberForEDR(cdr1, "2222"), "1120820");

		 // TODO
		 //		 // not roaming
//		 CDR cdr2 = new BaseCDR.Builder().addCDRType("MST").addOriginPLMN("20820").addServedIMSI("1111").addServedMSISDN("2222").addNature("NOT_ROAMING").build();
//		 Assert.assertEquals(CDRUtils.getRealCalledNumberForEDR(cdr2, "2222"), null);
//		 // not incoming
//		 CDR cdr3 = new BaseCDR.Builder().addCDRType("MSG").addOriginPLMN("11120820").addServedIMSI("1111").addServedMSISDN("2222").addNature("GUE").build();
//		 Assert.assertEquals(CDRUtils.getRealCalledNumberForEDR(cdr3, "2222"), null);
	}
    
    @Test(groups = { "unit" })
    public void testGetDATASource() {
        Calendar openingTime = Calendar.getInstance();
        openingTime.set(2010, 0, 1, 12, 15, 45);
        Calendar closingTime = Calendar.getInstance();
        closingTime.set(2010, 0, 1, 12, 0, 1);
        Calendar cellTime = Calendar.getInstance();
        cellTime.set(2010, 0, 1, 12, 0, 2);
        CDR cdr = new DATACDRWrapper(new BaseCDR.Builder()
            .addAccessPointNameNI("nameNI")
            .addCauseForRecordClosing("N")
            .addCDRType(CDRUtils.CDR_TYPE_DATA)
            .addChargingID("5")
            .addDownloadedDataVolume(1L)
            .addDuration(10L)
            .addEtatELU("aaa")
            .addIOT(new BigDecimal("1.1"))
            .addIPBinV4Address("123.255.255.255")
            .addNodeID("sss")
            .addOnNET("_ON")
            .addOriginPLMN("20280")
            .addRecordOpeningTime(openingTime.getTime())
            .addRecordSequenceNumber("3")
            .addServedIMEI("1111")
            .addServedIMSI("2222")
            .addServedMSISDN("3333")
            .addTicketID("1234")
            .addUploadedDataVolume(2L)
            .addNature("GUE")
            .addIdnCom("111-111")
            .addPdpConnectionStatus("Open")
            .addRecordClosingTime(closingTime.getTime())
            .addCellId("099")
            .addCellChangeDate(cellTime.getTime())
            .addPDPIpAddress("33.33.33.33")            
            .addMVNORouting("8876")
            .build());
        
        String expectedResult = "3,0,GPRS,1,2010-01-01 12:15:45,0,10,,,,,,,,,,,8876,,1,,,,uplink;2048;0,,downlink;1024;0,3|3";
        
        Assert.assertEquals(DigiParser.buildSource(cdr), expectedResult);
    }
    
//    @Test( groups = { "unit" })
//    public void testGetVOICESource() {
//        Calendar openingTime = Calendar.getInstance();
//        openingTime.set(2010, 0, 1, 12, 0, 0);
//        Calendar cellTime = Calendar.getInstance();
//        cellTime.set(2010, 0, 1, 12, 0, 2);
//        CDR cdr = new VOICECDRWrapper(new BaseCDR.Builder()
//            .addAccessPointNameNI("nameNI")
//            .addCalledNumber("111")
//            .addCauseForRecordClosing("N")
//            .addCDRType("VOICE") // TODO fix when known CDR type
//            .addChargingID("5")
//            .addDownloadedDataVolume(1L)
//            .addDuration(10L)
//            .addEtatELU("aaa")
//            .addIOT(new BigDecimal("1.1"))
//            .addIPBinV4Address("123.255.255.255")
//            .addOnNET("_ON")
//            .addOriginPLMN("20280")
//            .addRecordOpeningTime(openingTime.getTime())
//            .addRecordSequenceNumber("3")
//            .addServedIMEI("1111")
//            .addServedIMSI("2222")
//            .addServedMSISDN("3333")
//            .addTicketID("1234")
//            .addUploadedDataVolume(2L)
//            .addCallingNumber("1234567890")
//            .addMVNORouting("5701")
//            .addNature("GUE")
//            .addOperator("BYTEL")
//            .addSSCode("91")
//            .addNodeID("5321")
//            .addIdnCom("111-111")
//            .addCellId("099")
//            .addCellChangeDate(cellTime.getTime())
//            .build());
//        
//
//        String expectedResult = "";
//        
//        Assert.assertEquals(MyevoCDRTextParser.buildSource(cdr), expectedResult);
//    }
    
    @Test(groups = { "unit" })
    public void testGetSMSSource() {
        Calendar openingTime = Calendar.getInstance();
        openingTime.set(2010, 0, 1, 12, 15, 45);
        Calendar cellTime = Calendar.getInstance();
        cellTime.set(2010, 0, 1, 12, 0, 2);
        CDR cdr = new SMSCDRWrapper(new BaseCDR.Builder()
            .addAccessPointNameNI("nameNI")
            .addCalledNumber("111")
            .addCalledNumberIMSI("IMSI111")
            .addCalledNumberType("NAT")
            .addCalledNumberOriginalRoaming("OrigRoaming111")
            .addCauseForRecordClosing("N")
            .addCDRType(CDRUtils.CDR_TYPE_SMS)
            .addChargingID("5")
            .addDownloadedDataVolume(1L)
            .addDuration(10L)
            .addEtatELU("aaa")
            .addIOT(new BigDecimal("1.1"))
            .addIPBinV4Address("123.255.255.255")
            .addOnNET("_ON")
            .addOriginPLMN("20280")
            .addRecordOpeningTime(openingTime.getTime())
            .addRecordSequenceNumber("3")
            .addServedIMEI("1111")
            .addServedIMSI("2222")
            .addServedMSISDN("3333")
            .addTicketID("1234")
            .addUploadedDataVolume(2L)
            .addCallingNumber("1234567890")
            .addCallingNumberIMSI("IMSI1234567890")
            .addCallingNumberType("INT")
            .addCallingNumberOriginalRoaming("OrigRoaming1234567890")
            .addNature("GUE")
            .addOperator("BYTEL")
            .addIdnCom("111-111")
            .addCellId("099")
            .addCellChangeDate(cellTime.getTime())
            .addMVNORouting("8876")
            .build());
        

        String expectedResult = "3,0,SMS,1,2010-01-01 12:15:45,0,0,1234567890,INT,,IMSI1234567890,,111,NAT,,IMSI111,,8876,,1,,,,,OrigRoaming1234567890|OrigRoaming111,,3|3";
        
        Assert.assertEquals(DigiParser.buildSource(cdr), expectedResult);
    }
}