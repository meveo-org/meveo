/**
 * 
 */
package org.meveo.api.rest.custom.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.neo4j.GraphQLRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
@Path("/graphql/{neo4jConfiguration}/")
@Api("Graph query")
public interface GraphQLRS {

	@GET
	@Path("/idl")
	String getIdl();

	@POST
	@Path("/idl")
	void updateIdl() throws BusinessException;

	@GET
	@Path("/")
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Execute query of graphql in get")
	Response executeGraphQLQueryInGet(@QueryParam("query") @ApiParam("Query of graphql") String query);

	@POST
	@Path("/")
	@Consumes("application/graphql")
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Execute query of graphql in post")
	Response executeGraphQLQueryInPost(@ApiParam("Query of graphql")  String query);

	@POST
	@Path("/")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Execute graphQL request information")
	Response executeGraphQLRequest(@ApiParam("GraphQL request information") GraphQLRequest graphQLRequest);

}