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

package org.meveo.jmeter.sampler.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.meveo.jmeter.function.FunctionManager;
import org.meveo.model.scripts.Function;

import java.awt.*;
import java.util.Map;

public class MeveoSampler extends AbstractSampler {

    public static final String ARGUMENTS = "arguments";
    public static final String CODE = "code";

    public void setFunction(String code){
        setProperty(new StringProperty(CODE, code));
    }
    public void setArguments(Arguments args) {
        setProperty(new TestElementProperty(ARGUMENTS, args));
    }

    public Arguments getArguments() {
        final JMeterProperty property = getProperty(ARGUMENTS);
        if(property instanceof NullProperty){
            return null;
        }
        return (Arguments) property.getObjectValue();
    }

    public String getFunction(){
        return getPropertyAsString(CODE);
    }

    @Override
    public SampleResult sample(Entry entry) {

        final SampleResult sampleResult = new SampleResult();

        sampleResult.sampleStart();

        final Map<String, Object> results = FunctionManager.test(getFunction(), getArguments());

        try {
            final String serializedResults = new ObjectMapper().writeValueAsString(results);
            sampleResult.setSuccessful(true);
            sampleResult.setDataType(SampleResult.TEXT);
            sampleResult.setResponseData(serializedResults, "UTF-8");
            sampleResult.setSampleLabel(getName());
            sampleResult.sampleEnd();
        } catch (JsonProcessingException e) {
            sampleResult.sampleEnd();
            sampleResult.setSuccessful(false);
            throw new RuntimeException(e);
        }

        return sampleResult;
    }

}
