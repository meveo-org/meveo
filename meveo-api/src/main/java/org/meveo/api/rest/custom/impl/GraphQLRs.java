package org.meveo.api.rest.custom.impl;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;
import org.jboss.logging.Logger;
import org.meveo.admin.exception.BusinessException;
import org.meveo.model.neo4j.GraphQLRequest;
import org.meveo.persistence.neo4j.service.graphql.GraphQLService;

@Path("/graphql/{neo4jConfiguration}/")
public class GraphQLRs {

  protected static final Logger LOGGER = Logger.getLogger(GraphQLRs.class);

  @Inject
  protected GraphQLService graphQLService;

  @PathParam("neo4jConfiguration")
  private String neo4jConfiguration;

  @GET
  @Path("/idl")
  public String getIdl(){
    return graphQLService.getIDL();
  }

  @POST
  @Path("/idl")
  public void updateIdl() throws BusinessException{
	  try {
	  	graphQLService.updateIDL(neo4jConfiguration);
	  } catch(Exception e) {
		  throw new BusinessException("Cannot update IDL for repository " + neo4jConfiguration, e);
	  }
  }

  @GET
  @Path("/")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response executeGraphQLQueryInGet(@QueryParam("query") @ApiParam("Query of graphql") String query){
    Map<String, Object> result = graphQLService.executeGraphQLRequest(query, neo4jConfiguration);
    return Response.ok(result).build();
  }

  @POST
  @Path("/")
  @Consumes("application/graphql")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response executeGraphQLQueryInPost(@ApiParam("Query of graphql") String query){
    Map<String, Object> result = graphQLService.executeGraphQLRequest(query, neo4jConfiguration);
    return Response.ok(result).build();
  }

  @POST
  @Path("/")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public Response executeGraphQLRequest(@ApiParam("GraphQL request information") GraphQLRequest graphQLRequest){
    Map<String, Object> result = graphQLService.executeGraphQLRequest(graphQLRequest, neo4jConfiguration);
    return Response.ok(result).build();
  }
}
