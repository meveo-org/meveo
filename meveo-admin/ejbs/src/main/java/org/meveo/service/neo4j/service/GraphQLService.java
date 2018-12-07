package org.meveo.service.neo4j.service;

import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.model.neo4j.HTTPGraphQLRequest;
import org.meveo.service.neo4j.base.Neo4jDao;

@Stateless
public class GraphQLService {

  @Inject
  private Neo4jDao neo4jDao;

  public List<Map> executeGraphQLRequest(HTTPGraphQLRequest httpGraphQLRequest){

    return neo4jDao.executeGraphQLQuery(httpGraphQLRequest.getQuery(), httpGraphQLRequest.getVariables(),
            httpGraphQLRequest.getOperationName());
  }
  public List<Map> executeGraphQLRequest(String query){

    return neo4jDao.executeGraphQLQuery(query, null, null);
  }
}

