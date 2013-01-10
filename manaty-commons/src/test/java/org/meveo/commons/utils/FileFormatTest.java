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
 * Tests for FileFormat class.
 * 
 * @author Donatas Remeika
 * @created Mar 18, 2009
 */
public class FileFormatTest {

    @Test(groups = { "unit" })
    public void testParseFromNullExtension() {
        FileFormat format = FileFormat.parseFromExtension(null);
        Assert.assertEquals(format, FileFormat.OTHER);
    }

    @Test(groups = { "unit" })
    public void testParseFromCSVExtension() {
        FileFormat format = FileFormat.parseFromExtension("cSv");
        Assert.assertEquals(format, FileFormat.CSV);
    }

    @Test(groups = { "unit" })
    public void testParseFromASNExtension() {
        FileFormat format = FileFormat.parseFromExtension("aSn");
        Assert.assertEquals(format, FileFormat.ASN);
    }

    @Test(groups = { "unit" })
    public void testParseFromOtherExtension() {
        FileFormat format = FileFormat.parseFromExtension("unknown");
        Assert.assertEquals(format, FileFormat.OTHER);
    }

}
