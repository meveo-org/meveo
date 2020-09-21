
package org.meveo.api.dto.neo4j;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Neo4jQueryResultDto {

	@JsonProperty("columns")
    private List<String> columns = null;
    
	@JsonProperty("data")
    private List<List<String>> data = null;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }

}
