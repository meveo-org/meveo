package org.meveo.model.neo4j;

import java.util.Map;

public class HTTPGraphQLRequest {

  private String query;
  private String operationName;
  private Map<String,Object> variables;
  private String neo4jConfiguration;

  public HTTPGraphQLRequest(){

  }

  public String getNeo4jConfiguration() {
    return neo4jConfiguration;
  }

  public void setNeo4jConfiguration(String neo4jConfiguration) {
    this.neo4jConfiguration = neo4jConfiguration;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  public String getOperationName() {
    return operationName;
  }

  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }
}
