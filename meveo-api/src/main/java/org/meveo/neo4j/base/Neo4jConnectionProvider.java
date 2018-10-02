package org.meveo.neo4j.base;

import org.meveo.commons.utils.ParamBean;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * @author Rachid
 */
@Startup
@Singleton
public class Neo4jConnectionProvider {

    private String neo4jUrl;
    private String neo4jLogin;
    private String neo4jPassword;
    private Integer Neo4jRestPort;

    @PostConstruct
    public void loadConfig() {
        neo4jUrl = ParamBean.getInstance().getProperty("neo4j.host", "localhost");
        Neo4jRestPort = Integer.valueOf(ParamBean.getInstance().getProperty("neo4j.rest.port", "7474"));
        neo4jLogin = ParamBean.getInstance().getProperty("neo4j.login", "neo4j");
        neo4jPassword = ParamBean.getInstance().getProperty("neo4j.password", "meveo");
    }

    public Session getSession() {
        Driver driver = GraphDatabase.driver("bolt://" + neo4jUrl, AuthTokens.basic(neo4jLogin, neo4jPassword));
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
}
