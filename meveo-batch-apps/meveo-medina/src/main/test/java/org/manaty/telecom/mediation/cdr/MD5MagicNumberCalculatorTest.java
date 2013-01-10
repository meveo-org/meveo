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

import org.manaty.model.telecom.mediation.cdr.MagicNumberCalculator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * Tests for Magic number calculator.
 * 
 * @author Ignas
 *
 */
public class MD5MagicNumberCalculatorTest {
	
	@Test(groups = { "unit" })
    public void testCalculateForSMSAndVOICE() {
		MagicNumberCalculator calculator = MagicNumberCalculator.getInstance();
		Date now = new Date();
		byte[] magicNumber1 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		byte[] magicNumber2 = calculator.calculateForSMSAndVOICE("2222", "1111", "1", now, "1234", "20280", null, null, null, true, true, null);
		byte[] magicNumber3 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		Assert.assertEquals(magicNumber1, magicNumber3);
		Assert.assertFalse(magicNumber1.equals(magicNumber2));
	}

	@Test(groups = { "unit" })
    public void testCalculateForSMSAndVOICEDifferenIMSI() {
		MagicNumberCalculator calculator = MagicNumberCalculator.getInstance();
		Date now = new Date();
		byte[] magicNumber1 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		byte[] magicNumber2 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "4321", "20280", null, null, null, true, true, null);
		byte[] magicNumber3 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		Assert.assertEquals(magicNumber1, magicNumber3);
		Assert.assertFalse(magicNumber1.equals(magicNumber2));
	}
	
	@Test(groups = { "unit" })
    public void testCalculateForSMSAndVOICEDifferentPLMN() {
		MagicNumberCalculator calculator = MagicNumberCalculator.getInstance();
		Date now = new Date();
		byte[] magicNumber1 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		byte[] magicNumber2 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20281", null, null, null, true, true, null);
		byte[] magicNumber3 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		Assert.assertEquals(magicNumber1, magicNumber3);
		Assert.assertFalse(magicNumber1.equals(magicNumber2));
	}
	
	@Test(groups = { "unit" })
    public void testCalculateForSMSAndVOICEDifferentDirrection() {
		MagicNumberCalculator calculator = MagicNumberCalculator.getInstance();
		Date now = new Date();
		byte[] magicNumber1 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		byte[] magicNumber2 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, false, true, null);
		byte[] magicNumber3 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		Assert.assertEquals(magicNumber1, magicNumber3);
		Assert.assertFalse(magicNumber1.equals(magicNumber2));
	}
	
	@Test(groups = { "unit" })
    public void testCalculateForSMSAndVOICEDifferentCDRType() {
		MagicNumberCalculator calculator = MagicNumberCalculator.getInstance();
		Date now = new Date();
		byte[] magicNumber1 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		byte[] magicNumber2 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, false, null);
		byte[] magicNumber3 = calculator.calculateForSMSAndVOICE("1111", "2222", "1", now, "1234", "20280", null, null, null, true, true, null);
		Assert.assertEquals(magicNumber1, magicNumber3);
		Assert.assertFalse(magicNumber1.equals(magicNumber2));
	}
}
