

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

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

class SegmentElement implements LineElement
{
    private int fieldIdentStart;
    private int fieldIdentLength;
    private List<String> fieldIdentMatchStrings;
    private int minCount;
    private int maxCount;
    private String name;
    private String beanRef;
    private String parentBeanRef;
    private String addMethod;
    private CardinalityMode cardinalityMode;
    private List<LineElement> elements;
    
    SegmentElement() {
        this.fieldIdentStart = 0;
        this.fieldIdentLength = 0;
        this.fieldIdentMatchStrings = new ArrayList<String>();
        this.elements = new ArrayList<LineElement>();
    }
    
    public int getFieldIdentStart() {
        return this.fieldIdentStart;
    }
    
    public void setFieldIdentStart(final int fieldIdentStart) {
        this.fieldIdentStart = fieldIdentStart;
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
    
    public boolean matchesId(final String id) {
        return this.fieldIdentMatchStrings.contains(id);
    }
    
    public char getIdentTypeFlag() {
        return 'F';
    }
    
    public int getMinCount() {
        return this.minCount;
    }
    
    public void setMinCount(final int minCount) {
        this.minCount = minCount;
    }
    
    public int getMaxCount() {
        return this.maxCount;
    }
    
    public void setMaxCount(final int maxCount) {
        this.maxCount = maxCount;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getBeanRef() {
        return this.beanRef;
    }
    
    public void setBeanRef(final String beanRef) {
        this.beanRef = beanRef;
    }
    
    public String getParentBeanRef() {
        return this.parentBeanRef;
    }
    
    public void setParentBeanRef(final String parentBeanRef) {
        this.parentBeanRef = parentBeanRef;
    }
    
    public String getAddMethod() {
        return this.addMethod;
    }
    
    public void setAddMethod(final String addMethod) {
        this.addMethod = addMethod;
    }
    
    public CardinalityMode getCardinalityMode() {
        return this.cardinalityMode;
    }
    
    public void setCardinalityMode(final CardinalityMode cardinalityMode) {
        this.cardinalityMode = cardinalityMode;
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
}
