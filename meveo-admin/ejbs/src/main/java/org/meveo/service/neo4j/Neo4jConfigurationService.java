package org.meveo.service.neo4j;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.service.base.BusinessService;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
@Stateless
public class Neo4jConfigurationService extends BusinessService<Neo4JConfiguration> {

	@Inject
	@Updated
	private Event<Neo4JConfiguration> neo4jConfigurationUpdatedEvent;

	@Override
	public Neo4JConfiguration update(Neo4JConfiguration entity) throws BusinessException {

		entity = super.update(entity);
		neo4jConfigurationUpdatedEvent.fire(entity);
		
		return entity;
	}
}
