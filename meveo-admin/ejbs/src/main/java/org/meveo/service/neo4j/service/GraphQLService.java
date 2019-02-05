package org.meveo.service.neo4j.service;

import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.model.neo4j.GraphQLRequest;
import org.meveo.service.neo4j.base.Neo4jDao;

@Stateless
public class GraphQLService {

  @Inject
  private Neo4jDao neo4jDao;

  public Map<String, Object> executeGraphQLRequest(GraphQLRequest graphQLRequest, String neo4jConfiguration){

    return neo4jDao.executeGraphQLQuery(
            neo4jConfiguration,
            graphQLRequest.getQuery(),
            graphQLRequest.getVariables(),
            graphQLRequest.getOperationName()
    );
  }
  public Map<String, Object> executeGraphQLRequest(String query, String neo4jConfiguration){

    return neo4jDao.executeGraphQLQuery(neo4jConfiguration, query, null, null);
  }
}

