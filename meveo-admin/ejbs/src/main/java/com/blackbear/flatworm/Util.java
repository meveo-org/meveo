

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

import org.apache.commons.lang.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

public final class Util
{
    private static Pattern numbersOnly;
    private static Pattern lettersOnly;
    private static Pattern numbersOrLettersOnly;
    
    public static String[] split(final String str, final char chrSplit, final char chrQuote) {
        final List<String> tokens = new ArrayList<String>();
        String str2 = new String();
        boolean inQuote = false;
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == chrSplit && !inQuote) {
                tokens.add(str2);
                str2 = new String();
            }
            else if (str.charAt(i) == chrQuote) {
                inQuote = !inQuote;
            }
            else {
                str2 += str.charAt(i);
            }
        }
        tokens.add(str2);
        return tokens.toArray(new String[0]);
    }
    
    public static String formatDate(final Date date, final String defaultDateFormat, final Map<String, ConversionOption> options) throws Exception {
        String format = getValue(options, "format");
        if (null == format) {
            if (null == defaultDateFormat) {
                throw new Exception("You must define a conversion-option with a date format or supply one, I can find neither");
            }
            format = defaultDateFormat;
        }
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
    
    public static String justify(String str, String value, final Map<String, ConversionOption> options, final int length) {
        if (value == null) {
            value = "both";
        }
        boolean justifyLeft = false;
        boolean justifyRight = false;
        if (value.equalsIgnoreCase("left")) {
            justifyLeft = true;
        }
        if (value.equalsIgnoreCase("right")) {
            justifyRight = true;
        }
        if (value.equalsIgnoreCase("both")) {
            justifyLeft = true;
            justifyRight = true;
        }
        String strPadChar = " ";
        final String arg = getValue(options, "pad-character");
        if (arg != null) {
            strPadChar = arg;
        }
        if (0 == length) {
            if (justifyLeft) {
                int i;
                for (i = str.length() - 1; i > -1 && isPadChar(str.charAt(i), strPadChar); --i) {}
                if (i != str.length() - 1) {
                    str = str.substring(0, i + 1);
                }
            }
            if (justifyRight) {
                int i;
                for (i = 0; i < str.length() && isPadChar(str.charAt(i), strPadChar); ++i) {}
                if (i != 0) {
                    str = str.substring(i, str.length());
                }
            }
        }
        else {
            strPadChar = strPadChar.substring(0, 1);
            if (str.length() < length) {
                final int lenDiff = length - str.length();
                String padding = "";
                for (int j = 0; j < lenDiff; ++j) {
                    padding += strPadChar;
                }
                if (justifyLeft) {
                    str += padding;
                }
                if (justifyRight) {
                    str = padding + str;
                }
            }
        }
        return str;
    }
    
    private static boolean isPadChar(final char c, final String strPadChar) {
        return strPadChar.indexOf(c) != -1;
    }
    
    public static String strip(String str, final String value, final Map<String, ConversionOption> options) {
        if (value.equalsIgnoreCase("non-numeric")) {
            str = Util.numbersOnly.matcher(str).replaceAll("");
        }
        if (value.equalsIgnoreCase("non-alpha")) {
            str = Util.lettersOnly.matcher(str).replaceAll("");
        }
        if (value.equalsIgnoreCase("non-alphanumeric")) {
            str = Util.numbersOrLettersOnly.matcher(str).replaceAll("");
        }
        return str;
    }
    
    public static String substring(String str, final String value, final Map<String, ConversionOption> options) {
        final String[] args = value.split(",");
        str = str.substring(new Integer(args[0]), new Integer(args[1]));
        return str;
    }
    
    public static String defaultValue(final String str, final String value, final Map<String, ConversionOption> options) {
        return StringUtils.isBlank(str) ? value : str;
    }
    
    public static String getValue(final Map<String, ConversionOption> options, final String key) {
        if (options.containsKey(key)) {
            return options.get(key).getValue();
        }
        return null;
    }
    
    static {
        Util.numbersOnly = Pattern.compile("[\\D]+");
        Util.lettersOnly = Pattern.compile("[^A-Za-z]+");
        Util.numbersOrLettersOnly = Pattern.compile("[^A-Za-z0-9]+");
    }
}
