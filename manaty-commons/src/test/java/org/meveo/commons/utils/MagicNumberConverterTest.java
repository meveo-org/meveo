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
package org.meveo.commons.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for HashesConverter utility class.
 * 
 * @author Ignas Lelys
 * @created Apr 7, 2009
 */
public class MagicNumberConverterTest {
    
    @Test(groups = { "unit" })
    public void testHashToArray() {
        String hash = "7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f";
        byte[] expectedArray = {127, 127, 127, 127, 127 ,127, 127, 127,
                127, 127, 127, 127, 127, 127, 127, 127};
        Assert.assertEquals(MagicNumberConverter.convertToArray(hash), expectedArray);
        String hash2 = "00000000000000000000000000000000";
        byte[] expectedArray2 = {0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0};
        Assert.assertEquals(MagicNumberConverter.convertToArray(hash2), expectedArray2);
    }
    
    @Test(groups = { "unit" })
    public void testHashToString() {
        byte[] hash = {127, 127, 127, 127, 127 ,127, 127, 127,
                127, 127, 127, 127, 127, 127, 127, 127};
        String expectedString = "7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f";
        Assert.assertEquals(MagicNumberConverter.convertToString(hash), expectedString);
        byte[] hash2 = {0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0};
        String expectedString2 = "00000000000000000000000000000000";
        Assert.assertEquals(MagicNumberConverter.convertToString(hash2), expectedString2);
    }

}
