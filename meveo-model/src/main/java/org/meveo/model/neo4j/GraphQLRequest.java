package org.meveo.model.neo4j;

import java.util.Map;

public class GraphQLRequest {

  private String query;
  private String operationName;
  private Map<String,Object> variables;

  public GraphQLRequest(){

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
