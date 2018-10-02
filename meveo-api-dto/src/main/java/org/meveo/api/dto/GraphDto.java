
package org.meveo.api.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.*;

public class GraphDto {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("label")
    @Expose
    private String label = null;
    @SerializedName("properties")
    @Expose
    private Map<String, String> properties;

    private Set<GraphDto> subGraphs = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Set<GraphDto> getSubGraphs() {
        return subGraphs;
    }

    public void setSubGraphs(Set<GraphDto> subGraphs) {
        this.subGraphs = subGraphs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphDto)) return false;

        GraphDto graphDto = (GraphDto) o;

        if (id != null ? !id.equals(graphDto.id) : graphDto.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
