package org.meveo.api.dto.response.neo4j;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.meveo.api.dto.neo4j.Neo4jConfigurationDto;
import org.meveo.api.dto.response.BaseResponse;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
public class Neo4jConfigurationsResponseDto extends BaseResponse {

	private static final long serialVersionUID = -7286623219042815900L;

	@ApiModelProperty("List of neo4j configurations information")
	private List<Neo4jConfigurationDto> neo4jConfigurations;

	public List<Neo4jConfigurationDto> getNeo4jConfigurations() {
		return neo4jConfigurations;
	}

	public void setNeo4jConfigurations(List<Neo4jConfigurationDto> neo4jConfigurations) {
		this.neo4jConfigurations = neo4jConfigurations;
	}
}
