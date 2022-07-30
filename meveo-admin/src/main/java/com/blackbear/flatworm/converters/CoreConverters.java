

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

package com.blackbear.flatworm.converters;

import org.apache.commons.logging.LogFactory;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import com.blackbear.flatworm.errors.FlatwormConversionException;
import java.text.SimpleDateFormat;
import com.blackbear.flatworm.Util;
import java.util.Date;
import com.blackbear.flatworm.ConversionOption;
import java.util.Map;
import org.apache.commons.logging.Log;

public class CoreConverters
{
    private static Log log;
    
    public String convertChar(final String str, final Map<String, ConversionOption> options) {
        return str;
    }
    
    public String convertChar(final Object obj, final Map<String, ConversionOption> options) {
        return obj.toString();
    }
    
    public Date convertDate(final String str, final Map<String, ConversionOption> options) throws FlatwormConversionException {
        try {
            String format = Util.getValue(options, "format");
            if (str.length() == 0) {
                return null;
            }
            if (format == null) {
                format = "yyyy-MM-dd";
            }
            final SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(str);
        }
        catch (ParseException ex) {
            CoreConverters.log.error((Object)ex);
            throw new FlatwormConversionException(str);
        }
    }
    
    public String convertDate(final Object obj, final Map<String, ConversionOption> options) {
        final Date date = (Date)obj;
        String format = Util.getValue(options, "format");
        if (obj == null) {
            return null;
        }
        if (format == null) {
            format = "yyyy-MM-dd";
        }
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
    
    public Double convertDecimal(final String str, final Map<String, ConversionOption> options) throws FlatwormConversionException {
        try {
            int decimalPlaces = 0;
            final ConversionOption conv = options.get("decimal-places");
            String decimalPlacesOption = null;
            if (null != conv) {
                decimalPlacesOption = conv.getValue();
            }
            final boolean decimalImplied = "true".equals(Util.getValue(options, "decimal-implied"));
            if (decimalPlacesOption != null) {
                decimalPlaces = Integer.parseInt(decimalPlacesOption);
            }
            if (str.length() == 0) {
                return new Double(0.0);
            }
            if (decimalImplied) {
                return new Double(Double.parseDouble(str) / Math.pow(10.0, decimalPlaces));
            }
            return Double.valueOf(str);
        }
        catch (NumberFormatException ex) {
            CoreConverters.log.error((Object)ex);
            throw new FlatwormConversionException(str);
        }
    }
    
    public String convertDecimal(final Object obj, final Map<String, ConversionOption> options) {
        Double d = (Double)obj;
        if (d == null) {
            return null;
        }
        int decimalPlaces = 0;
        final ConversionOption conv = options.get("decimal-places");
        String decimalPlacesOption = null;
        if (null != conv) {
            decimalPlacesOption = conv.getValue();
        }
        final boolean decimalImplied = "true".equals(Util.getValue(options, "decimal-implied"));
        if (decimalPlacesOption != null) {
            decimalPlaces = Integer.parseInt(decimalPlacesOption);
        }
        final DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(!decimalImplied);
        format.setGroupingUsed(false);
        if (decimalImplied) {
            format.setMaximumFractionDigits(0);
            d = new Double(d * Math.pow(10.0, decimalPlaces));
        }
        else {
            format.setMinimumFractionDigits(decimalPlaces);
            format.setMaximumFractionDigits(decimalPlaces);
        }
        return format.format(d);
    }
    
    public Integer convertInteger(final String str, final Map<String, ConversionOption> options) throws FlatwormConversionException {
        try {
            if (str.length() == 0) {
                return new Integer(0);
            }
            return Integer.valueOf(str);
        }
        catch (NumberFormatException ex) {
            CoreConverters.log.error((Object)ex);
            throw new FlatwormConversionException(str);
        }
    }
    
    public String convertInteger(final Object obj, final Map<String, ConversionOption> options) {
        if (obj == null) {
            return null;
        }
        final Integer i = (Integer)obj;
        return Integer.toString(i);
    }
    
    public Long convertLong(final String str, final Map<String, ConversionOption> options) throws FlatwormConversionException {
        try {
            if (str.length() == 0) {
                return new Long(0L);
            }
            return Long.valueOf(str);
        }
        catch (NumberFormatException ex) {
            CoreConverters.log.error((Object)ex);
            throw new FlatwormConversionException(str);
        }
    }
    
    public String convertLong(final Object obj, final Map<String, ConversionOption> options) {
        if (obj == null) {
            return null;
        }
        final Long l = (Long)obj;
        return Long.toString(l);
    }
    
    public BigDecimal convertBigDecimal(final String str, final Map<String, ConversionOption> options) throws FlatwormConversionException {
        try {
            int decimalPlaces = 0;
            final String decimalPlacesOption = Util.getValue(options, "decimal-places");
            final boolean decimalImplied = "true".equals(Util.getValue(options, "decimal-implied"));
            if (decimalPlacesOption != null) {
                decimalPlaces = Integer.parseInt(decimalPlacesOption);
            }
            if (str.length() == 0) {
                return new BigDecimal(0.0);
            }
            if (decimalImplied) {
                return new BigDecimal(Double.parseDouble(str) / Math.pow(10.0, decimalPlaces));
            }
            return new BigDecimal(Double.parseDouble(str));
        }
        catch (NumberFormatException ex) {
            CoreConverters.log.error((Object)ex);
            throw new FlatwormConversionException(str);
        }
    }
    
    public String convertBigDecimal(final Object obj, final Map<String, ConversionOption> options) {
        if (obj == null) {
            return null;
        }
        final BigDecimal bd = (BigDecimal)obj;
        int decimalPlaces = 0;
        final String decimalPlacesOption = Util.getValue(options, "decimal-places");
        final boolean decimalImplied = "true".equals(Util.getValue(options, "decimal-implied"));
        if (decimalPlacesOption != null) {
            decimalPlaces = Integer.parseInt(decimalPlacesOption);
        }
        final DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(!decimalImplied);
        format.setMinimumFractionDigits(decimalPlaces);
        format.setMaximumFractionDigits(decimalPlaces);
        return format.format(bd.doubleValue());
    }
    
    static {
        CoreConverters.log = LogFactory.getLog((Class)CoreConverters.class);
    }
}
