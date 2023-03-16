package org.meveo.service.neo4j;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.persistence.neo4j.base.Neo4jConnectionProvider;
import org.meveo.service.base.BusinessService;
import org.neo4j.driver.Session;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
@Stateless
public class Neo4jConfigurationService extends BusinessService<Neo4JConfiguration> {

	@Inject
	@Updated
	private Event<Neo4JConfiguration> neo4jConfigurationUpdatedEvent;
	
	@Inject
	private Neo4jConnectionProvider neo4jConnectionProvider;
	
	@Override
	public void create(Neo4JConfiguration entity) throws BusinessException {
		try {
			// Retrieve version number
			Session session = neo4jConnectionProvider.getSession(entity);
			String dbVersion = session.readTransaction(tx -> tx.run("call dbms.components() yield versions unwind versions as version return version").single().get(0).asString());
			entity.setDbVersion(dbVersion);
		} catch (Exception e) {
			throw new BusinessException("Cannot determine db version", e);
		}
		
		super.create(entity);
	}

	@Override
	public Neo4JConfiguration update(Neo4JConfiguration entity) throws BusinessException {
		try {
			// Retrieve version number
			Session session = neo4jConnectionProvider.getSession(entity);
			String dbVersion = session.readTransaction(tx -> tx.run("call dbms.components() yield versions unwind versions as version return version").single().get(0).asString());
			entity.setDbVersion(dbVersion);
		} catch (Exception e) {
			throw new BusinessException("Cannot determine db version", e);
		}
		
		entity = super.update(entity);
		neo4jConfigurationUpdatedEvent.fire(entity);
		
		return entity;
	}
}
