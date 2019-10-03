

/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.blackbear.flatworm;

import java.lang.reflect.Method;
import com.blackbear.flatworm.errors.FlatwormCreatorException;

public class ParseUtils
{
    public static Object newBeanInstance(final Object beanType) throws FlatwormCreatorException {
        try {
            return beanType.getClass().newInstance();
        }
        catch (Exception e) {
            throw new FlatwormCreatorException("Unable to create new instance of bean '" + beanType.getClass() + "'", e);
        }
    }
    
    public static void invokeAddMethod(final Object target, final String methodName, final Object toAdd) throws FlatwormCreatorException {
        try {
            final Method method = target.getClass().getMethod(methodName, toAdd.getClass());
            method.invoke(target, toAdd);
        }
        catch (Exception e) {
            throw new FlatwormCreatorException(String.format("Unable to invoke add method %s on bean %s with object of type %s", methodName, target.getClass().getSimpleName(), toAdd.getClass().getSimpleName()), e);
        }
    }
}
