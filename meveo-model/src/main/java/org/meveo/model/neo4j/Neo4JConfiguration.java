/*
 * (C) Copyright 2019-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.model.neo4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.meveo.model.BusinessEntity;
import org.meveo.security.PasswordUtils;

/**
 * Configuration used to access a Neo4j repository
 *
 * @author clement.bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since 6.6.0
 * 
 */
@Entity
@Table(name = "neo4j_configuration", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@Parameter(name = "sequence_name", value = "neo4j_configuration_seq"), })
public class Neo4JConfiguration extends BusinessEntity {

	private static final long serialVersionUID = 5788790630004555788L;

	public transient static final String DEFAULT_NEO4J_CONNECTION = "default";
	
	/**
	 * Protocol used to connect the database. Default is "bolt".
	 */
	@Column(name = "protocol")
	private String protocol = "bolt";

	/**
	 * Url of the Neo4j repository
	 */
	@Column(name = "neo4j_url")
	private String neo4jUrl;

	/**
	 * Login to connect the repository
	 */
	@Column(name = "neo4j_login")
	private String neo4jLogin;

	/**
	 * Password to connect the repository
	 */
	@Column(name = "neo4j_password")
	private String neo4jPassword;
	
	@Transient
	private String clearPassword;
	
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
	

	/**
	 * @return the {@link #protocol}
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	

	/**
	 * @return the {@link #clearPassword}
	 */
	public String getClearPassword() {
		return clearPassword;
	}

	/**
	 * @param clearPassword the clearPassword to set
	 */
	public void setClearPassword(String clearPassword) {
    	if(clearPassword != null) {
    		if (getCode() == null || getNeo4jUrl() == null)
    			throw new java.lang.IllegalStateException("code and url are required to set password");
    		String salt = PasswordUtils.getSalt(getCode(), getNeo4jUrl());
    		this.neo4jPassword = PasswordUtils.encrypt(salt, clearPassword);
    	}
	}
}
