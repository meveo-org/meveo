package org.meveo.api.dto.response.neo4j;

import io.swagger.annotations.ApiModelProperty;
import org.meveo.api.dto.neo4j.Neo4jConfigurationDto;
import org.meveo.api.dto.response.BaseResponse;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
public class Neo4jConfigurationResponseDto extends BaseResponse {

	private static final long serialVersionUID = -1643983552282801327L;

	@ApiModelProperty("Neo4j configuration information")
	private Neo4jConfigurationDto neo4jConfiguration;

	public Neo4jConfigurationDto getNeo4jConfiguration() {
		return neo4jConfiguration;
	}

	public void setNeo4jConfiguration(Neo4jConfigurationDto neo4jConfiguration) {
		this.neo4jConfiguration = neo4jConfiguration;
	}
}
