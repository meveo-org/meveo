package org.meveo.api.rest.custom.impl;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.model.neo4j.GraphQLRequest;
import org.meveo.persistence.neo4j.service.graphql.GraphQLService;
import org.meveo.service.neo4j.Neo4jConfigurationService;

@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
public class GraphQLRsImpl implements GraphQLRS {

	protected static final Logger LOGGER = Logger.getLogger(GraphQLRsImpl.class);

	@Inject
	protected GraphQLService graphQLService;

	@Inject
	private Neo4jConfigurationService neo4jRepoService;

	@PathParam("neo4jConfiguration")
	private String neo4jConfiguration;

	@Override
	public String getIdl() {
		return graphQLService.getIDL();
	}

	@Override
	public void updateIdl() throws BusinessException {
		if (neo4jRepoService.findByCode(neo4jConfiguration) == null) {
			throw new NotFoundException("Repository " + neo4jConfiguration + " does not exists");
		}

		try {
			graphQLService.updateIDL(neo4jConfiguration);
		} catch (Exception e) {
			throw new BusinessException("Cannot update IDL for repository " + neo4jConfiguration, e);
		}
	}

	@Override
	public Response executeGraphQLQueryInGet(String query) {
		Map<String, Object> result = graphQLService.executeGraphQLRequest(query, neo4jConfiguration);
		return Response.ok(result).build();
	}

	@Override
	public Response executeGraphQLQueryInPost(String query) {
		Map<String, Object> result = graphQLService.executeGraphQLRequest(query, neo4jConfiguration);
		return Response.ok(result).build();
	}

	@Override
	public Response executeGraphQLRequest(GraphQLRequest graphQLRequest) {
		Map<String, Object> result = graphQLService.executeGraphQLRequest(graphQLRequest, neo4jConfiguration);
		return Response.ok(result).build();
	}
}
