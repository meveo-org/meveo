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
package org.meveo.service.technicalservice.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;

/**
 * Template for building an Endpoint interface in JS code.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.9.0
 */
public class EndpointJSInterface {

	private String endpointCode;
	private String requestSchema;
	private String responseSchema;
	private String endpointDescription;
	private String template;
	private boolean isCet;
	private EndpointHttpMethod httpMethod;
	private String apiUrl;

	public String build() {
		StringBuilder sb = new StringBuilder(template);

		Map<String, String> valuesMap = new HashMap<>();
		valuesMap.put("ENDPOINT_CODE", endpointCode);
		valuesMap.put("ENDPOINT_DESCRIPTION", endpointDescription);
		valuesMap.put("REQUEST_SCHEMA", requestSchema);
		valuesMap.put("RESPONSE_SCHEMA", responseSchema);
		if (!StringUtils.isBlank(apiUrl)) {
			valuesMap.put("API_BASE_URL", apiUrl);
		}

		StrSubstitutor sub = new StrSubstitutor(valuesMap);
		sub.setVariablePrefix("#{");

		return sub.replace(sb.toString());
	}

	public String getRequestSchema() {
		return requestSchema;
	}

	public void setRequestSchema(String requestSchema) {
		this.requestSchema = requestSchema;
	}

	public String getResponseSchema() {
		return responseSchema;
	}

	public void setResponseSchema(String responseSchema) {
		this.responseSchema = responseSchema;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getEndpointCode() {
		return endpointCode;
	}

	public void setEndpointCode(String endpointCode) {
		this.endpointCode = endpointCode;
	}

	public String getEndpointDescription() {
		return endpointDescription;
	}

	public void setEndpointDescription(String endpointDescription) {
		this.endpointDescription = endpointDescription;
	}

	public boolean isCet() {
		return isCet;
	}

	public void setCet(boolean isCet) {
		this.isCet = isCet;
	}

	public EndpointHttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(EndpointHttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
}
