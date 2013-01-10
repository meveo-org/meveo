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
 * ASN1 utilities tests.
 * 
 * @author Ignas Lelys
 * @created Aug 4, 2010
 *
 */
public class ASN1UtilsTest {

    @Test(groups = { "unit" })
    public void testIsTypeConstructed() {
        Assert.assertTrue(ASN1Utils.isTypeConstructed(0x20));
        Assert.assertFalse(ASN1Utils.isTypeConstructed(0x40));
    }
    
    @Test(groups = { "unit" })
    public void testIsLongFormLenghtOctet() {
        Assert.assertTrue(ASN1Utils.isLongFormLenghtOctet(0x80));
        Assert.assertFalse(ASN1Utils.isLongFormLenghtOctet(0x70));
    }
    
    @Test(groups = { "unit" })
    public void testGetTagValue() {
        Assert.assertEquals(ASN1Utils.getTagValue(0x11), 17);
    }
    
}