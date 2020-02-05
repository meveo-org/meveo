package org.meveo.persistence.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.service.base.BusinessService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomTableCreatorService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.8.0
 * @since 6.6.0
 */
@Stateless
public class SqlConfigurationService extends BusinessService<SqlConfiguration> {

	@Inject
	private SQLConnectionProvider sqlConnectionProvider;

	@Inject
	@Updated
	private Event<SqlConfiguration> sqlConfigurationUpdatedEvent;

	@Inject
	@Created
	private Event<SqlConfiguration> sqlConfigurationCreatedEvent;

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	@Inject
	private CustomTableCreatorService customTableCreatorService;

	@Inject
	private SqlConfigurationService sqlConfigurationService;
	
	/**
	 * Gets the {@link SQLStorageConfiguration#schema}
	 * 
	 * @param sqlConfigurationCode Code of the {@link SqlConfiguration}
	 * @return
	 */
	public String getSchema(String sqlConfigurationCode) {
		String sql = "SELECT schema FROM " + SqlConfiguration.class.getName() + " WHERE code =:code";
		return getEntityManager().createQuery(sql, String.class)
			.setParameter("code", sqlConfigurationCode)
			.getSingleResult();
	}

	@Override
	public void create(SqlConfiguration entity) throws BusinessException {
		setDbSchema(entity);

		sqlConfigurationService.createInNewTx(entity);
		
		if(!entity.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
			sqlConfigurationService.initializeCet(entity);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createInNewTx(SqlConfiguration entity) throws BusinessException {
		if (entity.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION) || testConnection(entity)) {
			super.create(entity);

		} else {
			throw new BusinessException("SqlConfiguration with code " + entity.getCode() + " is not valid.");
		}
	}

	@Override
	public SqlConfiguration update(SqlConfiguration entity) throws BusinessException {
		setDbSchema(entity);

		if (testConnection(entity)) {
			entity = super.update(entity);
			sqlConfigurationUpdatedEvent.fire(entity);

		} else {
			throw new BusinessException("SqlConfiguration with code " + entity.getCode() + " is not valid.");
		}

		return entity;
	}

	/**
	 * Check if the given connection code is valid.
	 * 
	 * @param sqlConfigurationCode the sqlConfiguration stored in the database
	 * @return true if the connection is valid, false otherwise
	 */
	public boolean testConnection(SqlConfiguration sqlConfiguration) {

		boolean result = false;
		Session session = sqlConnectionProvider.testSession(sqlConfiguration);
		if (session != null) {
			result = true;
			session.close();
		}

		return result;
	}

	/**
	 * Check if the given connection code is valid.
	 * 
	 * @param sqlConfigurationCode the sqlConfiguration stored in the database
	 * @return true if the connection is valid, false otherwise
	 */
	public boolean testConnection(String sqlConfigurationCode) {

		boolean result = false;
		Session session = sqlConnectionProvider.getSession(sqlConfigurationCode);
		if (session != null) {
			result = true;
			session.close();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<SqlConfiguration> listActiveAndInitialized() {

		QueryBuilder qb = new QueryBuilder(SqlConfiguration.class, "s");
		qb.addCriterion("initialized", "=", true, false);
		qb.addCriterion("disabled", "=", false, true);

		return qb.getQuery(getEntityManager()).getResultList();
	}

	/**
	 * Initialize the schema of this SqlConfiguration from the default datasource.
	 * 
	 * @param entity target data source
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void initializeCet(SqlConfiguration entity) {

		// commented for now as it requires an admin role for the database user
		// if (entity.getDriverClass().toLowerCase().contains("postgresql")) {
		// customTableCreatorService.executePostgreSqlExtension(entity.getCode());
		// }
		
		boolean initialized = true;

		// get all cet
		List<CustomEntityTemplate> cets = new ArrayList<>(customEntityTemplateService.listNoCache());
		
		// Sort cet by references
		Collections.sort(cets, (c1, c2) -> {
			Map<String, CustomFieldTemplate> cftsC1 = customFieldTemplateService.findByAppliesToNoCache(c1.getAppliesTo());
			boolean c1RefersC2 = cftsC1.values().stream().anyMatch(cft -> c2.getCode().equals(cft.getEntityClazzCetCode()));
			if(c1RefersC2) {
				return 1;
			}
			
			Map<String, CustomFieldTemplate> cftsC2 = customFieldTemplateService.findByAppliesToNoCache(c2.getAppliesTo());
			boolean c2RefersC1 = cftsC2.values().stream().anyMatch(cft -> c1.getCode().equals(cft.getEntityClazzCetCode()));
			if(c2RefersC1) {
				return -1;
			}
			
			return 0;
		});
		
		for (CustomEntityTemplate cet : cets) {
			
			String tableName = SQLStorageConfiguration.getCetDbTablename(cet.getCode());
			customTableCreatorService.createTable(entity.getCode(), tableName);
			Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesToNoCache(cet.getAppliesTo());

			try {
				Thread.sleep(250);
				for (Entry<String, CustomFieldTemplate> cftEntry : cfts.entrySet()) {
					customTableCreatorService.addField(entity.getCode(), tableName, cftEntry.getValue());
					Thread.sleep(250);
				}
			} catch (InterruptedException e) {
				log.error("Interrupted creating table for {}", cet);
			}
		}

		entity = findByCode(entity.getCode());
		entity.setInitialized(initialized);
	}
	
	private void setDbSchema(SqlConfiguration entity) {
		if(!StringUtils.isBlank(entity.getSchema())) {
			if(!entity.getUrl().contains("currentSchema=" + entity.getSchema())) {
				if(entity.getUrl().contains("?")) {
					entity.setUrl(entity.getUrl() + "&currentSchema=" + entity.getSchema());
				} else {
					entity.setUrl(entity.getUrl() + "?currentSchema=" + entity.getSchema());
				}
			}
		}
	}
}
