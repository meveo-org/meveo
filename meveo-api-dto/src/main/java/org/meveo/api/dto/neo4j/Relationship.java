
package org.meveo.api.dto.neo4j;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Relationship {

    @JsonProperty("id")
    
    private String id;
    @JsonProperty("type")
    
    private String type;
    @JsonProperty("startNode")
    
    private String startNode;
    @JsonProperty("endNode")
    
    private String endNode;
    @JsonProperty("properties")
    
    private Properties_ properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartNode() {
        return startNode;
    }

    public void setStartNode(String startNode) {
        this.startNode = startNode;
    }

    public String getEndNode() {
        return endNode;
    }

    public void setEndNode(String endNode) {
        this.endNode = endNode;
    }

    public Properties_ getProperties() {
        return properties;
    }

    public void setProperties(Properties_ properties) {
        this.properties = properties;
    }

}
