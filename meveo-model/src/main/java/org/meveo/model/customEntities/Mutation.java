package org.meveo.model.customEntities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class Mutation implements Serializable {

	private static final long serialVersionUID = 2535597940280255114L;
	
	private String code;
    private Map<String, String> parameters = new HashMap<>();
    private String cypherQuery;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getCypherQuery() {
        return cypherQuery;
    }

    public void setCypherQuery(String cypherQuery) {
        this.cypherQuery = cypherQuery;
    }

	@Override
	public String toString() {
		StringBuilder mutationLine = new StringBuilder(getCode());
		
		StringJoiner parametersJoiner = new StringJoiner(", ","(", ")");
		getParameters().forEach((name, type) -> {
			parametersJoiner.add(name + ": " + type);
		});
		mutationLine.append(parametersJoiner.toString());
		
		String escapedQuery = getCypherQuery()
				.replaceAll("\"", "\\\\\"")
				.replaceAll("'", "\\\\'")
				.replaceAll("\n", " ");
		
		mutationLine.append(": String @cypher(statement: \"")
			.append(escapedQuery)
			.append("\")");
		
		return mutationLine.toString();
	}
    
    
}
