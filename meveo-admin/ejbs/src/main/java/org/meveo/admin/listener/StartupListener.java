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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.ParamBean;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.git.GitRepository;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.RemoteRepository;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.config.impl.MavenConfigurationService;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
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
	
	@Inject
	private MavenConfigurationService mavenConfigurationService;
	
	@Inject
	private GitClient gitClient;
	
	@Inject
	@CurrentUser
	private Instance<MeveoUser> appInitUser;
	
    @Inject
    private Instance<MeveoInitializer> initializers;
    
	@SuppressWarnings("unchecked")
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
			if (meveoRepo == null) {
				try {
					meveoRepo = gitRepositoryService.create(GitRepositoryService.MEVEO_DIR,
							false,
							GitRepositoryService.MEVEO_DIR.getDefaultRemoteUsername(),
							GitRepositoryService.MEVEO_DIR.getClearDefaultRemotePassword());

					log.info("Created Meveo GIT repository");

				} catch (BusinessException e) {
					log.error("Cannot create Meveo GIT repository", e);
				}

			} else {
				log.info("Meveo GIT repository already created");
				try {
					GitRepositoryService.MEVEO_DIR = meveoRepo;
					gitClient.create(GitRepositoryService.MEVEO_DIR, //
							false, //
							GitRepositoryService.MEVEO_DIR.getDefaultRemoteUsername(), //
							GitRepositoryService.MEVEO_DIR.getDefaultRemotePassword());
				} catch (BusinessException e) {
					log.error("Cannot create Meveo Git folder", e);
				}
			}
			
			// Generate .gitignore file
			List<String> ignoredFiles = List.of(
					".classpath",
					".project",
					".settings/*",
					".vscode/*",
					"target/*"
			);
			
			File gitRepo = GitHelper.getRepositoryDir(appInitUser.get(), meveoRepo.getCode());
			File gitIgnoreFile = new File(gitRepo, ".gitignore");
			
			try {
				List<String> actualIgnoredFiles = gitIgnoreFile.exists() ? Files.readAllLines(gitIgnoreFile.toPath()) : List.of();
				Collection<String> missingEntries = CollectionUtils.subtract(ignoredFiles, actualIgnoredFiles);
				try (
						FileWriter fw = new FileWriter(gitIgnoreFile, true);
						BufferedWriter output = new BufferedWriter(fw);
					) {
					for(String missingEntry : missingEntries) {
						output.append(missingEntry);
						output.newLine();
					}
				}
				gitClient.commitFiles(meveoRepo, List.of(gitIgnoreFile), "Update .gitignore");
			} catch (IOException e1) {
				log.error("Can't read / write .gitignore file");
			} catch (BusinessException e) {
				log.error("Can't commit .gitignore file", e);
			}

			// Create default pom file
			mavenConfigurationService.createDefaultPomFile(meveoRepo);

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
		
		try {
			// Set-up secret key
			String secret = System.getProperty("meveo.security.secret");
			if(secret == null) {
				var paramBean = ParamBean.getInstance("meveo-security.properties");
				secret = paramBean.getProperty("meveo.security.secret", null);
				if(secret == null) {
					KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
					SecureRandom secureRandom = new SecureRandom();
					int keyBitSize = 256;
					keyGenerator.init(keyBitSize, secureRandom);
					SecretKey secretKey = keyGenerator.generateKey();
					byte[] encodedKey = Base64.getEncoder().encode(secretKey.getEncoded());
					var randomSecret = 	new String(encodedKey, StandardCharsets.UTF_8);
					paramBean.setProperty("meveo.security.secret", randomSecret);
					paramBean.saveProperties();
					secret = randomSecret;
				}
				System.setProperty("meveo.security.secret", secret);
			}
			
		} catch (NoSuchAlgorithmException e1) {
			throw new RuntimeException(e1);
		}
				
	    for(MeveoInitializer initializer : initializers) {
	    	try {
	    		initializer.init();
	    	} catch (Exception e) {
	    		log.error("Error during execution of {}", initializer.getClass(), e);
	    	}
	    }
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
