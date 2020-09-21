
package org.meveo.api.dto.neo4j;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {

    @JsonProperty("columns")
    
    private List<String> columns = null;
    @JsonProperty("data")
    
    private List<Datum> data = null;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

}
