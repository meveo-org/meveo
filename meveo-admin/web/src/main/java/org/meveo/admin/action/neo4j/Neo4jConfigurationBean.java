package org.meveo.admin.action.neo4j;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.action.BaseBean;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.persistence.neo4j.base.Neo4jConnectionProvider;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.neo4j.Neo4jConfigurationService;

/**
 * Controller for managing {@link Neo4JConfiguration} CRUD operations.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 */
@Named
@ViewScoped
public class Neo4jConfigurationBean extends BaseBean<Neo4JConfiguration> {

	private static final long serialVersionUID = -7370952817444175628L;

	@Inject
	private Neo4jConfigurationService neo4jConfigurationService;
	
	@Inject
	private Neo4jConnectionProvider neo4jConnectionProvider;

	public Neo4jConfigurationBean() {
		super(Neo4JConfiguration.class);
	}

	@Override
	protected IPersistenceService<Neo4JConfiguration> getPersistenceService() {
		return neo4jConfigurationService;
	}

	@Override
	protected String getListViewName() {
		return "neo4jConfigurations";
	}

	@Override
	public String getEditViewName() {
		return "neo4jConfigurationDetail";
	}
	
	/**
	 * Test connection to database
	 */
	public void execute() {
		try {
			if(!entity.isTransient()) {
				var conn = neo4jConnectionProvider.getSession(entity.getCode());
				if(conn != null) {
					conn.close();
					messages.info("Connection success");
				} else {
					messages.error("Can't connect to database");
				}
			} else {
				messages.error("Can't test connection in creation mode");
			}
		} catch (Exception e) {
			messages.error("Can't connect to database:", e.getMessage());
		}
	}
}
