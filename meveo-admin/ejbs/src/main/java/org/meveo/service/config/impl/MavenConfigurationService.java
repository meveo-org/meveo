package org.meveo.service.config.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Updated;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.git.GitRepository;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.storage.RemoteRepository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.security.keycloak.CurrentUserProvider;
import org.meveo.security.keycloak.MeveoUserKeyCloakImpl;
import org.meveo.service.aether.ConsoleRepositoryListener;
import org.meveo.service.aether.ManualRepositorySystemFactory;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.GitRepositoryService;
import org.meveo.service.git.MeveoRepository;
import org.meveo.service.script.MavenDependencyService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.storage.RemoteRepositoryService;
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
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @since 6.5.0
 * @version 6.9.0
 */
@Singleton
public class MavenConfigurationService implements Serializable {

	private static final long serialVersionUID = 7814020640577283116L;
    private final static String M2_DIR = "/.m2";

	private static Map<String, ChangeBuffer> buffers = new HashMap<>();

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
	private GitRepositoryService gitRepositoryService;

	@Inject
	private Logger log;

	@Inject
	private RemoteRepositoryService remoteRepositoryService;

	@Resource
	private TimerService timerService;
	
	@Inject
	private CurrentUserProvider currentUserProvider;
	
	@Inject
	private MavenDependencyService mavenDependencyService;
	
	@Inject
	private ScriptInstanceService scriptInstanceService;

	private javax.ejb.Timer ejbTimer;

	public static final AtomicReference<String> CLASSPATH_REFERENCE = new AtomicReference<>("");

    /**
     * @param currentUser Logged user
     * @return the maven directory relative to the file explorer directory for the
     *         user's provider
     */
    public static String getM2Directory(MeveoUser currentUser) {
        String rootDir = ParamBean.getInstance().getChrootDir(currentUser.getProviderCode());
        String m2 = rootDir + M2_DIR;
        File m2Folder = new File(m2);
        if (!m2Folder.exists()) {
            m2Folder.mkdir();
        }
        return m2;
    }

	public void onDependencyCreated(@Observes @Created MavenDependency d) {
		d.getScriptInstances()
			.stream()
				.map(scriptInstanceService::findModuleOf)
				.forEach(module -> {
					buffers.computeIfAbsent(module.getCode(), key -> new ChangeBuffer())
						.getCreatedBuffer()
						.add(d);
					schedulePomGeneration();
				});
	}

	public void onDependencyUpdated(@Observes @Updated MavenDependency d) {
		d.getScriptInstances()
			.stream()
				.map(scriptInstanceService::findModuleOf)
				.forEach(module -> {
					buffers.computeIfAbsent(module.getCode(), key -> new ChangeBuffer())
						.getUpdatedBuffer()
						.add(d);
					schedulePomGeneration();
				});
	}

