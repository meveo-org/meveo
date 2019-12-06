package org.meveo.service.config.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.UserTransaction;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.commons.utils.ParamBean;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.git.GitRepository;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.security.keycloak.MeveoUserKeyCloakImpl;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;
import org.meveo.util.Version;
import org.slf4j.Logger;

/**
 * Manage the maven configuration.
 * 
 * <pre>
 * mavenExecutablePath - path to the local maven executable
 * m2FolderPath - path to the local .m2 folder
 * mavenRepositories - list of maven repositories
 * </pre>
 * 
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class MavenConfigurationService implements Serializable {

	private static final long serialVersionUID = 7814020640577283116L;

	private static List<MavenDependency> createdBuffer = new ArrayList<>();
	private static List<MavenDependency> updatedBuffer = new ArrayList<>();
	private static List<MavenDependency> deletedBuffer = new ArrayList<>();
	
	@Resource
	private UserTransaction transaction;

	@Inject
	@MeveoRepository
	private Instance<GitRepository> meveoRepository;
	
	@Inject
	@CurrentUser
	private Instance<MeveoUser> currentUser;
	
	@Inject
	@MeveoJpa
	private EntityManagerWrapper emWrapper;
	
    @PersistenceContext(unitName = "MeveoAdmin", type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;

	@Inject
	private GitClient gitClient;

	@Inject
	private Logger log;
	
	@Resource
	private TimerService timerService;
	
	private javax.ejb.Timer ejbTimer;
	
	public void onDependencyCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created MavenDependency d) {
		createdBuffer.add(d);
		schedulePomGeneration();
	}

	public void onDependencyUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated MavenDependency d) {
		if (!createdBuffer.contains(d)) {
			updatedBuffer.add(d);
		}
		schedulePomGeneration();
	}
	
	public void onDependencyRemoved(@Observes(during = TransactionPhase.AFTER_COMPLETION) @Removed MavenDependency d) {
		createdBuffer.remove(d);
		updatedBuffer.remove(d);
		deletedBuffer.add(d);
		schedulePomGeneration();
	}

	private void schedulePomGeneration() {
		
		if(ejbTimer != null) {
			try {
				ejbTimer.cancel();
			} catch(NoSuchObjectLocalException e) {
				// Timer has expired
			}
		}
		
		TimerConfig timeconfig = new TimerConfig();
		
		MeveoUser meveoUser = currentUser.get();
		String serializedUser = JacksonUtil.toString(meveoUser.unProxy());

		timeconfig.setInfo(serializedUser);
		timeconfig.setPersistent(true);
		ejbTimer = timerService.createSingleActionTimer(100, timeconfig);
		
	}
	
	@Timeout
	public void generatePom(javax.ejb.Timer t) {
		if(t.getInfo() instanceof String) {
			MeveoUser user;

			try {
				user = JacksonUtil.fromString((String) t.getInfo(), MeveoUserKeyCloakImpl.class);
			} catch(Exception e) {
				log.debug("Can't parse string to MeveoUser", e);
				return;
			}
			
			List<String> lines = new ArrayList<>();
	
			createdBuffer.forEach(d -> lines.add("Add dependency " + d.getCoordinates()));
			updatedBuffer.forEach(d -> lines.add("Update dependency " + d.getCoordinates()));
			deletedBuffer.forEach(d -> lines.add("Delete dependency " + d.getCoordinates()));
	
			StringBuilder message = new StringBuilder();
	
			// If commit will only contain one change on dependencies, this change will be the header of the commit.
			// If commit will contains many changes on dependencies, then the header is generic and all changes are detailed in the body.
			if(lines.size() == 1) {
				message.append(lines.get(0));
			} else {
				message.append("Update pom.xml (").append(lines.size()).append(" modifications) \n");
				lines.forEach(l -> message.append("\n").append(l));
			}
	
			createdBuffer.clear();
			updatedBuffer.clear();
			deletedBuffer.clear();
			
			log.info("User passed to the timer : " + user);
			
			MeveoUser meveoUser = currentUser.get();
			log.info("User in timer : " + meveoUser);
			meveoUser.loadUser(user);
			
			log.info("User in timer (2) : " + currentUser.get());
			generatePom(message.toString(), meveoRepository.get());
		}
	}

	private void generatePom(String message, GitRepository repository) {
		log.debug("Generating pom.xml file");
		
		Model model = new Model();
		model.setGroupId("org.meveo");
		model.setArtifactId("meveo-application");
		model.setVersion("1.0.0");

		Dependency meveoDependency = new Dependency();
		meveoDependency.setGroupId("org.meveo");
		meveoDependency.setArtifactId("meveo-api");
		meveoDependency.setVersion(Version.appVersion);
		meveoDependency.setScope("provided");
		model.addDependency(meveoDependency);

		try {
			
			transaction.begin();
			entityManager.createQuery("SELECT d FROM MavenDependency d", MavenDependency.class)
					.getResultStream()
					.forEach(mavenDependency -> {
						Dependency dependency = new Dependency();
						dependency.setGroupId(mavenDependency.getGroupId());
						dependency.setArtifactId(mavenDependency.getArtifactId());
						dependency.setVersion(mavenDependency.getVersion());
						dependency.setScope("provided");
						model.addDependency(dependency);
					});
			transaction.commit();
			
		} catch (Exception e) {
			log.error("Error retrieving maven dependencies", e);
		}

		final File repositoryDir = GitHelper.getRepositoryDir(null, repository.getCode());
		File pomFile = new File(repositoryDir, "pom.xml");

		try {
			MavenXpp3Writer xmlWriter = new MavenXpp3Writer();
			FileWriter fileWriter = new FileWriter(pomFile);
			xmlWriter.write(fileWriter, model);

			gitClient.commitFiles(repository, Collections.singletonList(pomFile), message);

		} catch (IOException e) {
			log.error("Can't write to pom.xml", e);
		} catch (BusinessException e) {
			log.error("Can't commit pom.xml file", e);
		}

	}

	public String getM2FolderPath() {
		return ParamBean.getInstance().getProperty("maven.path.m2", null);
	}

	public void setM2FolderPath(String m2FolderPath) {
		ParamBean.getInstance().setProperty("maven.path.m2", m2FolderPath);
	}

	public List<String> getMavenRepositories() {
		return ParamBean.getInstance().getListProperty("maven.path.repositories", new ArrayList<String>());
	}

	public void setMavenRepositories(List<String> mavenRepositories) {
		ParamBean.getInstance().setListProperty("maven.path.repositories", mavenRepositories);
	}

	public void saveConfiguration() {
		ParamBean.getInstance().saveProperties();
	}

	public void saveConfiguration(MavenConfigurationDto mavenConfiguration) {

		setM2FolderPath(mavenConfiguration.getM2FolderPath());
		setMavenRepositories(mavenConfiguration.getMavenRepositories());

		saveConfiguration();
	}

	public MavenConfigurationDto loadConfig() {

		MavenConfigurationDto result = new MavenConfigurationDto();
		result.setM2FolderPath(getM2FolderPath());
		result.setMavenRepositories(getMavenRepositories());

		return result;
	}

	public RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
		locator.addService( TransporterFactory.class, FileTransporterFactory.class );
		locator.addService( TransporterFactory.class, HttpTransporterFactory.class );
		return locator.getService( RepositorySystem.class );
	}

	public RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository( getM2FolderPath() );
		session.setLocalRepositoryManager(system.newLocalRepositoryManager( session, localRepo ) );

		return session;
	}
}
