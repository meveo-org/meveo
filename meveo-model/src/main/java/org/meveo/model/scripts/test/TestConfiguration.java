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

package org.meveo.model.scripts.test;

import java.util.List;
import java.util.Map;

public class TestConfiguration {

    private Map<String, Object> testInputs;
    private List<ExpectedOutput> expectedOutputs;

    public Map<String, Object> getTestInputs() {
        return testInputs;
    }

    public void setTestInputs(Map<String, Object> testInputs) {
        this.testInputs = testInputs;
    }

    public List<ExpectedOutput> getExpectedOutputs() {
        return expectedOutputs;
    }

    public void setExpectedOutputs(List<ExpectedOutput> expectedOutputs) {
        this.expectedOutputs = expectedOutputs;
    }
}
