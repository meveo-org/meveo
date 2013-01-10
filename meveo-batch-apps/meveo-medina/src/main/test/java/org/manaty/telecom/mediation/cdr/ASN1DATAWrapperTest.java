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
package org.manaty.telecom.mediation.cdr;

import java.util.Date;

import org.manaty.model.telecom.mediation.cdr.BaseCDR;
import org.manaty.model.telecom.mediation.cdr.CDR;
import org.manaty.model.telecom.mediation.cdr.DATACDRWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ASN1DATAWrapperTest {

    @SuppressWarnings("deprecation")
    @Test(groups = { "unit" })
    public void testGetMagicNumber() {
        CDR cdr = new BaseCDR.Builder().addIPBinV4Address("111.12.2.254")
        .addNodeID("GGSN014")
                .addRecordSequenceNumber("1").addRecordOpeningTime(new Date(2009, 5, 25, 14, 22, 59)).build();
        
        CDR dataCDR = new DATACDRWrapper(cdr);
        byte[] arr = dataCDR.getMagicNumber();
        Assert.assertEquals(arr, new byte[] {17, 18, -74, -25, 23, -93, -45, -94, 111, -54, -3, -26, -32, 49, -122, -76});
    }

}
