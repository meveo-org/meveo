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
package org.meveo.persistence.neo4j.base;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.security.PasswordUtils;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rachid
 * @author clement.bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class Neo4jConnectionProvider {

    @Inject
    @MeveoJpa
    private Provider<EntityManagerWrapper> emWrapperProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jConnectionProvider.class);

    private static final Map<String, Neo4JConfiguration> configurationMap = new ConcurrentHashMap<>();
    private static final Map<String, Driver> DRIVER_MAP = new ConcurrentHashMap<>();

    static {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        for (Map.Entry<String, Driver> driverEntry : DRIVER_MAP.entrySet()) {
          try {
            driverEntry.getValue().close();
          } catch (Exception e) {
            LOGGER.error("Error in shutdownHOOK : Error close neo4j driver for mission {}", driverEntry.getKey(), e);
          }
        }
      }));
    }

    private String neo4jUrl;
    private String neo4jLogin;
    private String neo4jPassword;
    private String neo4jRestUrl;

    private Neo4JConfiguration defaultConfiguration = null; 

    @PostConstruct
    public void loadConfig() {
    	Properties sysProperties = System.getProperties();
		neo4jUrl = sysProperties.getProperty("neo4j.host", null);
        neo4jRestUrl = "http://" + StringUtils.substringBefore(neo4jUrl, ":") + ":" +Integer.valueOf(sysProperties.getProperty("neo4j.rest.port", "-1"));
        neo4jLogin = sysProperties.getProperty("neo4j.login", null);
        neo4jPassword = sysProperties.getProperty("neo4j.password", null);

        if(neo4jUrl != null && neo4jLogin != null && neo4jUrl != null) {
        	defaultConfiguration = new Neo4JConfiguration(); 
	        defaultConfiguration.setCode(Neo4JConfiguration.DEFAULT_NEO4J_CONNECTION);
	        defaultConfiguration.setNeo4jLogin(neo4jLogin);
	        defaultConfiguration.setNeo4jUrl(neo4jUrl);
	        defaultConfiguration.setClearPassword(neo4jPassword);
	    }
    }

    public Session getSession() {
        return getSession(null);
    }

    /**
     * @return a neo4j session, or null if a problem has occured
     */
    public Session getSession(String neo4JConfigurationCode) {

        Neo4JConfiguration neo4JConfiguration = defaultConfiguration;
        if (neo4JConfigurationCode != null) {
            try {
                neo4JConfiguration = configurationMap.computeIfAbsent(neo4JConfigurationCode, this::findByCode);
            } catch (NoResultException e) {
                LOGGER.warn("Unknown Neo4j repository {}, using default configuration", neo4JConfigurationCode);
            }
        }

        try{
        	Driver driver = DRIVER_MAP.computeIfAbsent(neo4JConfigurationCode, this::generateDriver);
        	synchronized (this) {
                return driver.session();
            }
        }catch (Exception e){
            LOGGER.warn("Can't connect to {} ({}): {}", neo4JConfigurationCode, neo4JConfiguration.getNeo4jUrl(), e.getMessage(),e);
        	DRIVER_MAP.remove(neo4JConfigurationCode);
            return null;
        }

    }

	private synchronized Driver generateDriver(String neo4JConfigurationCode) {
		Neo4JConfiguration neo4JConfiguration = defaultConfiguration;
		if (neo4JConfigurationCode != null) {
			try {
				neo4JConfiguration = configurationMap.computeIfAbsent(neo4JConfigurationCode, this::findByCode);
			} catch (NoResultException e) {
				LOGGER.warn("Unknown Neo4j repository {}, using default configuration", neo4JConfigurationCode);
			}
		}

		return createDriver(neo4JConfiguration);
	}

	public Driver createDriver(Neo4JConfiguration neo4JConfiguration) {
		String salt = PasswordUtils.getSalt(neo4JConfiguration.getCode(), neo4JConfiguration.getNeo4jUrl());
		String pwd = PasswordUtils.decrypt(salt, neo4JConfiguration.getNeo4jPassword());
		var driver =  GraphDatabase.driver(neo4JConfiguration.getProtocol() + "://" + neo4JConfiguration.getNeo4jUrl(), AuthTokens.basic(neo4JConfiguration.getNeo4jLogin(), pwd));
		// Test connection
		driver.session().close();
		return driver;
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
        return neo4jRestUrl;
    }

    /**
     * Retrieve a configuration from code
     *
     * @param code Code of the configuration
     * @return The configuration corresponding to the code
     */
	public Neo4JConfiguration findByCode(String code) {
		EntityManager entityManager = emWrapperProvider.get().getEntityManager();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Neo4JConfiguration> query = cb.createQuery(Neo4JConfiguration.class);
		Root<Neo4JConfiguration> root = query.from(Neo4JConfiguration.class);
		query.select(root);
		query.where(cb.equal(root.get("code"), code));
		return entityManager.createQuery(query).getSingleResult();
	}

	public void onNeo4jConnectionCreated(@Observes Neo4JConfiguration entity) {
		configurationMap.remove(entity.getCode());
		DRIVER_MAP.remove(entity.getCode());
	}

	public Neo4JConfiguration getDefaultConfiguration() {
		return defaultConfiguration;
	}
	

}
