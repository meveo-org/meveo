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
package org.manaty.telecom.mediation.edr;

import java.math.BigDecimal;
import java.util.Calendar;

import org.manaty.model.mediation.NumberingPlan.SpecialNumberEnum;
import org.manaty.model.telecom.mediation.edr.EDR;
import org.manaty.model.telecom.mediation.edr.EDRFileBuilder;
import org.manaty.model.telecom.mediation.edr.VOICEEDRBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for VOICE EDR file builder.
 * 
 * @author Ignas Lelys
 * @created 2009.07.30
 */
public class VOICEEDRBuilderTest {
    
    @Test(expectedExceptions = RuntimeException.class, groups = { "unit" })
    public void testConstructorException() {
        new VOICEEDRBuilder("~/file/not/exists/~");
    }

    @Test(groups = { "unit" })
    public void testAppend() {
        
        EDRFileBuilder builder = new VOICEEDRBuilder("test/dummy.csv");
        EDR edr = new EDR();
        edr.setServiceId("DATA");
        edr.setUserId("USER_ID");
        edr.setId("0123FFAACC1123");
        Calendar cal = Calendar.getInstance();
        cal.set(2009, Calendar.JANUARY, 1, 1, 1, 59);
        edr.setConsumptionDate(cal.getTime());
        edr.setAccessPointId(1L);
        edr.setIMSI("123456");
        edr.setMSISDN("20280555555");
        edr.setDuration(1L);
        edr.setOriginZone("ORIGIN");
        edr.setTargetZone("TARGET");
        edr.setCalledNumber("863301180");
        edr.setPlmn("20280");
        edr.setAccessPointNameNI("accessName");
        edr.setDownloadVolume(11L);
        edr.setUploadVolume(11L);
        edr.setIOT(new BigDecimal("0.11"));
        edr.setIncoming(true);
        edr.setRoaming(false);
        edr.setSpecialCalledNumber(SpecialNumberEnum.DATA_PLUS);
        edr.setCallingNumber("3369111111");
        
        String line = builder.append(edr);
        
        Assert.assertEquals(line, "USER_ID;DATA;0123FFAACC1123;20090101010159;1;123456;20280555555;1;ORIGIN;TARGET;863301180;20280;;0.11;1;0;DATA_PLUS;3369111111");

    }

}
