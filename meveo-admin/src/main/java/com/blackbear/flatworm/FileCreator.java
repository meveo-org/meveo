

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
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException;
import com.blackbear.flatworm.errors.FlatwormConfigurationValueException;
import java.io.InputStream;
import com.blackbear.flatworm.errors.FlatwormCreatorException;
import java.util.HashMap;
import java.io.OutputStream;
import java.util.Map;
import java.io.BufferedWriter;
import org.apache.commons.logging.Log;

public class FileCreator
{
    private static Log log;
    private String file;
    private FileFormat ff;
    private BufferedWriter bufOut;
    private Map<String, Object> beans;
    private String recordSeperator;
    private OutputStream outputStream;
    
    public FileCreator(final String config, final String file) throws FlatwormCreatorException {
        this.beans = new HashMap<String, Object>();
        this.recordSeperator = null;
        this.file = file;
        this.outputStream = null;
        this.loadConfigurationFile(config);
    }
    
    public FileCreator(final String config, final OutputStream stream) throws FlatwormCreatorException {
        this.beans = new HashMap<String, Object>();
        this.recordSeperator = null;
        this.file = null;
        this.outputStream = stream;
        this.loadConfigurationFile(config);
    }
    
    public FileCreator(final InputStream config, final String file) throws FlatwormCreatorException {
        this.beans = new HashMap<String, Object>();
        this.recordSeperator = null;
        this.file = file;
        this.outputStream = null;
        this.loadConfigurationFile(config);
    }
    
    public FileCreator(final InputStream config, final OutputStream stream) throws FlatwormCreatorException {
        this.beans = new HashMap<String, Object>();
        this.recordSeperator = null;
        this.file = null;
        this.outputStream = stream;
        this.loadConfigurationFile(config);
    }
    
    private void loadConfigurationFile(final InputStream configStream) throws FlatwormCreatorException {
        final ConfigurationReader parser = new ConfigurationReader();
        try {
            this.ff = parser.loadConfigurationFile(configStream);
        }
        catch (FlatwormConfigurationValueException ex) {
            throw new FlatwormCreatorException(ex.getMessage());
        }
        catch (FlatwormUnsetFieldValueException ex2) {
            throw new FlatwormCreatorException(ex2.getMessage());
        }
    }
    
    private void loadConfigurationFile(final String config) throws FlatwormCreatorException {
        try {
            final ConfigurationReader parser = new ConfigurationReader();
            final InputStream configStream = this.getClass().getClassLoader().getResourceAsStream(config);
            if (configStream != null) {
                this.ff = parser.loadConfigurationFile(configStream);
            }
            else {
                this.ff = parser.loadConfigurationFile(config);
            }
        }
        catch (FlatwormConfigurationValueException ex) {
            throw new FlatwormCreatorException(ex.getMessage());
        }
        catch (FlatwormUnsetFieldValueException ex2) {
            throw new FlatwormCreatorException(ex2.getMessage());
        }
    }
    
    public void open() throws FlatwormCreatorException, UnsupportedEncodingException {
        try {
            if (this.file != null) {
                this.outputStream = new FileOutputStream(this.file);
            }
            this.bufOut = new BufferedWriter(new OutputStreamWriter(this.outputStream, this.ff.getEncoding()));
        }
        catch (FileNotFoundException ex) {
            throw new FlatwormCreatorException(ex.getMessage());
        }
    }
    
    public void setBean(final String name, final Object bean) {
        this.beans.put(name, bean);
    }
    
    public void setRecordSeperator(final String recordSeperator) {
        this.recordSeperator = recordSeperator;
    }
    
    public void close() throws IOException {
        this.bufOut.close();
    }
    
    public void write(final String recordName) throws IOException, FlatwormCreatorException {
        final Record record = this.ff.getRecord(recordName);
        final RecordDefinition recDef = record.getRecordDefinition();
        final List<Line> lines = recDef.getLines();
        boolean first = true;
        for (final Line line : lines) {
            String delimit = line.getDelimeter();
            if (null == delimit) {
                delimit = "";
            }
            final List<String> recIdents = record.getFieldIdentMatchStrings();
            if (first) {
                for (final String id : recIdents) {
                    this.bufOut.write(id + delimit);
                }
            }
            final List<LineElement> recElements = line.getElements();
            final Iterator<LineElement> itRecElements = recElements.iterator();
            while (itRecElements.hasNext()) {
                final LineElement lineElement = itRecElements.next();
                if (lineElement instanceof RecordElement) {
                    final RecordElement recElement = (RecordElement)lineElement;
                    final Map<String, ConversionOption> convOptions = recElement.getConversionOptions();
                    int length = 0;
                    String beanRef = "";
                    String type = "";
                    try {
                        beanRef = recElement.getBeanRef();
                        type = recElement.getType();
                        length = recElement.getFieldLength();
                    }
                    catch (FlatwormUnsetFieldValueException ex) {
                        throw new FlatwormCreatorException("Could not deduce field length (please provide more data in your xml file for : " + beanRef + " " + ex.getMessage());
                    }
                    String val = "";
                    final ConversionHelper convHelper = this.ff.getConvertionHelper();
                    try {
                        if (beanRef != null) {
                            Object bean = null;
                            String property = "";
                            try {
                                final int posOfFirstDot = beanRef.indexOf(46);
                                bean = this.beans.get(beanRef.substring(0, posOfFirstDot));
                                property = beanRef.substring(posOfFirstDot + 1);
                            }
                            catch (ArrayIndexOutOfBoundsException ex3) {
                                throw new FlatwormCreatorException("Had trouble parsing : " + beanRef + " Its format should be <bean_name>.<property_name>");
                            }
                            final Object value = PropertyUtils.getProperty(bean, property);
                            val = convHelper.convert(type, value, convOptions, beanRef);
                            PropertyUtils.setProperty(bean, property, value);
                        }
                        if (val == null) {
                            val = "";
                        }
                        val = convHelper.transformString(val, recElement.getConversionOptions(), recElement.getFieldLength());
                        if (itRecElements.hasNext()) {
                            this.bufOut.write(val + delimit);
                        }
                        else {
                            this.bufOut.write(val);
                        }
                    }
                    catch (Exception ex2) {
                        throw new FlatwormCreatorException("Exception getting/converting bean property : " + beanRef + " : " + ex2.getMessage());
                    }
                }
            }
            if (null != this.recordSeperator) {
                this.bufOut.write(this.recordSeperator);
            }
            first = false;
        }
    }
    
    static {
        FileCreator.log = LogFactory.getLog((Class)FileCreator.class);
    }
}
