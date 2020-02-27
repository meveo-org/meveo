package org.meveo.api.dto.neo4j;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.neo4j.Neo4JConfiguration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Stores the connection setting to connect to a Neo4j server.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@ApiModel("Neo4jConfigurationDto")
public class Neo4jConfigurationDto extends BusinessEntityDto {

	private static final long serialVersionUID = -4634237210669083632L;

	/*
	 * Neo4j url
	 */
	@ApiModelProperty("Neo4j url")
	private String neo4jUrl;

	/*
	 * Neo4j login
	 */
	@ApiModelProperty("Neo4j login")
	private String neo4jLogin;

	/*
	 * Neo4j password
	 */
	@ApiModelProperty("Neo4j password")
	private String neo4jPassword;

	public Neo4jConfigurationDto() {

	}

	public Neo4jConfigurationDto(Neo4JConfiguration e) {
		super(e);

		if (e != null) {
			neo4jUrl = e.getNeo4jUrl();
			neo4jLogin = e.getNeo4jLogin();
			neo4jPassword = e.getNeo4jPassword();
		}
	}

	public String getNeo4jUrl() {
		return neo4jUrl;
	}

	public void setNeo4jUrl(String neo4jUrl) {
		this.neo4jUrl = neo4jUrl;
	}

	public String getNeo4jLogin() {
		return neo4jLogin;
	}

	public void setNeo4jLogin(String neo4jLogin) {
		this.neo4jLogin = neo4jLogin;
	}

	public String getNeo4jPassword() {
		return neo4jPassword;
	}

	public void setNeo4jPassword(String neo4jPassword) {
		this.neo4jPassword = neo4jPassword;
	}
}
