
package org.meveo.api.dto.neo4j;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Graph {

    @SerializedName("nodes")
    @Expose
    private List<Node> nodes = null;
    @SerializedName("relationships")
    @Expose
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
