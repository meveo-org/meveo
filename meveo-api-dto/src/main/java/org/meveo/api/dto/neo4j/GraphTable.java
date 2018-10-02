
package org.meveo.api.dto.neo4j;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GraphTable {

    @SerializedName("nodes")
    @Expose
    private List<NodeTable> nodes = null;
    @SerializedName("relationships")
    @Expose
    private List<Relationship> relationships = null;

    public List<NodeTable> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeTable> nodes) {
        this.nodes = nodes;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

}
