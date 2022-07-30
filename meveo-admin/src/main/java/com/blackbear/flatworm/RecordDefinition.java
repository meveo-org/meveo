

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RecordDefinition
{
    private Map<String, Bean> beansUsed;
    private List<Line> lines;
    
    public RecordDefinition() {
        this.beansUsed = new HashMap<String, Bean>();
        this.lines = new ArrayList<Line>();
    }
    
    public Map<String, Bean> getBeansUsed() {
        return this.beansUsed;
    }
    
    public void setBeansUsed(final Map<String, Bean> beansUsed) {
        this.beansUsed = beansUsed;
    }
    
    public void addBeanUsed(final Bean bean) {
        this.beansUsed.put(bean.getBeanName(), bean);
    }
    
    public List<Line> getLines() {
        return this.lines;
    }
    
    public void setLines(final List<Line> lines) {
        this.lines = lines;
    }
    
    public void addLine(final Line line) {
        this.lines.add(line);
    }
    
    @Override
    public String toString() {
        final StringBuffer b = new StringBuffer();
        b.append(super.toString() + "[");
        b.append("beans = " + this.beansUsed);
        b.append(",lines=" + this.lines);
        b.append("]");
        return b.toString();
    }
}
