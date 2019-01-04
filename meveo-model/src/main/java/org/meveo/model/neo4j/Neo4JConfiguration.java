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

import javax.persistence.*;

/**
 * Configuration used to access a Neo4j repository
 *
 * @author clement.bareth
 */
@Entity
@Table(name = "neo4j_configuration", uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
public class Neo4JConfiguration {

    /**
     * Code of the configuration
     */
    @Id
    private String code;

    /**
     * Url of the Neo4j repository
     */
    @Column(name = "neo4j_url")
    private String neo4jUrl;

    /**
     * Login to connect the repository
     */
    @Column(name="neo4j_login")
    private String neo4jLogin;

    /**
     * Password to connect the repository
     */
    @Column(name="neo4j_password")
    private String neo4jPassword;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
