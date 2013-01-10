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
package org.myevo.rating.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.meveo.core.parser.ParserException;
import org.myevo.rating.model.EDR;

public class EDRParserTest {

    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    @Test
    public void extractEDR() throws ParseException, ParserException {
        String fileName = "test-data/edr/EDR_DATA_Ticket_16_Ferme-anormale_PA3_idcel_59-idcom306-VM-VD.txt.csv";

        EDRParser parser = new EDRParser();
        parser.setParsingFile(fileName);

        EDR edr = parser.next().getEdr();

        assertNotNull("Was not able to parse EDR from a file", edr);

        assertEquals("1", edr.getAPid());
        assertEquals("DATA_TEST", edr.getServiceId());
        assertEquals("161803c2281f0707fdb113f1d4d8da61", edr.getId());
        assertEquals(DATE_FORMAT.parse("20110125100700"), edr.getConsumptionDate());
        assertEquals("1", edr.getAccessPointId());
        assertEquals("null", edr.getIMSI());
        assertEquals("33670190299", edr.getMSISDN());
        assertEquals(20000L, edr.getDownloadVolume().longValue());
        assertEquals(10000L, edr.getUploadVolume().longValue());
        assertEquals(240L, edr.getDuration().longValue());
        assertEquals("M1", edr.getOriginZone());
        assertEquals("toto1.ingenico.com", edr.getAccessPointNameNI());
        assertEquals("23410", edr.getPlmn());
        assertEquals("23410", edr.getPlmnFromTicket());
        assertEquals(false, edr.getRoaming());
        
        assertNull("Only a single EDR should be found", parser.next());
        
        parser.close();
    }
    
    @Test
    public void extractEDRWithEmptyValues() throws ParseException, ParserException {
        String fileName = "test-data/edr/EDR_empty_values.csv";

        EDRParser parser = new EDRParser();
        parser.setParsingFile(fileName);

        EDR edr = parser.next().getEdr();

        assertNotNull("Was not able to parse EDR from a file", edr);

        assertEquals("2", edr.getAPid());
        assertEquals("PA_DATA", edr.getServiceId());
        assertEquals("0a0814267eafa75f4da734df6eaec81f", edr.getId());
        assertEquals(DATE_FORMAT.parse("20110816060134"), edr.getConsumptionDate());
        assertEquals("2", edr.getAccessPointId());
        assertEquals("null", edr.getIMSI());
        assertEquals("33670190299", edr.getMSISDN());
        assertEquals(0L, edr.getDownloadVolume().longValue());
        assertEquals(551348L, edr.getUploadVolume().longValue());
        assertEquals(0L, edr.getDuration().longValue());
        assertNull(edr.getOriginZone());
        assertEquals("null", edr.getAccessPointNameNI());
        assertEquals("20820", edr.getPlmn());
        assertEquals("null", edr.getPlmnFromTicket());
        assertEquals(false, edr.getRoaming());
        
        assertNull("Only a single EDR should be found", parser.next());
        
        parser.close();
    }
}