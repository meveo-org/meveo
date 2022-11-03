/**
 * 
 */
package org.meveo.persistence.neo4j.service.graphql;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.meveo.model.neo4j.GraphQLRequest;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;

/**
 * A java client for the meveo neo4j-graphql server
 * 
 * @see https://hub.docker.com/r/manaty/neo4j-graphql
 * 
 * @author ClementBareth
 * @since 7.2.0
 * @version 7.2.0
 */
public class GraphQlClient {
	
	@Inject
	@CurrentUser	
	private MeveoUser currentUser;
	
	private Client client;

	@PostConstruct
	public void init() {
		client = ResteasyClientBuilder.newClient();
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> executeGraphQlRequest(GraphQLRequest graphQLRequest, String graphqlEndpoint) {
		try {
			return client.target(graphqlEndpoint)
					.path("/graphql")
					.request()
					.header("Authorization", "Bearer " + currentUser.getToken())
					.buildPost(Entity.entity(graphQLRequest, MediaType.APPLICATION_JSON))
					.invoke(Map.class);
		} catch (BadRequestException e) {
			String errorMessage = e.getResponse().readEntity(String.class);
			throw new IllegalArgumentException(errorMessage);
		} catch (ServerErrorException e) {
			String errorMessage = e.getResponse().readEntity(String.class);
			throw new RuntimeException(errorMessage);
		}

	}
	
	public void updateIdl(String idl, String graphqlEndpoint) {
		try {
			var response = client.target(graphqlEndpoint)
				.path("/update")
				.request()
				.header("Authorization", "Bearer " + currentUser.getToken())
				.buildPost(Entity.entity(idl, MediaType.TEXT_PLAIN))
				.invoke();
			
			if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				String errorMessage = response.readEntity(String.class);
				throw new RuntimeException(errorMessage);
			}
			
		} catch (WebApplicationException e) {
			String errorMessage = e.getResponse().readEntity(String.class);
			throw new RuntimeException(errorMessage);
		}
	}
	
	@PreDestroy
	public void close() {
		if (client != null) {
			client.close();
		}
	}
}
