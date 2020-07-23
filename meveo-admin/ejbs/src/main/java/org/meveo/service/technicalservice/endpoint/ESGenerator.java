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

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
public class ESGenerator {

    public static String generateHtmlForm(Endpoint endpoint) {
        String code = endpoint.getCode();
        String formId = code + "-form";

        StringBuilder buffer = new StringBuilder("const ").append(code).append("Form = (container) => {\n")
                .append("\tconst html = `<form id='").append(formId).append("'>");

        List<String> params = new ArrayList<>();

        for (EndpointPathParameter pathParameter : endpoint.getPathParametersNullSafe()) {
            String parameter = pathParameter.getEndpointParameter().getParameter();
            appendFormField(code, buffer, parameter);
            params.add(parameter);
        }

        for (TSParameterMapping tsParameterMapping : endpoint.getParametersMappingNullSafe()) {
            String parameter = tsParameterMapping.getParameterName();
            appendFormField(code, buffer, parameter);
            params.add(parameter);
        }

        buffer.append("\n\t\t<button type='button'>Test</button>")
                .append("\n\t</form>`;\n")
                .append("\n\tcontainer.insertAdjacentHTML('beforeend', html)\n\n");

        for(String param : params) {
            buffer.append("\tconst ").append(param).append(" = container.querySelector('#")
                    .append(code).append("-").append(param).append("-param');\n");
        }

        buffer.append("\n\tcontainer.querySelector('#").append(formId).append(" button').onclick = () => {\n")
                .append("\t\tconst params = {\n");

        String prefix = "";
        for(String param : params) {
        	String paramValue = param + ".value";
            buffer.append(prefix).append("\t\t\t").append(param).append(" : ").append(paramValue).append(" !== \"\" ? ").append(paramValue).append( " : undefined");
            prefix = ",\n";
        }
        buffer.append("\n\t\t};");

        buffer.append("\n\n\t\t").append(code).append("(params).then(r => r.text().then(\n\t\t\t\tt => alert(t)\n\t\t\t));");

        buffer.append("\n\t};");

        buffer.append("\n}");
        return buffer.toString();
    }

    public static String generate(Endpoint endpoint) {
        StringBuilder buffer = new StringBuilder("const ")
                .append(endpoint.getCode())
                .append(" = async (parameters) => ");

        // Path parameters
        StringJoiner pathParamJoiner = new StringJoiner("/", "/", "`, baseUrl);");
        endpoint.getPathParametersNullSafe().forEach(endpointPathParameter -> pathParamJoiner.add("${parameters." + endpointPathParameter.getEndpointParameter().getParameter() + "}"));

        buffer.append(" {\n");

        buffer.append("\tconst baseUrl = window.location.origin;\n");

        buffer.append("\tconst url = new URL(`${window.location.pathname.split('/')[1]}/rest/")
                .append(endpoint.getCode())
                .append(pathParamJoiner.toString());

        // Query parameters
        if (endpoint.getMethod() == EndpointHttpMethod.GET) {
            StringJoiner getParamJoiner = new StringJoiner("", "", "");
            endpoint.getParametersMappingNullSafe().forEach(tsParameterMapping -> {
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
        	buffer.append(", \n\t\theaders : new Headers({\n \t\t\t'Content-Type': 'application/json'\n\t\t})");
            StringJoiner bodyParamJoiner = new StringJoiner(",\n\t\t\t", "JSON.stringify({\n\t\t\t", "\n\t\t})");
            endpoint.getParametersMappingNullSafe().forEach(tsParameterMapping -> bodyParamJoiner.add(tsParameterMapping.getParameterName() + " : parameters." + tsParameterMapping.getParameterName()));
            buffer.append(",\n\t\tbody: ").append(bodyParamJoiner.toString());
        }

        // Export the function
        return buffer.append("\n\t});\n}")
                .toString();

    }

    public static String generateFile(Endpoint endpoint) {
        return new StringBuilder()
                .append(generate(endpoint))
                .append("\n\n")
                .append(generateHtmlForm(endpoint))
                .append("\n\nexport { ").append(endpoint.getCode()).append(", ").append(endpoint.getCode()).append("Form }").append(";")
                .toString();
    }

    private static void appendFormField(String code, StringBuilder buffer, String parameter) {
        buffer.append("\n\t\t<div id='").append(code).append("-").append(parameter).append("-form-field'>\n")
                .append("\t\t\t<label for='").append(parameter).append("'>").append(parameter).append("</label>\n")
                .append("\t\t\t<input type='text' id='").append(code).append("-").append(parameter).append("-param'").append(" name='").append(parameter).append("'/>\n")
                .append("\t\t</div>");
    }
}
