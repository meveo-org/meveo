
package org.meveo.api.dto.neo4j;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResultTable {

    @SerializedName("columns")
    @Expose
    private List<String> columns = null;
    @SerializedName("data")
    @Expose
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
