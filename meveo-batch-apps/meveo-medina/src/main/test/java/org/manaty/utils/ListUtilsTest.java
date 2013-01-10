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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for ListUtils utility class.
 * 
 * @author Ignas Lelys
 * @created Mar 18, 2009
 *
 */
public class ListUtilsTest {
    
    @Test(groups = { "unit" })
    public void testCreateList() {
        List<String> stringsList = ListUtils.createList("test1", "test2");
        Assert.assertEquals(stringsList.get(0), "test1");
        Assert.assertEquals(stringsList.get(1), "test2");
        Assert.assertEquals(stringsList.size(), 2);
        List<String> emtyList = ListUtils.createList();
        Assert.assertTrue(emtyList.isEmpty());
    }

}
