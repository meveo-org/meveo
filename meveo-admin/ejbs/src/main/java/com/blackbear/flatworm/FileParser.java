

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
import java.lang.reflect.Method;
import com.blackbear.flatworm.errors.FlatwormConversionException;
import com.blackbear.flatworm.errors.FlatwormInputLineLengthException;
import com.blackbear.flatworm.errors.FlatwormInvalidRecordException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException;
import com.blackbear.flatworm.errors.FlatwormConfigurationValueException;
import com.blackbear.flatworm.errors.FlatwormParserException;
import java.util.HashMap;
import java.io.BufferedReader;
import java.util.Map;
import org.apache.commons.logging.Log;

public class FileParser
{
    private static Log log;
    private static Class[] METHODSIG;
    private static Class[] EXCEPTIONSIG;
    private static String EXCEPTIONS;
    private Map<String, Callback> callbacks;
    private Map<String, RecordCallback> recordCallbacks;
    private ExceptionCallback exceptionCallback;
    private String file;
    protected FileFormat ff;
    protected BufferedReader bufIn;
    
    public FileParser(final String config, final String file) throws FlatwormParserException {
        this.callbacks = new HashMap<String, Callback>();
        this.recordCallbacks = new HashMap<String, RecordCallback>();
        this.bufIn = null;
        this.file = file;
        try {
            final ConfigurationReader parser = new ConfigurationReader();
            this.ff = parser.loadConfigurationFile(config);
        }
        catch (FlatwormConfigurationValueException ex) {
            throw new FlatwormParserException(ex.getMessage());
        }
        catch (FlatwormUnsetFieldValueException ex2) {
            throw new FlatwormParserException(ex2.getMessage());
        }
    }
    
    public void addRecordCallback(final String recordName, final RecordCallback callback) {
        this.recordCallbacks.put(recordName, callback);
    }
    
    public void setExceptionCallback(final ExceptionCallback callback) {
        this.exceptionCallback = callback;
    }
    
    public void open() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        final InputStream in = new FileInputStream(this.file);
        final String encoding = this.ff.getEncoding();
        this.bufIn = new BufferedReader(new InputStreamReader(in, encoding));
    }
    
    public void close() throws IOException {
        if (this.bufIn != null) {
            this.bufIn.close();
        }
    }
    
    public void read() {
        MatchedRecord results = null;
        boolean exception = false;
        do {
            exception = true;
            try {
                results = this.ff.getNextRecord(this.bufIn);
                exception = false;
            }
            catch (FlatwormInvalidRecordException ex) {
                this.doExceptionCallback(ex, "FlatwormInvalidRecordException", this.ff.getLastLine());
            }
            catch (FlatwormInputLineLengthException ex2) {
                FileParser.log.warn((Object)"Exception", (Throwable)ex2);
                this.doExceptionCallback(ex2, "FlatwormInputLineLengthException", this.ff.getLastLine());
            }
            catch (FlatwormUnsetFieldValueException ex3) {
                this.doExceptionCallback(ex3, "FlatwormUnsetFieldValueException", this.ff.getLastLine());
            }
            catch (FlatwormConversionException ex4) {
                this.doExceptionCallback(ex4, "FlatwormConversionException", this.ff.getLastLine());
            }
            catch (Exception ex5) {
                this.doExceptionCallback(ex5, ex5.getMessage(), this.ff.getLastLine());
            }
            if (null != results) {
                final String recordName = results.getRecordName();
                this.doCallback(recordName, results);
            }
        } while (null != results || exception);
    }
    
    private void doCallback(final Callback callback, final Object arg1, final Object arg2) {
        try {
            final Method method = callback.getMethod();
            Object[] args = null;
            if (null == arg2) {
                args = new Object[] { arg1 };
            }
            else {
                args = new Object[] { arg1, arg2 };
            }
            method.invoke(callback.getInstance(), args);
        }
        catch (Exception ex) {
            final String details = callback.getInstance().getClass().getName() + "." + callback.getMethod().getName();
            FileParser.log.error((Object)("Bad handler method call: " + details + " - " + ex));
        }
    }
    
    private void doCallback(final String recordType, final MatchedRecord record) {
        final Callback oldType = this.callbacks.get(recordType);
        if (oldType != null) {
            this.doCallback(oldType, record, null);
        }
        else {
            final RecordCallback callback = this.recordCallbacks.get(recordType);
            if (callback != null) {
                callback.processRecord(record);
            }
        }
    }
    
    private void doExceptionCallback(final Exception ex, final String message, final String lastLine) {
        final Callback oldType = this.callbacks.get(FileParser.EXCEPTIONS);
        if (oldType != null) {
            this.doCallback(oldType, message, lastLine);
        }
        else {
            if (this.exceptionCallback == null) {
                throw new RuntimeException("No callback specified for Exceptions. Exception occurred: " + ex);
            }
            this.exceptionCallback.processException(ex, lastLine);
        }
    }
    
    static {
        FileParser.log = LogFactory.getLog((Class)FileParser.class);
        FileParser.METHODSIG = new Class[] { MatchedRecord.class };
        FileParser.EXCEPTIONSIG = new Class[] { String.class, String.class };
        FileParser.EXCEPTIONS = "exception";
    }
}
