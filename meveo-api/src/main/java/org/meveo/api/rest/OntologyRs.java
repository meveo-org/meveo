package org.meveo.api.rest;

import org.meveo.api.rest.impl.BaseRs;
import org.meveo.service.crm.impl.JSONSchemaGenerator;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/ontology")
@Produces({MediaType.APPLICATION_JSON})
public class OntologyRs extends BaseRs {

    @Inject
    private JSONSchemaGenerator jsonSchemaGenerator;

    @GET
    public String getSchema(@DefaultValue("true") @QueryParam("onlyActivated") boolean onlyActivated){
        return jsonSchemaGenerator.generateSchema("ontology", onlyActivated);
    }
}
