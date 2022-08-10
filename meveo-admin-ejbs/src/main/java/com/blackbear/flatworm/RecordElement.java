

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

import com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException;
import java.util.HashMap;
import java.util.Map;

class RecordElement implements LineElement
{
    private Integer fieldEnd;
    private Integer fieldStart;
    private Integer fieldLength;
    private Integer spacerLength;
    private char fieldType;
    private String beanRef;
    private String type;
    private Map<String, ConversionOption> conversionOptions;
    
    public RecordElement() {
        this.fieldEnd = null;
        this.fieldStart = null;
        this.fieldLength = null;
        this.spacerLength = null;
        this.fieldType = '\0';
        this.beanRef = null;
        this.type = null;
        this.conversionOptions = new HashMap<String, ConversionOption>();
    }
    
    public boolean isFieldStartSet() {
        return this.fieldStart != null;
    }
    
    public boolean isFieldEndSet() {
        return this.fieldEnd != null;
    }
    
    public boolean isFieldLengthSet() {
        return this.fieldLength != null;
    }
    
    public int getFieldStart() throws FlatwormUnsetFieldValueException {
        if (this.fieldStart == null) {
            throw new FlatwormUnsetFieldValueException("fieldStart is unset");
        }
        return this.fieldStart;
    }
    
    public void setFieldStart(final int fieldStart) {
        this.fieldStart = new Integer(fieldStart);
    }
    
    public int getFieldEnd() throws FlatwormUnsetFieldValueException {
        if (this.fieldEnd == null) {
            throw new FlatwormUnsetFieldValueException("fieldEnd is unset");
        }
        return this.fieldEnd;
    }
    
    public void setFieldEnd(final int fieldEnd) {
        this.fieldEnd = new Integer(fieldEnd);
    }
    
    public int getFieldLength() throws FlatwormUnsetFieldValueException {
        if (this.fieldLength != null) {
            return this.fieldLength;
        }
        if (!this.isFieldStartSet() || !this.isFieldEndSet()) {
            throw new FlatwormUnsetFieldValueException("length is unset");
        }
        return this.fieldEnd - this.fieldStart;
    }
    
    public void setFieldLength(final int fieldLength) {
        this.fieldLength = new Integer(fieldLength);
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    public Map<String, ConversionOption> getConversionOptions() {
        return this.conversionOptions;
    }
    
    public void setConversionOptions(final Map<String, ConversionOption> conversionOptions) {
        this.conversionOptions = conversionOptions;
    }
    
    public void addConversionOption(final String name, final ConversionOption option) {
        this.conversionOptions.put(name, option);
    }
    
    public String getBeanRef() {
        return this.beanRef;
    }
    
    public void setBeanRef(final String beanRef) {
        this.beanRef = beanRef;
    }
}
