package org.meveo.service.technicalservice.endpoint;

import org.junit.Test;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.EndpointParameter;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EndpointRetrieval {
    EndpointCacheContainer cache = new EndpointCacheContainer();

    public EndpointRetrieval(){
        Endpoint endpoint = new Endpoint();
        endpoint.setCode("mytest");
        endpoint.setBasePath("mytest");
        endpoint.setMethod(EndpointHttpMethod.GET);
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
        cache.addEndpoint(endpoint);

        endpoint = new Endpoint();
        endpoint.setCode("country");
        endpoint.setBasePath("country");
        endpoint.setMethod(EndpointHttpMethod.GET);
        endpointPathParameter = new EndpointPathParameter();
        first = new EndpointParameter();
        endpointPathParameter.setEndpointParameter(first);
        first.setParameter("first");
        endpoint.addPathParameter(endpointPathParameter);

        cache.addEndpoint(endpoint);

        endpoint = new Endpoint();
        endpoint.setCode("country-by-status");
        endpoint.setBasePath("country-by-status");
        endpoint.setMethod(EndpointHttpMethod.GET);
        endpointPathParameter = new EndpointPathParameter();
        first = new EndpointParameter();
        endpointPathParameter.setEndpointParameter(first);
        first.setParameter("first");
        endpoint.addPathParameter(endpointPathParameter);
        endpointPathParameter2 = new EndpointPathParameter();
        second = new EndpointParameter();
        endpointPathParameter2.setEndpointParameter(second);
        second.setParameter("second");
        endpoint.addPathParameter(endpointPathParameter2);
        endpoint.setPath("/{first}/id/{second}");
        cache.addEndpoint(endpoint);

        endpoint = new Endpoint();
        endpoint.setCode("idservice");
        endpoint.setBasePath("idservice");
        endpoint.setMethod(EndpointHttpMethod.GET);
        endpointPathParameter = new EndpointPathParameter();
        first = new EndpointParameter();
        endpointPathParameter.setEndpointParameter(first);
        first.setParameter("first");
        endpoint.addPathParameter(endpointPathParameter);
        endpointPathParameter2 = new EndpointPathParameter();
        second = new EndpointParameter();
        endpointPathParameter2.setEndpointParameter(second);
        second.setParameter("second");
        endpoint.addPathParameter(endpointPathParameter2);
        endpoint.setPath("/{first}/id/{second}");
        cache.addEndpoint(endpoint);

        endpoint = new Endpoint();
        endpoint.setCode("manifestservice");
        endpoint.setMethod(EndpointHttpMethod.GET);
        endpointPathParameter = new EndpointPathParameter();
        first = new EndpointParameter();
        endpointPathParameter.setEndpointParameter(first);
        first.setParameter("first");
        endpoint.addPathParameter(endpointPathParameter);
        endpointPathParameter2 = new EndpointPathParameter();
        second = new EndpointParameter();
        endpointPathParameter2.setEndpointParameter(second);
        second.setParameter("second");
        endpoint.addPathParameter(endpointPathParameter2);
        endpoint.setBasePath("idservice");
        endpoint.setPath("/{first}/manifest/{second}");
        cache.addEndpoint(endpoint);

        endpoint = new Endpoint();
        endpoint.setCode("manifestservicePost");
        endpoint.setMethod(EndpointHttpMethod.POST);
        endpointPathParameter = new EndpointPathParameter();
        first = new EndpointParameter();
        endpointPathParameter.setEndpointParameter(first);
        first.setParameter("first");
        endpoint.addPathParameter(endpointPathParameter);
        endpointPathParameter2 = new EndpointPathParameter();
        second = new EndpointParameter();
        endpointPathParameter2.setEndpointParameter(second);
        second.setParameter("second");
        endpoint.addPathParameter(endpointPathParameter2);
        endpoint.setBasePath("idservice");
        endpoint.setPath("/{first}/manifest/{second}");
        cache.addEndpoint(endpoint);

        endpoint = new Endpoint();
        endpoint.setCode("city");
        endpoint.setBasePath("city");
        endpoint.setMethod(EndpointHttpMethod.GET);

        cache.addEndpoint(endpoint);
    }

    @Test
    public void retrieveEndpoint(){

        Endpoint endpoint = cache.getEndpointForPath("/mytest/param1/param2?param3=1","GET");
        assert("mytest".equals(endpoint.getCode()));

        endpoint = cache.getEndpointForPath("/mytest2/param1/param2?param3=1","GET");
        assertNull(endpoint);

        endpoint = cache.getEndpointForPath("/idservice/param1/id/param2?param3=1","GET");
        assert("idservice".equals(endpoint.getCode()));

        endpoint = cache.getEndpointForPath("/idservice/param1/manifest/param2?param3=1","GET");
        assert("manifestservice".equals(endpoint.getCode()));

        endpoint = cache.getEndpointForPath("/idservice/param1/manifest/param2?param3=1","POST");
        assert("manifestservicePost".equals(endpoint.getCode()));

        endpoint = cache.getEndpointForPath("/country-by-status/param1/id/param2?param3=1","GET");
        assert("country-by-status".equals(endpoint.getCode()));

        endpoint = cache.getEndpointForPath("/country-by-name/param1/param2?param3=1","GET");
        assertNull(endpoint);

        endpoint = cache.getEndpointForPath("/country-by-status/param1","GET");
        assertNull(endpoint);

    }
}
