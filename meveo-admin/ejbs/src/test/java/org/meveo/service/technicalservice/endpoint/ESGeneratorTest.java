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

import org.junit.Test;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.EndpointParameter;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;

public class ESGeneratorTest {

    private static final Endpoint getEndpoint = initGetEndpoint();
    private static final Endpoint postEndpoint = initPostEndpoint();

    private static Endpoint initGetEndpoint() {
        Endpoint endpoint = new Endpoint();
        endpoint.setMethod(EndpointHttpMethod.GET);
        endpoint.setCode("testGet");

        EndpointPathParameter endpointPathParameter = new EndpointPathParameter();
        EndpointParameter first = new EndpointParameter();
        endpointPathParameter.setEndpointParameter(first);
        first.setParameter("first");
        endpoint.getPathParameters().add(endpointPathParameter);

        TSParameterMapping parameterMapping = new TSParameterMapping();
        EndpointParameter body = new EndpointParameter();
        body.setParameter("bodyParam1");
        parameterMapping.setParameterName("myFunctionParam");
        parameterMapping.setEndpointParameter(body);
        endpoint.getParametersMapping().add(parameterMapping);

        return endpoint;
    }

    private static Endpoint initPostEndpoint() {
        Endpoint endpoint = new Endpoint();
        endpoint.setMethod(EndpointHttpMethod.POST);
        endpoint.setCode("testPost");

        EndpointPathParameter endpointPathParameter = new EndpointPathParameter();
        EndpointParameter first = new EndpointParameter();
        endpointPathParameter.setEndpointParameter(first);
        first.setParameter("first");
        endpoint.getPathParameters().add(endpointPathParameter);

        TSParameterMapping parameterMapping = new TSParameterMapping();
        EndpointParameter body = new EndpointParameter();
        body.setParameter("bodyParam1");
        parameterMapping.setParameterName("myFunctionParam");
        parameterMapping.setEndpointParameter(body);
        endpoint.getParametersMapping().add(parameterMapping);

        TSParameterMapping parameterMapping2 = new TSParameterMapping();
        EndpointParameter body2 = new EndpointParameter();
        body2.setParameter("bodyParam2");
        parameterMapping2.setParameterName("myFunctionParam2");
        parameterMapping2.setEndpointParameter(body2);
        endpoint.getParametersMapping().add(parameterMapping2);

        return endpoint;
    }

    @Test
    public void testGenerate() {
        String postGenerated = ESGenerator.generate(postEndpoint);
        System.out.println(postGenerated);

        String getGenerated = ESGenerator.generate(getEndpoint);
        System.out.println(getGenerated);
    }

    @Test
    public void testGenerateForm() {
        String postGenerated = ESGenerator.generateHtmlForm(postEndpoint);
        System.out.println(postGenerated);
    }

}