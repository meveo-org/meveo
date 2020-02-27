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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.meveo.jmeter.function.FunctionManager;
import org.meveo.model.typereferences.GenericTypeReferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeveoSampler extends AbstractSampler {

	private static final Logger LOG = LoggerFactory.getLogger(MeveoSampler.class);

	private static final long serialVersionUID = -2922361002888059421L;

	public static final String ARGUMENTS = "arguments";
	public static final String CODE = "code";

	public void setFunction(String code) {
		setProperty(new StringProperty(CODE, code));
	}

	public void setArguments(Arguments args) {
		setProperty(new TestElementProperty(ARGUMENTS, args));
	}

	public Arguments getArguments() {
		final JMeterProperty property = getProperty(ARGUMENTS);
		if (property instanceof NullProperty) {
			return null;
		}
		return (Arguments) property.getObjectValue();
	}

	public String getFunction() {
		return getPropertyAsString(CODE);
	}

	@Override
	public SampleResult sample(Entry entry) {

		final HTTPSampleResult sampleResult = new HTTPSampleResult();
		sampleResult.sampleStart();

		Map<String, Object> argsMap = new HashMap<>();
		getArguments().getArguments().forEach(e -> {
			Object argVal = argsMap.get(e.getName());
			Argument arg = (Argument) e.getObjectValue();
			if (argVal == null) {
				argsMap.put(e.getName(), arg.getValue());

			} else if (argVal instanceof String) {
				// The value is already set - we have a multi-valued arg
				List<String> argValAsList = new ArrayList<>();
				argValAsList.add(arg.getValue());
				argValAsList.add((String) argVal);

				argsMap.put(e.getName(), argValAsList);
			} else if (argVal instanceof List) {
				// Arg is multi-valued
				((List<String>) argVal).add(arg.getValue());
			}
		});

		LOG.info("Start test of function {}", getFunction());

		try (CloseableHttpClient client = FunctionManager.createAcceptSelfSignedCertificateClient()) {
			String serialiazedArgs = FunctionManager.OBJECT_MAPPER.writeValueAsString(argsMap);
			String testUrl = String.format(FunctionManager.getHostUri() + FunctionManager.UPLOAD_URL, getFunction());
			HttpPost post = new HttpPost(testUrl);

			FunctionManager.setBearer(post);
			FunctionManager.setContentType(post, "application/json");
			
			sampleResult.setQueryString(serialiazedArgs);
			post.setEntity(new StringEntity(serialiazedArgs));

			final HttpUriRequest request = post;
			try (CloseableHttpResponse response = client.execute(request)) {

				LOG.info("Test done");
				sampleResult.setResponseCode(String.valueOf(response.getStatusLine().getStatusCode()));
				sampleResult.setSampleLabel(getName());
				sampleResult.setDataType(SampleResult.TEXT);

				HttpEntity responseEntity = response.getEntity();


				if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 400) {
					final Map<String, Object> results = FunctionManager.OBJECT_MAPPER.readValue(responseEntity.getContent(), GenericTypeReferences.MAP_STRING_OBJECT);
					final String serializedResults = new ObjectMapper().writeValueAsString(results);
					sampleResult.setSuccessful(true);
					sampleResult.setResponseData(serializedResults, "UTF-8");

				} else {
					sampleResult.setSuccessful(false);
					if (responseEntity == null) {
						sampleResult.setResponseData("Unsuccessful request", "UTF-8");
					} else {
						String responseAsString = EntityUtils.toString(responseEntity);
						EntityUtils.consume(responseEntity);
						sampleResult.setResponseData(responseAsString, "UTF-8");
					}
				}
			}
			
		} catch (Exception e) {
			sampleResult.setSuccessful(false);
			sampleResult.setResponseData(e.getMessage(), "UTF-8");

		} finally {
			sampleResult.sampleEnd();
		}

		return sampleResult;
	}

}
