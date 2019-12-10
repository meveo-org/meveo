package org.meveo.service.config.impl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

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
import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;

/**
 * Manage the maven configuration.
 * 
 * <pre>
 * mavenExecutablePath - path to the local maven executable
 * m2FolderPath - path to the local .m2 folder
 * mavenRepositories - list of maven repositories
 * </pre>
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 * @since 6.5.0
 */
public class MavenConfigurationService implements Serializable {

	private static final long serialVersionUID = 7814020640577283116L;

	private final static String M2_DIR = "/.m2";

	@Inject
	@CurrentUser
	protected MeveoUser currentUser;

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

	public String getM2FolderPath() {
		return MavenConfigurationService.getM2Directory(currentUser);
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
		setMavenRepositories(mavenConfiguration.getMavenRepositories());
		saveConfiguration();
	}

	public MavenConfigurationDto loadConfig() {

		MavenConfigurationDto result = new MavenConfigurationDto();
		result.setMavenRepositories(getMavenRepositories());

		return result;
	}

	public RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
		return locator.getService(RepositorySystem.class);
	}

	public RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository(getM2FolderPath());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

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
}
