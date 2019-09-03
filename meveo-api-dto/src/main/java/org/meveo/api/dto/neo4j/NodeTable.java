
package org.meveo.api.dto.neo4j;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class NodeTable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("labels")
    @Expose
    private List<String> labels = null;
    @SerializedName("properties")
    @Expose
    private Map<String, String> properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
