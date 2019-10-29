

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
import com.blackbear.flatworm.errors.FlatwormInvalidRecordException;
import com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException;
import com.blackbear.flatworm.errors.FlatwormInputLineLengthException;
import java.util.Iterator;
import java.io.IOException;
import com.blackbear.flatworm.errors.FlatwormConversionException;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;

class Record
{
    private static Log log;
    private String name;
    private int lengthIdentMin;
    private int lengthIdentMax;
    private int fieldIdentStart;
    private int fieldIdentLength;
    private List<String> fieldIdentMatchStrings;
    private char identTypeFlag;
    private RecordDefinition recordDefinition;
    
    public Record() {
        this.lengthIdentMin = 0;
        this.lengthIdentMax = 0;
        this.fieldIdentStart = 0;
        this.fieldIdentLength = 0;
        this.fieldIdentMatchStrings = new ArrayList<String>();
        this.identTypeFlag = '\0';
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public int getLengthIdentMin() {
        return this.lengthIdentMin;
    }
    
    public void setLengthIdentMin(final int lengthIdentMin) {
        this.lengthIdentMin = lengthIdentMin;
    }
    
    public int getLengthIdentMax() {
        return this.lengthIdentMax;
    }
    
    public void setLengthIdentMax(final int lengthIdentMax) {
        this.lengthIdentMax = lengthIdentMax;
    }
    
    public int getFieldIdentLength() {
        return this.fieldIdentLength;
    }
    
    public void setFieldIdentLength(final int fieldIdentLength) {
        this.fieldIdentLength = fieldIdentLength;
    }
    
    public List<String> getFieldIdentMatchStrings() {
        return this.fieldIdentMatchStrings;
    }
    
    public void setFieldIdentMatchStrings(final List<String> fieldIdentMatchStrings) {
        this.fieldIdentMatchStrings = fieldIdentMatchStrings;
    }
    
    public void addFieldIdentMatchString(final String s) {
        this.fieldIdentMatchStrings.add(s);
    }
    
    public char getIdentTypeFlag() {
        return this.identTypeFlag;
    }
    
    public void setIdentTypeFlag(final char identTypeFlag) {
        this.identTypeFlag = identTypeFlag;
    }
    
    public RecordDefinition getRecordDefinition() {
        return this.recordDefinition;
    }
    
    public void setRecordDefinition(final RecordDefinition recordDefinition) {
        this.recordDefinition = recordDefinition;
    }
    
    public int getFieldIdentStart() {
        return this.fieldIdentStart;
    }
    
    public void setFieldIdentStart(final int fieldIdentStart) {
        this.fieldIdentStart = fieldIdentStart;
    }
    
    public boolean matchesLine(final String line, final FileFormat ff) {
        switch (this.identTypeFlag) {
            case 'F': {
                if (line.length() < this.fieldIdentStart + this.fieldIdentLength) {
                    return false;
                }
                for (int i = 0; i < this.fieldIdentMatchStrings.size(); ++i) {
                    final String s = this.fieldIdentMatchStrings.get(i);
                    if (line.regionMatches(this.fieldIdentStart, s, 0, this.fieldIdentLength)) {
                        return true;
                    }
                }
                return false;
            }
            case 'L': {
                return line.length() >= this.lengthIdentMin && line.length() <= this.lengthIdentMax;
            }
            default: {
                return true;
            }
        }
    }
    
    @Override
    public String toString() {
        final StringBuffer b = new StringBuffer();
        b.append(super.toString() + "[");
        b.append("name = " + this.getName());
        if (this.getIdentTypeFlag() == 'L') {
            b.append(", identLength=(" + this.getLengthIdentMin() + "," + this.getLengthIdentMax() + ")");
        }
        if (this.getIdentTypeFlag() == 'F') {
            b.append(", identField=(" + this.getFieldIdentStart() + "," + this.getFieldIdentLength() + "," + this.getFieldIdentMatchStrings().toString() + ")");
        }
        if (this.getRecordDefinition() != null) {
            b.append(", recordDefinition = " + this.getRecordDefinition().toString());
        }
        b.append("]");
        return b.toString();
    }
    
    public Map<String, Object> parseRecord(final String firstLine, final BufferedReader in, final ConversionHelper convHelper) throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException, FlatwormInvalidRecordException, FlatwormCreatorException {
        final Map<String, Object> beans = new HashMap<String, Object>();
        try {
            final Map<String, Bean> beanHash = this.recordDefinition.getBeansUsed();
            for (final String beanName : beanHash.keySet()) {
                final Bean bean = beanHash.get(beanName);
                final Object beanObj = bean.getBeanObjectClass().newInstance();
                beans.put(beanName, beanObj);
            }
            final List<Line> lines = this.recordDefinition.getLines();
            String inputLine = firstLine;
            for (int i = 0; i < lines.size(); ++i) {
                final Line line = lines.get(i);
                line.parseInput(inputLine, beans, convHelper);
                if (i + 1 < lines.size()) {
                    inputLine = in.readLine();
                }
            }
        }
        catch (SecurityException e) {
            Record.log.error((Object)"Invoking method", (Throwable)e);
            throw new FlatwormConversionException("Couldn't invoke Method");
        }
        catch (IOException e2) {
            Record.log.error((Object)"Reading input", (Throwable)e2);
            throw new FlatwormConversionException("Couldn't read line");
        }
        catch (InstantiationException e3) {
            Record.log.error((Object)"Creating bean", (Throwable)e3);
            throw new FlatwormConversionException("Couldn't create bean");
        }
        catch (IllegalAccessException e4) {
            Record.log.error((Object)"No access to class", (Throwable)e4);
            throw new FlatwormConversionException("Couldn't access class");
        }
        return beans;
    }
    
    private String[] getFieldNames() {
        final List<String> names = new ArrayList<String>();
        final List<Line> lines = this.recordDefinition.getLines();
        for (int i = 0; i < lines.size(); ++i) {
            final Line l = lines.get(i);
            final List<LineElement> el = l.getElements();
            for (int j = 0; j < el.size(); ++j) {
                final LineElement re = el.get(j);
                names.add(re.getBeanRef());
            }
        }
        final String[] propertyNames = new String[names.size()];
        for (int k = 0; k < names.size(); ++k) {
            propertyNames[k] = names.get(k);
        }
        return propertyNames;
    }
    
    static {
        Record.log = LogFactory.getLog((Class)Record.class);
    }
}
