
package org.meveo.api.dto;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Graph dto.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@ApiModel
public class GraphDto {

	/**
	 * Entity id
	 */
	@JsonProperty("id")
	@ApiModelProperty("Entity id")
	private String id;

	/**
	 * Display label
	 */
	@JsonProperty("label")
	@ApiModelProperty("Display label")
	private String label = null;

	/**
	 * Map of properties
	 */
	@JsonProperty("properties")
	@ApiModelProperty("Map of properties")
	private Map<String, String> properties;

	/**
	 * Sub graphs
	 */
	@ApiModelProperty("Sub graphs")
	private Set<GraphDto> subGraphs = new HashSet<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public Set<GraphDto> getSubGraphs() {
		return subGraphs;
	}

	public void setSubGraphs(Set<GraphDto> subGraphs) {
		this.subGraphs = subGraphs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof GraphDto))
			return false;

		GraphDto graphDto = (GraphDto) o;

		if (id != null ? !id.equals(graphDto.id) : graphDto.id != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
