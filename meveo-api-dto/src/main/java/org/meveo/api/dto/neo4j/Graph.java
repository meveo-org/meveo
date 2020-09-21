
package org.meveo.api.dto.neo4j;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Graph {

	@JsonProperty("nodes")
    private List<Node> nodes = null;
	
	@JsonProperty("relationships")
    private List<Relationship> relationships = null;

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

}
