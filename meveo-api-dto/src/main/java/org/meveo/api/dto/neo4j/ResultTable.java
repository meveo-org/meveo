
package org.meveo.api.dto.neo4j;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultTable {

    @JsonProperty("columns")
    
    private List<String> columns = null;
    @JsonProperty("data")
    
    private List<DatumTable> data = null;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<DatumTable> getData() {
        return data;
    }

    public void setData(List<DatumTable> data) {
        this.data = data;
    }

}
