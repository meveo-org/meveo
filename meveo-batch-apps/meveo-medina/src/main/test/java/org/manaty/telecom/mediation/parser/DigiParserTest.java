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
package org.manaty.telecom.mediation.parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.DATACDRWrapper;
import org.manaty.model.telecom.mediation.cdr.SMSCDRWrapper;
import org.manaty.telecom.mediation.parser.DigiParser;
import org.manaty.telecom.mediation.parser.Parser;
import org.manaty.telecom.mediation.parser.ParserException;
import org.manaty.utils.CDRUtils;
import org.manaty.utils.ListUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * Tests for TextParser.
 * 
 * @author Ignas Lelys
 * @created Mar 19, 2009
 * 
 */
public class DigiParserTest {

    private static final String PARSING_TEST_FILE_NAME = "test/tickets.txt";

    private List<CDR> sampleCDRs;

    @BeforeGroups(groups = { "unit" })
    public void init() throws ParseException {

        BaseCDR.Builder builder1 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_DATA).addRecordSequenceNumber("1").addRecordOpeningTime(
            (new GregorianCalendar(2009, Calendar.FEBRUARY, 14, 21, 26, 40).getTime())).addDuration(900L).addServedMSISDN("+5493546464352").addCallingNumber("+5493546464352")
            .addCallingNumberType("INT").addCallingNumberIMSI("IMSI1").addServedIMSI("IMSI1").addCallingNumberOriginalRoaming("11111").addMVNORouting("MVNOid1")
            .addUploadedDataVolume(88L).addDownloadedDataVolume(79L).addOriginPLMN("20820");

