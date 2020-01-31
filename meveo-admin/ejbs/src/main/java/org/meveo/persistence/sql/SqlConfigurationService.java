package org.meveo.persistence.sql;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

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
 * @version 6.7.0
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

	@Override
	public void create(SqlConfiguration entity) throws BusinessException {
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

		// get all cet
		List<CustomEntityTemplate> cets = customEntityTemplateService.listActive();
		for (CustomEntityTemplate cet : cets) {
			String tableName = SQLStorageConfiguration.getCetDbTablename(cet.getCode());
			customTableCreatorService.createTable(entity.getCode(), tableName);
			Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

			for (Entry<String, CustomFieldTemplate> cftEntry : cfts.entrySet()) {
				customTableCreatorService.addField(entity.getCode(), tableName, cftEntry.getValue());
			}
		}

		entity = findByCode(entity.getCode());
		entity.setInitialized(true);
	}
}
