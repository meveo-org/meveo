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

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.ThreadGroup;

@GUIMenuSortOrder(Integer.MIN_VALUE)
public class MeveoThreadGroup extends ThreadGroup {

    private static final String FUNCTION_CODE = "functionCode";
    private static final String TIMER_CODE = "periodicity";

    public MeveoThreadGroup(){
        setNumThreads(1);
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        setSamplerController(loopController);
    }

    public String getFunctionCode() {
        return getPropertyAsString(FUNCTION_CODE);
    }

    public void setFunctionCode(String functionCode) {
        setProperty(new StringProperty(FUNCTION_CODE, functionCode));
    }

    public String getPeriodicity() {
        return getPropertyAsString(TIMER_CODE);
    }

    public void setPeriodicity(String periodicity) {
        setProperty(new StringProperty(TIMER_CODE, periodicity));
    }

}
