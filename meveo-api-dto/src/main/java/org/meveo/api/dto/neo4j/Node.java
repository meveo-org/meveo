
package org.meveo.api.dto.neo4j;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Node {

	@JsonProperty("id")
    private String id;
    
	@JsonProperty("labels")
    private List<String> labels = null;
    
	@JsonProperty("properties")
    private Properties properties;

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

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