        BaseCDR.Builder builder2 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_SMS).addRecordSequenceNumber("1").addRecordOpeningTime(
            (new GregorianCalendar(2009, Calendar.FEBRUARY, 15, 20, 27, 41).getTime())).addCallingNumber("+5493546464353").addCallingNumberType("NAT")
            .addCallingNumberIMSI("IMSI2").addCallingNumberOriginalRoaming("11112").addCalledNumber("+3702776523").addCalledNumberType("UNK").addCalledNumberIMSI("IMSI3")
            .addCalledNumberOriginalRoaming("22222").addMVNORouting("MVNOid2").addServedMSISDN("+5493546464353").addOriginPLMN("20820");

        BaseCDR.Builder builder3 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROAMING_DATA).addRecordSequenceNumber("1").addRecordOpeningTime(
            (new GregorianCalendar(2009, Calendar.FEBRUARY, 16, 19, 28, 42).getTime())).addDuration(1900L).addCallingNumber("+5493546464354").addCallingNumberType("NAT")
            .addOriginPLMN("PLMN3").addUploadedDataVolume(10L).addDownloadedDataVolume(69L).addServedMSISDN("+5493546464354");

        BaseCDR.Builder builder4 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROAMING_SMS_IN).addRecordSequenceNumber("1").addRecordOpeningTime(
            (new GregorianCalendar(2009, Calendar.FEBRUARY, 17, 18, 29, 43).getTime())).addCallingNumber("+5493546464355").addCallingNumberType("UNK")
            .addCalledNumber("+3702776524").addCalledNumberType("UNK").addCalledNumberOriginalRoaming(
                "22224").addCalledNumberIMSI("IMSI3").addOriginPLMN("PLMN4").addServedMSISDN("+3702776524");

        BaseCDR.Builder builder5 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROAMING_SMS_OUT).addRecordSequenceNumber("1").addRecordOpeningTime(
            (new GregorianCalendar(2009, Calendar.FEBRUARY, 18, 17, 30, 44).getTime())).addCallingNumber("+5493546464356").addCallingNumberType("UNK")
            .addCallingNumberIMSI("IMSI5").addCalledNumber("+3702776525").addCalledNumberType("INT").addOriginPLMN("PLMN5").addServedMSISDN("+5493546464356");

        sampleCDRs = ListUtils.createList(new DATACDRWrapper(builder1.build()), new SMSCDRWrapper(builder2.build()), new DATACDRWrapper(builder3.build()), new SMSCDRWrapper(
            builder4.build()), new SMSCDRWrapper(builder5.build()));

    }

    @Test(groups = { "unit" })
    public void testParsing() throws ParserException {

        Parser parser = new DigiParser(PARSING_TEST_FILE_NAME, false);
        CDR cdr;
        List<CDR> parsedCDRs = new ArrayList<CDR>();
        while ((cdr = parser.next()) != null) {
            parsedCDRs.add(cdr);
        }

        Assert.assertEquals(parsedCDRs.size(), sampleCDRs.size());
        for (int i = 0; i < parsedCDRs.size(); i++) {
            CDR parsedCDR = parsedCDRs.get(i);
			CDR expectedCDR = sampleCDRs.get(i);
			
//			Assert.assertEquals((String) parsedCDR.getSource(), (String) expectedCDR.getSource(), "CDR line " + (i + 1));
            Assert.assertEquals(parsedCDR.getCDRType(), expectedCDR.getCDRType(), "CDR line " + (i + 1));
            //Assert.assertEquals(parsedCDRs.get(i).getMagicNumber(), sampleCDRs.get(i).getMagicNumber(), "CDR line " + (i + 1));
            Assert.assertEquals(parsedCDR.getClass().getName(), expectedCDR.getClass().getName(), "CDR line " + (i + 1));
            Assert.assertEquals(parsedCDR.getSequenceNumber(), "1", "CDR line " + (i + 1));
            Assert.assertEquals(parsedCDR.getRecordOpeningTime(), expectedCDR.getRecordOpeningTime(), "CDR line " + (i + 1));

            if (parsedCDR instanceof DATACDRWrapper) {

                Assert.assertEquals(parsedCDR.getIMSI(), expectedCDR.getIMSI(), "CDR line " + (i + 1));
                Assert.assertEquals(parsedCDR.getMSISDN(), expectedCDR.getMSISDN(), "CDR line " + (i + 1));

                Assert.assertEquals(parsedCDR.getDownloadedDataVolume(), expectedCDR.getDownloadedDataVolume(), "CDR line " + (i + 1));
                Assert.assertEquals(parsedCDR.getUploadedDataVolume(), expectedCDR.getUploadedDataVolume(), "CDR line " + (i + 1));

            } else if (parsedCDR instanceof SMSCDRWrapper) {
                Assert.assertEquals(parsedCDR.getCallingNumber(), expectedCDR.getCallingNumber(), "CDR line " + (i + 1));
                Assert.assertEquals(parsedCDR.getCallingNumberIMSI(), expectedCDR.getCallingNumberIMSI(), "CDR line " + (i + 1));
                Assert.assertEquals(parsedCDR.getCallingNumberOriginalRoaming(), expectedCDR.getCallingNumberOriginalRoaming(), "CDR line " + (i + 1));
                Assert.assertEquals(parsedCDR.getCallingNumberType(), expectedCDR.getCallingNumberType(), "CDR line " + (i + 1));
                Assert.assertEquals(parsedCDR.getCalledNumber(), expectedCDR.getCalledNumber(), "CDR line " + (i + 1));
                Assert.assertEquals(parsedCDR.getCalledNumberIMSI(), expectedCDR.getCalledNumberIMSI(), "CDR line " + (i + 1));
                Assert.assertEquals(parsedCDR.getCalledNumberOriginalRoaming(), expectedCDR.getCalledNumberOriginalRoaming(), "CDR line " + (i + 1));
                Assert.assertEquals(parsedCDR.getCalledNumberType(), expectedCDR.getCalledNumberType(), "CDR line " + (i + 1));
            } else {
                Assert.assertEquals(parsedCDR.getDuration(), expectedCDR.getDuration(), "CDR line " + (i + 1));

                Assert.assertFalse(true, "Unknown CDR type");
            }

            if (CDRUtils.isRoamingTicket(parsedCDR)) {
                Assert.assertEquals(parsedCDR.getMVNORouting(), expectedCDR.getMVNORouting(), "CDR line " + (i + 1));

            } else {
                Assert.assertEquals(parsedCDR.getOriginPLMN(), expectedCDR.getOriginPLMN(), "CDR line " + (i + 1));
            }
            
        }
    }
}