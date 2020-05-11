/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.admin.listener;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.meveo.admin.exception.BusinessException;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.git.GitRepository;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.RemoteRepository;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.git.GitRepositoryService;
import org.meveo.service.storage.RemoteRepositoryService;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author Clément Bareth
 * @version 6.9.0
 */
@Startup
@Singleton
public class StartupListener {

	@Inject
	private ApplicationInitializer applicationInitializer;

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private SqlConfigurationService sqlConfigurationService;
	
	@Inject
	private GitRepositoryService gitRepositoryService;

	@Inject
	private RemoteRepositoryService remoteRepositoryService;

	@Inject
	private Logger log;

	@Inject
	@MeveoJpa
	private EntityManagerWrapper entityManagerWrapper;

	@PostConstruct
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void init() {
		entityManagerWrapper.getEntityManager().joinTransaction();
		Session session = entityManagerWrapper.getEntityManager().unwrap(Session.class);
		session.doWork(connection -> {
			applicationInitializer.init();
			
			// A default Repository and SQL Configuration should be genarated/updated at Meveo first initialization
			try {
				SqlConfiguration defaultSqlConfiguration;
				Repository defaultRepository;
				defaultSqlConfiguration = sqlConfigurationService.findByCode(SqlConfiguration.DEFAULT_SQL_CONNECTION);
				if (defaultSqlConfiguration == null) {
					defaultSqlConfiguration = new SqlConfiguration();
					defaultSqlConfiguration.setCode(SqlConfiguration.DEFAULT_SQL_CONNECTION);
					setSqlConfiguration(defaultSqlConfiguration);
					sqlConfigurationService.create(defaultSqlConfiguration);
				} else {
					setSqlConfiguration(defaultSqlConfiguration);
					sqlConfigurationService.update(defaultSqlConfiguration);
				}
				defaultRepository = repositoryService.findByCode(Repository.DEFAULT_REPOSITORY);
				if (defaultRepository == null) {
					defaultRepository = new Repository();
					defaultRepository.setCode(Repository.DEFAULT_REPOSITORY);
					defaultRepository.setSqlConfiguration(defaultSqlConfiguration);
					repositoryService.create(defaultRepository);
					log.info("Created default repository");
				}
			} catch (BusinessException e) {
				log.error("Cannot create default repository", e);
			}
			
			// Create Meveo git repository
			GitRepository meveoRepo = gitRepositoryService.findByCode("Meveo");
			if(meveoRepo == null) {
				try {
					gitRepositoryService.create(
						GitRepositoryService.MEVEO_DIR, 
						false,
						GitRepositoryService.MEVEO_DIR.getDefaultRemoteUsername(),
						GitRepositoryService.MEVEO_DIR.getDefaultRemotePassword()
					);
					
					log.info("Created Meveo GIT repository");
					
					
				} catch (BusinessException e) {
					log.error("Cannot create Meveo GIT repository", e);
				}
				
			} else {
				log.info("Meveo GIT repository already created");
				try {
					GitRepositoryService.MEVEO_DIR = meveoRepo;
					gitRepositoryService.createGitMeveoFolder(meveoRepo);
				} catch (BusinessException e) {
					log.error("Cannot create Meveo Git folder", e);
				}
			}

			try {
				// Create default maven repository
				RemoteRepository remoteRepository = remoteRepositoryService.findByCode("maven central");
				if (remoteRepository == null) {
					remoteRepository = new RemoteRepository();
					remoteRepository.setCode("maven central");
					remoteRepository.setUrl("https://repo1.maven.org/maven2/");
					remoteRepositoryService.create(remoteRepository);
					log.info("Created default maven repository");
				}
			} catch (BusinessException e) {
				log.error("Cannot create default maven repository", e);
			}
			
			log.info("Thank you for running Meveo Community code.");
		});
		
		session.flush();	
	}

	private SqlConfiguration setSqlConfiguration(SqlConfiguration sqlConfiguration) {
		sqlConfiguration.setDriverClass("MeveoAdmin driver class");
		sqlConfiguration.setUrl("MeveoAdmin jdbc url");
		sqlConfiguration.setUsername("MeveoAdmin username");
		sqlConfiguration.setPassword("MeveoAdmin password");
		sqlConfiguration.setDialect("MeveoAdmin dialect");
		sqlConfiguration.setInitialized(true);
		
		return sqlConfiguration;
	}

}
