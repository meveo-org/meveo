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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.manaty.model.telecom.mediation.cdr.MagicNumberCalculator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for CDRIdentifierCalculator.
 * 
 * @author Ignas
 * @created Apr 20, 2009
 */
public class MagicNumberCalculatorTest {
    
    @Test(groups = { "unit" })
    public void testJoinFields() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        byte[] field1 = new byte[]{1, 2};
        byte[] field2 = new byte[]{3, 4, 5, 6};
        byte[] field3 = new byte[]{7, 8};
        byte[] field4 = new byte[]{9, 10, 11, 12, 13, 14};
        byte[] field5 = new byte[]{15};
        
        final Method joinFields = MagicNumberCalculator.class.getDeclaredMethod("joinFields", byte[][].class);
        joinFields.setAccessible(true);
        byte[][] params = new byte[5][];
        params[0] = field1;
        params[1] = field2;
        params[2] = field3;
        params[3] = field4;
        params[4] = field5;
        Object paramsAsObject = (Object)params;
        byte[] returnedValue = (byte[]) joinFields.invoke(MagicNumberCalculator.getInstance(), paramsAsObject);
        Assert.assertTrue(returnedValue.length == 19);
        assert (Arrays.equals(returnedValue, new byte[]{1, 2, 38, 3, 4, 5, 6, 38, 7, 8, 38, 9, 10, 11, 12, 13, 14, 38, 15}));
    }

}
