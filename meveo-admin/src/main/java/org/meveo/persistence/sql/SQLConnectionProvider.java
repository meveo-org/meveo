package org.meveo.persistence.sql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.qualifier.Updated;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.security.PasswordUtils;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since 6.6.0
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SQLConnectionProvider {

	@Inject
	@MeveoJpa
	private Provider<EntityManagerWrapper> emWrapperProvider;

	@PersistenceUnit(unitName = "MeveoAdmin")
	private EntityManagerFactory emf;

	@Inject
	private Logger log;

	private SqlConfiguration defaultSqlConfiguration = new SqlConfiguration();
	private static final Map<String, SqlConfiguration> configurationMap = new ConcurrentHashMap<>();
	private static final Map<String, SessionFactory> SESSION_FACTORY_MAP = new ConcurrentHashMap<>();

	@PostConstruct
	public void loadConfig() {

		defaultSqlConfiguration.setCode(SqlConfiguration.DEFAULT_SQL_CONNECTION);
	}

	public SqlConfiguration getSqlConfiguration(String sqlConfigurationCode) {

		SqlConfiguration sqlConfiguration = defaultSqlConfiguration;
		if (StringUtils.isNotBlank(sqlConfigurationCode)) {
			try {
				sqlConfiguration = configurationMap.computeIfAbsent(sqlConfigurationCode, this::findByCode);

			} catch (NoResultException e) {
				log.warn("Unknown SQL repository {}, using default configuration", sqlConfigurationCode);
			}
		}

		return sqlConfiguration;
	}

	public org.hibernate.Session getSession(String sqlConfigurationCode) {
		
		if(sqlConfigurationCode == null) {
			throw new NullPointerException("sqlConfigurationCode is null");
		}

		SqlConfiguration sqlConfiguration = getSqlConfiguration(sqlConfigurationCode);

		try {
			SessionFactory sessionFactory = SESSION_FACTORY_MAP.computeIfAbsent(sqlConfigurationCode, this::buildSessionFactory);
			synchronized (this) {
				return sessionFactory.openSession();
			}

		} catch (Exception e) {
			log.warn("Can't connect to sql configuration with code={}, url={}, error={}", sqlConfigurationCode, sqlConfiguration.getUrl(), e.getCause());
			return null;
		}
	}
	
	public EntityManager getEntityManager(String sqlConfigurationCode) {

		SqlConfiguration sqlConfiguration = getSqlConfiguration(sqlConfigurationCode);

		try {
			SessionFactory sessionFactory = SESSION_FACTORY_MAP.computeIfAbsent(sqlConfigurationCode, this::buildSessionFactory);
			synchronized (this) {
				return sessionFactory.createEntityManager();
			}

		} catch (Exception e) {
			log.warn("Can't connect to sql configuration with code={}, url={}, error={}", sqlConfigurationCode, sqlConfiguration.getUrl(), e.getCause());
			return null;
		}
	}

	public boolean testSession(SqlConfiguration sqlConfiguration) {
		Session session = null;
		try {
			SessionFactory sessionFactory = buildSessionFactory(sqlConfiguration);
			synchronized (this) {
				return (sessionFactory.openSession() != null);
			}

		} catch (Exception e) {
			log.warn("Can't connect to sql configuration with code={}, url={}, error={}", sqlConfiguration.getCode(), sqlConfiguration.getUrl(), e.getCause());
			return false;
		} finally {
			try {
				if (session != null)
					session.close();
			} catch (JMSException ignored) {}
		}
	}

	public synchronized SessionFactory buildSessionFactory(String sqlConfigurationCode) {

		SqlConfiguration sqlConfiguration = getSqlConfiguration(sqlConfigurationCode);

		return buildSessionFactory(sqlConfiguration);
	}

	public synchronized SessionFactory buildSessionFactory(SqlConfiguration sqlConfiguration) {

		// Return the SessionFactory initialized by wildfly in case of using default configuration
		if(sqlConfiguration.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
			return (SessionFactory) emf; 

		} else {
			Configuration config = new Configuration();
			
			if (sqlConfiguration.getUrl()!= null && sqlConfiguration.getUrl().startsWith("java:")) {
				config.setProperty(org.hibernate.cfg.AvailableSettings.DATASOURCE, sqlConfiguration.getUrl());
				config.setProperty( org.hibernate.cfg.AvailableSettings.JPA_TRANSACTION_TYPE, "RESOURCE_LOCAL");
				//config.setProperty( org.hibernate.cfg.AvailableSettings.CONNECTION_PROVIDER, "org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl");
			} else {
				config.setProperty("hibernate.connection.driver_class", sqlConfiguration.getDriverClass());
				config.setProperty("hibernate.connection.url", sqlConfiguration.getUrl());
				config.setProperty("hibernate.connection.username", sqlConfiguration.getUsername());
				if(sqlConfiguration.getClearPassword() == null) {
					String salt = PasswordUtils.getSalt(sqlConfiguration.getCode(), sqlConfiguration.getUrl());
					var clearPwd = PasswordUtils.decrypt(salt, sqlConfiguration.getPassword());
					config.setProperty("hibernate.connection.password", clearPwd);
				} else {
					config.setProperty("hibernate.connection.password", sqlConfiguration.getClearPassword());
				}				
			}
			if (log.isTraceEnabled())
				config.setProperty("hibernate.generate_statistics", "true");
			else
				config.setProperty("hibernate.generate_statistics", "false");
			config.setProperty("hibernate.jmx.enabled", "true");
	
			
			
			if(!StringUtils.isBlank(sqlConfiguration.getSchema())) {
				config.setProperty("hibernate.default_schema", sqlConfiguration.getSchema());
			}

			if (StringUtils.isNotBlank(sqlConfiguration.getDialect())) {
				config.setProperty("hibernate.dialect", sqlConfiguration.getDialect());
			}

			return config.buildSessionFactory();
		}
	}

	public SqlConfiguration findByCode(String code) {

		EntityManager entityManager = emWrapperProvider.get().getEntityManager();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<SqlConfiguration> query = cb.createQuery(SqlConfiguration.class);
		Root<SqlConfiguration> root = query.from(SqlConfiguration.class);
		query.select(root);
		query.where(cb.equal(root.get("code"), code));

		return entityManager.createQuery(query).getSingleResult();
	}

	public void onSqlConnectionUpdated(@Observes @Updated SqlConfiguration entity) {

		configurationMap.put(entity.getCode(), entity);

		SessionFactory oldSessionFactory = SESSION_FACTORY_MAP.get(entity.getCode());
		if (!entity.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION) && oldSessionFactory != null && oldSessionFactory.isOpen()) {
			oldSessionFactory.close();
		}

		SESSION_FACTORY_MAP.remove(entity.getCode());
	}


	public SqlConfiguration getDefaultSqlConfiguration() {
		return defaultSqlConfiguration;
	}

	public void setDefaultSqlConfiguration(SqlConfiguration defaultSqlConfiguration) {
		this.defaultSqlConfiguration = defaultSqlConfiguration;
	}
}
