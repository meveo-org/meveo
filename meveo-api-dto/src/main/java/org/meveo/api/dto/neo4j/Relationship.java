
package org.meveo.api.dto.neo4j;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Relationship {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("startNode")
    @Expose
    private String startNode;
    @SerializedName("endNode")
    @Expose
    private String endNode;
    @SerializedName("properties")
    @Expose
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
