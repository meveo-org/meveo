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
import org.manaty.utils.CDRUtils;
import org.manaty.utils.ListUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * Tests for TextParser.
 * 
 * @author Ignas Lelys
 * @created Jul 8, 2011
 * 
 */
public class RouterParserTest {
	
	private List<? extends CDR> sampleCDRs;
	
	@BeforeGroups(groups = { "unit" })
    public void init() throws ParseException {

        BaseCDR.Builder builder1 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROUTER_DATA).addRecordSequenceNumber("1").addRecordOpeningTime(
            (new GregorianCalendar(2011, Calendar.AUGUST, 5, 16, 18, 41).getTime())).addDuration(0L).addIPBinV4Address("1.1.1.1").
            addUploadedDataVolume(11L).addOriginPLMN("20820");

        BaseCDR.Builder builder2 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROUTER_DATA).addRecordSequenceNumber("1").addRecordOpeningTime(
        		(new GregorianCalendar(2011, Calendar.AUGUST, 5, 16, 18, 41).getTime())).addDuration(0L).addIPBinV4Address("3.3.3.3").
        		addDownloadedDataVolume(11L).addOriginPLMN("20820");

        BaseCDR.Builder builder3 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROUTER_DATA).addRecordSequenceNumber("1").addRecordOpeningTime(
        		(new GregorianCalendar(2011, Calendar.AUGUST, 5, 16, 18, 41).getTime())).addDuration(0L).addIPBinV4Address("2.2.2.2").
        		addUploadedDataVolume(12L).addOriginPLMN("20820");

        BaseCDR.Builder builder4 = new BaseCDR.Builder().addCDRType(CDRUtils.CDR_TYPE_ROUTER_DATA).addRecordSequenceNumber("1").addRecordOpeningTime(
        		(new GregorianCalendar(2011, Calendar.AUGUST, 5, 16, 18, 41).getTime())).addDuration(0L).addIPBinV4Address("4.4.4.4").
        		addDownloadedDataVolume(12L).addOriginPLMN("20820");


        sampleCDRs = ListUtils.createList(new DATACDRWrapper(builder1.build()), new DATACDRWrapper(builder2.build()), new DATACDRWrapper(builder3.build()), new DATACDRWrapper(builder4.build()));

    }
	
	@Test(groups = { "unit" })
    public void testParsing() throws ParserException {
		RouterParser parser = new RouterParser("test/routerCDRs.router", false);
		CDR cdr;
        List<CDR> parsedCDRs = new ArrayList<CDR>();
		while ((cdr = parser.next()) != null) {
            parsedCDRs.add(cdr);
        }
		Assert.assertEquals(parsedCDRs.size(), 6);
		
		for (int i = 0; i < sampleCDRs.size(); i++) {
			CDR actualCDR = parsedCDRs.get(i);
			CDR expectedCDR = sampleCDRs.get(i);
			Assert.assertEquals(actualCDR.getIPBinV4Address(), expectedCDR.getIPBinV4Address());
//			Assert.assertEquals(actualCDR.getRecordOpeningTime(), expectedCDR.getRecordOpeningTime());
			Assert.assertEquals(actualCDR.getDownloadedDataVolume(), expectedCDR.getDownloadedDataVolume());
			Assert.assertEquals(actualCDR.getUploadedDataVolume(), expectedCDR.getUploadedDataVolume());
			Assert.assertEquals(actualCDR.getCDRType(), expectedCDR.getCDRType());
			Assert.assertEquals(actualCDR.getDuration(), expectedCDR.getDuration());
		}
	}

}
