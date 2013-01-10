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
package org.manaty.telecom.mediation.parser;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.manaty.model.telecom.mediation.cdr.CDR;

/**
 * Abstract class for parsers.
 * 
 * @author Ignas Lelys
 * @created Mar 19, 2009
 *
 */
/**
 * @author Ignas
 *
 */
public abstract class AbstractParser implements Parser {
    
    private static final Logger logger = Logger.getLogger(AbstractParser.class);

    protected String fileName;
    
    public AbstractParser(String fileName) {
        super();
        this.fileName = fileName;
    }

    /**
     * @see org.manaty.telecom.mediation.parser.Parser#close()
     */
    public abstract void close();

    /**
     * @see org.manaty.telecom.mediation.parser.Parser#next()
     */
    public abstract CDR next() throws ParserException;
    
    /**
     * Filters empty string value, by returning null instead of it.
     * 
     * @param value String value
     * @return same string value or null if string was empty
     */
    protected static String getFieldValue(String value) {
        return !"".equals(value) ? value : null;
    }
    
    /**
     * Filters empty string value, by returning defaultValue instead of it.
     * 
     * @param value String value
     * @return same string value or defaultValue if string was empty
     */
    protected static String getFieldValue(String value, String defaultValue) {
        return !"".equals(value) ? value : defaultValue;
    }

    /**
     * Get date value parsed by given format.
     * 
     * @param value
     *            String representation.
     * @param format
     *            Format to parse by.
     * @return Date object.
     */
    protected static Date getDateFieldValue(String value, SimpleDateFormat format, String dateFormatString) {
        if (value != null && value.length() > dateFormatString.length()) {
            value = value.substring(0, dateFormatString.length());
        } 
        return (value != null && value.length() == dateFormatString.length()) ? parseDate(value, format) : null;
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
    private static Date parseDate(String date, SimpleDateFormat format) {
        if (date != null) {
            try {
                return format.parse(date);
            } catch (ParseException e) {
                logger.warn(
                        String.format("Could not parse date from '%s' using format '%s'", date, format
                                .toPattern()));
            }
        }
        return null;
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
            logger.warn(String.format("Could not parse Long from '%s'", value), e);
        }
        return null;
    }

    /**
     * Get field value as Int.
     * 
     * @param value
     *            String representation of integer.
     * @return integer value or 0 if was not able to parse
     */
    protected int getIntFieldValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn(String.format("Could not parse integer from '%s'", value), e);
        }
        return 0;
    }
    
    /**
     * Get field value as BigDecimal.
     * 
     * @param value String representation of BigDecimal.
     * @return BigDecimal object.
     */
    protected BigDecimal getBigDecimalFieldValue(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            logger.warn(String.format("Could not parse BigDecimal from '%s'", value), e);
        } catch (NullPointerException e) {
            logger.warn(String.format("BigDecimal value was null"), e);
        }
        return null;
    }

}
