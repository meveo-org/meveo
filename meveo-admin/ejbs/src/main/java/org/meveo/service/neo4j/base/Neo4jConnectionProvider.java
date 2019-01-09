/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.neo4j.base;

import org.meveo.commons.utils.ParamBean;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rachid
 * @author clement.bareth
 */
@Startup
@Singleton
public class Neo4jConnectionProvider {

    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    private Logger LOGGER = LoggerFactory.getLogger(Neo4jConnectionProvider.class);

    private static final Map<String, Neo4JConfiguration> configurationMap = new HashMap<>();

    private String neo4jUrl;
    private String neo4jLogin;
    private String neo4jPassword;
    private Integer Neo4jRestPort;

    private final Neo4JConfiguration defaultConfiguration = new Neo4JConfiguration();

    @PostConstruct
    public void loadConfig() {
        neo4jUrl = ParamBean.getInstance().getProperty("neo4j.host", "localhost");
        Neo4jRestPort = Integer.valueOf(ParamBean.getInstance().getProperty("neo4j.rest.port", "7474"));
        neo4jLogin = ParamBean.getInstance().getProperty("neo4j.login", "neo4j");
        neo4jPassword = ParamBean.getInstance().getProperty("neo4j.password", "meveo");

        defaultConfiguration.setCode("default");
        defaultConfiguration.setNeo4jLogin(neo4jLogin);
        defaultConfiguration.setNeo4jPassword(neo4jPassword);
        defaultConfiguration.setNeo4jUrl(neo4jUrl);
    }

    public Session getSession() {
        return getSession(null);
    }

    public Session getSession(String neo4JConfigurationCode) {

        Neo4JConfiguration neo4JConfiguration = defaultConfiguration;
        if (neo4JConfigurationCode != null) {
            try {
                neo4JConfiguration = configurationMap.computeIfAbsent(neo4JConfigurationCode, this::findByCode);
            } catch (NoResultException e) {
                LOGGER.warn("Unknown Neo4j repository {}, using default configuration", neo4JConfigurationCode);
            }
        }

        Driver driver = GraphDatabase.driver("bolt://" + neo4JConfiguration.getNeo4jUrl(), AuthTokens.basic(neo4JConfiguration.getNeo4jLogin(), neo4JConfiguration.getNeo4jPassword()));
        return driver.session();
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

    public String getRestUrl() {
        return "http://" + neo4jUrl + ":" + Neo4jRestPort;
    }

    /**
     * Retrieve a configuration from code
     *
     * @param code Code of the configuration
     * @return The configuration corresponding to the code
     */
    public Neo4JConfiguration findByCode(String code){
        CriteriaBuilder cb = emWrapper.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Neo4JConfiguration> query = cb.createQuery(Neo4JConfiguration.class);
        Root<Neo4JConfiguration> root = query.from(Neo4JConfiguration.class);
        query.select(root);
        query.where(cb.equal(root.get("code"), code));
        return emWrapper.getEntityManager().createQuery(query).getSingleResult();
    }

}
