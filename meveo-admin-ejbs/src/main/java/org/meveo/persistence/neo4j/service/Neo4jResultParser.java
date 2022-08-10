/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.persistence.neo4j.service;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
