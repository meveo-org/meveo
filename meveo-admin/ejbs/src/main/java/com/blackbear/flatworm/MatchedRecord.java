

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

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

public class MatchedRecord
{
    private Map<String, Object> beans;
    private String recordName;
    
    public MatchedRecord(final String name, final Map<String, Object> beans) {
        this.beans = new HashMap<String, Object>();
        this.recordName = name;
        this.beans.putAll(beans);
    }
    
    public String getRecordName() {
        return this.recordName;
    }
    
    public Object getBean(final String beanName) {
        return this.beans.get(beanName);
    }
    
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("[");
        sb.append("MatchedRecord: recordName = ");
        sb.append(this.recordName);
        sb.append(", beans = {");
        final Iterator<String> itBeans = this.beans.keySet().iterator();
        while (itBeans.hasNext()) {
            final Object key = itBeans.next();
            final Object val = this.beans.get(key);
            sb.append(key);
            sb.append("=");
            sb.append(val.toString());
            if (itBeans.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("}]");
        return sb.toString();
    }
}
