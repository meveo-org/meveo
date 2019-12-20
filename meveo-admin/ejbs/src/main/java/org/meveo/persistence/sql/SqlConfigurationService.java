package org.meveo.persistence.sql;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.hibernate.Session;
import org.meveo.admin.exception.BusinessException;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.service.base.BusinessService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 * @since 6.6.0
 */
@Stateless
public class SqlConfigurationService extends BusinessService<SqlConfiguration> {

	@Inject
	private SQLConnectionProvider sqlConnectionProvider;

	@Inject
	@Updated
	private Event<SqlConfiguration> sqlConfigurationUpdatedEvent;

	@Override
	public void create(SqlConfiguration entity) throws BusinessException {

		if (testConnection(entity)) {
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
}
