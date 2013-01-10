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
package org.meveo.core.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Abstract class that provides some helper method for parser implementations.
 * 
 * @author Ignas Lelys
 * @created Mar 19, 2009
 * 
 */
public abstract class AbstractParser<T> implements Parser<T> {

    private static final Logger logger = Logger.getLogger(AbstractParser.class);

    /**
     * Filters empty string value, by returning null instead of it.
     * 
     * @param value
     *            String value
     * @return same string value or null if string was empty
     */
    protected String getFieldValue(String value) {
        return !"".equals(value) ? value : null;
    }

    /**
     * Filters empty string value, by returning defaultValue instead of it.
     * 
     * @param value
     *            String value
     * @return same string value or defaultValue if string was empty
     */
    protected String getFieldValue(String value, String defaultValue) {
        return !"".equals(value) ? value : defaultValue;
    }

    /**
     * Get date value parsed by given format.
     * 
     * @param value
     *            String representation.
     * @param format
     *            Format to parse by.
     * @param dateFormatString
     *            Date format string representation. Used to check if value is
     *            not too long for current format, and if so value is cut to the
     *            lenght of dateFormatString
     * @return Date object.
     */
    protected Date getDateFieldValue(String value, SimpleDateFormat format, String dateFormatString) {
        if (value != null && value.length() > dateFormatString.length()) {
            value = value.substring(0, dateFormatString.length());
        }
        return (value != null && value.length() == dateFormatString.length()) ? parseDate(value, format) : null;
    }

    /**
     * Get field value as Long.
     * 
     * @param value
     *            String representation of long.
     * @return Long object.
     */
    protected Long getLongFieldValue(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            logger.warn(String.format("Could not parse long from '%s'", value), e);
        }
        return null;
    }

    /**
     * Get field value as Boolean.
     * 
     * @param value
     *            String representation of Boolean.
     * @return Boolean object.
     */
    protected Boolean getBooleanFieldValue(String value) {
        if ("0".equals(value) || "false".equals(value)) {
            return false;
        } else if ("1".equals(value) || "true".equals(value)) {
            return true;
        } else {
            logger.warn(String.format("Could not parse boolean from '%s'", value));
            return null;
        }
    }

    /**
     * Parse date from String.
     * 
     * @param date
     *            String representation of the date.
     * @param format
     *            SimpleDateFormat to parse from.
     * @return Date object.
     */
    private Date parseDate(String date, SimpleDateFormat format) {
        if (date != null) {
            try {
                return format.parse(date);
            } catch (ParseException e) {
                logger
                        .warn(String.format("Could not parse date from '%s' using format '%s'", date, format
                                .toPattern()));
            }
        }
        return null;
    }

}
