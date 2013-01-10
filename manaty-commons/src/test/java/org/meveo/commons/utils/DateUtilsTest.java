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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for DateUtils.
 * 
 * @author Donatas Remeika
 * @created Apr 8, 2009
 */
@Test(groups = {"unit"})
public class DateUtilsTest {

    @Test(invocationCount = 50, threadPoolSize = 5)
    public void testUniqueCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("ddHHmmss");
        Date date1 = DateUtils.getCurrentDateWithUniqueSeconds();
        Date date2 = DateUtils.getCurrentDateWithUniqueSeconds();
        Assert.assertTrue(Integer.valueOf(format.format(date1)) < Integer.valueOf(format.format(date2)));

        Date date3 = DateUtils.getCurrentDateWithUniqueSeconds();
        Date date4 = DateUtils.getCurrentDateWithUniqueSeconds();
        Assert.assertTrue(Integer.valueOf(format.format(date2)) < Integer.valueOf(format.format(date3)));
        Assert.assertTrue(Integer.valueOf(format.format(date3)) < Integer.valueOf(format.format(date4)));
    }
    
    @Test(groups = { "unit" })
    public void testMonthsBetween() {
        Date date1 = DateUtils.newDate(2010, Calendar.NOVEMBER, 10, 0, 0, 0);
        Date date2 = DateUtils.newDate(2011, Calendar.JANUARY, 11, 0, 0, 0);
        Assert.assertEquals(DateUtils.monthsBetween(date2, date1), 2);
    }
    
}
