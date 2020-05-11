package org.meveo.admin.action.storage;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseCrudBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.sql.SqlConfigurationDto;
import org.meveo.api.sql.SqlConfigurationApi;
import org.meveo.elresolver.ELException;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.persistence.sql.SQLConnectionProvider;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.base.local.IPersistenceService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 * @since 6.6.0
 */
@Named
@ViewScoped
public class SqlConfigurationBean extends BaseCrudBean<SqlConfiguration, SqlConfigurationDto> {

	private static final long serialVersionUID = 1L;

	@Inject
	private SqlConfigurationApi sqlConfigurationApi;

	@Inject
	private SqlConfigurationService sqlConfigurationService;
	
	@Inject
	private SQLConnectionProvider provider;

	public SqlConfigurationBean() {
		super(SqlConfiguration.class);
	}

	@Override
	public BaseCrudApi<SqlConfiguration, SqlConfigurationDto> getBaseCrudApi() {
		return sqlConfigurationApi;
	}

	@Override
	protected IPersistenceService<SqlConfiguration> getPersistenceService() {
		return sqlConfigurationService;
	}

	@Override
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

		if (testConnection()) {
			return super.saveOrUpdate(killConversation);
		}

		return null;
	}

	public boolean testConnection() {

		if (provider.testSession(entity) ) {
			messages.info(new BundleKey("messages", "sqlConfiguration.connection.ok"));

		} else {
			messages.error(new BundleKey("messages", "sqlConfiguration.connection.ko"));
			return false;
		}

		return true;
	}
	
	public void initializeSchema() {
		sqlConfigurationService.initializeCet(entity);
	}
}
