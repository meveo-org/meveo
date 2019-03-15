package org.meveo.api.rest;

import org.meveo.api.rest.impl.BaseRs;
import org.meveo.service.crm.impl.JSONSchemaGenerator;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ontology")
@Produces({MediaType.APPLICATION_JSON})
public class OntologyRs extends BaseRs {

    @Inject
    private JSONSchemaGenerator jsonSchemaGenerator;

    @GET
    public Response getSchema(@DefaultValue("true") @QueryParam("onlyActivated") boolean onlyActivated){
        final String ontology = jsonSchemaGenerator.generateSchema("ontology", onlyActivated);

        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(86400);
        cacheControl.setPrivate(true);

        Response.ResponseBuilder builder = Response.ok(ontology);
        builder.cacheControl(cacheControl);

        return builder.build();
    }
}
