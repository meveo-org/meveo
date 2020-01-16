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
import org.meveo.commons.utils.ParamBean;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.sql.SQLConnectionProvider;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author Clément Bareth
 * @version 6.7.0
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
				defaultSqlConfiguration = sqlConfigurationService.findByCode("default");
				if (defaultSqlConfiguration == null) {
					defaultSqlConfiguration = new SqlConfiguration();
					defaultSqlConfiguration.setCode("default");
					setSqlConfiguration(defaultSqlConfiguration);
					sqlConfigurationService.create(defaultSqlConfiguration);
				} else {
					setSqlConfiguration(defaultSqlConfiguration);
					sqlConfigurationService.update(defaultSqlConfiguration);
				}
				defaultRepository = repositoryService.findByCode("default");
				if (defaultRepository == null) {
					defaultRepository = new Repository();
					defaultRepository.setCode("default");
					defaultRepository.setSqlConfiguration(defaultSqlConfiguration);
					repositoryService.create(defaultRepository);
				}
			} catch (BusinessException e) {
				log.error("Cannot create default repository", e);
			}
			
			log.info("Created default repository");
			
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
