

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
import java.util.Iterator;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;
import com.blackbear.flatworm.errors.FlatwormConversionException;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.Map;
import org.apache.commons.logging.Log;

public class ConversionHelper
{
    private static Log log;
    private Map<String, Converter> converters;
    private Map<Converter, Method> converterMethodCache;
    private Map<Converter, Method> converterToStringMethodCache;
    private Map<String, Object> converterObjectCache;
    
    public ConversionHelper() {
        this.converters = new HashMap<String, Converter>();
        this.converterMethodCache = new HashMap<Converter, Method>();
        this.converterToStringMethodCache = new HashMap<Converter, Method>();
        this.converterObjectCache = new HashMap<String, Object>();
    }
    
    public Object convert(final String type, String fieldChars, final Map<String, ConversionOption> options, final String beanRef) throws FlatwormConversionException {
        Object value = null;
        try {
            final Object object = this.getConverterObject(type);
            final Method method = this.getConverterMethod(type);
            fieldChars = this.transformString(fieldChars, options, 0);
            final Object[] args = { fieldChars, options };
            value = method.invoke(object, args);
        }
        catch (IllegalAccessException e) {
            ConversionHelper.log.error((Object)("While running convert method for " + beanRef), (Throwable)e);
            throw new FlatwormConversionException("Converting field " + beanRef + " with value '" + fieldChars + "'");
        }
        catch (InvocationTargetException e2) {
            ConversionHelper.log.error((Object)("While running convert method for " + beanRef), (Throwable)e2);
            throw new FlatwormConversionException("Converting field " + beanRef + " with value '" + fieldChars + "'");
        }
        catch (IllegalArgumentException e3) {
            ConversionHelper.log.error((Object)("While running convert method for " + beanRef), (Throwable)e3);
            throw new FlatwormConversionException("Converting field " + beanRef + " with value '" + fieldChars + "'");
        }
        return value;
    }
    
    public String convert(final String type, final Object obj, final Map<String, ConversionOption> options, final String beanRef) throws FlatwormConversionException {
        try {
            final Object converter = this.getConverterObject(type);
            final Method method = this.getToStringConverterMethod(type);
            final Object[] args = { obj, options };
            final String result = (String)method.invoke(converter, args);
            return result;
        }
        catch (IllegalArgumentException e) {
            ConversionHelper.log.error((Object)("While running toString convert method for " + beanRef), (Throwable)e);
            throw new FlatwormConversionException("Converting field " + beanRef + " to string for value '" + obj + "'");
        }
        catch (IllegalAccessException e2) {
            ConversionHelper.log.error((Object)("While running toString convert method for " + beanRef), (Throwable)e2);
            throw new FlatwormConversionException("Converting field " + beanRef + " to string for value '" + obj + "'");
        }
        catch (InvocationTargetException e3) {
            ConversionHelper.log.error((Object)("While running toString convert method for " + beanRef), (Throwable)e3);
            throw new FlatwormConversionException("Converting field " + beanRef + " to string for value '" + obj + "'");
        }
    }
    
