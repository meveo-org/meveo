
package org.meveo.api.dto.neo4j;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatumTable {

	@JsonProperty("graph")
    private GraphTable graph;
    
	@JsonProperty("row")
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
