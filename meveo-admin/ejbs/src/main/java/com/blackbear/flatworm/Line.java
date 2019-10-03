

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
import com.blackbear.flatworm.errors.FlatwormConversionException;
import com.blackbear.flatworm.errors.FlatwormInputLineLengthException;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import org.apache.commons.logging.Log;

class Line
{
    private static Log log;
    private List<LineElement> elements;
    private String delimit;
    private char chrQuote;
    private ConversionHelper convHelper;
    private Map<String, Object> beans;
    private BeanMappingStrategy mappingStrategy;
    private String[] delimitedFields;
    private int currentField;
    
    public Line() {
        this.elements = new ArrayList<LineElement>();
        this.delimit = null;
        this.chrQuote = '\0';
        this.mappingStrategy = new PropertyUtilsMappingStrategy();
        this.currentField = 0;
    }
    
    public void setQuoteChar(final String quote) {
        this.chrQuote = quote.charAt(0);
    }
    
    public boolean isDelimeted() {
        return null != this.delimit;
    }
    
    public void setDelimeter(final String delimit) {
        this.delimit = delimit;
    }
    
    public String getDelimeter() {
        return this.delimit;
    }
    
    public List<LineElement> getElements() {
        return Collections.unmodifiableList((List<? extends LineElement>)this.elements);
    }
    
    public void setElements(final List<LineElement> recordElements) {
        this.elements.clear();
        this.elements.addAll(recordElements);
    }
    
    public void addElement(final LineElement re) {
        this.elements.add(re);
    }
    
    @Override
    public String toString() {
        final StringBuffer b = new StringBuffer();
        b.append(super.toString() + "[");
        b.append("elements = " + this.elements);
        b.append("]");
        return b.toString();
    }
    
    public void parseInput(final String inputLine, final Map<String, Object> beans, final ConversionHelper convHelper) throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException, FlatwormInvalidRecordException, FlatwormCreatorException {
        this.convHelper = convHelper;
        this.beans = beans;
        if (this.isDelimeted() && inputLine != null && !inputLine.isEmpty()) {
            this.parseInputDelimited(inputLine);
            return;
        }
        int charPos = 0;
        for (int i = 0; i < this.elements.size(); ++i) {
            final LineElement le = this.elements.get(i);
            if (le instanceof RecordElement) {
                final RecordElement re = (RecordElement)le;
                int start = charPos;
                int end = charPos;
                if (re.isFieldStartSet()) {
                    start = re.getFieldStart();
                }
                if (re.isFieldEndSet()) {
                    end = (charPos = re.getFieldEnd());
                }
                if (re.isFieldLengthSet()) {
                    end = (charPos = start + re.getFieldLength());
                }
                if (end > inputLine.length()) {
                    throw new FlatwormInputLineLengthException("Looking for field " + re.getBeanRef() + " at pos " + start + ", end " + end + ", input length = " + inputLine.length());
                }
                final String beanRef = re.getBeanRef();
                if (beanRef != null) {
                    final String fieldChars = inputLine.substring(start, end);
                    this.mapField(fieldChars, re);
                }
            }
            else if (le instanceof SegmentElement) {
                final SegmentElement se = (SegmentElement)le;
            }
        }
    }
    
    private void mapField(final String fieldChars, final RecordElement re) throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException {
        final Object value = this.convHelper.convert(re.getType(), fieldChars, re.getConversionOptions(), re.getBeanRef());
        final String beanRef = re.getBeanRef();
        final int posOfFirstDot = beanRef.indexOf(46);
        final String beanName = beanRef.substring(0, posOfFirstDot);
        final String property = beanRef.substring(posOfFirstDot + 1);
        final Object bean = this.beans.get(beanName);
        this.mappingStrategy.mapBean(bean, beanName, property, value, re.getConversionOptions());
    }
    
