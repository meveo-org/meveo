/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

package org.meveo.jmeter.threadgroup.model;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.ThreadGroup;

import java.util.concurrent.TimeUnit;

@GUIMenuSortOrder(Integer.MIN_VALUE)
public class MeveoThreadGroup extends ThreadGroup {

    private static final String FUNCTION_CODE = "functionCode";
    private static final String PERIODICITY = "periodicity";
    private static final String TIME_UNIT = "timeUnit";

    public String getFunctionCode() {
        return getPropertyAsString(FUNCTION_CODE);
    }

    public void setFunctionCode(String functionCode) {
        setProperty(new StringProperty(FUNCTION_CODE, functionCode));
    }

    public Integer getPeriodicity() {
        return getPropertyAsInt(PERIODICITY);
    }

    public void setPeriodicity(Integer periodicity) {
        setProperty(new IntegerProperty(PERIODICITY, periodicity));
    }

    public TimeUnit getTimeUnit() {
        return TimeUnit.valueOf(getPropertyAsString(TIME_UNIT));
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        setProperty(new StringProperty(TIME_UNIT, timeUnit.toString()));
    }

}
