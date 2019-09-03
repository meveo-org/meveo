package org.meveo.admin.action.neo4j;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.action.BaseBean;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.neo4j.Neo4jConfigurationService;

/**
 * Controller for managing {@link Neo4JConfiguration} CRUD operations.
 * 
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
@Named
@ViewScoped
public class Neo4jConfigurationBean extends BaseBean<Neo4JConfiguration> {

	private static final long serialVersionUID = -7370952817444175628L;

	@Inject
	private Neo4jConfigurationService neo4jConfigurationService;

	public Neo4jConfigurationBean() {
		super(Neo4JConfiguration.class);
	}

	@Override
	protected IPersistenceService<Neo4JConfiguration> getPersistenceService() {
		return neo4jConfigurationService;
	}

}
