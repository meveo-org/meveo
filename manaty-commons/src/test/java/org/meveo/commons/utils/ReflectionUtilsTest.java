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

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Reflection API helper class tests.
 * 
 * @author Ignas Lelys
 * @created Aug 4, 2010
 *
 */
public class ReflectionUtilsTest {
    
    @Test(groups = { "unit" })
    public void testCreateObject() {
        Object objectCreated = ReflectionUtils.createObject("java.util.Date");
        Assert.assertTrue(objectCreated instanceof Date);
    }
    
    @Test(groups = { "unit" })
    public void testGetPrivateField() throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
        Object field = ReflectionUtils.getPrivateField(TestClass.class, new TestClass(), "privateField");
        Assert.assertTrue(field instanceof String);
        Assert.assertEquals(field, "unaccessible");
    }
    
    @Test(groups = { "unit" })
    public void testSetPrivateField() throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
        Object newValue = "accessible";
        TestClass instance = new TestClass();
        ReflectionUtils.setPrivateField(TestClass.class, instance, "privateField", newValue);
        Object field = ReflectionUtils.getPrivateField(TestClass.class, instance, "privateField");
        Assert.assertTrue(field instanceof String);
        Assert.assertEquals(field, newValue);
    }

    private class TestClass {
        @SuppressWarnings("unused")
        private String privateField = "unaccessible";
    }
}
