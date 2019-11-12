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

import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;

import java.util.StringJoiner;

public class ESGenerator {

    public static String generate(Endpoint endpoint) {
        StringBuilder buffer = new StringBuilder("async function ")
                .append(endpoint.getCode())
                .append("(parameters)");

        // Path parameters
        StringJoiner pathParamJoiner = new StringJoiner("/", "/", "`, baseUrl);");
        endpoint.getPathParameters().forEach(endpointPathParameter -> pathParamJoiner.add("${parameters." + endpointPathParameter.getEndpointParameter().getParameter() + "}"));

        buffer.append(" {\n");

        buffer.append("\tconst baseUrl = window.location.origin;\n");

        buffer.append("\tconst url = new URL(`${window.location.pathname.split('/')[1]}/rest/")
                .append(endpoint.getCode())
                .append(pathParamJoiner.toString());

        // Query parameters
        if (endpoint.getMethod() == EndpointHttpMethod.GET) {
            StringJoiner getParamJoiner = new StringJoiner("", "", "");
            endpoint.getParametersMapping().forEach(tsParameterMapping -> {
                buffer.append("\n\tif (parameters.")
                        .append(tsParameterMapping.getEndpointParameter().getParameter())
                        .append(" !== undefined) {");

                if(!tsParameterMapping.isMultivalued()) {
                    buffer.append("\n\t\t")
                            .append("url.searchParams.append('")
                            .append(tsParameterMapping.getEndpointParameter().getParameter())
                            .append("', parameters.").append(tsParameterMapping.getEndpointParameter().getParameter())
                            .append(");");
                } else {
                    buffer.append("\n")
                            .append("\tfor(let v of parameters.").append(tsParameterMapping.getEndpointParameter().getParameter()).append(")")
                            .append("{")
                            .append("\t\turl.searchParams.append('")
                            .append(tsParameterMapping.getEndpointParameter().getParameter())
                            .append("', v);\n\t}");
                }

                buffer.append("\n\t}\n");

            });
            buffer.append(getParamJoiner.toString());
        }

        buffer.append("\n\treturn fetch(url.toString(), {\n\t\tmethod: '")
                .append(endpoint.getMethod().getLabel())
                .append("'");

        // Body parameters
        if (endpoint.getMethod() == EndpointHttpMethod.POST) {
            StringJoiner bodyParamJoiner = new StringJoiner(",\n\t\t\t", "`{\n\t\t\t", "\n\t\t}`");
            endpoint.getParametersMapping().forEach(tsParameterMapping -> bodyParamJoiner.add("\"" + tsParameterMapping.getEndpointParameter().getParameter() + "\" : ${" + tsParameterMapping.getEndpointParameter().getParameter() + "}"));
            buffer.append(",\n\t\tbody: ").append(bodyParamJoiner.toString());
        }

        // Export the function
        return buffer.append("\n\t});\n}")
                .append("\n\nexport default ")
                .append(endpoint.getCode())
                .append(";")
                .toString();
    }
}
