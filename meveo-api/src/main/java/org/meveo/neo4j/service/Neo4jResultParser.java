package org.meveo.neo4j.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.meveo.api.dto.GraphDto;
import org.meveo.api.dto.SearchTableResultDTO;
import org.meveo.api.dto.neo4j.DatumTable;
import org.meveo.api.dto.neo4j.NodeTable;
import org.meveo.api.dto.neo4j.Relationship;
import org.meveo.api.dto.neo4j.ResultTable;
import org.meveo.model.admin.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.*;

/**
 * @author Clément Bareth
 */
@Stateless
public class Neo4jResultParser {

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
    public List<GraphDto> searchGraphResultParser(String jsonResult, User user) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        SearchTableResultDTO searchTableResultDTO = gson.fromJson(jsonResult, SearchTableResultDTO.class);
        List<GraphDto> graphDTOs = new ArrayList<>();
        Map<String, GraphDto> graphs = new HashMap<>();
        for (ResultTable table : searchTableResultDTO.getResults()) {
            for (DatumTable data : table.getData()) {
                if (data.getGraph() != null) {
                    for (NodeTable node : data.getGraph().getNodes()) {
                        GraphDto graphDto = new GraphDto();
                        graphDto.setId(node.getId());
                        graphDto.setLabel(node.getLabels().get(0));
                        graphDto.setProperties(node.getProperties());
                        if (!graphs.containsKey(node.getId()) && !"Source".equals(graphDto.getLabel())) {
                            graphs.put(graphDto.getId(), graphDto);
                        }
                    }
                    for (Relationship relationship : data.getGraph().getRelationships()) {
                        GraphDto parentNode = graphs.get(relationship.getStartNode());
                        GraphDto childNode = graphs.get(relationship.getEndNode());
                        if (parentNode != null && childNode != null) {
                            if ("has_category".equalsIgnoreCase(relationship.getType())) {
                                childNode.getSubGraphs().add(parentNode);
                                graphs.put(childNode.getId(), childNode);
                            } else {
                                parentNode.getSubGraphs().add(childNode);
                                graphs.put(parentNode.getId(), parentNode);
                            }
                        }
                    }
                }
            }
        }
        return graphDTOs;
    }
}