    private void parseInputDelimited(final String inputLine) throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException, FlatwormInvalidRecordException, FlatwormCreatorException {
        char split = this.delimit.charAt(0);
        if (this.delimit.length() == 2 && this.delimit.charAt(0) == '\\') {
            final char specialChar = this.delimit.charAt(1);
            switch (specialChar) {
                case 't': {
                    split = '\t';
                    break;
                }
                case 'n': {
                    split = '\n';
                    break;
                }
                case 'r': {
                    split = '\r';
                    break;
                }
                case 'f': {
                    split = '\f';
                    break;
                }
                case '\\': {
                    split = '\\';
                    break;
                }
            }
        }
        this.delimitedFields = Util.split(inputLine, split, this.chrQuote);
        this.currentField = 0;
        this.doParseDelimitedInput(this.elements);
    }
    
    private void doParseDelimitedInput(final List<LineElement> elements) throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException, FlatwormCreatorException, FlatwormInvalidRecordException {
        for (int i = 0; i < elements.size(); ++i) {
            final LineElement le = elements.get(i);
            if (le instanceof RecordElement) {
                try {
                    this.parseDelimitedRecordElement((RecordElement)le, this.delimitedFields[this.currentField]);
                    ++this.currentField;
                }
                catch (ArrayIndexOutOfBoundsException ex) {
                    Line.log.warn((Object)("Ran out of data on field " + i));
                }
            }
            else if (le instanceof SegmentElement) {
                this.parseDelimitedSegmentElement((SegmentElement)le);
            }
        }
    }
    
    private void parseDelimitedRecordElement(final RecordElement re, final String fieldStr) throws FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException {
        final String beanRef = re.getBeanRef();
        if (beanRef != null) {
            this.mapField(fieldStr, re);
        }
    }
    
    private void parseDelimitedSegmentElement(final SegmentElement segment) throws FlatwormCreatorException, FlatwormInputLineLengthException, FlatwormConversionException, FlatwormUnsetFieldValueException, FlatwormInvalidRecordException {
        int minCount = segment.getMinCount();
        int maxCount = segment.getMaxCount();
        if (maxCount <= 0) {
            maxCount = Integer.MAX_VALUE;
        }
        if (minCount < 0) {
            minCount = 0;
        }
        final String beanRef = segment.getBeanRef();
        if (!segment.matchesId(this.delimitedFields[this.currentField]) && minCount > 0) {
            Line.log.error((Object)("Segment " + segment.getName() + " with minimun required count of " + minCount + " missing."));
        }
        int cardinality = 0;
        try {
            while (this.currentField < this.delimitedFields.length && segment.matchesId(this.delimitedFields[this.currentField])) {
                if (beanRef != null) {
                    ++cardinality;
                    final String parentRef = segment.getParentBeanRef();
                    final String addMethod = segment.getAddMethod();
                    if (parentRef != null && addMethod != null) {
                        final Object instance = ParseUtils.newBeanInstance(this.beans.get(beanRef));
                        this.beans.put(beanRef, instance);
                        if (cardinality > maxCount) {
                            if (segment.getCardinalityMode() == CardinalityMode.STRICT) {
                                throw new FlatwormInvalidRecordException("Cardinality exceeded with mode set to STRICT");
                            }
                            if (segment.getCardinalityMode() != CardinalityMode.RESTRICTED) {
                                ParseUtils.invokeAddMethod(this.beans.get(parentRef), addMethod, instance);
                            }
                        }
                        else {
                            ParseUtils.invokeAddMethod(this.beans.get(parentRef), addMethod, instance);
                        }
                    }
                    this.doParseDelimitedInput(segment.getElements());
                }
            }
        }
        finally {
            if (cardinality > maxCount) {
                Line.log.error((Object)("Segment '" + segment.getName() + "' with maximum of " + maxCount + " encountered actual count of " + cardinality));
            }
        }
    }
    
    static {
        Line.log = LogFactory.getLog((Class)Line.class);
    }
}
