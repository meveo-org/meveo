/**
 * 
 */
package org.meveo.admin.patches.v6_12_0;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.meveo.admin.patches.Patch;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.communication.MeveoInstance;
import org.meveo.model.git.GitRepository;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.communication.impl.MeveoInstanceService;
import org.meveo.service.git.GitRepositoryService;
import org.meveo.service.neo4j.Neo4jConfigurationService;

/**
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
public class EncryptPasswords implements Patch {
	
	@Inject
	private MeveoInstanceService meveoInstanceService;
	
	@Inject
	private Neo4jConfigurationService neo4jConfigurationService;
	
	@Inject
	private SqlConfigurationService sqlConfigurationService;
	
	@Inject
	private GitRepositoryService gitRepositoryService;
	
	@Override
	public void execute() throws Exception {
		for(MeveoInstance meveoInstance : meveoInstanceService.list()) {
			meveoInstance.setClearPassword(meveoInstance.getAuthPassword());
			meveoInstanceService.update(meveoInstance);
		}
		
		for(Neo4JConfiguration neo4jConf : neo4jConfigurationService.list()) {
			neo4jConf.setClearPassword(neo4jConf.getNeo4jPassword());
			neo4jConfigurationService.update(neo4jConf);
		}
		
		for(SqlConfiguration sqlService : sqlConfigurationService.list()) {
			sqlService.setClearPassword(sqlService.getPassword());
			sqlConfigurationService.update(sqlService);
		}
		
		for(GitRepository gitRepo : gitRepositoryService.list()) {
			if(gitRepo.getDefaultRemoteUsername() != null) {
				gitRepo.setClearDefaultRemotePassword(gitRepo.getDefaultRemotePassword());
				gitRepositoryService.update(gitRepo);
			}
		}
	}

	@Override
	public int order() {
		return 1;
	}

	@Override
	public String name() {
		return "EncryptPasswords";
	}
}
