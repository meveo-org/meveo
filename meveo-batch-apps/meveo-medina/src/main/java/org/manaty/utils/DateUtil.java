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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date utilities.
 * 
 * @author Donatas Remeika
 * @created Apr 8, 2009
 */
public class DateUtil {

    private static long lastTime = System.currentTimeMillis() / 1000;

    public static synchronized Date getCurrentDateWithUniqueSeconds() {
        long current = System.currentTimeMillis();
        while (current / 1000 <= lastTime / 1000) {
            current += 1000;
        }
        lastTime = current;
        return new Date(lastTime);
    }
    
    private static final ThreadLocal<SimpleDateFormat> asnDateFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }

    };
    
    public static final SimpleDateFormat getASNDateFormat() {
        return asnDateFormat.get();
    }
    
    private static final ThreadLocal<SimpleDateFormat> medinaDateFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
        	SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        	format.setLenient(false);
            return format;
        }

    };
    
    public static final SimpleDateFormat getMedinaDateFormat() {
        return medinaDateFormat.get();
    }
}
