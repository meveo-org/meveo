

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

import org.apache.commons.logging.LogFactory;
import java.lang.reflect.InvocationTargetException;
import com.blackbear.flatworm.errors.FlatwormConversionException;
import org.apache.commons.beanutils.PropertyUtils;
import java.util.Map;
import org.apache.commons.logging.Log;

public class PropertyUtilsMappingStrategy implements BeanMappingStrategy
{
    private static Log log;
    
    public void mapBean(final Object bean, final String beanName, final String property, Object value, final Map<String, ConversionOption> conv) throws FlatwormConversionException {
        try {
            final ConversionOption option = conv.get("append");
            if (option != null && "true".equalsIgnoreCase(option.getValue())) {
                final Object currentValue = PropertyUtils.getProperty(bean, property);
                if (currentValue != null) {
                    value = currentValue.toString() + value;
                }
            }
            PropertyUtils.setProperty(bean, property, value);
        }
        catch (IllegalAccessException e) {
            PropertyUtilsMappingStrategy.log.error((Object)("While running set property method for " + beanName + "." + property + "with value '" + value + "'"), (Throwable)e);
            throw new FlatwormConversionException("Setting field " + beanName + "." + property);
        }
        catch (InvocationTargetException e2) {
            PropertyUtilsMappingStrategy.log.error((Object)("While running set property method for " + beanName + "." + property + "with value '" + value + "'"), (Throwable)e2);
            throw new FlatwormConversionException("Setting field " + beanName + "." + property);
        }
        catch (NoSuchMethodException e3) {
            PropertyUtilsMappingStrategy.log.error((Object)("While running set property method for " + beanName + "." + property + "with value '" + value + "'"), (Throwable)e3);
            throw new FlatwormConversionException("Setting field " + beanName + "." + property);
        }
    }
    
    static {
        PropertyUtilsMappingStrategy.log = LogFactory.getLog((Class)PropertyUtilsMappingStrategy.class);
    }
}
