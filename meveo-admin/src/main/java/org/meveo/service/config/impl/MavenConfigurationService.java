package org.meveo.service.config.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.ejb.Lock;
import javax.ejb.LockType;
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
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.MavenUtils;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Updated;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.storage.RemoteRepository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.security.keycloak.CurrentUserProvider;
import org.meveo.security.keycloak.MeveoUserKeyCloakImpl;
import org.meveo.service.admin.impl.MeveoModuleService;
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
@Lock(LockType.READ)
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
	
	@Inject
	private MeveoModuleService moduleService;

	private javax.ejb.Timer ejbTimer;

	public static final AtomicReference<String> CLASSPATH_REFERENCE = new AtomicReference<>("");

    /**
     * @param currentUser Logged user
     * @return the maven directory relative to the file explorer directory for the
     *         user's provider
     */
    public static String getM2Directory(MeveoUser currentUser) {
    	String userCustomM2 = ParamBean.getInstance().getProperty("maven.path.m2", null);
    	if (userCustomM2 != null) {
    		File m2Dir = new File(userCustomM2);
    		if (m2Dir.exists()) {
    			return userCustomM2;
    		}
    	}
    	
        String rootDir = ParamBean.getInstance().getChrootDir(currentUser == null ? null : currentUser.getProviderCode());
        String m2 = rootDir + M2_DIR;
        File m2Folder = new File(m2);
        if (!m2Folder.exists()) {
            m2Folder.mkdir();
        }
        return m2;
    }
    
    public void updatePomOnSave(@Observes @Updated MeveoModule module) {
    	generatePom("Update pom", module);
    }


	public void onDependencyCreated(@Observes @Created MavenDependency d) {
		mavenDependencyService.findRelatedScripts(d)
			.stream()
				.map(scriptInstanceService::findModuleOf)
				.filter(Objects::nonNull)
				.forEach(module -> {
					buffers.computeIfAbsent(module.getCode(), key -> new ChangeBuffer())
						.getCreatedBuffer()
						.add(d);
					schedulePomGeneration();
				});
	}

	public void onDependencyUpdated(@Observes @Updated MavenDependency d) {
		mavenDependencyService.findRelatedScripts(d)
			.stream()
				.map(scriptInstanceService::findModuleOf)
				.filter(Objects::nonNull)
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

				if (message.length() == 0) {
					message.append("Update pom.xml");
				}
				
				MeveoModule module = moduleService.findByCode(moduleCode, List.of("moduleDependencies"));

				generatePom(message.toString(), module);
			});
		}
	}

	private void generatePom(String message, MeveoModule module) {
		GitRepository repository = gitRepositoryService.findByCode(module.getCode());
		generatePom(message,  module, repository);
	}
	
	public void generatePom(String message, MeveoModule module,GitRepository repository) {
		//TODO: Avoid this code when module just got uninstalled
		
		File gitRepo = GitHelper.getRepositoryDir(currentUser.get(), module.getCode());
		Paths.get(gitRepo.getPath(), "facets", "maven").toFile().mkdirs();

		log.debug("Generating pom.xml file");
		
		File pomFile = this.moduleService.findPom(module);
		final Model model = MavenUtils.readModel(pomFile);
		 
		model.setGroupId("org.meveo");//TODO: Add group id to module
		model.setArtifactId(module.getCode());
		model.setVersion(module.getCurrentVersion());
		model.setModelVersion("4.0.0");
		
		if (model.getPomFile() == null) {
			model.setBuild(new Build());
		}
		
		// Create symlink for java folder
		Path source = Paths.get(gitRepo.getPath(), "facets", "java");
		source.toFile().mkdirs();
		
		Path link = Paths.get(gitRepo.getPath(), "facets", "maven", "src", "main", "java");
		Path relativeSrc = link.getParent().relativize(source);
		
		try {

			link.getParent().toFile().mkdirs();
			if (!link.toFile().exists()) {
				Files.createSymbolicLink(link, relativeSrc);
			}
		} catch (IOException e1) {
			log.error("Failed to create symbolic link for java source", e1);
		}
		
		// Create .gitignore file
		Path gitIgnore = Paths.get(gitRepo.getPath(), "facets", "maven", ".gitignore");
		List<String> ignoredPatterns = List.of(
				"src/main/java/**",
				"target/"
			);
		
		String gitIgnoreFile = String.join("\n", ignoredPatterns);
		try {
			MeveoFileUtils.writeAndPreserveCharset(gitIgnoreFile, gitIgnore.toFile());
		} catch (IOException e1) {
			log.error("Failed to create gitignore", e1);
		}
		
		Properties properties = new Properties();
		properties.setProperty("maven.compiler.target", "11");
		properties.setProperty("maven.compiler.source", "11");
		model.setProperties(properties);

		List<RemoteRepository> remoteRepositories = remoteRepositoryService.list();
		if (CollectionUtils.isNotEmpty(remoteRepositories)) {
			for (RemoteRepository remoteRepository : remoteRepositories) {
				MavenUtils.addRepository(model, remoteRepository);
			}
		}
		
		// Include meveo bom
		DependencyManagement dependencyManagement = new DependencyManagement();
		model.setDependencyManagement(dependencyManagement);
		
		Dependency meveoBom = new Dependency();
		meveoBom.setGroupId("org.meveo");
		meveoBom.setArtifactId("meveo");
		meveoBom.setType("pom");
		meveoBom.setScope("import");
		meveoBom.setVersion(System.getProperty("meveo.version", Version.appVersion));
		MavenUtils.addOrUpdateDependency(dependencyManagement, meveoBom);
		
		Dependency wildflyBom = new Dependency();
		wildflyBom.setGroupId("org.wildfly.bom");
		wildflyBom.setArtifactId("wildfly-jakartaee8-with-tools");
		wildflyBom.setType("pom");
		wildflyBom.setScope("import");
		wildflyBom.setVersion("18.0.1.Final");
		MavenUtils.addOrUpdateDependency(dependencyManagement, wildflyBom);
		
		Dependency wildflyBom2 = new Dependency();
		wildflyBom2.setGroupId("org.wildfly.bom");
		wildflyBom2.setArtifactId("wildfly-jakartaee8");
		wildflyBom2.setType("pom");
		wildflyBom2.setScope("import");
		wildflyBom2.setVersion("18.0.1.Final");
		MavenUtils.addOrUpdateDependency(dependencyManagement, wildflyBom2);
		
		Dependency wildflyBom3 = new Dependency();
		wildflyBom3.setGroupId("org.wildfly");
		wildflyBom3.setArtifactId("wildfly-feature-pack");
		wildflyBom3.setType("pom");
		wildflyBom3.setScope("import");
		wildflyBom3.setVersion("18.0.1.Final");
		MavenUtils.addOrUpdateDependency(dependencyManagement, wildflyBom3);
		
		// Use maven pkg repo before meveo instance repo
		Repository githubRepo = new Repository();
		githubRepo.setId("github");
		githubRepo.setUrl("https://maven.pkg.github.com/meveo-org/meveo");
		RepositoryPolicy policy = new RepositoryPolicy();
		policy.setEnabled(true);
		githubRepo.setSnapshots(policy);
		MavenUtils.addRepository(model, githubRepo);

		Repository ownInstance = new Repository();
		String baseUrl = ParamBean.getInstance().getProperty("meveo.admin.baseUrl", "http://localhost:8080/meveo");
		ownInstance.setId("meveo-repo");
		if (!baseUrl.endsWith("/")) {
			baseUrl = baseUrl + "/";
		}
		ownInstance.setUrl(baseUrl + System.getProperty("meveo.moduleName", "meveo") + "/maven");
		MavenUtils.addRepository(model, ownInstance);
		
		Dependency meveoDependency = new Dependency();
		meveoDependency.setGroupId("org.meveo");
		meveoDependency.setArtifactId("meveo-api");
		meveoDependency.setVersion(System.getProperty("meveo.version", Version.appVersion));
		meveoDependency.setScope("provided");
		MavenUtils.addOrUpdateDependency(model, meveoDependency);
		
		module.getModuleDependencies().forEach(meveoModuleDependency -> {
			Dependency dependency = new Dependency();
			dependency.setGroupId("org.meveo");
			dependency.setArtifactId(meveoModuleDependency.getCode());
			dependency.setVersion(meveoModuleDependency.getCurrentVersion());
			dependency.setScope("compile");
			MavenUtils.addOrUpdateDependency(model, dependency);
		});

		try {
			mavenDependencyService.findModuleDependencies(module.getCode())
				.forEach(mavenDependency -> {
					Dependency dependency = new Dependency();
					dependency.setGroupId(mavenDependency.getGroupId());
					dependency.setArtifactId(mavenDependency.getArtifactId());
					dependency.setVersion(mavenDependency.getVersion());
					dependency.setScope("provided");
					MavenUtils.addOrUpdateDependency(model, dependency);
				});
		} catch (Exception e) {
			log.error("Error retrieving maven dependencies", e);
		}

		List<File> updatedFiles = List.of(pomFile, gitIgnore.toFile(), link.toFile());
		
		writeToPom(model, pomFile); 
		
		try {
			gitClient.commitFiles(repository, updatedFiles, message);
		} catch (BusinessException e) {
			log.error("Can't commit pom.xml file", e);
		}

	}

	private void writeToPom(Model model, File pomFile) {
		try {
			MavenXpp3Writer xmlWriter = new MavenXpp3Writer();
			try (FileWriter fileWriter = new FileWriter(pomFile)) {
				xmlWriter.write(fileWriter, model);
			}
		} catch (IOException e) {
			log.error("Can't write to pom.xml", e);
		}
	}

	public String getM2FolderPath() {
		MeveoUser meveoUser = currentUser.get();
		return MavenConfigurationService.getM2Directory(meveoUser);
	}
	
	public String getNodeModulesFolderPath() {
		String rootDir = ParamBean.getInstance().getChrootDir(null);
		return rootDir + File.separator + "node_modules";
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

	@Lock(LockType.READ)
	public RepositorySystem newRepositorySystem() {
		return ManualRepositorySystemFactory.newRepositorySystem();
	}

	@Lock(LockType.READ)
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
			MeveoModule module = moduleService.findByCode(repositoryCode, List.of("moduleDependencies"));
			generatePom("Initialized default repository", module);
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
