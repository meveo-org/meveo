

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
import com.blackbear.flatworm.errors.FlatwormCreatorException;
import com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException;
import com.blackbear.flatworm.errors.FlatwormConversionException;
import com.blackbear.flatworm.errors.FlatwormInputLineLengthException;
import java.io.IOException;
import com.blackbear.flatworm.errors.FlatwormInvalidRecordException;
import java.io.BufferedReader;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;

public class FileFormat
{
    private static Log log;
    private Map<String, Record> records;
    private List<Record> recordOrder;
    private ConversionHelper convHelper;
    private String lastLine;
    private String encoding;
    
    public FileFormat() {
        this.convHelper = null;
        this.lastLine = "";
        this.records = new HashMap<String, Record>();
        this.recordOrder = new ArrayList<Record>();
        this.convHelper = new ConversionHelper();
    }
    
    public String getLastLine() {
        return this.lastLine;
    }
    
    public Map<String, Record> getRecords() {
        return Collections.unmodifiableMap((Map<? extends String, ? extends Record>)this.records);
    }
    
    public void setRecords(final Map<String, Record> records) {
        this.records.clear();
        this.records.putAll(records);
    }
    
    public void addRecord(final Record r) {
        this.records.put(r.getName(), r);
        this.recordOrder.add(r);
    }
    
    public Record getRecord(final String name) {
        return this.records.get(name);
    }
    
    private Record findMatchingRecord(final String firstLine) {
        for (int i = 0; i < this.recordOrder.size(); ++i) {
            final Record record = this.recordOrder.get(i);
            if (record.matchesLine(firstLine, this)) {
                return record;
            }
        }
        return null;
    }
    
    public void addConverter(final Converter converter) {
        this.convHelper.addConverter(converter);
    }
    
    public ConversionHelper getConvertionHelper() {
        return this.convHelper;
    }
    
    public MatchedRecord getNextRecord(final BufferedReader in) throws FlatwormInvalidRecordException, FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException, FlatwormCreatorException {
        try {
            final String firstLine = in.readLine();
            this.lastLine = firstLine;
            if (firstLine == null) {
                return null;
            }
            final Record rd = this.findMatchingRecord(firstLine);
            if (rd == null) {
                throw new FlatwormInvalidRecordException("Unmatched line in input file");
            }
            final Map<String, Object> beans = rd.parseRecord(firstLine, in, this.convHelper);
            return new MatchedRecord(rd.getName(), beans);
        }
        catch (IOException e) {
            return null;
        }
    }
    
    public String getEncoding() {
        return this.encoding;
    }
    
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }
    
    static {
        FileFormat.log = LogFactory.getLog((Class)FileFormat.class);
    }
}
