
package org.meveo.api.dto.neo4j;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {

    @SerializedName("graph")
    @Expose
    private Graph graph;
    
    @SerializedName("row")
    @Expose
    private List<String> row = new  ArrayList<String>();

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

	public List<String> getRow() {
		return row;
	}

	public void setRow(List<String> row) {
		this.row = row;
	}

    
}
