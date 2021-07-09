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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.EndpointParameter;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;

import java.util.regex.Pattern;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
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
        endpoint.getPathParametersNullSafe().add(endpointPathParameter);

        TSParameterMapping parameterMapping = new TSParameterMapping();
        EndpointParameter body = new EndpointParameter();
        body.setParameter("bodyParam1");
        parameterMapping.setParameterName("myFunctionParam");
        parameterMapping.setEndpointParameter(body);
        endpoint.getParametersMappingNullSafe().add(parameterMapping);

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
        endpoint.getPathParametersNullSafe().add(endpointPathParameter);

        TSParameterMapping parameterMapping = new TSParameterMapping();
        EndpointParameter body = new EndpointParameter();
        body.setParameter("bodyParam1");
        parameterMapping.setParameterName("myFunctionParam");
        parameterMapping.setEndpointParameter(body);
        endpoint.getParametersMappingNullSafe().add(parameterMapping);

        TSParameterMapping parameterMapping2 = new TSParameterMapping();
        EndpointParameter body2 = new EndpointParameter();
        body2.setParameter("bodyParam2");
        parameterMapping2.setParameterName("myFunctionParam2");
        parameterMapping2.setEndpointParameter(body2);
        endpoint.getParametersMappingNullSafe().add(parameterMapping2);

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


    @Test(expected = RuntimeException.class)
    public void testInvalidCode() {
        Endpoint endpoint = new Endpoint();
        endpoint.setCode("mytest/toto");
    }

    @Test
    public void testBasePath() {
        Endpoint endpoint = new Endpoint();
        endpoint.setCode("my-test01_EX");
        assert (endpoint.getBasePath().equals("my-test01_EX"));
        assert (endpoint.getPath().equals("/"));
        endpoint.setBasePath("my-own_basepath");
        assert (endpoint.getBasePath().equals("my-own_basepath"));
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidBasePath() {
        Endpoint endpoint = new Endpoint();
        endpoint.setCode("mytest");
        endpoint.setBasePath("basepath/is invalid");
    }

    private Endpoint createEndpointWithTwoPathParams() {
        Endpoint endpoint = new Endpoint();
        endpoint.setCode("mytest");
        EndpointPathParameter endpointPathParameter = new EndpointPathParameter();
        EndpointParameter first = new EndpointParameter();
        endpointPathParameter.setEndpointParameter(first);
        first.setParameter("first");
        endpoint.addPathParameter(endpointPathParameter);
        EndpointPathParameter endpointPathParameter2 = new EndpointPathParameter();
        EndpointParameter second = new EndpointParameter();
        endpointPathParameter2.setEndpointParameter(second);
        second.setParameter("second");
        endpoint.addPathParameter(endpointPathParameter2);
        return endpoint;
    }

    @Test
    public void testPath() {
        Endpoint endpoint = createEndpointWithTwoPathParams();
        assert (endpoint.getPath().equals("/{first}/{second}"));
        String url=endpoint.getEndpointUrl();
        assert (url.equals("/rest/mytest/{first}/{second}"));
        Pattern regex= endpoint.getPathRegex();
        System.out.println(regex.pattern());
        assert (regex.matcher("mytest/un/deux").matches());
        assert (!regex.matcher("mytest/un").matches());
        assert (!regex.matcher("mytest/un/deux/trois").matches());
    }

    @Test
    public void testPathOverride() {
        Endpoint endpoint = createEndpointWithTwoPathParams();
        endpoint.setPath("/{first}/id/{second}");
        assert (endpoint.getPath().equals("/{first}/id/{second}"));
        Pattern regex= endpoint.getPathRegex();
        System.out.println(regex.pattern());
        assert (regex.matcher("mytest/un/id/deux").matches());
        assert (!regex.matcher("mytest/un/deux").matches());
        assert (!regex.matcher("mytest/un/id/deux/trois").matches());
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testInvalidPath() {
        Endpoint endpoint = createEndpointWithTwoPathParams();
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("1th path param is expected to be first while actual value is second");
        endpoint.setPath("/id/{second}");
    }

    @Test
    public void testInvalidPath2() {
        Endpoint endpoint = createEndpointWithTwoPathParams();
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("missing param second");
        endpoint.setPath("/{first}/id/");
    }

    @Test
    public void testInvalidPathAdditionalParam() {
        Endpoint endpoint = createEndpointWithTwoPathParams();
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("unexpected param third");
        endpoint.setPath("/{first}/id/{second}/{third}");
    }


    @Test
    public void testInvalidPathOrder() {
        Endpoint endpoint = createEndpointWithTwoPathParams();
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("1th path param is expected to be first while actual value is second");
        endpoint.setPath("/{second}/id/{first}");
    }

}