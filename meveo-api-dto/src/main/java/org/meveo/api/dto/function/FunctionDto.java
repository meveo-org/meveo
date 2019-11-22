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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.FunctionIO;

import com.fasterxml.jackson.core.type.TypeReference;
import org.meveo.model.scripts.Sample;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.5.0
 */
public class FunctionDto extends BusinessEntityDto implements Serializable {

	private static final long serialVersionUID = -5247505855284959649L;

	public static final TypeReference<List<FunctionDto>> DTO_LIST_TYPE_REF = new TypeReference<List<FunctionDto>>() {
	};

	private String testSuite;

	private List<FunctionIO> inputs = new ArrayList<>();
	private List<FunctionIO> outputs = new ArrayList<>();
	private List<Sample> samples;
	
	private Boolean generateOutputs = false;

	public FunctionDto() {

	}

	public FunctionDto(Function e) {

		super(e);
		this.description = e.getDescription();
		this.generateOutputs = e.getGenerateOutputs();
	}

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

	public String getTestSuite() {
		return testSuite;
	}

	public void setTestSuite(String testSuite) {
		this.testSuite = testSuite;
	}

	public List<Sample> getSamples() {
		return samples;
	}

	public void setSamples(List<Sample> samples) {
		this.samples = samples;
	}

	public Boolean getGenerateOutputs() {
		return generateOutputs;
	}

	public void setGenerateOutputs(Boolean generateOutputs) {
		this.generateOutputs = generateOutputs;
	}
}
