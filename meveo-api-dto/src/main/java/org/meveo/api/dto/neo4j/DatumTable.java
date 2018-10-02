
package org.meveo.api.dto.neo4j;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DatumTable {

    @SerializedName("graph")
    @Expose
    private GraphTable graph;
    
    @SerializedName("row")
    @Expose
    private List<String> row = new  ArrayList<String>();

    public GraphTable getGraph() {
        return graph;
    }

    public void setGraph(GraphTable graph) {
        this.graph = graph;
    }

	public List<String> getRow() {
		return row;
	}

	public void setRow(List<String> row) {
		this.row = row;
	}

    
}