	public void onDependencyRemoved(MavenDependency d, ScriptInstance script) {
		Optional.ofNullable(script)
			.map(scriptInstanceService::findModuleOf)
			.filter(Objects::nonNull)
			.ifPresent(module -> {
				buffers.computeIfAbsent(module.getCode(), key -> new ChangeBuffer())
					.getUpdatedBuffer()
					.remove(d);
				buffers.computeIfAbsent(module.getCode(), key -> new ChangeBuffer())
					.getCreatedBuffer()
					.remove(d);
				buffers.computeIfAbsent(module.getCode(), key -> new ChangeBuffer())
					.getDeletedBuffer()
					.add(d);
				schedulePomGeneration();
			});
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
			
			buffers.forEach((moduleCode, buffer) -> {
				
				List<String> lines = new ArrayList<>();

				buffer.getCreatedBuffer().forEach(d -> lines.add("Add dependency " + d.getBuiltCoordinates()));
				buffer.getUpdatedBuffer().forEach(d -> lines.add("Update dependency " + d.getBuiltCoordinates()));
				buffer.getDeletedBuffer().forEach(d -> lines.add("Delete dependency " + d.getBuiltCoordinates()));

				StringBuilder message = new StringBuilder();

				// If commit will only contain one change on dependencies, this change will be the header of the commit.
				// If commit will contains many changes on dependencies, then the header is generic and all changes are detailed in the body.
				if (lines.size() == 1) {
					message.append(lines.get(0));
				} else {
					message.append("Update pom.xml (").append(lines.size()).append(" modifications) \n");
					lines.forEach(l -> message.append("\n").append(l));
				}

				buffer.getCreatedBuffer().clear();
				buffer.getUpdatedBuffer().clear();
				buffer.getDeletedBuffer().clear();

				currentUserProvider.reestablishAuthentication(user);

				generatePom(message.toString(), moduleCode);
			});
		}
	}

	private void generatePom(String message, String repositoryCode) {
		
		GitRepository repository = gitRepositoryService.findByCode(repositoryCode);
		
		log.debug("Generating pom.xml file");

		Model model = new Model();
		model.setGroupId("org.meveo");//TODO: Add group id to module
		model.setArtifactId(repositoryCode);
		model.setVersion("1.0.0");
		model.setModelVersion("4.0.0");
		
		model.setBuild(new Build());
		model.getBuild().setSourceDirectory("../java");
		
		Properties properties = new Properties();
		properties.setProperty("maven.compiler.target", "11");
		properties.setProperty("maven.compiler.source", "11");
		model.setProperties(properties);

		List<RemoteRepository> remoteRepositories = remoteRepositoryService.list();
		if (CollectionUtils.isNotEmpty(remoteRepositories)) {
			for (RemoteRepository remoteRepository : remoteRepositories) {
				Repository repositoryMaven = new Repository();
				repositoryMaven.setId(remoteRepository.getCode());
				repositoryMaven.setUrl(remoteRepository.getUrl());
				model.addRepository(repositoryMaven);
			}
		}

		Repository ownInstance = new Repository();
		// String contextRoot = ParamBean.getInstance().getProperty("meveo.moduleName", "meveo");
		String baseUrl = ParamBean.getInstance().getProperty("meveo.admin.baseUrl", "http://localhost:8080/meveo");
		ownInstance.setId("meveo-repo");
		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		//ownInstance.setUrl(baseUrl + contextRoot + "/maven");
		ownInstance.setUrl(baseUrl + "/maven");
		model.addRepository(ownInstance);

		Dependency meveoDependency = new Dependency();
		meveoDependency.setGroupId("org.meveo");
		meveoDependency.setArtifactId("meveo-api");
		meveoDependency.setVersion(Version.appVersion);
		meveoDependency.setScope("provided");
		model.addDependency(meveoDependency);

		try {

			mavenDependencyService.findModuleDependencies(repositoryCode)
					.forEach(mavenDependency -> {
						Dependency dependency = new Dependency();
						dependency.setGroupId(mavenDependency.getGroupId());
						dependency.setArtifactId(mavenDependency.getArtifactId());
						dependency.setVersion(mavenDependency.getVersion());
						dependency.setScope("provided");
						model.addDependency(dependency);
					});

		} catch (Exception e) {
			log.error("Error retrieving maven dependencies", e);
		}

		final File repositoryDir = GitHelper.getRepositoryDir(null, repository.getCode());
		File pomFile = new File(repositoryDir, "/facets/maven/pom.xml");

		try {
			pomFile.getParentFile().mkdirs();
			
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
		MeveoUser meveoUser = currentUser.get();
		return MavenConfigurationService.getM2Directory(meveoUser);
	}

	public List<String> getMavenRepositories() {
    	List<String> mavenRemoteRepositories = new ArrayList<>();
    	List<RemoteRepository> remoteRepositories = remoteRepositoryService.list();
    	if (CollectionUtils.isNotEmpty(remoteRepositories)) {
    		for (RemoteRepository remoteRepository : remoteRepositories) {
				mavenRemoteRepositories.add(remoteRepository.getUrl());
			}
		}
		return mavenRemoteRepositories;
	}

	public void setMavenRepositories(List<String> mavenRepositories) {
		ParamBean.getInstance().setListProperty("maven.path.repositories", mavenRepositories);
	}

	public void saveConfiguration() {
		ParamBean.getInstance().saveProperties();
	}

	public void saveConfiguration(MavenConfigurationDto mavenConfiguration) {
		setMavenRepositories(mavenConfiguration.getMavenRepositories());
		saveConfiguration();
	}

	public MavenConfigurationDto loadConfig() {

		MavenConfigurationDto result = new MavenConfigurationDto();
		result.setMavenRepositories(getMavenRepositories());

		return result;
	}

	public RepositorySystem newRepositorySystem() {
		return ManualRepositorySystemFactory.newRepositorySystem();
	}

	public RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository(getM2FolderPath());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        session.setRepositoryListener(new ConsoleRepositoryListener());
        
		return session;
	}

	public String createDirectory(String groupId, String artifactId, String version, String classifier) {
		String m2Folder = getM2FolderPath();
		if (!m2Folder.endsWith(File.separator)) {
			m2Folder = m2Folder + File.separator;
		}

		if (!StringUtils.isBlank(groupId)) {
			String[] groupDirs = groupId.split("\\.");
			for (String groupDir : groupDirs) {
				m2Folder = m2Folder + File.separator + groupDir;
			}
			File f = new File(m2Folder);
			f.mkdirs();
		}
		if (!StringUtils.isBlank(artifactId)) {
			m2Folder = m2Folder + File.separator + artifactId;
			File fileDir = new File(m2Folder);
			if (!fileDir.exists()) {
				fileDir.mkdir();
			}
		}
		if (!StringUtils.isBlank(version)) {
			m2Folder = m2Folder + File.separator + version;
			File fileDir = new File(m2Folder);
			if (!fileDir.exists()) {
				fileDir.mkdir();
			}
		}
		return m2Folder;
	}

	public String buildArtifactName(String artifactId, String version, String classifier) {
		if (StringUtils.isBlank(classifier)) {
			return artifactId + "-" + version + ".jar";

		} else {
			return artifactId + "-" + version + "-" + classifier + ".jar";
		}
	}

	/**
	 * Updates the list of local maven repositories.
	 *
	 * @param remoteRepositories remote repositories to add
	 */
	public void updateRepository(List<String> remoteRepositories) {

		Set<String> localRepos = new HashSet<>(getMavenRepositories());
		localRepos.addAll(remoteRepositories);
		setMavenRepositories(new ArrayList<>(localRepos));
	}

	public static void construct() {
		try {
			if (CLASSPATH_REFERENCE.get().length() == 0) {
				synchronized (CLASSPATH_REFERENCE) {
					if (CLASSPATH_REFERENCE.get().length() == 0) {
						String classpath = CLASSPATH_REFERENCE.get();

						File realFile = new File(System.getProperty("jboss.server.config.dir")).getAbsoluteFile();
						File deploymentDir = realFile.getParentFile().getParentFile();

						Set<String> classPathEntries = new HashSet<>();

						for (File modulesDir : Objects.requireNonNull(deploymentDir.listFiles((dir, name) -> name.contains("modules")))) {
							if (!modulesDir.isDirectory()) {
								continue;
							}

							for (File systemDir : Objects.requireNonNull(modulesDir.listFiles((dir, name) -> name.contains("system")))) {
								if (!systemDir.isDirectory()) {
									continue;
								}

								for (File layersDir : Objects.requireNonNull(systemDir.listFiles((dir, name) -> name.contains("layers")))) {
									if (!layersDir.isDirectory()) {
										continue;
									}

									for (File baseDir : Objects.requireNonNull(layersDir.listFiles((dir, name) -> name.equals("base")))) {
										if (baseDir.isDirectory()) {
											getSubFolder(baseDir, classPathEntries);
										}
									}
								}
							}
						}

						File deployDir = realFile.getParentFile();

						for (File deploymentsDir : Objects.requireNonNull(deployDir.listFiles((dir, name) -> name.contains("deployments")))) {
							if (!deploymentsDir.isDirectory()) {
								continue;
							}

							for (File warDir : Objects.requireNonNull(deploymentsDir.listFiles((dir, name) -> name.contains(ParamBean.getInstance().getProperty("meveo.moduleName", "meveo") + ".war")))) {
								if (!warDir.isDirectory()) {
									continue;
								}

								for (File webDir : Objects.requireNonNull(warDir.listFiles((dir, name) -> name.contains("WEB-INF")))) {
									if (!webDir.isDirectory()) {
										continue;
									}

									for (File libDir : Objects.requireNonNull(webDir.listFiles((dir, name) -> name.equals("lib")))) {
										if (libDir.isDirectory()) {
											for (File f : FileUtils.getFilesToProcess(libDir, "*", "jar")) {
												classPathEntries.add(f.getCanonicalPath());
											}
										}
									}
								}
							}
						}
						classpath = String.join(File.pathSeparator, classPathEntries);
						CLASSPATH_REFERENCE.set(classpath);
					}
				}
			}
		} catch (IOException e) {}
	}

	private static void getSubFolder (File file, Set<String> classPathEntries) {
		try {
			ArrayList<File> directories = new ArrayList<File>(
					Arrays.asList(
							new File(file.getAbsolutePath()).listFiles(File::isDirectory)
					)
			);
			for (File subFile : directories) {
				if (!subFile.getName().equals("main")) {
					getSubFolder(subFile, classPathEntries);
				} else {
					for (File f : FileUtils.getFilesToProcess(subFile, "*", "jar")) {
						classPathEntries.add(f.getCanonicalPath());
					}
				}
			}
		} catch (IOException e) {
		}
	}
	
	/**
	 * Create the default pom.xml file for the default Meveo git repository if it
	 * does not exists.
	 * 
	 * @param repositoryCode code of the repository
	 */
	public void createDefaultPomFile(String repositoryCode) {

		File gitRepo = GitHelper.getRepositoryDir(currentUser.get(), repositoryCode);
		File pomFile = new File(gitRepo.getPath() + File.separator + "facets" + File.separator + "maven" + File.separator + "pom.xml");

		if (!pomFile.exists()) {
			generatePom("Initialized default repository", repositoryCode);
		}
	}
	
	private static class ChangeBuffer {
		private List<MavenDependency> createdBuffer = new ArrayList<>();
		private List<MavenDependency> updatedBuffer = new ArrayList<>();
		private List<MavenDependency> deletedBuffer = new ArrayList<>();
		/**
		 * @return the {@link #createdBuffer}
		 */
		public List<MavenDependency> getCreatedBuffer() {
			return createdBuffer;
		}
		/**
		 * @return the {@link #updatedBuffer}
		 */
		public List<MavenDependency> getUpdatedBuffer() {
			return updatedBuffer;
		}
		/**
		 * @return the {@link #deletedBuffer}
		 */
		public List<MavenDependency> getDeletedBuffer() {
			return deletedBuffer;
		}
		
	}
}
