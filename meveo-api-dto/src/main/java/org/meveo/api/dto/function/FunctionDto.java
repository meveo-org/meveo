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

package org.meveo.api.dto.function;

import com.fasterxml.jackson.core.type.TypeReference;
import org.meveo.model.scripts.FunctionIO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FunctionDto implements Serializable {

    public static final TypeReference<List<FunctionDto>> DTO_LIST_TYPE_REF = new TypeReference<List<FunctionDto>>() {};

    private String code;

    private String testSuite;

    private List<FunctionIO> inputs = new ArrayList<>();

    private List<FunctionIO> outputs = new ArrayList<>();

    public List<FunctionIO> getInputs() {
        return inputs;
    }

    public void setInputs(List<FunctionIO> inputs) {
        this.inputs = inputs;
    }

    public List<FunctionIO> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<FunctionIO> outputs) {
        this.outputs = outputs;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(String testSuite) {
        this.testSuite = testSuite;
    }
}