    public String transformString(String fieldChars, final Map<String, ConversionOption> options, final int length) {
        final Set<String> keys = options.keySet();
        final Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            final ConversionOption conv = options.get(it.next());
            if (conv.getName().equals("justify")) {
                fieldChars = Util.justify(fieldChars, conv.getValue(), options, length);
            }
            if (conv.getName().equals("strip-chars")) {
                fieldChars = Util.strip(fieldChars, conv.getValue(), options);
            }
            if (conv.getName().equals("substring")) {
                fieldChars = Util.substring(fieldChars, conv.getValue(), options);
            }
            if (conv.getName().equals("default-value")) {
                fieldChars = Util.defaultValue(fieldChars, conv.getValue(), options);
            }
        }
        if (length > 0 && fieldChars.length() > length) {
            fieldChars = fieldChars.substring(0, length);
        }
        return fieldChars;
    }
    
    public void addConverter(final Converter converter) {
        this.converters.put(converter.getName(), converter);
    }
    
    public Converter getConverter(final String name) {
        Converter result = null;
        final Converter convert = this.converters.get(name);
        if (convert != null) {
            result = new Converter();
            result.setConverterClass(convert.getConverterClass());
            result.setMethod(convert.getMethod());
            result.setName(convert.getName());
            result.setReturnType(convert.getReturnType());
        }
        return result;
    }
    
    private Method getConverterMethod(final String type) throws FlatwormConversionException {
        try {
            final Converter c = this.converters.get(type);
            if (this.converterMethodCache.get(c) != null) {
                return this.converterMethodCache.get(c);
            }
            final Class<?> cl = Class.forName(c.getConverterClass());
            final Class[] args = { String.class, Map.class };
            final Method meth = cl.getMethod(c.getMethod(), (Class<?>[])args);
            this.converterMethodCache.put(c, meth);
            return meth;
        }
        catch (NoSuchMethodException e) {
            ConversionHelper.log.error((Object)"Finding method", (Throwable)e);
            throw new FlatwormConversionException("Couldn't Find Method");
        }
        catch (ClassNotFoundException e2) {
            ConversionHelper.log.error((Object)"Finding class", (Throwable)e2);
            throw new FlatwormConversionException("Couldn't Find Class");
        }
    }
    
    private Method getToStringConverterMethod(final String type) throws FlatwormConversionException {
        final Converter c = this.converters.get(type);
        if (this.converterToStringMethodCache.get(c) != null) {
            return this.converterToStringMethodCache.get(c);
        }
        try {
            final Class<?> cl = Class.forName(c.getConverterClass());
            final Class[] args = { Object.class, Map.class };
            final Method meth = cl.getMethod(c.getMethod(), (Class<?>[])args);
            this.converterToStringMethodCache.put(c, meth);
            return meth;
        }
        catch (NoSuchMethodException e) {
            ConversionHelper.log.error((Object)"Finding method", (Throwable)e);
            throw new FlatwormConversionException("Couldn't Find Method 'String " + c.getMethod() + "(Object, HashMap)'");
        }
        catch (ClassNotFoundException e2) {
            ConversionHelper.log.error((Object)"Finding class", (Throwable)e2);
            throw new FlatwormConversionException("Couldn't Find Class");
        }
    }
    
    private Object getConverterObject(final String type) throws FlatwormConversionException {
        try {
            final Converter c = this.converters.get(type);
            if (c == null) {
                throw new FlatwormConversionException("type '" + type + "' not registered");
            }
            if (this.converterObjectCache.get(c.getConverterClass()) != null) {
                return this.converterObjectCache.get(c.getConverterClass());
            }
            final Class<?> cl = Class.forName(c.getConverterClass());
            final Class[] args = new Class[0];
            final Object[] objArgs = new Object[0];
            final Object o = cl.getConstructor((Class<?>[])args).newInstance(objArgs);
            this.converterObjectCache.put(c.getConverterClass(), o);
            return o;
        }
        catch (NoSuchMethodException e) {
            ConversionHelper.log.error((Object)"Finding method", (Throwable)e);
            throw new FlatwormConversionException("Couldn't Find Method");
        }
        catch (IllegalAccessException e2) {
            ConversionHelper.log.error((Object)"No access to class", (Throwable)e2);
            throw new FlatwormConversionException("Couldn't access class");
        }
        catch (InvocationTargetException e3) {
            ConversionHelper.log.error((Object)"Invoking method", (Throwable)e3);
            throw new FlatwormConversionException("Couldn't invoke method");
        }
        catch (InstantiationException e4) {
            ConversionHelper.log.error((Object)"Instantiating", (Throwable)e4);
            throw new FlatwormConversionException("Couldn't instantiate converter");
        }
        catch (ClassNotFoundException e5) {
            ConversionHelper.log.error((Object)"Finding class", (Throwable)e5);
            throw new FlatwormConversionException("Couldn't Find Class");
        }
    }
    
    static {
        ConversionHelper.log = LogFactory.getLog((Class)ConversionHelper.class);
    }
}
